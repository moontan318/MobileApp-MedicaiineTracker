package com.medtracker.core.online

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * Drug information retrieved from public online sources (US National Library of Medicine
 * RxNorm and the openFDA drug label API). Cached on the device by [query].
 */
data class OnlineDrugInfo(
    /** Normalised name that was looked up (the cache key). */
    val query: String,
    val found: Boolean,
    val fetchedAtMillis: Long,
    val genericName: String? = null,
    val brandNames: List<String> = emptyList(),
    /** Established pharmacologic classes and mechanisms, e.g. "Xanthine Oxidase Inhibitor [EPC]". */
    val pharmClasses: List<String> = emptyList(),
    val interactionsText: String? = null,
    val contraindicationsText: String? = null,
    val warningsText: String? = null,
    val dosageText: String? = null,
    /** DailyMed set id of the label, for linking to the full label. */
    val labelSetId: String? = null,
    /** Human-readable description of where the data came from. */
    val sources: List<String> = emptyList(),
) {
    val dailyMedUrl: String?
        get() = labelSetId?.let { "https://dailymed.nlm.nih.gov/dailymed/lookup.cfm?setid=$it" }

    fun toJson(): JsonObject = buildJsonObject {
        put("query", query)
        put("found", found)
        put("fetchedAt", fetchedAtMillis)
        genericName?.let { put("genericName", it) }
        put("brandNames", buildJsonArray { brandNames.forEach { add(JsonPrimitive(it)) } })
        put("pharmClasses", buildJsonArray { pharmClasses.forEach { add(JsonPrimitive(it)) } })
        interactionsText?.let { put("interactions", it) }
        contraindicationsText?.let { put("contraindications", it) }
        warningsText?.let { put("warnings", it) }
        dosageText?.let { put("dosage", it) }
        labelSetId?.let { put("setId", it) }
        put("sources", buildJsonArray { sources.forEach { add(JsonPrimitive(it)) } })
    }

    companion object {
        fun notFound(query: String, now: Long) = OnlineDrugInfo(query = query, found = false, fetchedAtMillis = now)

        fun fromJson(obj: JsonObject): OnlineDrugInfo? = runCatching {
            fun str(key: String) = obj[key]?.jsonPrimitive?.takeIf { it.isString }?.content
            fun list(key: String) = obj[key]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
            OnlineDrugInfo(
                query = str("query")!!,
                found = obj["found"]?.jsonPrimitive?.booleanOrNull ?: false,
                fetchedAtMillis = obj["fetchedAt"]?.jsonPrimitive?.longOrNull ?: 0L,
                genericName = str("genericName"),
                brandNames = list("brandNames"),
                pharmClasses = list("pharmClasses"),
                interactionsText = str("interactions"),
                contraindicationsText = str("contraindications"),
                warningsText = str("warnings"),
                dosageText = str("dosage"),
                labelSetId = str("setId"),
                sources = list("sources"),
            )
        }.getOrNull()

        /** Serialises a cache map to a JSON string. */
        fun encodeCache(cache: Map<String, OnlineDrugInfo>): String =
            JsonArray(cache.values.map { it.toJson() }).toString()

        /** Parses a cache written by [encodeCache]; malformed entries are skipped. */
        fun decodeCache(text: String): Map<String, OnlineDrugInfo> = runCatching {
            Json.parseToJsonElement(text).jsonArray
                .mapNotNull { fromJson(it.jsonObject) }
                .associateBy { it.query }
        }.getOrDefault(emptyMap())
    }
}
