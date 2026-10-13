package com.medtracker.core.knowledge

import com.medtracker.core.model.ItemType
import com.medtracker.core.model.RegimenItem

/**
 * Recognises homeopathic products from their name (a potency such as "30C", "6X", "200CH", "1M",
 * "mother tincture" or the word "homeopathic") or from the item type.
 */
object Homeopathy {

    /** @param potency e.g. "30C", or null when the item is homeopathic but no potency was given. */
    data class Info(val potency: String?, val lowPotency: Boolean)

    private val POTENCY = Regex("(\\d+)(c|ch|ck|cc|x|d|dh|m|lm)")
    private val MARKER_WORDS = setOf("homeopathic", "homoeopathic", "homeopathy", "homoeopathy")
    private val MOTHER_TINCTURE = listOf(listOf("mother", "tincture"), listOf("mt"))

    /** Highest X/D potency and C potency still treated as "low" (may contain measurable material). */
    private const val LOW_DECIMAL_MAX = 6
    private const val LOW_CENTESIMAL_MAX = 3

    fun detect(item: RegimenItem): Info? = detect(item.name, item.type == ItemType.HOMEOPATHIC)

    fun detect(name: String, typedHomeopathic: Boolean = false): Info? {
        val words = KnowledgeBase.tokenize(name)
        if (MOTHER_TINCTURE.any { KnowledgeBase.containsSequence(words, it) } &&
            (typedHomeopathic || words.any { it in MARKER_WORDS } || words.any { POTENCY.matches(it) })
        ) {
            return Info("Mother tincture", lowPotency = true)
        }
        for (word in words) {
            val match = POTENCY.matchEntire(word) ?: continue
            val number = match.groupValues[1].toInt()
            val scale = match.groupValues[2]
            val low = when (scale) {
                "x", "d", "dh" -> number <= LOW_DECIMAL_MAX
                "c", "ch", "ck", "cc" -> number <= LOW_CENTESIMAL_MAX
                else -> false // M (1000C) and LM potencies are very dilute
            }
            return Info(word.uppercase(), low)
        }
        if (typedHomeopathic || words.any { it in MARKER_WORDS }) return Info(null, lowPotency = false)
        return null
    }
}
