package com.medtracker.core.knowledge

/** Read-only access to the built-in substance, interaction, timing and dose data. */
class KnowledgeBase(
    val substances: List<Substance> = SUBSTANCES,
    val interactions: List<InteractionRule> = INTERACTIONS,
    val advice: List<SubstanceAdvice> = SUBSTANCE_ADVICE,
    val doseLimits: List<DoseLimit> = DOSE_LIMITS,
) {
    private val aliasIndex: List<Pair<List<String>, Substance>> =
        substances.flatMap { sub -> (sub.aliases + sub.displayName).map { tokenize(it) to sub } }
            .filter { it.first.isNotEmpty() }

    private val limitsById = doseLimits.associateBy { it.substanceId }

    fun substance(id: String): Substance? = substances.firstOrNull { it.id == id }

    fun doseLimit(substanceId: String): DoseLimit? = limitsById[substanceId]

    /**
     * Finds every known substance named in [name]. Aliases must appear as whole words, so a
     * combination product such as "Calcium + Vitamin D3" resolves to both calcium and vitamin D.
     */
    fun identify(name: String): List<Substance> {
        val words = tokenize(name)
        if (words.isEmpty()) return emptyList()
        return aliasIndex
            .filter { (aliasWords, _) -> containsSequence(words, aliasWords) }
            .map { it.second }
            .distinctBy { it.id }
    }

    /** Suggestions for autocomplete: substances whose name or alias starts with the query. */
    fun suggest(query: String, limit: Int = 8): List<Pair<String, Substance>> {
        val q = normalize(query)
        if (q.length < 2) return emptyList()
        val results = LinkedHashMap<String, Pair<String, Substance>>()
        for (sub in substances) {
            val names = listOf(sub.displayName) + sub.aliases
            val hit = names.firstOrNull { normalize(it).startsWith(q) }
                ?: names.firstOrNull { normalize(it).contains(" $q") }
            if (hit != null) {
                val label = if (normalize(hit) == normalize(sub.displayName)) sub.displayName
                else "${hit.replaceFirstChar { it.uppercase() }} (${sub.displayName})"
                results.putIfAbsent(sub.id, label to sub)
            }
            if (results.size >= limit) break
        }
        return results.values.toList()
    }

    /** Advice entries that apply to any of the given tags. */
    fun adviceFor(tags: Set<String>): List<SubstanceAdvice> = advice.filter { it.tags.any(tags::contains) }

    companion object {
        fun normalize(text: String): String =
            text.lowercase().replace(Regex("[^a-z0-9]+"), " ").trim()

        fun tokenize(text: String): List<String> =
            normalize(text).split(' ').filter { it.isNotEmpty() }

        fun containsSequence(haystack: List<String>, needle: List<String>): Boolean = indexOfSequence(haystack, needle) >= 0

        /** Index of the first occurrence of [needle] as consecutive words in [haystack], or -1. */
        fun indexOfSequence(haystack: List<String>, needle: List<String>, from: Int = 0): Int {
            if (needle.isEmpty() || needle.size > haystack.size) return -1
            for (start in from..haystack.size - needle.size) {
                if (needle.indices.all { haystack[start + it] == needle[it] }) return start
            }
            return -1
        }
    }
}
