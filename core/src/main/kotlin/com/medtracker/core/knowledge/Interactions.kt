package com.medtracker.core.knowledge

import com.medtracker.core.knowledge.Severity.INFO
import com.medtracker.core.knowledge.Severity.MAJOR
import com.medtracker.core.knowledge.Severity.MINOR
import com.medtracker.core.knowledge.Severity.MODERATE

private fun set(vararg tags: String) = tags.toSet()

/**
 * Interaction rules between pairs of substances or classes. Content is a simplified summary of
 * widely published interaction guidance and is not a substitute for a pharmacist's review.
 */
internal val INTERACTIONS: List<InteractionRule> = listOf(
    // ---------- Bleeding risk ----------
    InteractionRule(
        id = "anticoag_nsaid", sideA = set("anticoagulant"), sideB = set("nsaid", "antiplatelet"), severity = MAJOR,
        title = "Increased bleeding risk",
        effect = "Combining an anticoagulant with an anti-inflammatory painkiller or antiplatelet greatly increases the risk of serious bleeding, including stomach bleeds.",
        advice = "Avoid unless your prescriber has specifically advised it. Paracetamol is usually a safer painkiller - check with your pharmacist.",
        topic = "bleeding",
    ),
    InteractionRule(
        id = "antiplatelet_nsaid", sideA = set("antiplatelet"), sideB = set("nsaid"), severity = MODERATE,
        title = "Increased bleeding risk",
        effect = "Anti-inflammatory painkillers add to the bleeding risk of antiplatelets. Ibuprofen can also block the heart-protective effect of low-dose aspirin.",
        advice = "Use the lowest dose for the shortest time. If you take low-dose aspirin, take it at least 30 minutes before ibuprofen, or ibuprofen at least 8 hours before aspirin.",
        topic = "bleeding",
    ),
    InteractionRule(
        id = "antiplatelet_dual", sideA = set("antiplatelet"), sideB = set("antiplatelet"), severity = MODERATE,
        title = "Two antiplatelet medicines",
        effect = "Dual antiplatelet therapy increases bleeding risk.",
        advice = "This is often intentional (e.g. after a stent) - confirm with your prescriber that both are meant to be taken and for how long.",
        topic = "bleeding",
    ),
    InteractionRule(
        id = "bleeding_supplements", sideA = set("anticoagulant", "antiplatelet"), sideB = set("bleeding_supplement"), severity = MODERATE,
        title = "Supplement may increase bleeding",
        effect = "Ginkgo, garlic, turmeric, high-dose fish oil and high-dose vitamin E have mild blood-thinning effects that add to anticoagulant/antiplatelet medicines.",
        advice = "Tell your prescriber or anticoagulation clinic. Watch for unusual bruising or bleeding and stop the supplement before surgery.",
        topic = "bleeding_supp",
    ),
    InteractionRule(
        id = "nsaid_bleeding_supp", sideA = set("nsaid"), sideB = set("bleeding_supplement"), severity = MINOR,
        title = "Slightly increased bleeding risk",
        effect = "Some supplements have mild anti-clotting effects that add to anti-inflammatory painkillers.",
        advice = "Usually fine for short courses. Watch for bruising and avoid long-term combined use without advice.",
        topic = "bleeding_supp",
    ),
    InteractionRule(
        id = "ssri_nsaid", sideA = set("ssri"), sideB = set("nsaid", "anticoagulant"), severity = MODERATE,
        title = "Increased stomach-bleeding risk",
        effect = "SSRI/SNRI antidepressants reduce platelet function; combined with anti-inflammatories or anticoagulants the risk of bleeding rises.",
        advice = "Prefer paracetamol for pain where possible. If an NSAID is needed, ask about stomach protection (e.g. a PPI).",
        topic = "bleeding",
    ),

    // ---------- Warfarin specifics ----------
    InteractionRule(
        id = "warfarin_vitk", sideA = set("vka"), sideB = set("vitamin_k", "multivitamin"), severity = MODERATE,
        title = "Vitamin K affects warfarin",
        effect = "Vitamin K directly counteracts warfarin; changing intake can make your INR unstable.",
        advice = "Don't start or stop vitamin K-containing supplements without telling your anticoagulation clinic. Keep intake consistent day to day.",
    ),
    InteractionRule(
        id = "warfarin_coq10", sideA = set("vka"), sideB = set("coq10"), severity = MODERATE,
        title = "CoQ10 may reduce warfarin's effect",
        effect = "Coenzyme Q10 is structurally similar to vitamin K and may lower INR.",
        advice = "Tell your anticoagulation clinic; extra INR checks may be needed when starting or stopping.",
    ),
    InteractionRule(
        id = "warfarin_misc", sideA = set("vka"), sideB = set("cranberry", "glucosamine"), severity = MODERATE,
        title = "May increase warfarin's effect",
        effect = "Cranberry and glucosamine have been reported to raise INR in some people taking warfarin.",
        advice = "Avoid large amounts, or arrange extra INR monitoring.",
    ),
    InteractionRule(
        id = "sjw_anticoag", sideA = set("enzyme_inducer"), sideB = set("anticoagulant"), severity = MAJOR,
        title = "St John's wort weakens anticoagulants",
        effect = "St John's wort speeds up breakdown of warfarin and the newer anticoagulants, increasing the risk of clots and stroke.",
        advice = "Do not combine. Speak to your prescriber before stopping St John's wort, as levels can then rise.",
    ),

    // ---------- Serotonin syndrome ----------
    InteractionRule(
        id = "serotonin_major", sideA = set("ssri"), sideB = set("st_johns_wort", "five_htp", "tramadol"), severity = MAJOR,
        title = "Risk of serotonin syndrome",
        effect = "Both increase serotonin. Symptoms of serotonin syndrome include agitation, sweating, shivering, fast heartbeat and muscle twitching.",
        advice = "Avoid this combination unless supervised by your prescriber. Seek urgent help if symptoms appear.",
        topic = "serotonin",
    ),
    InteractionRule(
        id = "serotonin_moderate", sideA = set("serotonergic"), sideB = set("serotonergic"), severity = MODERATE,
        title = "Additive serotonin effects",
        effect = "Both products raise serotonin levels; together there is a small risk of serotonin syndrome.",
        advice = "Often prescribed together deliberately - make sure your prescriber knows about both, and be aware of symptoms (agitation, sweating, tremor, fast heartbeat).",
        topic = "serotonin",
    ),

    // ---------- St John's wort (enzyme inducer) ----------
    InteractionRule(
        id = "sjw_contraceptive", sideA = set("enzyme_inducer"), sideB = set("hormonal_contraceptive"), severity = MAJOR,
        title = "Contraception may fail",
        effect = "St John's wort reduces hormone levels from contraceptive pills, implants and patches.",
        advice = "Use additional non-hormonal contraception, or stop St John's wort (continue precautions for 28 days after stopping).",
    ),
    InteractionRule(
        id = "sjw_critical", sideA = set("enzyme_inducer"), sideB = set("digoxin", "immunosuppressant"), severity = MAJOR,
        title = "St John's wort lowers medicine levels",
        effect = "St John's wort can lower blood levels of this medicine enough to make it ineffective.",
        advice = "Avoid the combination and speak to your prescriber.",
    ),

    // ---------- Sedation ----------
    InteractionRule(
        id = "opioid_benzo", sideA = set("opioid"), sideB = set("benzo_z"), severity = MAJOR,
        title = "Dangerous sedation and slowed breathing",
        effect = "Opioids combined with benzodiazepines or 'Z' sleeping tablets can cause profound drowsiness, slowed breathing and death.",
        advice = "Only take together if your prescriber has confirmed it. Avoid alcohol, and never exceed prescribed doses.",
        topic = "sedation",
    ),
    InteractionRule(
        id = "cns_cns", sideA = set("cns_depressant"), sideB = set("cns_depressant"), severity = MODERATE,
        title = "Additive drowsiness",
        effect = "Both can cause drowsiness; together they may impair driving, balance and breathing.",
        advice = "Check with your prescriber. Avoid alcohol and take care driving or operating machinery.",
        topic = "sedation",
    ),
    InteractionRule(
        id = "cns_herb", sideA = set("cns_depressant"), sideB = set("sedative_herb"), severity = MODERATE,
        title = "Additive drowsiness",
        effect = "Sedating herbs and sleep supplements add to the drowsiness of this medicine.",
        advice = "Avoid combining, or discuss with your pharmacist. Do not drive if you feel drowsy.",
        topic = "sedation",
    ),
    InteractionRule(
        id = "herb_herb", sideA = set("sedative_herb"), sideB = set("sedative_herb"), severity = MINOR,
        title = "Two calming/sleep supplements",
        effect = "Combining calming supplements increases drowsiness.",
        advice = "Usually one product is enough. Take care the next morning.",
        topic = "sedation",
    ),

    // ---------- Lithium / methotrexate ----------
    InteractionRule(
        id = "lithium_nsaid", sideA = set("lithium"), sideB = set("nsaid", "acei", "arb", "diuretic"), severity = MAJOR,
        title = "Lithium levels may rise to toxic levels",
        effect = "This medicine reduces lithium excretion by the kidneys.",
        advice = "Only combine under close supervision with lithium level monitoring. Avoid over-the-counter NSAIDs.",
    ),
    InteractionRule(
        id = "mtx_nsaid", sideA = set("methotrexate"), sideB = set("nsaid"), severity = MAJOR,
        title = "Methotrexate toxicity",
        effect = "NSAIDs reduce methotrexate clearance, raising the risk of serious side effects.",
        advice = "Do not take over-the-counter NSAIDs without checking with your specialist.",
    ),
    InteractionRule(
        id = "mtx_folate", sideA = set("methotrexate"), sideB = set("folate"), severity = INFO,
        title = "Folic acid with methotrexate",
        effect = "Folic acid is usually prescribed to reduce methotrexate side effects.",
        advice = "Take folic acid exactly as prescribed - normally NOT on the same day as your weekly methotrexate dose.",
    ),

    // ---------- Potassium & kidneys ----------
    InteractionRule(
        id = "k_raise_supp", sideA = set("raises_potassium"), sideB = set("potassium"), severity = MAJOR,
        title = "Risk of high potassium",
        effect = "This medicine already raises potassium; adding potassium supplements can cause dangerous heart rhythm problems.",
        advice = "Avoid potassium supplements (and potassium-based salt substitutes) unless prescribed with blood test monitoring.",
    ),
    InteractionRule(
        id = "k_raise_raise", sideA = set("raises_potassium"), sideB = set("raises_potassium"), severity = MODERATE,
        title = "Combined potassium-raising medicines",
        effect = "Both medicines can raise potassium.",
        advice = "Can be intentional, but regular blood tests for potassium and kidney function are needed.",
    ),
    InteractionRule(
        id = "liquorice_bp", sideA = set("liquorice"), sideB = set("antihypertensive", "lowers_potassium", "digoxin"), severity = MODERATE,
        title = "Liquorice raises blood pressure and lowers potassium",
        effect = "Liquorice root counteracts blood pressure medicines and can lower potassium, especially with diuretics or digoxin.",
        advice = "Avoid regular liquorice root supplements while on these medicines.",
    ),
    InteractionRule(
        id = "digoxin_diuretic", sideA = set("digoxin"), sideB = set("lowers_potassium"), severity = MODERATE,
        title = "Low potassium increases digoxin toxicity",
        effect = "Diuretics can lower potassium, which makes digoxin side effects more likely.",
        advice = "Usually monitored with blood tests - report nausea, visual changes or palpitations.",
    ),
    InteractionRule(
        id = "thiazide_calcium", sideA = set("thiazide"), sideB = set("calcium", "vitamin_d"), severity = MODERATE,
        title = "Risk of high calcium",
        effect = "Thiazide diuretics reduce calcium excretion; high-dose calcium or vitamin D may push blood calcium too high.",
        advice = "Keep supplement doses modest and ask for a calcium level check if you take both long-term.",
    ),

    // ---------- Cholesterol ----------
    InteractionRule(
        id = "statin_dup", sideA = set("statin"), sideB = set("statin"), severity = MAJOR,
        title = "Duplicate statin therapy",
        effect = "Red yeast rice contains a natural statin (monacolin K). Two statins together raise the risk of muscle damage and liver problems.",
        advice = "Do not combine - stop one after discussing with your prescriber.",
    ),
    InteractionRule(
        id = "statin_niacin", sideA = set("statin"), sideB = set("niacin"), severity = MODERATE,
        title = "Increased muscle side effects",
        effect = "High-dose niacin with a statin increases the risk of muscle pain and damage.",
        advice = "Avoid high-dose niacin supplements unless prescribed. Report unexplained muscle pain.",
    ),
    InteractionRule(
        id = "statin_coq10", sideA = set("statin"), sideB = set("coq10"), severity = INFO,
        title = "CoQ10 with statins",
        effect = "Statins lower the body's CoQ10. Some people take CoQ10 hoping to reduce muscle aches (evidence is mixed).",
        advice = "No harmful interaction - fine to continue.",
    ),

    // ---------- Stomach / acid ----------
    InteractionRule(
        id = "clopidogrel_ppi", sideA = set("clopidogrel"), sideB = set("ppi_cyp2c19"), severity = MODERATE,
        title = "PPI may weaken clopidogrel",
        effect = "Omeprazole and esomeprazole block the enzyme that activates clopidogrel.",
        advice = "Ask your prescriber about switching to pantoprazole or lansoprazole.",
    ),
    InteractionRule(
        id = "ppi_b12", sideA = set("ppi", "antidiabetic"), sideB = set("vitamin_b12"), severity = INFO,
        title = "B12 supplement is helpful here",
        effect = "Long-term PPIs and metformin can reduce vitamin B12 absorption.",
        advice = "Continuing B12 is reasonable; ask for occasional B12 blood tests.",
    ),
    InteractionRule(
        id = "ppi_minerals", sideA = set("ppi"), sideB = set("iron", "calcium"), severity = MINOR,
        title = "Reduced mineral absorption",
        effect = "Stomach acid helps absorb iron and calcium carbonate; PPIs reduce this.",
        advice = "Calcium citrate is absorbed better than carbonate when on a PPI. Take iron with vitamin C (e.g. orange juice).",
    ),
    InteractionRule(
        id = "ppi_magnesium", sideA = set("ppi"), sideB = set("magnesium"), severity = INFO,
        title = "PPIs can lower magnesium",
        effect = "Long-term PPI use can cause low magnesium levels.",
        advice = "A magnesium supplement may be helpful - ask for a blood test if you have cramps or palpitations.",
    ),

    // ---------- Diabetes ----------
    InteractionRule(
        id = "diabetes_supp", sideA = set("antidiabetic"), sideB = set("glucose_lowering_supplement", "berberine"), severity = MODERATE,
        title = "Risk of low blood sugar",
        effect = "This supplement can lower blood glucose and add to your diabetes medicine.",
        advice = "Monitor blood glucose more closely and tell your diabetes team.",
    ),
    InteractionRule(
        id = "metformin_b12_info", sideA = set("metformin"), sideB = set("multivitamin"), severity = INFO,
        title = "Metformin and vitamin B12",
        effect = "Long-term metformin can lower vitamin B12 levels.",
        advice = "Check your multivitamin contains B12, and ask for occasional B12 tests.",
    ),

    // ---------- Heart ----------
    InteractionRule(
        id = "pde5_nitrate", sideA = set("pde5"), sideB = set("nitrate"), severity = MAJOR,
        title = "Dangerous drop in blood pressure",
        effect = "Sildenafil/tadalafil with nitrates can cause a severe, potentially fatal fall in blood pressure.",
        advice = "Contraindicated - do not take together. Speak to your prescriber.",
    ),

    // ---------- Other ----------
    InteractionRule(
        id = "retinoid_vita", sideA = set("retinoid"), sideB = set("vitamin_a", "multivitamin"), severity = MAJOR,
        title = "Vitamin A toxicity",
        effect = "Isotretinoin is a vitamin A derivative; extra vitamin A can cause toxicity (headaches, liver problems).",
        advice = "Avoid vitamin A supplements (check multivitamins) while taking isotretinoin.",
    ),
    InteractionRule(
        id = "thyroid_ashwagandha", sideA = set("thyroid"), sideB = set("thyroid_active"), severity = MODERATE,
        title = "May alter thyroid levels",
        effect = "Ashwagandha can increase thyroid hormone levels.",
        advice = "Tell your doctor; thyroid function tests may be needed.",
    ),
    InteractionRule(
        id = "thyroid_biotin", sideA = set("thyroid"), sideB = set("biotin", "b_complex"), severity = MINOR,
        title = "Biotin can distort thyroid blood tests",
        effect = "High-dose biotin interferes with many laboratory tests, giving falsely abnormal thyroid results.",
        advice = "Stop biotin 2-3 days before blood tests, and tell whoever takes your blood.",
    ),
    InteractionRule(
        id = "immuno_echinacea", sideA = set("immunosuppressant", "corticosteroid"), sideB = set("immunostimulant"), severity = MODERATE,
        title = "Echinacea may oppose immunosuppression",
        effect = "Echinacea stimulates the immune system, which may counteract immunosuppressant medicines.",
        advice = "Avoid echinacea unless your specialist agrees.",
    ),
    InteractionRule(
        id = "steroid_nsaid", sideA = set("corticosteroid"), sideB = set("nsaid"), severity = MODERATE,
        title = "Increased stomach-ulcer risk",
        effect = "Steroids and anti-inflammatory painkillers together increase the risk of stomach ulcers and bleeding.",
        advice = "Take with food and ask whether you need stomach protection.",
    ),

    // ---------- Duplicate therapy ----------
    InteractionRule(
        id = "nsaid_dup", sideA = set("nsaid"), sideB = set("nsaid"), severity = MAJOR,
        title = "Two anti-inflammatory painkillers",
        effect = "Taking two NSAIDs together increases stomach bleeding and kidney risk without improving pain relief.",
        advice = "Take only one NSAID. (Low-dose aspirin for the heart is an exception - see the aspirin timing advice.)",
        topic = "bleeding",
    ),
    InteractionRule(
        id = "anticoag_dup", sideA = set("anticoagulant"), sideB = set("anticoagulant"), severity = MAJOR,
        title = "Two anticoagulants",
        effect = "Two blood thinners together carry a very high bleeding risk.",
        advice = "This is usually an error, or a short switch-over period. Confirm with your prescriber urgently.",
        topic = "bleeding",
    ),
    InteractionRule(
        id = "ppi_dup", sideA = set("ppi"), sideB = set("ppi"), severity = MINOR,
        title = "Two acid-reducing medicines",
        effect = "Taking two PPIs gives no extra benefit.",
        advice = "Check with your pharmacist - you probably only need one.",
    ),

    // ---------- Beneficial combinations ----------
    InteractionRule(
        id = "iron_vitc", sideA = set("iron"), sideB = set("vitamin_c"), severity = INFO,
        title = "Good combination",
        effect = "Vitamin C improves iron absorption.",
        advice = "Taking these at the same time is beneficial.",
    ),
    InteractionRule(
        id = "calcium_vitd", sideA = set("calcium"), sideB = set("vitamin_d"), severity = INFO,
        title = "Good combination",
        effect = "Vitamin D is needed to absorb calcium.",
        advice = "Taking these together is beneficial, ideally with a meal.",
    ),

    // ---------- Absorption - need separating in time ----------
    InteractionRule(
        id = "sep_thyroid_cation", sideA = set("thyroid"), sideB = set("polyvalent_cation"), severity = MODERATE,
        title = "Reduces levothyroxine absorption",
        effect = "Calcium, iron, magnesium, zinc, antacids and multivitamins bind levothyroxine in the gut so less is absorbed.",
        advice = "Take them at least 4 hours apart.",
        separationHours = 4.0,
    ),
    InteractionRule(
        id = "sep_quinolone_cation", sideA = set("quinolone"), sideB = set("polyvalent_cation"), severity = MODERATE,
        title = "Antibiotic may not work",
        effect = "Minerals and antacids bind quinolone antibiotics (e.g. ciprofloxacin), which can cause treatment failure.",
        advice = "Take the antibiotic at least 2 hours before or 6 hours after the mineral product - aim for 4+ hours apart.",
        separationHours = 4.0,
    ),
    InteractionRule(
        id = "sep_tetracycline_cation", sideA = set("tetracycline"), sideB = set("polyvalent_cation"), severity = MODERATE,
        title = "Antibiotic may not work",
        effect = "Calcium, iron, magnesium, zinc and antacids bind tetracycline antibiotics (e.g. doxycycline).",
        advice = "Take them at least 3 hours apart.",
        separationHours = 3.0,
    ),
    InteractionRule(
        id = "sep_bisphosphonate_cation", sideA = set("bisphosphonate"), sideB = set("polyvalent_cation"), severity = MODERATE,
        title = "Stops bone medicine being absorbed",
        effect = "Minerals prevent the absorption of bisphosphonates such as alendronic acid.",
        advice = "Take the bisphosphonate first thing, and minerals at a different time of day (at least 2 hours later).",
        separationHours = 2.0,
    ),
    InteractionRule(
        id = "sep_iron_calcium", sideA = set("iron"), sideB = set("calcium", "antacid"), severity = MINOR,
        title = "Calcium reduces iron absorption",
        effect = "Calcium and antacids reduce the amount of iron absorbed.",
        advice = "Take them at least 2 hours apart.",
        separationHours = 2.0,
    ),
    InteractionRule(
        id = "sep_iron_zinc", sideA = set("iron"), sideB = set("zinc"), severity = MINOR,
        title = "Iron and zinc compete for absorption",
        effect = "Iron and zinc taken together reduce each other's absorption.",
        advice = "Take them at least 2 hours apart.",
        separationHours = 2.0,
    ),
    InteractionRule(
        id = "sep_levodopa_iron", sideA = set("levodopa"), sideB = set("iron"), severity = MODERATE,
        title = "Iron reduces levodopa absorption",
        effect = "Iron binds levodopa, which may worsen Parkinson's symptoms.",
        advice = "Take them at least 2 hours apart.",
        separationHours = 2.0,
    ),
    InteractionRule(
        id = "sep_probiotic_antibiotic", sideA = set("antibiotic"), sideB = set("probiotic"), severity = MINOR,
        title = "Antibiotic can kill probiotic bacteria",
        effect = "Probiotics taken at the same time as antibiotics are less effective.",
        advice = "Take the probiotic at least 2 hours after the antibiotic.",
        separationHours = 2.0,
    ),
    InteractionRule(
        id = "sep_thyroid_ppi_food", sideA = set("thyroid"), sideB = set("fish_oil", "coq10"), severity = MINOR,
        title = "Keep levothyroxine on an empty stomach",
        effect = "Supplements usually taken with food can reduce levothyroxine absorption if taken at the same time.",
        advice = "Take levothyroxine first and wait 30-60 minutes; ideally keep other supplements to later meals.",
        separationHours = 1.0,
    ),
)
