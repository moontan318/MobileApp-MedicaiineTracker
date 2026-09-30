package com.medtracker.core

import com.medtracker.core.analysis.RegimenAnalyzer
import com.medtracker.core.knowledge.KnowledgeBase
import com.medtracker.core.knowledge.Severity
import com.medtracker.core.model.DoseTime
import com.medtracker.core.model.DoseUnit
import com.medtracker.core.model.ItemType
import com.medtracker.core.model.RegimenItem
import com.medtracker.core.online.DrugInfoClient
import com.medtracker.core.online.HttpResult
import com.medtracker.core.online.OnlineDrugInfo
import com.medtracker.core.online.PharmClassMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OnlineLookupTest {
    private val kb = KnowledgeBase()
    private val analyzer = RegimenAnalyzer(kb)

    private fun item(id: String, name: String, amount: Double = 1.0, unit: DoseUnit = DoseUnit.MG, time: String = "08:00") =
        RegimenItem(id, name, ItemType.MEDICINE, amount, unit, listOf(DoseTime.parse(time)!!))

    private fun info(name: String, generic: String?, classes: List<String> = emptyList(), interactions: String? = null, contraindications: String? = null) =
        OnlineDrugInfo(
            query = KnowledgeBase.normalize(name), found = true, fetchedAtMillis = 0, genericName = generic,
            pharmClasses = classes, interactionsText = interactions, contraindicationsText = contraindications,
            sources = listOf(DrugInfoClient.OPENFDA_SOURCE),
        )

    // ---------------------------------------------------------------- built-in additions

    @Test
    fun allopurinolIsBuiltIn() {
        assertEquals(listOf("allopurinol"), kb.identify("Allopurinol 300mg tablets").map { it.id })
        assertEquals(listOf("allopurinol"), kb.identify("Zyloric").map { it.id })
    }

    @Test
    fun allopurinolWithAzathioprineIsMajor() {
        val result = analyzer.analyze(listOf(item("a", "Allopurinol", 300.0), item("z", "Azathioprine", 50.0)))
        assertTrue(result.findings.any { it.id == "ix:xo_thiopurine:a:z" && it.severity == Severity.MAJOR })
    }

    @Test
    fun allopurinolLargeSingleDoseSuggestsSplitting() {
        val result = analyzer.analyze(listOf(item("a", "Allopurinol", 600.0)))
        assertTrue(result.findings.any { it.id == "single:allopurinol:a" })
        assertTrue(result.findings.any { it.id == "food:allopurinol_food:a" })
    }

    // ---------------------------------------------------------------- online resolution

    @Test
    fun onlineGenericNameMapsBackToBuiltInSubstance() {
        val items = listOf(item("f", "Fenbid Forte"), item("w", "Warfarin", 3.0, time = "18:00"))
        val before = analyzer.analyze(items)
        assertTrue(before.findings.any { it.id == "unrecognised" })

        val online = mapOf("fenbid forte" to info("Fenbid Forte", "ibuprofen"))
        val after = analyzer.analyze(items, online)
        val resolved = after.resolved.first { it.item.id == "f" }
        assertTrue(resolved.identifiedOnline)
        assertEquals(listOf("ibuprofen"), resolved.substances.map { it.id })
        assertTrue(after.findings.any { it.severity == Severity.MAJOR && "bleeding" in it.title.lowercase() })
        assertTrue(after.findings.any { it.id == "identified_online" })
        assertFalse(after.findings.any { it.id == "unrecognised" })
    }

    @Test
    fun onlineDrugClassDrivesBuiltInRules() {
        val items = listOf(item("t", "Tapentadol", 50.0), item("d", "Diazepam", 5.0))
        val online = mapOf("tapentadol" to info("Tapentadol", "tapentadol", listOf("Opioid Agonist [EPC]")))
        val result = analyzer.analyze(items, online)
        assertTrue(result.findings.any { it.id == "ix:opioid_benzo:t:d" && it.severity == Severity.MAJOR })
    }

    @Test
    fun labelTextMentioningAnotherItemIsReported() {
        val items = listOf(
            item("x", "Obscurazole"),
            item("z", "Azathioprine", 50.0),
            item("c", "Calcium carbonate", 500.0, time = "13:00"),
        )
        val online = mapOf(
            "obscurazole" to info(
                "Obscurazole", "obscurazole",
                interactions = "7 DRUG INTERACTIONS. Calcium channel blockers: monitor blood pressure. " +
                    "Azathioprine: concomitant use increases the risk of myelosuppression.",
            )
        )
        val result = analyzer.analyze(items, online)
        val aza = result.findings.single { it.id == "label:x:z" }
        assertEquals(Severity.MODERATE, aza.severity)
        assertTrue("myelosuppression" in aza.detail)
        // "Calcium channel blockers" must not be read as a calcium supplement interaction.
        assertFalse(result.findings.any { it.id == "label:x:c" })
    }

    @Test
    fun contraindicationMentionIsMajor() {
        val items = listOf(item("x", "Obscurazole"), item("s", "Sildenafil", 50.0))
        val online = mapOf("obscurazole" to info("Obscurazole", "obscurazole", contraindications = "Do not use with sildenafil."))
        val result = analyzer.analyze(items, online)
        assertEquals(Severity.MAJOR, result.findings.single { it.id == "label:x:s" }.severity)
    }

    @Test
    fun cacheRoundTrips() {
        val original = info("Allopurinol", "allopurinol", listOf("Xanthine Oxidase Inhibitor [EPC]"), interactions = "Mercaptopurine...")
            .copy(labelSetId = "abc", brandNames = listOf("Zyloprim"))
        val decoded = OnlineDrugInfo.decodeCache(OnlineDrugInfo.encodeCache(mapOf(original.query to original)))
        assertEquals(original, decoded[original.query])
    }

    @Test
    fun lookupCandidatesStripStrengthAndMapUkNames() {
        assertEquals(listOf("acetaminophen", "paracetamol"), PharmClassMapper.lookupCandidates("Paracetamol 500mg tablets"))
        assertEquals(listOf("allopurinol"), PharmClassMapper.lookupCandidates("Allopurinol 100 mg"))
        assertEquals(setOf("xanthine_oxidase_inhibitor"), PharmClassMapper.tagsFor(listOf("Xanthine Oxidase Inhibitor [EPC]")))
    }

    // ---------------------------------------------------------------- client with a fake network

    private val allopurinolLabel = """
        {"meta":{},"results":[
          {"set_id":"combo-1","drug_interactions":["x"],"openfda":{"generic_name":["ALLOPURINOL AND LESINURAD"],"brand_name":["DUZALLO"]}},
          {"set_id":"set-123",
           "drug_interactions":["7 DRUG INTERACTIONS 7.1 Mercaptopurine/Azathioprine: reduce dose to one third."],
           "contraindications":["Patients who have developed a severe reaction."],
           "dosage_and_administration":["Start at 100 mg daily."],
           "openfda":{"generic_name":["ALLOPURINOL"],"brand_name":["ZYLOPRIM","Allopurinol"],
                      "pharm_class_epc":["Xanthine Oxidase Inhibitor [EPC]"],"pharm_class_moa":["Xanthine Oxidase Inhibitors [MoA]"]}}
        ]}
    """.trimIndent()

    @Test
    fun clientParsesOpenFdaLabelAndPrefersExactGeneric() {
        val client = DrugInfoClient(http = { url ->
            if ("api.fda.gov" in url && "allopurinol" in url) HttpResult(200, allopurinolLabel) else HttpResult(404, "{}")
        }, clock = { 42L })
        val info = client.lookup("Allopurinol 100mg tablets")
        assertTrue(info.found)
        assertEquals("allopurinol 100mg tablets", info.query)
        assertEquals("allopurinol", info.genericName)
        assertEquals("set-123", info.labelSetId)
        assertTrue(info.interactionsText!!.contains("Mercaptopurine"))
        assertTrue("Xanthine Oxidase Inhibitor [EPC]" in info.pharmClasses)
        assertEquals(listOf("ZYLOPRIM", "Allopurinol"), info.brandNames)
    }

    @Test
    fun clientFallsBackToRxNormForMisspellings() {
        val requested = mutableListOf<String>()
        val client = DrugInfoClient(http = { url ->
            requested += url
            when {
                "approximateTerm" in url -> HttpResult(200, """{"approximateGroup":{"candidate":[{"rxcui":"519","name":"allopurinol","rank":"1"}]}}""")
                "related.json" in url -> HttpResult(200, """{"relatedGroup":{"conceptGroup":[{"tty":"IN","conceptProperties":[{"rxcui":"519","name":"allopurinol"}]}]}}""")
                "api.fda.gov" in url && "%22allopurinol%22" in url -> HttpResult(200, allopurinolLabel)
                else -> HttpResult(404, """{"error":{"code":"NOT_FOUND"}}""")
            }
        })
        val info = client.lookup("alopurinol")
        assertTrue(info.found)
        assertEquals("alopurinol", info.query)
        assertEquals("allopurinol", info.genericName)
        assertEquals(listOf(DrugInfoClient.RXNORM_SOURCE, DrugInfoClient.OPENFDA_SOURCE), info.sources)
    }

    @Test
    fun clientRejectsUnrelatedRxNormSuggestions() {
        val client = DrugInfoClient(http = { url ->
            if ("approximateTerm" in url) HttpResult(200, """{"approximateGroup":{"candidate":[{"rxcui":"1","name":"tonic water"}]}}""")
            else HttpResult(404, "{}")
        })
        assertFalse(client.lookup("Grandma's tonic").found)
    }

    @Test
    fun liveLookupWhenEnabled() {
        // Runs only in CI (ONLINE_TESTS=1) because it needs internet access.
        if (System.getenv("ONLINE_TESTS") != "1") return
        val client = DrugInfoClient()
        val allopurinol = client.lookup("Allopurinol")
        assertTrue(allopurinol.found, "allopurinol not found")
        assertTrue(allopurinol.pharmClasses.any { "Xanthine" in it }, allopurinol.pharmClasses.toString())
        assertNotNull(allopurinol.interactionsText)
        println("allopurinol: ${allopurinol.genericName} ${allopurinol.pharmClasses} setId=${allopurinol.labelSetId}")

        val misspelt = client.lookup("alopurinol")
        assertEquals("allopurinol", misspelt.genericName)
        println("alopurinol -> ${misspelt.genericName} via ${misspelt.sources}")

        val paracetamol = client.lookup("Paracetamol 500mg")
        assertTrue(paracetamol.found)
        println("paracetamol -> ${paracetamol.genericName}")

        val tapentadol = client.lookup("Tapentadol")
        assertTrue(PharmClassMapper.tagsFor(tapentadol.pharmClasses).contains("opioid"), tapentadol.pharmClasses.toString())
        println("tapentadol -> ${tapentadol.pharmClasses}")
    }
}
