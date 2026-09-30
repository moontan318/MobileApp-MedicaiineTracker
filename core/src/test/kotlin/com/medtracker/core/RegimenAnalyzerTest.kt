package com.medtracker.core

import com.medtracker.core.analysis.FindingCategory
import com.medtracker.core.analysis.RegimenAnalyzer
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.DoseUnit
import com.medtracker.core.model.FoodRelation
import com.medtracker.core.model.ItemType
import com.medtracker.core.model.RegimenItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegimenAnalyzerTest {
    private val kb = KnowledgeBase()
    private val analyzer = RegimenAnalyzer(kb)

    private fun item(
        id: String,
        name: String,
        amount: Double,
        unit: DoseUnit,
        vararg times: String,
        type: ItemType = ItemType.MEDICINE,
        food: FoodRelation = FoodRelation.ANY,
    ) = RegimenItem(id, name, type, amount, unit, times.map { DoseTime.parse(it)!! }, food)

    @Test
    fun identifiesBrandNamesAndCombinationProducts() {
        assertEquals(listOf("ibuprofen"), kb.identify("Nurofen Express").map { it.id })
        assertEquals(setOf("calcium", "vitamin_d"), kb.identify("Calcium + Vitamin D3").map { it.id }.toSet())
        assertEquals(listOf("st_johns_wort"), kb.identify("St. John's Wort").map { it.id })
        // "vitamin b" must not match vitamin B12
        assertEquals(listOf("vitamin_b12"), kb.identify("Vitamin B12").map { it.id })
        assertTrue(kb.identify("Mystery Tonic").isEmpty())
    }

    @Test
    fun warfarinAndIbuprofenIsMajor() {
        val result = analyzer.analyze(
            listOf(
                item("w", "Warfarin", 3.0, DoseUnit.MG, "18:00"),
                item("i", "Ibuprofen", 400.0, DoseUnit.MG, "08:00", "16:00", food = FoodRelation.WITH_FOOD),
            )
        )
        val ix = result.findings.filter { it.category == FindingCategory.INTERACTION }
        assertTrue(ix.any { it.severity == Severity.MAJOR && "bleeding" in it.title.lowercase() })
        // Only one bleeding finding for the pair, even though several rules match.
        assertEquals(1, ix.count { it.itemIds.toSet() == setOf("w", "i") && "bleeding" in it.title.lowercase() })
    }

    @Test
    fun levothyroxineAndCalciumAreSeparated() {
        val result = analyzer.analyze(
            listOf(
                item("l", "Levothyroxine", 100.0, DoseUnit.MCG, "07:00", food = FoodRelation.EMPTY_STOMACH),
                item("c", "Calcium carbonate", 500.0, DoseUnit.MG, "07:30", type = ItemType.MINERAL, food = FoodRelation.WITH_FOOD),
            )
        )
        val sep = result.findings.single { it.id.startsWith("sep:sep_thyroid_cation") }
        assertEquals(Severity.MODERATE, sep.severity)

        // Levothyroxine (a medicine) stays put; calcium moves at least 4 hours away.
        assertNull(result.changeFor("l"))
        val change = assertNotNull(result.changeFor("c"))
        val newTime = change.suggestedTimes.single()
        assertTrue(DoseTime.circularDistance(newTime, DoseTime.of(7)) >= 240, "was $newTime")
        assertTrue("Calcium carbonate" in sep.recommendation)
    }

    @Test
    fun wellSeparatedProductsDoNotRaiseTimingFinding() {
        val result = analyzer.analyze(
            listOf(
                item("l", "Levothyroxine", 100.0, DoseUnit.MCG, "07:00", food = FoodRelation.EMPTY_STOMACH),
                item("c", "Calcium", 500.0, DoseUnit.MG, "13:00", type = ItemType.MINERAL, food = FoodRelation.WITH_FOOD),
            )
        )
        assertFalse(result.findings.any { it.id.startsWith("sep:") })
        assertTrue(result.scheduleChanges.isEmpty())
    }

    @Test
    fun simvastatinInMorningIsMovedToEvening() {
        val result = analyzer.analyze(listOf(item("s", "Simvastatin", 40.0, DoseUnit.MG, "08:00")))
        assertTrue(result.findings.any { it.id == "time:statin_evening:s" })
        val suggested = assertNotNull(result.changeFor("s")).suggestedTimes.single()
        assertTrue(suggested.hour >= 18, "was $suggested")
    }

    @Test
    fun paracetamolOverdoseAcrossProductsIsDetected() {
        val result = analyzer.analyze(
            listOf(
                item("p1", "Paracetamol", 1000.0, DoseUnit.MG, "06:00", "10:00", "14:00", "18:00"),
                item("p2", "Panadol Extra", 1000.0, DoseUnit.MG, "22:00"),
            )
        )
        val daily = result.findings.single { it.id == "daily:paracetamol" }
        assertEquals(Severity.MAJOR, daily.severity)
        assertTrue("5000 mg" in daily.title)
        assertTrue(result.findings.any { it.id == "dup:paracetamol" })
    }

    @Test
    fun vitaminDInIuIsConverted() {
        val ok = analyzer.analyze(listOf(item("d", "Vitamin D3", 1000.0, DoseUnit.IU, "09:00", type = ItemType.VITAMIN)))
        assertFalse(ok.findings.any { it.id.startsWith("daily:") })

        val high = analyzer.analyze(listOf(item("d", "Vitamin D3", 10000.0, DoseUnit.IU, "09:00", type = ItemType.VITAMIN)))
        val daily = high.findings.single { it.id == "daily:vitamin_d" }
        assertTrue("250 mcg" in daily.title, daily.title)
    }

    @Test
    fun calciumSingleDoseAboveAbsorptionLimitSuggestsSplitting() {
        val result = analyzer.analyze(listOf(item("c", "Calcium", 1200.0, DoseUnit.MG, "12:00", type = ItemType.MINERAL)))
        val single = result.findings.single { it.id == "single:calcium:c" }
        assertEquals(Severity.MINOR, single.severity)
        assertTrue("3 smaller doses" in single.recommendation)
    }

    @Test
    fun tabletsRequestStrength() {
        val result = analyzer.analyze(listOf(item("z", "Zinc", 1.0, DoseUnit.TABLET, "09:00", type = ItemType.MINERAL)))
        assertTrue(result.findings.any { it.id == "strength:z" })
    }

    @Test
    fun serotoninFindingsCollapseToMostSevere() {
        val result = analyzer.analyze(
            listOf(
                item("s", "Sertraline", 50.0, DoseUnit.MG, "08:00"),
                item("j", "St John's Wort", 300.0, DoseUnit.MG, "08:00", type = ItemType.HERBAL),
            )
        )
        val serotonin = result.findings.filter { "serotonin" in it.title.lowercase() }
        assertEquals(1, serotonin.size)
        assertEquals(Severity.MAJOR, serotonin.single().severity)
    }

    @Test
    fun paracetamolDosesTooCloseAreRespaced() {
        val result = analyzer.analyze(
            listOf(item("p", "Paracetamol", 500.0, DoseUnit.MG, "08:00", "09:00", "12:00"))
        )
        assertTrue(result.findings.any { it.id == "gap:paracetamol" })
        val times = assertNotNull(result.changeFor("p")).suggestedTimes
        times.zipWithNext().forEach { (a, b) -> assertTrue(b.minuteOfDay - a.minuteOfDay >= 240, "$times") }
    }

    @Test
    fun foodAdviceFlagsConflicts() {
        val result = analyzer.analyze(
            listOf(item("i", "Ibuprofen", 200.0, DoseUnit.MG, "08:00", food = FoodRelation.EMPTY_STOMACH))
        )
        val food = result.findings.single { it.id == "food:nsaid_food:i" }
        assertEquals(Severity.MODERATE, food.severity)
    }

    @Test
    fun unknownItemsAreReported() {
        val result = analyzer.analyze(listOf(item("x", "Grandma's Tonic", 5.0, DoseUnit.ML, "08:00", type = ItemType.OTHER_SUPPLEMENT)))
        assertTrue(result.findings.any { it.id == "unrecognised" })
    }

    @Test
    fun infoWindowsDoNotForceScheduleChanges() {
        val result = analyzer.analyze(listOf(item("m", "Magnesium glycinate", 200.0, DoseUnit.MG, "08:00", type = ItemType.MINERAL)))
        assertTrue(result.findings.any { it.id == "time:magnesium_evening:m" && it.severity == Severity.INFO })
        assertNull(result.changeFor("m"))
    }

    @Test
    fun suggestionsListIncludesAutocompleteForBrands() {
        val labels = kb.suggest("nuro").map { it.second.id }
        assertTrue("ibuprofen" in labels)
    }
}
