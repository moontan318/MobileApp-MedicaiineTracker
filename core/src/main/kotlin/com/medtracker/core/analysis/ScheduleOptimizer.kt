package com.medtracker.core.analysis

import com.medtracker.core.knowledge.InteractionRule
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.knowledge.hm
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.ItemType
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Proposes dose times that satisfy absorption-separation rules, preferred time-of-day windows and
 * minimum gaps between repeated doses, while moving each dose as little as possible.
 *
 * Medicines are scheduled first so that supplements (which are usually more flexible) are the
 * ones that move when two products clash.
 */
class ScheduleOptimizer(private val kb: KnowledgeBase) {

    private data class Separation(val otherId: String, val minMinutes: Int, val rule: InteractionRule)

    fun optimise(items: List<ResolvedItem>): List<ScheduleChange> {
        val separations = items.associate { it.item.id to separationsFor(it, items) }
        val order = items.withIndex().sortedWith(
            compareBy({ priority(it.value) }, { it.index })
        ).map { it.value }

        val assigned = LinkedHashMap<String, List<DoseTime>>()
        for (resolved in order) {
            val item = resolved.item
            val windows = windowsFor(resolved)
            val gap = minGapMinutes(resolved)
            val chosen = mutableListOf<DoseTime>()
            for (original in item.sortedTimes) {
                val candidates = (listOf(original.minuteOfDay) + (hm(6)..hm(23) step 30)).distinct()
                val best = candidates.minBy { c ->
                    var cost = abs(c - original.minuteOfDay).toDouble()
                    if (windows != null && windows.none { c in it }) cost += 10_000
                    for (sep in separations.getValue(item.id)) {
                        val others = assigned[sep.otherId] ?: continue
                        cost += 100_000.0 * others.count { distance(c, it.minuteOfDay) < sep.minMinutes }
                    }
                    cost += 100_000.0 * chosen.count { distance(c, it.minuteOfDay) < gap }
                    cost
                }
                chosen += DoseTime(best)
            }
            assigned[item.id] = chosen.sorted()
        }

        val byId = items.associateBy { it.item.id }
        return items.mapNotNull { resolved ->
            val item = resolved.item
            val suggested = assigned[item.id] ?: return@mapNotNull null
            if (suggested == item.sortedTimes) return@mapNotNull null
            ScheduleChange(
                itemId = item.id,
                itemName = item.name,
                currentTimes = item.sortedTimes,
                suggestedTimes = suggested,
                reasons = reasonsFor(resolved, separations.getValue(item.id), byId),
            )
        }
    }

    private fun reasonsFor(
        resolved: ResolvedItem,
        separations: List<Separation>,
        byId: Map<String, ResolvedItem>,
    ): List<String> {
        val item = resolved.item
        val reasons = mutableListOf<String>()
        val windows = windowsFor(resolved)
        if (windows != null && item.times.any { t -> windows.none { t.minuteOfDay in it } }) {
            val label = kb.adviceFor(resolved.tags)
                .firstOrNull { it.windows != null && it.severity != Severity.INFO }?.windowLabel
            reasons += "Best taken ${label ?: "at a different time of day"}"
        }
        for (sep in separations) {
            val other = byId[sep.otherId]?.item ?: continue
            val clash = item.times.any { t -> other.times.any { distance(t.minuteOfDay, it.minuteOfDay) < sep.minMinutes } }
            if (clash) reasons += "Keep ${formatHours(sep.minMinutes)} apart from ${other.name}"
        }
        val gap = minGapMinutes(resolved)
        val sorted = item.sortedTimes
        if (sorted.zipWithNext().any { (a, b) -> b.minuteOfDay - a.minuteOfDay < gap }) {
            reasons += "Leave at least ${formatHours(gap)} between doses"
        }
        return reasons.distinct()
    }

    private fun separationsFor(resolved: ResolvedItem, all: List<ResolvedItem>): List<Separation> =
        all.filter { it.item.id != resolved.item.id }.flatMap { other ->
            RuleMatcher.matching(kb.interactions, resolved.tags, other.tags)
                .filter { it.separationHours != null }
                .map { Separation(other.item.id, (it.separationHours!! * 60).roundToInt(), it) }
        }

    /**
     * Union of the preferred windows for the item, or null if any time is fine. Info-level
     * preferences (e.g. magnesium in the evening) are shown as tips but never force a move.
     */
    private fun windowsFor(resolved: ResolvedItem): List<IntRange>? =
        kb.adviceFor(resolved.tags).filter { it.severity != Severity.INFO }
            .mapNotNull { it.windows }.flatten().ifEmpty { null }

    private fun minGapMinutes(resolved: ResolvedItem): Int {
        val hours = resolved.substances.mapNotNull { kb.doseLimit(it.id)?.minHoursBetweenDoses }.maxOrNull()
        return if (hours != null) (hours * 60).roundToInt() else DEFAULT_GAP_MINUTES
    }

    private fun priority(resolved: ResolvedItem): Int {
        val medicine = resolved.item.type == ItemType.MEDICINE
        val hasWindow = windowsFor(resolved) != null
        return when {
            medicine && hasWindow -> 0
            medicine -> 1
            hasWindow -> 2
            else -> 3
        }
    }

    companion object {
        private const val DEFAULT_GAP_MINUTES = 60

        fun distance(a: Int, b: Int): Int = DoseTime.circularDistance(DoseTime(a), DoseTime(b))

        fun formatHours(minutes: Int): String {
            val h = minutes / 60
            val m = minutes % 60
            return when {
                m == 0 && h == 1 -> "1 hour"
                m == 0 -> "$h hours"
                h == 0 -> "$m minutes"
                else -> "${h}h ${m}m"
            }
        }
    }
}

/** Helper for matching interaction rules against the tags of two items. */
object RuleMatcher {
    fun matches(rule: InteractionRule, a: Set<String>, b: Set<String>): Boolean =
        (rule.sideA.any(a::contains) && rule.sideB.any(b::contains)) ||
            (rule.sideA.any(b::contains) && rule.sideB.any(a::contains))

    fun matching(rules: List<InteractionRule>, a: Set<String>, b: Set<String>): List<InteractionRule> =
        rules.filter { matches(it, a, b) }
}
