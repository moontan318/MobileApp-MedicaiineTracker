package com.medtracker.core

import com.medtracker.core.analysis.RegimenAnalyzer
import com.medtracker.core.knowledge.Homeopathy
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.DoseUnit
import com.medtracker.core.model.ItemType
import com.medtracker.core.model.RegimenItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HerbalHomeopathicTest {
    private val kb = KnowledgeBase()
    private val analyzer = RegimenAnalyzer(kb)

    private fun item(id: String, name: String, type: ItemType, time: String = "08:00", amount: Double = 1.0, unit: DoseUnit = DoseUnit.CAPSULE) =
        RegimenItem(id, name, type, amount, unit, listOf(DoseTime.parse(time)!!))

    // ---------------------------------------------------------------- herbal

    @Test
    fun blackSeedOilIsRecognisedUnderCommonNames() {
        for (name in listOf("Black Seed Oil", "Nigella sativa capsules", "Kalonji oil", "Black cumin seed oil")) {
            assertEquals(listOf("black_seed"), kb.identify(name).map { it.id }, name)
        }
    }

    @Test
    fun blackSeedOilInteractsWithDiabetesBloodPressureAndAnticoagulants() {
        val result = analyzer.analyze(
            listOf(
                item("b", "Black seed oil", ItemType.HERBAL),
                item("m", "Metformin", ItemType.MEDICINE),
                item("a", "Amlodipine", ItemType.MEDICINE),
                item("w", "Warfarin", ItemType.MEDICINE, time = "18:00"),
            )
        )
        val ids = result.findings.map { it.id }
        assertTrue("ix:diabetes_supp:b:m" in ids, ids.toString())
        assertTrue("ix:herb_bp_lowering:b:a" in ids, ids.toString())
        assertTrue("ix:bleeding_supplements:b:w" in ids, ids.toString())
    }

    @Test
    fun hawthornBerryExtractWithDigoxinAndNitrates() {
        assertEquals(listOf("hawthorn"), kb.identify("Hawthorn berry extract").map { it.id })
        val result = analyzer.analyze(
            listOf(
                item("h", "Hawthorn berry extract", ItemType.HERBAL),
                item("d", "Digoxin", ItemType.MEDICINE),
                item("n", "Isosorbide mononitrate", ItemType.MEDICINE),
            )
        )
        assertEquals(Severity.MODERATE, result.findings.single { it.id == "ix:hawthorn_digoxin:h:d" }.severity)
        assertTrue(result.findings.any { it.id == "ix:hawthorn_vasodilators:h:n" })
    }

    @Test
    fun goldensealTriggersStatinEnzymeRule() {
        val result = analyzer.analyze(listOf(item("g", "Goldenseal", ItemType.HERBAL), item("s", "Simvastatin", ItemType.MEDICINE, "20:00")))
        assertTrue(result.findings.any { it.id == "ix:statin_3a4:g:s" && it.severity == Severity.MAJOR })
    }

    @Test
    fun charcoalMustBeSeparatedFromEverything() {
        val result = analyzer.analyze(
            listOf(
                item("c", "Activated charcoal", ItemType.OTHER_SUPPLEMENT),
                item("x", "Some unknown tablet", ItemType.MEDICINE),
            )
        )
        assertTrue(result.findings.any { it.id == "sep:sep_charcoal:c:x" })
        val moved = assertNotNull(result.changeFor("c")).suggestedTimes.single()
        assertTrue(DoseTime.circularDistance(moved, DoseTime.of(8)) >= 120)
    }

    @Test
    fun psylliumSeparatedFromLevothyroxine() {
        val result = analyzer.analyze(
            listOf(
                item("l", "Levothyroxine", ItemType.MEDICINE, "07:00"),
                item("p", "Psyllium husk", ItemType.HERBAL, "07:00"),
            )
        )
        assertTrue(result.findings.any { it.id == "sep:sep_fibre:l:p" })
    }

    @Test
    fun chlorellaAffectsWarfarin() {
        val result = analyzer.analyze(listOf(item("w", "Warfarin", ItemType.MEDICINE, "18:00"), item("c", "Chlorella", ItemType.OTHER_SUPPLEMENT)))
        assertTrue(result.findings.any { it.id == "ix:warfarin_vitk:w:c" })
    }

    // ---------------------------------------------------------------- homeopathic

    @Test
    fun potencyDetection() {
        assertEquals(Homeopathy.Info("30C", false), Homeopathy.detect("Arnica 30C"))
        assertEquals(Homeopathy.Info("6X", true), Homeopathy.detect("Nux vomica 6x"))
        assertEquals(Homeopathy.Info("200CH", false), Homeopathy.detect("Belladonna 200ch"))
        assertEquals(true, Homeopathy.detect("Calendula mother tincture homeopathic")?.lowPotency)
        assertNotNull(Homeopathy.detect("Homeopathic sleep drops"))
        assertNull(Homeopathy.detect("Vitamin D3 1000iu"))
        assertNull(Homeopathy.detect("Omega 3 1000mg"))
    }

    @Test
    fun homeopathicHypericumDoesNotTriggerStJohnsWortInteractions() {
        val result = analyzer.analyze(
            listOf(
                item("h", "Hypericum 30C", ItemType.HOMEOPATHIC),
                item("s", "Sertraline", ItemType.MEDICINE),
                item("w", "Warfarin", ItemType.MEDICINE, "18:00"),
            )
        )
        assertFalse(result.findings.any { "h" in it.itemIds && it.category.name == "INTERACTION" }, result.findings.toString())
        val resolved = result.resolved.first { it.item.id == "h" }
        assertEquals("St John's wort (homeopathic)", resolved.substances.single().displayName)
        assertTrue(result.findings.any { it.id == "note:homeopathic_note:h" && it.severity == Severity.INFO })
    }

    @Test
    fun herbalStJohnsWortStillInteracts() {
        val result = analyzer.analyze(listOf(item("h", "Hypericum perforatum 300mg", ItemType.HERBAL), item("s", "Sertraline", ItemType.MEDICINE)))
        assertTrue(result.findings.any { it.severity == Severity.MAJOR && "serotonin" in it.title.lowercase() })
    }

    @Test
    fun lowPotencyToxicRemedyIsFlagged() {
        val low = analyzer.analyze(listOf(item("n", "Nux vomica 3X", ItemType.HOMEOPATHIC)))
        assertTrue(low.findings.any { it.id == "note:homeopathic_low_toxic:n" && it.severity == Severity.MODERATE })

        val high = analyzer.analyze(listOf(item("n", "Nux vomica 30C", ItemType.HOMEOPATHIC)))
        assertFalse(high.findings.any { it.id.startsWith("note:homeopathic_low") })
    }

    @Test
    fun unknownHomeopathicRemediesAreNotDuplicatesOrUnrecognised() {
        val result = analyzer.analyze(
            listOf(
                item("a", "Mystery remedy 30C", ItemType.HOMEOPATHIC),
                item("b", "Another remedy 6C", ItemType.HOMEOPATHIC),
            )
        )
        assertFalse(result.findings.any { it.id == "unrecognised" || it.id.startsWith("dup:") }, result.findings.toString())
    }
}
