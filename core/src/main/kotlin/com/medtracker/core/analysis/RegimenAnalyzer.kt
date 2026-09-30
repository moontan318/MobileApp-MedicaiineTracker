package com.medtracker.core.analysis

import com.medtracker.core.knowledge.BaseUnit
import com.medtracker.core.knowledge.DoseLimit
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.knowledge.SubstanceAdvice
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.DoseUnit
import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.RegimenItem
import kotlin.math.ceil

/**
 * Checks a list of medicines and supplements for interactions, timing problems and dose
 * limits, and suggests a revised schedule.
 */
class RegimenAnalyzer(private val kb: KnowledgeBase = KnowledgeBase()) {

    private val optimizer = ScheduleOptimizer(kb)

    fun resolve(items: List<RegimenItem>): List<ResolvedItem> =
        items.map { ResolvedItem(it, kb.identify(it.name)) }

    fun analyze(items: List<RegimenItem>): AnalysisResult {
        val resolved = resolve(items)
        val changes = optimizer.optimise(resolved)
        val changesById = changes.associateBy { it.itemId }

        val findings = buildList {
            addAll(interactionFindings(resolved, changesById))
            addAll(duplicateFindings(resolved))
            addAll(timingFindings(resolved, changesById))
            addAll(doseFindings(resolved))
            addAll(generalFindings(resolved))
        }.sortedWith(compareBy({ it.severity.ordinal }, { it.category.ordinal }))

        return AnalysisResult(findings, changes, resolved)
    }

    // ---------------------------------------------------------------- interactions

    private fun interactionFindings(
        resolved: List<ResolvedItem>,
        changes: Map<String, ScheduleChange>,
    ): List<Finding> {
        val out = mutableListOf<Finding>()
        for (i in resolved.indices) for (j in i + 1 until resolved.size) {
            val a = resolved[i]
            val b = resolved[j]
            val rules = RuleMatcher.matching(kb.interactions, a.tags, b.tags)
                .groupBy { it.topic }
                .map { (_, group) -> group.minBy { it.severity.ordinal } }

            for (rule in rules) {
                val names = "${a.item.name} + ${b.item.name}"
                val sepHours = rule.separationHours
                if (sepHours == null) {
                    out += Finding(
                        id = "ix:${rule.id}:${a.item.id}:${b.item.id}",
                        category = FindingCategory.INTERACTION,
                        severity = rule.severity,
                        title = "${rule.title}: $names",
                        detail = rule.effect,
                        recommendation = rule.advice,
                        itemIds = listOf(a.item.id, b.item.id),
                    )
                    continue
                }
                val minMinutes = (sepHours * 60).toInt()
                val closest = a.item.times.flatMap { t -> b.item.times.map { DoseTime.circularDistance(t, it) } }.minOrNull()
                    ?: continue
                if (closest >= minMinutes) continue

                val suggestion = listOfNotNull(changes[a.item.id], changes[b.item.id])
                    .joinToString(" ") { describeChange(it) }
                out += Finding(
                    id = "sep:${rule.id}:${a.item.id}:${b.item.id}",
                    category = FindingCategory.INTERACTION,
                    severity = rule.severity,
                    title = "${rule.title}: $names",
                    detail = "${rule.effect} They are currently scheduled " +
                        (if (closest == 0) "at the same time." else "only ${ScheduleOptimizer.formatHours(closest)} apart."),
                    recommendation = rule.advice + if (suggestion.isNotEmpty()) " Suggested: $suggestion" else "",
                    itemIds = listOf(a.item.id, b.item.id),
                )
            }
        }
        return out
    }

    private fun duplicateFindings(resolved: List<ResolvedItem>): List<Finding> {
        val bySubstance = resolved.flatMap { r -> r.substances.map { it to r } }
            .groupBy({ it.first.id }, { it.second })
        return bySubstance.filter { it.value.size > 1 }.map { (substanceId, items) ->
            val substance = kb.substance(substanceId)!!
            Finding(
                id = "dup:$substanceId",
                category = FindingCategory.INTERACTION,
                severity = Severity.MODERATE,
                title = "${substance.displayName} appears in more than one product",
                detail = "${items.joinToString(", ") { it.item.name }} all contain ${substance.displayName}. " +
                    "Taking the same ingredient from several products is a common cause of accidental overdose.",
                recommendation = "Check this is intended. The total daily amount is checked in the Dosage section.",
                itemIds = items.map { it.item.id },
            )
        }
    }

    // ---------------------------------------------------------------- timing & food

