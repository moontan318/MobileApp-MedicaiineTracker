package com.medtracker.core.analysis

import com.medtracker.core.knowledge.Severity
import com.medtracker.core.knowledge.Substance
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.RegimenItem
import com.medtracker.core.online.OnlineDrugInfo

enum class FindingCategory(val label: String) {
    INTERACTION("Interactions & contraindications"),
    TIMING("Timing"),
    DOSAGE("Dosage"),
    GENERAL("General notes"),
}

/** One piece of feedback about the regimen. */
data class Finding(
    val id: String,
    val category: FindingCategory,
    val severity: Severity,
    val title: String,
    val detail: String,
    val recommendation: String,
    val itemIds: List<String>,
)

/** A proposed change to when an item is taken. */
data class ScheduleChange(
    val itemId: String,
    val itemName: String,
    val currentTimes: List<DoseTime>,
    val suggestedTimes: List<DoseTime>,
    val reasons: List<String>,
)

/**
 * A regimen item together with the substances recognised in its name.
 *
 * @param online information fetched from online sources for this item, if any.
 * @param identifiedOnline true when the built-in database didn't know the name and the
 *   substances were worked out from online data instead.
 */
data class ResolvedItem(
    val item: RegimenItem,
    val substances: List<Substance>,
    val online: OnlineDrugInfo? = null,
    val identifiedOnline: Boolean = false,
) {
    val tags: Set<String> = substances.flatMap { it.allTags }.toSet()
    val recognised: Boolean get() = substances.isNotEmpty()
}

data class AnalysisResult(
    val findings: List<Finding>,
    val scheduleChanges: List<ScheduleChange>,
    val resolved: List<ResolvedItem>,
) {
    fun count(severity: Severity): Int = findings.count { it.severity == severity }

    /** Number of findings worth drawing attention to (major + moderate). */
    val alertCount: Int get() = count(Severity.MAJOR) + count(Severity.MODERATE)

    fun findingsFor(itemId: String): List<Finding> = findings.filter { itemId in it.itemIds }

    fun changeFor(itemId: String): ScheduleChange? = scheduleChanges.firstOrNull { it.itemId == itemId }

    companion object {
        val EMPTY = AnalysisResult(emptyList(), emptyList(), emptyList())
    }
}
