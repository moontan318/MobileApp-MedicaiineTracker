package com.medtracker.core.analysis

import com.medtracker.core.knowledge.BaseUnit
import com.medtracker.core.knowledge.DoseLimit
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.knowledge.Substance
import com.medtracker.core.knowledge.SubstanceAdvice
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.DoseUnit
import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.RegimenItem
import com.medtracker.core.online.OnlineDrugInfo
import com.medtracker.core.online.PharmClassMapper
import kotlin.math.ceil

/**
 * Checks a list of medicines and supplements for interactions, timing problems and dose
 * limits, and suggests a revised schedule.
 */
class RegimenAnalyzer(private val kb: KnowledgeBase = KnowledgeBase()) {

    private val optimizer = ScheduleOptimizer(kb)

    /**
     * Matches each item against the built-in database. Items the database doesn't know are
     * identified from [online] data (keyed by [KnowledgeBase.normalize] of the item name) where
     * available: first by mapping the online generic name back to the database, otherwise by
     * creating a substance whose tags come from its FDA drug classes.
     */
    fun resolve(items: List<RegimenItem>, online: Map<String, OnlineDrugInfo> = emptyMap()): List<ResolvedItem> =
        items.map { item ->
            val info = online[KnowledgeBase.normalize(item.name)]?.takeIf { it.found }
            val builtIn = kb.identify(item.name)
            when {
                builtIn.isNotEmpty() -> ResolvedItem(item, builtIn, info)
                info == null -> ResolvedItem(item, emptyList())
                else -> {
                    val viaGeneric = info.genericName?.let(kb::identify).orEmpty()
                    ResolvedItem(item, viaGeneric.ifEmpty { listOf(onlineSubstance(item, info)) }, info, identifiedOnline = true)
                }
            }
        }

    private fun onlineSubstance(item: RegimenItem, info: OnlineDrugInfo): Substance {
        val generic = info.genericName ?: item.name
        return Substance(
            id = "online:" + KnowledgeBase.normalize(generic).replace(' ', '_'),
            displayName = generic.replaceFirstChar { it.uppercase() },
            defaultType = item.type,
            aliases = listOf(generic) + info.brandNames,
            tags = PharmClassMapper.tagsFor(info.pharmClasses),
        )
    }