    private fun timingFindings(
        resolved: List<ResolvedItem>,
        changes: Map<String, ScheduleChange>,
    ): List<Finding> {
        val out = mutableListOf<Finding>()
        for (r in resolved) {
            val item = r.item
            for (advice in kb.adviceFor(r.tags).distinctBy { it.id }) {
                windowFinding(item, advice, changes[item.id])?.let(out::add)
                foodFinding(item, advice)?.let(out::add)
                advice.generalNote?.let { note ->
                    out += Finding(
                        id = "note:${advice.id}:${item.id}",
                        category = FindingCategory.GENERAL,
                        severity = advice.severity,
                        title = "${item.name}: important information",
                        detail = note,
                        recommendation = "",
                        itemIds = listOf(item.id),
                    )
                }
            }
        }
        return out
    }

    private fun windowFinding(item: RegimenItem, advice: SubstanceAdvice, change: ScheduleChange?): Finding? {
        val windows = advice.windows ?: return null
        val outside = item.sortedTimes.filter { t -> windows.none { t.minuteOfDay in it } }
        if (outside.isEmpty()) return null
        val suggestion = if (change != null && advice.severity != Severity.INFO) {
            " Suggested: ${describeChange(change)}"
        } else ""
        return Finding(
            id = "time:${advice.id}:${item.id}",
            category = FindingCategory.TIMING,
            severity = advice.severity,
            title = "${item.name}: best taken ${advice.windowLabel}",
            detail = advice.reason + " Currently scheduled at ${outside.joinToString(", ")}.",
            recommendation = "Take ${advice.windowLabel}.$suggestion",
            itemIds = listOf(item.id),
        )
    }

    private fun foodFinding(item: RegimenItem, advice: SubstanceAdvice): Finding? {
        val wanted = advice.food ?: return null
        if (item.food == wanted) return null
        val unspecified = item.food == FoodRelation.ANY
        val severity = if (unspecified && advice.severity.ordinal < Severity.MINOR.ordinal) Severity.MINOR else advice.severity
        val action = when (wanted) {
            FoodRelation.WITH_FOOD -> "Take ${item.name} with food"
            FoodRelation.EMPTY_STOMACH -> "Take ${item.name} on an empty stomach"
            FoodRelation.ANY -> return null
        }
        return Finding(
            id = "food:${advice.id}:${item.id}",
            category = FindingCategory.TIMING,
            severity = severity,
            title = action,
            detail = advice.reason + if (unspecified) "" else " You currently take it: ${item.food.label.lowercase()}.",
            recommendation = "$action, then update the item so your schedule shows it.",
            itemIds = listOf(item.id),
        )
    }

    // ---------------------------------------------------------------- dosage

    private fun doseFindings(resolved: List<ResolvedItem>): List<Finding> {
        val out = mutableListOf<Finding>()
        val needsStrength = mutableSetOf<String>()
        val bySubstance = resolved.flatMap { r -> r.substances.map { it.id to r } }.groupBy({ it.first }, { it.second })

        for ((substanceId, items) in bySubstance) {
            val limit = kb.doseLimit(substanceId) ?: continue
            val name = kb.substance(substanceId)!!.displayName
            val measurable = mutableListOf<Pair<ResolvedItem, Double>>()
            for (r in items) {
                if (r.substances.size > 1) {
                    out += Finding(
                        id = "combo:$substanceId:${r.item.id}",
                        category = FindingCategory.DOSAGE,
                        severity = Severity.INFO,
                        title = "${r.item.name} is a combination product",
                        detail = "It contains ${r.substances.joinToString(", ") { it.displayName }}, so the $name dose can't be worked out from the product dose.",
                        recommendation = "Check the label for the amount of $name per dose. ${limit.note}",
                        itemIds = listOf(r.item.id),
                    )
                    continue
                }
                val perDose = toBase(r.item.doseAmount, r.item.doseUnit, limit)
                if (perDose == null) {
                    if (needsStrength.add(r.item.id)) {
                        out += Finding(
                            id = "strength:${r.item.id}",
                            category = FindingCategory.DOSAGE,
                            severity = Severity.INFO,
                            title = "Add the strength of ${r.item.name} to check the dose",
                            detail = "The dose is recorded as ${r.item.doseLabel()}, so it can't be compared with the recommended maximum.",
                            recommendation = "Edit the item and enter the amount per dose in mg, mcg" +
                                (if (limit.iuToBase != null) " or IU" else "") + " (see the product label).",
                            itemIds = listOf(r.item.id),
                        )
                    }
                    continue
                }
                measurable += r to perDose

                val maxSingle = limit.maxSingle
                if (maxSingle != null && perDose > maxSingle + EPSILON) {
                    val splits = ceil(perDose / maxSingle).toInt()
                    out += Finding(
                        id = "single:$substanceId:${r.item.id}",
                        category = FindingCategory.DOSAGE,
                        severity = limit.singleDoseSeverity,
                        title = "${r.item.name}: single dose above ${fmt(maxSingle, limit.unit)}",
                        detail = "Each dose is ${fmt(perDose, limit.unit)}. ${limit.note}",
                        recommendation = "Reduce each dose to ${fmt(maxSingle, limit.unit)} or less" +
                            (if (splits > 1) ", or split it into $splits smaller doses spread through the day" else "") +
                            " - unless your prescriber has told you otherwise.",
                        itemIds = listOf(r.item.id),
                    )
                }
            }

            val maxDaily = limit.maxDaily
            val total = measurable.sumOf { (r, perDose) -> perDose * r.item.times.size }
            if (maxDaily != null && measurable.isNotEmpty() && total > maxDaily + EPSILON) {
                val sources = measurable.joinToString("; ") { (r, perDose) ->
                    "${r.item.name}: ${fmt(perDose, limit.unit)} x ${r.item.times.size}"
                }
                val advice = if (measurable.size == 1) {
                    val (r, _) = measurable.first()
                    val perDoseMax = maxDaily / r.item.times.size
                    "Reduce to no more than ${fmt(maxDaily, limit.unit)} a day, e.g. ${fmt(perDoseMax, limit.unit)} per dose " +
                        "at your current ${r.item.times.size} dose(s) a day, unless your prescriber has advised a higher dose."
                } else {
                    "Your products together exceed the limit. Reduce or stop one of them so the total stays under " +
                        "${fmt(maxDaily, limit.unit)} a day, unless your prescriber has advised otherwise."
                }
                out += Finding(
                    id = "daily:$substanceId",
                    category = FindingCategory.DOSAGE,
                    severity = limit.severity,
                    title = "$name: ${fmt(total, limit.unit)} a day exceeds the ${fmt(maxDaily, limit.unit)} limit",
                    detail = "Daily total from $sources. ${limit.note}",
                    recommendation = advice,
                    itemIds = measurable.map { it.first.item.id },
                )
            }

            val minHours = limit.minHoursBetweenDoses
            if (minHours != null) {
                val allTimes = items.flatMap { it.item.times }.sorted()
                val minGap = (minHours * 60).toInt()
                val gaps = allTimes.zipWithNext { x, y -> y.minuteOfDay - x.minuteOfDay }
                if (gaps.any { it < minGap }) {
                    out += Finding(
                        id = "gap:$substanceId",
                        category = FindingCategory.DOSAGE,
                        severity = limit.severity,
                        title = "$name doses are too close together",
                        detail = "Doses are scheduled at ${allTimes.joinToString(", ")}. Leave at least ${ScheduleOptimizer.formatHours(minGap)} between doses.",
                        recommendation = "Space the doses at least ${ScheduleOptimizer.formatHours(minGap)} apart (see suggested schedule).",
                        itemIds = items.map { it.item.id },
                    )
                }
            }
        }
        return out
    }

