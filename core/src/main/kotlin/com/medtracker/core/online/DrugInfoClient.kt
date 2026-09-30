package com.medtracker.core.online

import com.medtracker.core.knowledge.KnowledgeBase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class HttpResult(val code: Int, val body: String)

/** Minimal HTTP GET abstraction so lookups can be tested without a network. */
fun interface HttpGetter {
    @Throws(IOException::class)
    fun get(url: String): HttpResult
}

/** [HttpGetter] backed by [HttpURLConnection]; works on the JVM and Android. */
class UrlConnectionGetter(private val timeoutMillis: Int = 15_000) : HttpGetter {
    override fun get(url: String): HttpResult {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = timeoutMillis
            connection.readTimeout = timeoutMillis
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
            return HttpResult(code, body)
        } finally {
            connection.disconnect()
        }
    }
}

/** Thrown when an online source can't be reached, so callers can retry later. */
class LookupException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Looks up a medicine by name using free public APIs:
 *  - openFDA drug labels (https://open.fda.gov) for drug class, interactions, contraindications and dosing text
 *  - NLM RxNorm (https://rxnav.nlm.nih.gov) to correct misspellings and resolve brand names to ingredients
 */
class DrugInfoClient(
    private val http: HttpGetter = UrlConnectionGetter(),
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val json = Json { ignoreUnknownKeys = true }

    @Throws(LookupException::class)
    fun lookup(name: String): OnlineDrugInfo {
        val key = KnowledgeBase.normalize(name)
        val candidates = PharmClassMapper.lookupCandidates(name)
        if (candidates.isEmpty()) return OnlineDrugInfo.notFound(key, clock())

        for (candidate in candidates) {
            searchLabel(candidate)?.let { return it.copy(query = key, sources = listOf(OPENFDA_SOURCE)) }
        }

        val ingredient = candidates.firstNotNullOfOrNull { rxNormIngredient(it) }
            ?: return OnlineDrugInfo.notFound(key, clock())
        val usName = PharmClassMapper.UK_TO_US[ingredient] ?: ingredient
        searchLabel(usName)?.let { return it.copy(query = key, sources = listOf(RXNORM_SOURCE, OPENFDA_SOURCE)) }
        return OnlineDrugInfo(
            query = key,
            found = true,
            fetchedAtMillis = clock(),
            genericName = ingredient,
            sources = listOf(RXNORM_SOURCE),
        )
    }

    // ---------------------------------------------------------------- openFDA

    private fun searchLabel(term: String): OnlineDrugInfo? {
        val quoted = "%22" + URLEncoder.encode(term, "UTF-8") + "%22"
        val search = LABEL_FIELDS.joinToString("+") { "$it:$quoted" }
        val result = fetch("https://api.fda.gov/drug/label.json?search=$search&limit=5")
        if (result.code == 404) return null
        if (result.code != 200) throw LookupException("openFDA returned HTTP ${result.code}")
        val labels = parse(result.body).obj()?.get("results").arr()?.mapNotNull { it.obj() } ?: return null
        val best = labels.minByOrNull { score(it, term) } ?: return null
        val info = labelToInfo(best)
        if (info.pharmClasses.isNotEmpty()) return info
        // Many labels (e.g. from repackagers) omit the drug class; borrow it from another label
        // for the same generic medicine.
        val sameGeneric = labels.filter { genericNames(it) == genericNames(best) }
        val classes = sameGeneric.flatMap(::pharmClasses).distinct()
        return info.copy(pharmClasses = classes)
    }

    private fun genericNames(label: JsonObject): Set<String> =
        label["openfda"].obj()?.get("generic_name").strings().map(KnowledgeBase::normalize).toSet()

    private fun pharmClasses(label: JsonObject): List<String> {
        val openfda = label["openfda"].obj()
        return (openfda?.get("pharm_class_epc").strings() + openfda?.get("pharm_class_moa").strings()).distinct()
    }

    private fun score(label: JsonObject, term: String): Int {
        val openfda = label["openfda"].obj()
        val generics = openfda?.get("generic_name").strings().map(KnowledgeBase::normalize)
        val brands = openfda?.get("brand_name").strings().map(KnowledgeBase::normalize)
        val t = KnowledgeBase.normalize(term)
        var score = when {
            t in generics -> 0
            t in brands -> 10
            else -> 20 + (generics.firstOrNull()?.split(' ')?.size ?: 10)
        }
        if (label["drug_interactions"] == null) score += 5
        if (pharmClasses(label).isEmpty()) score += 3
        return score
    }

    private fun labelToInfo(label: JsonObject): OnlineDrugInfo {
        val openfda = label["openfda"].obj()
        fun section(vararg keys: String): String? =
            keys.flatMap { label[it].strings() }.joinToString("\n\n").trim().ifEmpty { null }?.let(::truncate)
        val generic = openfda?.get("generic_name").strings().firstOrNull()?.lowercase()
            ?: openfda?.get("substance_name").strings().firstOrNull()?.lowercase()
        return OnlineDrugInfo(
            query = "",
            found = true,
            fetchedAtMillis = clock(),
            genericName = generic,
            brandNames = openfda?.get("brand_name").strings().map { it.trim() }.distinctBy { it.lowercase() }.take(6),
            pharmClasses = pharmClasses(label),
            interactionsText = section("drug_interactions"),
            contraindicationsText = section("contraindications"),
            warningsText = section("boxed_warning", "warnings_and_cautions", "warnings"),
            dosageText = section("dosage_and_administration"),
            labelSetId = label["set_id"].str(),
        )
    }

    // ---------------------------------------------------------------- RxNorm

    /** Returns the RxNorm ingredient name for a (possibly misspelled or brand) name, or null. */
    private fun rxNormIngredient(term: String): String? {
        val encoded = URLEncoder.encode(term, "UTF-8")
        val approx = fetch("$RXNAV/approximateTerm.json?term=$encoded&maxEntries=3")
        if (approx.code != 200) return null
        val candidates = parse(approx.body).obj()?.get("approximateGroup").obj()?.get("candidate").arr() ?: return null
        for (candidate in candidates.mapNotNull { it.obj() }) {
            val rxcui = candidate["rxcui"].str() ?: continue
            val name = candidate["name"].str() ?: propertiesName(rxcui) ?: continue
            if (!closeEnough(term, name)) continue
            return ingredientsOf(rxcui) ?: KnowledgeBase.normalize(name)
        }
        return null
    }

    private fun propertiesName(rxcui: String): String? {
        val r = fetch("$RXNAV/rxcui/$rxcui/properties.json")
        if (r.code != 200) return null
        return parse(r.body).obj()?.get("properties").obj()?.get("name").str()
    }

    private fun ingredientsOf(rxcui: String): String? {
        val r = fetch("$RXNAV/rxcui/$rxcui/related.json?tty=IN")
        if (r.code != 200) return null
        val groups = parse(r.body).obj()?.get("relatedGroup").obj()?.get("conceptGroup").arr() ?: return null
        val names = groups.mapNotNull { it.obj() }
            .flatMap { it["conceptProperties"].arr().orEmpty() }
            .mapNotNull { it.obj()?.get("name").str()?.lowercase() }
            .distinct()
        return if (names.isEmpty()) null else names.sorted().joinToString(" and ")
    }

    // ---------------------------------------------------------------- helpers

    private fun fetch(url: String): HttpResult = try {
        http.get(url)
    } catch (e: IOException) {
        throw LookupException("Couldn't reach ${URL(url).host}", e)
    }

    private fun parse(body: String): JsonElement? = runCatching { json.parseToJsonElement(body) }.getOrNull()

    private fun JsonElement?.obj(): JsonObject? = this as? JsonObject
    private fun JsonElement?.arr(): JsonArray? = this as? JsonArray
    private fun JsonElement?.str(): String? = (this as? JsonPrimitive)?.takeIf { it.isString }?.content
    private fun JsonElement?.strings(): List<String> = when (this) {
        is JsonArray -> mapNotNull { it.str() }
        is JsonPrimitive -> listOfNotNull(str())
        else -> emptyList()
    }

    companion object {
        private const val RXNAV = "https://rxnav.nlm.nih.gov/REST"
        private val LABEL_FIELDS = listOf("openfda.generic_name", "openfda.brand_name", "openfda.substance_name")
        const val OPENFDA_SOURCE = "openFDA drug label (US FDA)"
        const val RXNORM_SOURCE = "RxNorm (US National Library of Medicine)"
        private const val MAX_SECTION_CHARS = 8000

        private fun truncate(text: String): String =
            if (text.length <= MAX_SECTION_CHARS) text else text.take(MAX_SECTION_CHARS).trimEnd() + "…"

        /** True if [candidate] is plausibly the same word as [query] (tolerates small misspellings). */
        internal fun closeEnough(query: String, candidate: String): Boolean {
            val q = KnowledgeBase.normalize(query)
            val words = KnowledgeBase.normalize(candidate).split(' ')
            return (words + words.joinToString(" ")).any { w ->
                val distance = levenshtein(q, w)
                distance <= maxOf(1, q.length / 5)
            }
        }

        private fun levenshtein(a: String, b: String): Int {
            var prev = IntArray(b.length + 1) { it }
            for (i in 1..a.length) {
                val cur = IntArray(b.length + 1)
                cur[0] = i
                for (j in 1..b.length) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
                }
                prev = cur
            }
            return prev[b.length]
        }
    }
}
