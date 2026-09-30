package com.medtracker.core.online

import com.medtracker.core.knowledge.KnowledgeBase

/**
 * Maps FDA pharmacologic class names (EPC / MOA) to the tags used by the built-in interaction
 * rules, so a medicine found online is still checked against class-level rules.
 */
object PharmClassMapper {

    private val MAPPINGS: List<Pair<String, Set<String>>> = listOf(
        "nonsteroidal anti-inflammatory" to setOf("nsaid"),
        "vitamin k antagonist" to setOf("anticoagulant", "vka"),
        "factor xa inhibitor" to setOf("anticoagulant", "doac"),
        "direct thrombin inhibitor" to setOf("anticoagulant", "doac"),
        "p2y12" to setOf("antiplatelet"),
        "platelet aggregation inhibitor" to setOf("antiplatelet"),
        "serotonin reuptake inhibitor" to setOf("ssri", "serotonergic"),
        "serotonin and norepinephrine reuptake inhibitor" to setOf("ssri", "serotonergic"),
        "tricyclic antidepressant" to setOf("serotonergic", "cns_depressant", "anticholinergic"),
        "opioid agonist" to setOf("opioid", "cns_depressant"),
        "benzodiazepine" to setOf("benzo_z", "cns_depressant"),
        "hmg-coa reductase inhibitor" to setOf("statin"),
        "proton pump inhibitor" to setOf("ppi"),
        "angiotensin converting enzyme inhibitor" to setOf("acei", "raises_potassium", "antihypertensive"),
        "angiotensin 2 receptor blocker" to setOf("arb", "raises_potassium", "antihypertensive"),
        "aldosterone antagonist" to setOf("raises_potassium", "diuretic", "antihypertensive"),
        "thiazide" to setOf("thiazide", "diuretic", "lowers_potassium", "antihypertensive"),
        "loop diuretic" to setOf("diuretic", "lowers_potassium", "antihypertensive"),
        "beta adrenergic blocker" to setOf("beta_blocker", "antihypertensive"),
        "calcium channel blocker" to setOf("antihypertensive"),
        "alpha-1 adrenergic antagonist" to setOf("alpha_blocker"),
        "fluoroquinolone" to setOf("quinolone", "antibiotic"),
        "tetracycline-class" to setOf("tetracycline", "antibiotic"),
        "macrolide" to setOf("macrolide", "antibiotic"),
        "penicillin-class" to setOf("antibiotic"),
        "cephalosporin" to setOf("antibiotic"),
        "antibacterial" to setOf("antibiotic"),
        "bisphosphonate" to setOf("bisphosphonate"),
        "corticosteroid" to setOf("corticosteroid"),
        "sulfonylurea" to setOf("antidiabetic", "sulfonylurea"),
        "biguanide" to setOf("antidiabetic"),
        "insulin" to setOf("antidiabetic"),
        "dipeptidyl peptidase 4 inhibitor" to setOf("antidiabetic"),
        "glp-1 receptor agonist" to setOf("antidiabetic"),
        "sodium-glucose cotransporter 2 inhibitor" to setOf("antidiabetic", "sglt2"),
        "phosphodiesterase 5 inhibitor" to setOf("pde5"),
        "nitrate vasodilator" to setOf("nitrate"),
        "xanthine oxidase inhibitor" to setOf("xanthine_oxidase_inhibitor"),
        "thyroxine" to setOf("thyroid"),
        "serotonin-1b and serotonin-1d receptor agonist" to setOf("serotonergic"),
        "atypical antipsychotic" to setOf("antipsychotic", "cns_depressant"),
        "typical antipsychotic" to setOf("antipsychotic", "cns_depressant"),
        "anti-epileptic" to setOf("antiepileptic"),
        "cholinesterase inhibitor" to setOf("cholinesterase_inhibitor"),
        "cytochrome p450 3a4 inhibitor" to setOf("cyp3a4_inhibitor"),
        "cytochrome p450 3a4 inducer" to setOf("enzyme_inducer"),
        "cytochrome p450 2d6 inhibitor" to setOf("cyp2d6_strong"),
        "histamine-1 receptor antagonist" to setOf("antihistamine"),
        "cholinergic muscarinic antagonist" to setOf("anticholinergic"),
        "anticholinergic" to setOf("anticholinergic"),
        "calcineurin inhibitor immunosuppressant" to setOf("immunosuppressant"),
        "potassium compound" to setOf("potassium"),
    )

    fun tagsFor(classes: List<String>): Set<String> {
        val lower = classes.map { it.lowercase() }
        return MAPPINGS.filter { (needle, _) -> lower.any { needle in it } }.flatMap { it.second }.toSet()
    }