    // ---------------------------------------------------------------- general

    private fun generalFindings(resolved: List<ResolvedItem>): List<Finding> {
        val out = mutableListOf<Finding>()
        val unknown = resolved.filter { !it.recognised }
        if (unknown.isNotEmpty()) {
            out += Finding(
                id = "unrecognised",
                category = FindingCategory.GENERAL,
                severity = Severity.INFO,
                title = "Not in the built-in database: ${unknown.joinToString(", ") { it.item.name }}",
                detail = "Interactions, timing and dose limits for these products can't be checked automatically. " +
                    "Check the spelling, or try the generic (non-brand) name.",
                recommendation = "Ask your pharmacist to review these alongside the rest of your list.",
                itemIds = unknown.map { it.item.id },
            )
        }
        if (resolved.size >= POLYPHARMACY_THRESHOLD) {
            out += Finding(
                id = "polypharmacy",
                category = FindingCategory.GENERAL,
                severity = Severity.INFO,
                title = "You take ${resolved.size} different products",
                detail = "The more products taken together, the higher the chance of interactions and side effects.",
                recommendation = "Ask your GP or pharmacist for a structured medication review at least once a year.",
                itemIds = emptyList(),
            )
        }
        return out
    }

    // ---------------------------------------------------------------- helpers

    private fun describeChange(change: ScheduleChange): String =
        "take ${change.itemName} at ${change.suggestedTimes.joinToString(", ")} instead of ${change.currentTimes.joinToString(", ")}."

    private fun fmt(amount: Double, unit: BaseUnit): String = "${RegimenItem.formatAmount(amount)} ${unit.label}"

    companion object {
        private const val EPSILON = 1e-6
        private const val POLYPHARMACY_THRESHOLD = 5

        /** Converts a dose to the limit's base unit, or null if the unit isn't a measurable amount. */
        fun toBase(amount: Double, unit: DoseUnit, limit: DoseLimit): Double? {
            val mg = when (unit) {
                DoseUnit.MG -> amount
                DoseUnit.MCG -> amount / 1000.0
                DoseUnit.G -> amount * 1000.0
                DoseUnit.IU -> return limit.iuToBase?.let { amount * it }
                else -> return null
            }
            return when (limit.unit) {
                BaseUnit.MG -> mg
                BaseUnit.MCG -> mg * 1000.0
            }
        }
    }
}
