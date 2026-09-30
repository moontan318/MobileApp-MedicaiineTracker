package com.medtracker.core.knowledge

import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.ItemType

/** How serious a finding is. Ordered from most to least serious. */
enum class Severity(val label: String) {
    MAJOR("Major"),
    MODERATE("Moderate"),
    MINOR("Minor"),
    INFO("Info"),
}

/**
 * A known medicine, vitamin, mineral or herbal product.
 *
 * @param aliases names (generic, brand, chemical forms) that identify the substance when they
 *   appear as whole words in a user-entered name.
 * @param tags classes this substance belongs to (e.g. "nsaid"). The substance id is always
 *   treated as a tag too, so rules can target either a single substance or a whole class.
 */
data class Substance(
    val id: String,
    val displayName: String,
    val defaultType: ItemType,
    val aliases: List<String>,
    val tags: Set<String> = emptySet(),
) {
    val allTags: Set<String> get() = tags + id
}

/**
 * An interaction between any substance tagged with one of [sideA] and any substance tagged
 * with one of [sideB].
 *
 * When [separationHours] is set the problem is purely about absorption, so it is only reported
 * when the two products are scheduled closer together than that.
 *
 * Findings sharing the same [topic] for the same pair of items are collapsed so only the most
 * severe one is shown.
 */
data class InteractionRule(
    val id: String,
    val sideA: Set<String>,
    val sideB: Set<String>,
    val severity: Severity,
    val title: String,
    val effect: String,
    val advice: String,
    val separationHours: Double? = null,
    val topic: String = id,
)

/** Advice for a single substance on the time of day and/or relation to food. */
data class SubstanceAdvice(
    val id: String,
    val tags: Set<String>,
    val severity: Severity,
    val reason: String,
    /** Acceptable windows as minute-of-day ranges; null means any time is fine. */
    val windows: List<IntRange>? = null,
    val windowLabel: String? = null,
    val food: FoodRelation? = null,
    /** A general note shown whenever the substance is present, independent of timing/food. */
    val generalNote: String? = null,
)

/** Mass/IU unit used as the base for a dose limit. */
enum class BaseUnit(val label: String) { MG("mg"), MCG("mcg") }

/**
 * Typical adult upper limits for a substance, summed across every item containing it.
 *
 * @param iuToBase how many [unit]s one IU equals, for substances commonly labelled in IU.
 */
data class DoseLimit(
    val substanceId: String,
    val unit: BaseUnit,
    val maxDaily: Double? = null,
    val maxSingle: Double? = null,
    val minHoursBetweenDoses: Double? = null,
    val severity: Severity,
    val note: String,
    val iuToBase: Double? = null,
    /** Severity when a single dose exceeds [maxSingle]; defaults to [severity]. */
    val singleDoseSeverity: Severity = severity,
)

fun hm(hour: Int, minute: Int = 0): Int = hour * 60 + minute