    /**
     * Terms searched for in another product's label text when checking whether that label
     * mentions an item with one of these tags. Specific names are added separately.
     */
    val CLASS_TERMS: Map<String, List<String>> = mapOf(
        "nsaid" to listOf("nsaid", "nsaids", "nonsteroidal anti inflammatory"),
        "anticoagulant" to listOf("anticoagulant", "anticoagulants", "warfarin"),
        "antiplatelet" to listOf("antiplatelet", "antiplatelet agents", "aspirin"),
        "ssri" to listOf("ssri", "ssris", "snri", "snris", "serotonin reuptake inhibitors"),
        "serotonergic" to listOf("serotonergic drugs", "serotonergic"),
        "acei" to listOf("ace inhibitor", "ace inhibitors", "angiotensin converting enzyme inhibitors"),
        "arb" to listOf("angiotensin receptor blockers", "angiotensin ii receptor blockers", "arbs"),
        "diuretic" to listOf("diuretic", "diuretics"),
        "thiazide" to listOf("thiazide", "thiazides", "thiazide diuretics"),
        "antacid" to listOf("antacid", "antacids"),
        "polyvalent_cation" to listOf("polyvalent cations", "multivalent cations", "antacids"),
        "opioid" to listOf("opioid", "opioids", "opioid analgesics"),
        "benzo_z" to listOf("benzodiazepine", "benzodiazepines"),
        "cns_depressant" to listOf("cns depressants", "central nervous system depressants"),
        "statin" to listOf("statin", "statins", "hmg coa reductase inhibitors"),
        "antidiabetic" to listOf("antidiabetic", "hypoglycemic agents", "insulin", "sulfonylureas"),
        "corticosteroid" to listOf("corticosteroid", "corticosteroids"),
        "ppi" to listOf("proton pump inhibitor", "proton pump inhibitors"),
        "immunosuppressant" to listOf("immunosuppressant", "immunosuppressants", "immunosuppressive agents"),
        "hormonal_contraceptive" to listOf("oral contraceptive", "oral contraceptives", "hormonal contraceptives"),
        "potassium" to listOf("potassium supplements", "potassium supplementation", "potassium containing"),
        "thyroid" to listOf("thyroid hormones", "levothyroxine"),
        "beta_blocker" to listOf("beta blocker", "beta blockers", "beta adrenergic blocking agents"),
        "cyp3a4_inhibitor" to listOf("cyp3a4 inhibitors", "strong cyp3a4 inhibitors"),
        "enzyme_inducer" to listOf("cyp3a4 inducers", "enzyme inducers"),
        "thiopurine" to listOf("azathioprine", "mercaptopurine"),
        "xanthine_oxidase_inhibitor" to listOf("xanthine oxidase inhibitors", "allopurinol"),
        "antiepileptic" to listOf("anticonvulsants", "antiepileptic drugs"),
        "anticholinergic" to listOf("anticholinergic", "anticholinergics", "anticholinergic drugs"),
        "qt" to listOf("qt prolongation", "prolong the qt interval"),
        "lithium" to listOf("lithium"),
        "vitamin_k" to listOf("vitamin k"),
        "grapefruit" to listOf("grapefruit", "grapefruit juice"),
    )

    /** UK (British Approved) names whose US name differs, to improve lookups. */
    val UK_TO_US: Map<String, String> = mapOf(
        "paracetamol" to "acetaminophen",
        "co codamol" to "acetaminophen and codeine",
        "salbutamol" to "albuterol",
        "adrenaline" to "epinephrine",
        "noradrenaline" to "norepinephrine",
        "glyceryl trinitrate" to "nitroglycerin",
        "frusemide" to "furosemide",
        "colecalciferol" to "cholecalciferol",
        "ciclosporin" to "cyclosporine",
        "co amoxiclav" to "amoxicillin and clavulanate potassium",
        "amoxycillin" to "amoxicillin",
        "cefalexin" to "cephalexin",
        "chlorphenamine" to "chlorpheniramine",
        "sodium valproate" to "valproic acid",
        "thyroxine" to "levothyroxine",
        "alendronic acid" to "alendronate",
        "risedronic acid" to "risedronate",
        "ibandronic acid" to "ibandronate",
        "glibenclamide" to "glyburide",
        "pethidine" to "meperidine",
        "mesalazine" to "mesalamine",
        "beclometasone" to "beclomethasone",
        "betametasone" to "betamethasone",
        "hydroxycarbamide" to "hydroxyurea",
        "oestradiol" to "estradiol",
        "ethinylestradiol" to "ethinyl estradiol",
        "rifampicin" to "rifampin",
        "phenoxymethylpenicillin" to "penicillin v",
        "lignocaine" to "lidocaine",
        "dosulepin" to "dothiepin",
        "clomifene" to "clomiphene",
        "levomepromazine" to "methotrimeprazine",
        "co careldopa" to "carbidopa and levodopa",
        "co beneldopa" to "benserazide and levodopa",
    )

    private val STRENGTH = Regex("\\d+(mg|mcg|g|ml|iu|microgram|micrograms)?")

    /** Words that describe strength or form rather than the medicine itself. */
    private val FORM_WORDS = setOf(
        "mg", "mcg", "microgram", "micrograms", "g", "ml", "iu", "tablet", "tablets", "tab", "tabs",
        "capsule", "capsules", "caps", "mr", "xl", "sr", "er", "la", "cr", "modified", "release", "prolonged",
        "film", "coated", "oral", "solution", "suspension", "liquid", "syrup", "cream", "gel", "ointment",
        "dispersible", "effervescent", "soluble", "chewable", "gastro", "resistant", "ec", "sachet", "sachets",
        "injection", "inhaler", "spray", "patch", "patches", "drops", "once", "daily", "a", "day",
    )

    /** Candidate names to look up online for a user-entered product name, best first. */
    fun lookupCandidates(name: String): List<String> {
        val cleaned = KnowledgeBase.tokenize(name)
            .filter { it !in FORM_WORDS && !STRENGTH.matches(it) }
            .joinToString(" ")
        if (cleaned.isEmpty()) return emptyList()
        val mapped = UK_TO_US[cleaned]
        return listOfNotNull(mapped, cleaned).distinct()
    }
}