    fun analyze(items: List<RegimenItem>, online: Map<String, OnlineDrugInfo> = emptyMap()): AnalysisResult {
        val resolved = resolve(items, online)
        val changes = optimizer.optimise(resolved)
        val changesById = changes.associateBy { it.itemId }

        val findings = buildList {
            addAll(interactionFindings(resolved, changesById))
            addAll(labelFindings(resolved))
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

    // ---------------------------------------------------------------- drug label cross-check

    private data class LabelHit(val severity: Severity, val source: ResolvedItem, val section: String, val term: String, val snippet: String)

    /**
     * For pairs the built-in rules don't cover, checks whether one product's official label
     * (interactions, contraindications, warnings) mentions the other product or its class.
     */
    private fun labelFindings(resolved: List<ResolvedItem>): List<Finding> {
        val out = mutableListOf<Finding>()
        for (i in resolved.indices) for (j in i + 1 until resolved.size) {
            val a = resolved[i]
            val b = resolved[j]
            if (a.online == null && b.online == null) continue
            if (RuleMatcher.matching(kb.interactions, a.tags, b.tags).isNotEmpty()) continue
            if (a.substances.any { s -> b.substances.any { it.id == s.id } }) continue
            val hit = (labelMentions(a, b) + labelMentions(b, a)).minByOrNull { it.severity.ordinal } ?: continue
            val other = if (hit.source === a) b else a
            val sourceName = hit.source.online?.genericName ?: hit.source.item.name
            out += Finding(
                id = "label:${a.item.id}:${b.item.id}",
                category = FindingCategory.INTERACTION,
                severity = hit.severity,
                title = "Listed in the drug label: ${a.item.name} + ${b.item.name}",
                detail = "The official prescribing information for $sourceName mentions \"${hit.term}\" (relevant to ${other.item.name}) " +
                    "in its ${hit.section} section: \"${hit.snippet}\"",
                recommendation = "This comes from the product's US label rather than the app's own rules, so the severity is an estimate. " +
                    "Ask your pharmacist whether it applies to you.",
                itemIds = listOf(a.item.id, b.item.id),
            )
        }
        return out
    }

    private fun labelMentions(source: ResolvedItem, target: ResolvedItem): List<LabelHit> {
        val info = source.online ?: return emptyList()
        val specificTerms = buildSet {
            target.substances.forEach { s -> (s.aliases + s.displayName).forEach { add(KnowledgeBase.normalize(it)) } }
            target.online?.genericName?.let { add(KnowledgeBase.normalize(it)) }
            target.online?.brandNames?.forEach { add(KnowledgeBase.normalize(it)) }
            PharmClassMapper.lookupCandidates(target.item.name).forEach { add(it) }
        }.filter { it.length >= MIN_TERM_LENGTH }
        val classTerms = target.tags.flatMap { PharmClassMapper.CLASS_TERMS[it].orEmpty() }.distinct()

        val sections = listOf(
            Triple("contraindications", info.contraindicationsText, Severity.MAJOR to Severity.MODERATE),
            Triple("drug interactions", info.interactionsText, Severity.MODERATE to Severity.MINOR),
            Triple("warnings", info.warningsText, Severity.MODERATE to Severity.MINOR),
        )
        val hits = mutableListOf<LabelHit>()
        for ((section, text, severities) in sections) {
            if (text.isNullOrBlank()) continue
            findTerm(text, specificTerms)?.let { (term, snippet) -> hits += LabelHit(severities.first, source, section, term, snippet) }
                ?: findTerm(text, classTerms)?.let { (term, snippet) -> hits += LabelHit(severities.second, source, section, term, snippet) }
        }
        return hits
    }

    /** Finds the first sentence of [text] containing any of [terms] as whole words. */
    private fun findTerm(text: String, terms: Collection<String>): Pair<String, String>? {
        if (terms.isEmpty()) return null
        val termWords = terms.map { it to KnowledgeBase.tokenize(it) }.filter { it.second.isNotEmpty() }
        for (sentence in text.split(SENTENCE_BREAK)) {
            val words = KnowledgeBase.tokenize(sentence)
            for ((term, needle) in termWords) {
                var index = KnowledgeBase.indexOfSequence(words, needle)
                while (index >= 0) {
                    val next = words.getOrNull(index + needle.size)
                    if (FALSE_FOLLOWERS[term]?.contains(next) != true) {
                        val snippet = sentence.trim().replace(Regex("\\s+"), " ")
                        return term to if (snippet.length > MAX_SNIPPET) snippet.take(MAX_SNIPPET).trimEnd() + "…" else snippet
                    }
                    index = KnowledgeBase.indexOfSequence(words, needle, index + 1)
                }
            }
        }
        return null
    }

    // ---------------------------------------------------------------- duplicates

    private fun duplicateFindings(resolved: List<ResolvedItem>): List<Finding> {
        val substancesById = resolved.flatMap { it.substances }.associateBy { it.id }
        val bySubstance = resolved.flatMap { r -> r.substances.map { it to r } }
            .groupBy({ it.first.id }, { it.second })
        return bySubstance.filter { it.value.size > 1 }.map { (substanceId, items) ->
            val substance = substancesById.getValue(substanceId)
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
            val name = kb.substance(substanceId)?.displayName ?: continue
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
                title = "Not recognised: ${unknown.joinToString(", ") { it.item.name }}",
                detail = "These weren't found in the built-in database or (when online lookup is on) in the online sources, " +
                    "so interactions, timing and dose limits can't be checked. Check the spelling, or try the generic (non-brand) name.",
                recommendation = "Ask your pharmacist to review these alongside the rest of your list.",
                itemIds = unknown.map { it.item.id },
            )
        }
        val online = resolved.filter { it.identifiedOnline }
        if (online.isNotEmpty()) {
            out += Finding(
                id = "identified_online",
                category = FindingCategory.GENERAL,
                severity = Severity.INFO,
                title = "Identified using online sources: " + online.joinToString(", ") { r ->
                    val generic = r.online?.genericName
                    if (generic != null && !generic.equals(r.item.name, ignoreCase = true)) "${r.item.name} ($generic)" else r.item.name
                },
                detail = "These aren't in the built-in database, so they were identified from ${
                    online.flatMap { it.online?.sources.orEmpty() }.distinct().joinToString(" and ")
                }. Interaction checks use their drug class and official US label; dose limits and timing advice may be missing.",
                recommendation = "Open the item for the label details and links to the BNF and NHS pages.",
                itemIds = online.map { it.item.id },
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
        private const val MIN_TERM_LENGTH = 4
        private const val MAX_SNIPPET = 300
        private val SENTENCE_BREAK = Regex("(?<=[.;])\\s+|\\n+|•")

        /** Label words that look like a match but mean something else (e.g. "calcium channel blockers"). */
        private val FALSE_FOLLOWERS = mapOf(
            "calcium" to setOf("channel"),
            "potassium" to setOf("sparing", "channel"),
            "magnesium" to setOf("sulfate"),
            "insulin" to setOf("resistance", "secretion", "sensitivity"),
        )

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
