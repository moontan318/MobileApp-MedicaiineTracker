package com.medtracker.core.knowledge

import com.medtracker.core.knowledge.Severity.INFO
import com.medtracker.core.knowledge.Severity.MAJOR
import com.medtracker.core.knowledge.Severity.MINOR
import com.medtracker.core.knowledge.Severity.MODERATE
import com.medtracker.core.model.FoodRelation.EMPTY_STOMACH
import com.medtracker.core.model.FoodRelation.WITH_FOOD

private val MORNING = hm(5)..hm(9, 30)
private val EARLY_DAY = hm(5)..hm(14)
private val DAYTIME = hm(5)..hm(16)
private val EVENING = hm(18)..hm(23, 59)
private val BEFORE_BED = hm(19)..hm(23, 59)
private val BEFORE_EVENING_MEAL = hm(16)..hm(19, 30)
private val NOT_AT_BEDTIME = hm(5)..hm(21)

/** Single-substance advice on time of day and food. */
internal val SUBSTANCE_ADVICE: List<SubstanceAdvice> = listOf(
    SubstanceAdvice(
        id = "levothyroxine_morning", tags = setOf("thyroid"), severity = MODERATE,
        windows = listOf(MORNING), windowLabel = "early morning", food = EMPTY_STOMACH,
        reason = "Levothyroxine is best absorbed on an empty stomach, 30-60 minutes before breakfast or caffeine, at the same time every day.",
    ),
    SubstanceAdvice(
        id = "bisphosphonate_morning", tags = setOf("bisphosphonate"), severity = MODERATE,
        windows = listOf(MORNING), windowLabel = "first thing in the morning", food = EMPTY_STOMACH,
        reason = "Take first thing in the morning with a full glass of plain water, at least 30 minutes before food, drink or other medicines, and stay upright for 30 minutes.",
    ),
    SubstanceAdvice(
        id = "statin_evening", tags = setOf("statin_evening"), severity = MINOR,
        windows = listOf(EVENING), windowLabel = "in the evening",
        reason = "This statin is short-acting and works best when taken in the evening, when the body makes most cholesterol.",
    ),
    SubstanceAdvice(
        id = "diuretic_daytime", tags = setOf("diuretic"), severity = MINOR,
        windows = listOf(DAYTIME), windowLabel = "morning or early afternoon",
        reason = "Diuretics make you pass more urine for several hours; taking them late in the day can disturb sleep.",
    ),
    SubstanceAdvice(
        id = "ppi_before_meal", tags = setOf("ppi"), severity = MINOR,
        windows = listOf(MORNING, BEFORE_EVENING_MEAL), windowLabel = "30-60 minutes before breakfast (or the evening meal for a second dose)",
        food = EMPTY_STOMACH,
        reason = "PPIs work best when taken 30-60 minutes before a meal.",
    ),
    SubstanceAdvice(
        id = "steroid_morning", tags = setOf("steroid_morning"), severity = MINOR,
        windows = listOf(hm(5)..hm(10)), windowLabel = "in the morning", food = WITH_FOOD,
        reason = "Steroids taken in the morning mimic the body's natural rhythm and are less likely to cause insomnia. Take with food to protect the stomach.",
    ),
    SubstanceAdvice(
        id = "melatonin_bed", tags = setOf("melatonin"), severity = MINOR,
        windows = listOf(BEFORE_BED), windowLabel = "1-2 hours before bed",
        reason = "Melatonin signals the body that it is night-time; taking it earlier in the day can shift your body clock the wrong way.",
    ),
    SubstanceAdvice(
        id = "sleep_herbs_evening", tags = setOf("valerian", "passionflower"), severity = MINOR,
        windows = listOf(EVENING), windowLabel = "in the evening",
        reason = "This is a sedating herb - taking it in the daytime may make you drowsy.",
    ),
    SubstanceAdvice(
        id = "stimulant_herbs_day", tags = setOf("stimulant_herb"), severity = MINOR,
        windows = listOf(EARLY_DAY), windowLabel = "morning or early afternoon",
        reason = "Ginseng and rhodiola can be stimulating and may interfere with sleep if taken late.",
    ),
    SubstanceAdvice(
        id = "b_vitamins_day", tags = setOf("b_energising"), severity = INFO,
        windows = listOf(EARLY_DAY), windowLabel = "in the morning",
        reason = "B vitamins are involved in energy metabolism and some people find them stimulating in the evening.",
    ),
    SubstanceAdvice(
        id = "magnesium_evening", tags = setOf("magnesium"), severity = INFO,
        windows = listOf(hm(16)..hm(23, 59)), windowLabel = "in the evening",
        reason = "Magnesium can have a mild relaxing effect and is often better tolerated in the evening.",
    ),
    SubstanceAdvice(
        id = "tetracycline_upright", tags = setOf("tetracycline"), severity = MINOR,
        windows = listOf(NOT_AT_BEDTIME), windowLabel = "at least an hour before lying down",
        reason = "Doxycycline and similar antibiotics can irritate the gullet; take with a full glass of water, sitting or standing.",
    ),
    SubstanceAdvice(
        id = "fat_soluble_food", tags = setOf("fat_soluble"), severity = MINOR, food = WITH_FOOD,
        reason = "Fat-soluble vitamins, fish oil and CoQ10 are absorbed much better with a meal containing some fat.",
    ),
    SubstanceAdvice(
        id = "nsaid_food", tags = setOf("nsaid"), severity = MODERATE, food = WITH_FOOD,
        reason = "Anti-inflammatory painkillers can irritate the stomach - take with or just after food.",
    ),
    SubstanceAdvice(
        id = "metformin_food", tags = setOf("metformin"), severity = MODERATE, food = WITH_FOOD,
        reason = "Taking metformin with meals reduces stomach upset.",
    ),
    SubstanceAdvice(
        id = "sulfonylurea_food", tags = setOf("sulfonylurea"), severity = MODERATE, food = WITH_FOOD,
        reason = "Take with a meal (usually breakfast) to reduce the risk of low blood sugar.",
    ),
    SubstanceAdvice(
        id = "calcium_food", tags = setOf("calcium"), severity = MINOR, food = WITH_FOOD,
        reason = "Calcium carbonate needs stomach acid and is absorbed best with food (calcium citrate can be taken any time).",
    ),
    SubstanceAdvice(
        id = "iron_empty", tags = setOf("iron"), severity = INFO, food = EMPTY_STOMACH,
        reason = "Iron is absorbed best on an empty stomach with vitamin C. If it upsets your stomach, taking it with food is acceptable.",
    ),
    SubstanceAdvice(
        id = "allopurinol_food", tags = setOf("allopurinol"), severity = MINOR, food = WITH_FOOD,
        reason = "Allopurinol is best taken after food with plenty of water (aim for 2-3 litres of fluid a day) to reduce stomach upset and kidney stones.",
    ),
    SubstanceAdvice(
        id = "allopurinol_note", tags = setOf("allopurinol"), severity = INFO,
        reason = "Allopurinol", generalNote = "Stop allopurinol and seek medical advice immediately if you develop a skin rash. Gout flares can happen in the first months of treatment - don't stop taking it during a flare unless told to.",
    ),
    SubstanceAdvice(
        id = "sedating_night", tags = setOf("sedating_night"), severity = MINOR,
        windows = listOf(hm(18)..hm(23, 59)), windowLabel = "in the evening or at bedtime",
        reason = "This medicine commonly causes drowsiness, so it is usually taken at night.",
    ),
    SubstanceAdvice(
        id = "penicillin_empty", tags = setOf("penicillin_empty"), severity = MINOR, food = EMPTY_STOMACH,
        reason = "Flucloxacillin and penicillin V are absorbed best on an empty stomach - 1 hour before food or 2 hours after.",
    ),
    SubstanceAdvice(
        id = "nitrofurantoin_food", tags = setOf("nitrofurantoin", "metronidazole", "hydroxychloroquine"), severity = MINOR, food = WITH_FOOD,
        reason = "Taking this with or after food reduces nausea and stomach upset.",
    ),
    SubstanceAdvice(
        id = "metronidazole_alcohol", tags = setOf("metronidazole"), severity = MODERATE,
        reason = "Metronidazole", generalNote = "Do not drink alcohol while taking metronidazole and for 48 hours after the course - it can cause severe sickness, flushing and palpitations.",
    ),
    SubstanceAdvice(
        id = "gabapentinoid_note", tags = setOf("gabapentinoid"), severity = INFO,
        reason = "Gabapentinoids", generalNote = "Can cause dizziness and drowsiness. Don't stop suddenly - reduce gradually with your prescriber. Avoid alcohol.",
    ),
    SubstanceAdvice(
        id = "alpha_blocker_note", tags = setOf("alpha_blocker"), severity = INFO,
        reason = "Alpha-blockers", generalNote = "May cause dizziness when standing up, especially after the first dose - get up slowly. Tamsulosin is usually taken after the same meal each day.",
    ),
    SubstanceAdvice(
        id = "montelukast_note", tags = setOf("montelukast"), severity = INFO,
        reason = "Montelukast", generalNote = "Can occasionally cause sleep disturbance, nightmares or mood changes. Tell your doctor if you notice these.",
    ),
    SubstanceAdvice(
        id = "finasteride_note", tags = setOf("finasteride"), severity = INFO,
        reason = "Finasteride", generalNote = "Finasteride roughly halves PSA blood test results - tell your doctor you take it before a PSA test.",
    ),
    SubstanceAdvice(
        id = "sglt2_note", tags = setOf("sglt2"), severity = INFO,
        reason = "SGLT2 inhibitors", generalNote = "If you become unwell with vomiting, diarrhoea or can't eat and drink, pause this medicine and contact your team ('sick day rules').",
    ),
    SubstanceAdvice(
        id = "liver_herb_note", tags = setOf("liver_herb"), severity = MINOR,
        reason = "Liver", generalNote = "This supplement has been linked to rare but serious liver injury. Stop it and seek advice if you develop yellow skin/eyes, dark urine or abdominal pain.",
    ),
    SubstanceAdvice(
        id = "kava_liver", tags = setOf("kava"), severity = MODERATE,
        reason = "Kava", generalNote = "Kava has been linked to serious liver damage and is restricted in several countries. Avoid alcohol and discuss with your doctor.",
    ),
    SubstanceAdvice(
        id = "sjw_general", tags = setOf("st_johns_wort"), severity = MINOR,
        reason = "St John's wort", generalNote = "St John's wort interacts with a very large number of medicines. Always tell a pharmacist or doctor before starting any new medicine.",
    ),
    SubstanceAdvice(
        id = "biotin_tests", tags = setOf("biotin"), severity = INFO,
        reason = "Biotin", generalNote = "High-dose biotin can distort many blood test results (including heart and thyroid tests). Tell the person taking your blood.",
    ),
    SubstanceAdvice(
        id = "warfarin_consistency", tags = setOf("vka"), severity = INFO,
        reason = "Warfarin", generalNote = "Take warfarin at the same time each day (usually early evening) and tell your anticoagulation clinic about any new medicine or supplement.",
    ),
    SubstanceAdvice(
        id = "opioid_constipation", tags = setOf("opioid"), severity = INFO,
        reason = "Opioids", generalNote = "Opioids commonly cause constipation and drowsiness. Avoid alcohol.",
    ),
    SubstanceAdvice(
        id = "potassium_food", tags = setOf("potassium"), severity = MODERATE, food = WITH_FOOD,
        reason = "Potassium supplements can irritate the gut - take with food and plenty of water, and only with regular blood tests.",
    ),
)

/**
 * Typical adult upper limits (daily totals from supplements/medicines). Values follow widely
 * used tolerable upper intake levels and standard adult maximum doses; some national bodies
 * recommend lower figures, which are mentioned in the notes.
 */
internal val DOSE_LIMITS: List<DoseLimit> = listOf(
    DoseLimit("paracetamol", BaseUnit.MG, maxDaily = 4000.0, maxSingle = 1000.0, minHoursBetweenDoses = 4.0, severity = MAJOR,
        note = "Adult maximum is 1 g per dose and 4 g in 24 hours, at least 4 hours apart (lower if you weigh under 50 kg, drink heavily or have liver problems). Check other products for hidden paracetamol."),
    DoseLimit("ibuprofen", BaseUnit.MG, maxDaily = 1200.0, maxSingle = 400.0, minHoursBetweenDoses = 4.0, severity = MODERATE,
        note = "Over-the-counter maximum is 400 mg per dose and 1200 mg a day. Higher doses (up to 2400 mg) should only be taken on a prescriber's advice."),
    DoseLimit("naproxen", BaseUnit.MG, maxDaily = 1000.0, severity = MODERATE,
        note = "Usual maximum is 1000 mg a day unless your prescriber has advised otherwise."),
    DoseLimit("diclofenac", BaseUnit.MG, maxDaily = 150.0, severity = MODERATE,
        note = "Maximum is 150 mg a day."),
    DoseLimit("aspirin", BaseUnit.MG, maxDaily = 4000.0, maxSingle = 1000.0, minHoursBetweenDoses = 4.0, severity = MODERATE,
        note = "Pain-relief maximum is 1000 mg per dose, 4000 mg a day. Low-dose aspirin for the heart is usually 75-100 mg once daily."),
    DoseLimit("vitamin_a", BaseUnit.MCG, maxDaily = 3000.0, severity = MODERATE, iuToBase = 0.3,
        note = "Upper limit is 3000 mcg (10,000 IU) a day; UK advice is no more than 1500 mcg. Avoid high-dose vitamin A in pregnancy."),
    DoseLimit("vitamin_d", BaseUnit.MCG, maxDaily = 100.0, severity = MODERATE, iuToBase = 0.025,
        note = "Upper limit is 100 mcg (4000 IU) a day unless prescribed a higher dose to treat deficiency."),
    DoseLimit("vitamin_e", BaseUnit.MG, maxDaily = 1000.0, severity = MODERATE, iuToBase = 0.67,
        note = "Upper limit is 1000 mg a day (UK advice: 540 mg). High doses increase bleeding risk."),
    DoseLimit("vitamin_c", BaseUnit.MG, maxDaily = 2000.0, severity = MINOR,
        note = "Above 2000 mg a day can cause stomach pain, diarrhoea and, in some people, kidney stones."),
    DoseLimit("vitamin_b6", BaseUnit.MG, maxDaily = 100.0, severity = MODERATE,
        note = "Long-term intake above 100 mg a day can cause nerve damage (UK advice is no more than 10 mg a day)."),
    DoseLimit("niacin", BaseUnit.MG, maxDaily = 35.0, severity = MINOR,
        note = "Nicotinic acid above 35 mg a day commonly causes flushing; very high doses can affect the liver."),
    DoseLimit("folic_acid", BaseUnit.MCG, maxDaily = 1000.0, severity = MODERATE,
        note = "Upper limit is 1000 mcg (1 mg) a day unless prescribed (e.g. 5 mg in some pregnancies or with methotrexate). High doses can mask B12 deficiency."),
    DoseLimit("calcium", BaseUnit.MG, maxDaily = 2500.0, maxSingle = 500.0, severity = MODERATE, singleDoseSeverity = MINOR,
        note = "Absorption is best at 500 mg or less per dose. Total intake (food + supplements) should stay below 2500 mg a day; most people need no more than 1000-1200 mg from supplements."),
    DoseLimit("iron", BaseUnit.MG, maxDaily = 45.0, severity = MODERATE,
        note = "Upper limit is 45 mg of elemental iron a day unless prescribed for anaemia. Keep iron away from children."),
    DoseLimit("zinc", BaseUnit.MG, maxDaily = 40.0, severity = MODERATE,
        note = "Above 40 mg a day (UK: 25 mg) can cause copper deficiency over time."),
    DoseLimit("magnesium", BaseUnit.MG, maxDaily = 350.0, severity = MINOR,
        note = "Supplemental magnesium above 350 mg a day often causes diarrhoea. Use caution with kidney problems."),
    DoseLimit("selenium", BaseUnit.MCG, maxDaily = 400.0, severity = MODERATE,
        note = "Above 400 mcg a day (UK: 350 mcg) can cause hair loss, brittle nails and nerve problems."),
    DoseLimit("iodine", BaseUnit.MCG, maxDaily = 1100.0, severity = MODERATE,
        note = "Above 1100 mcg a day can disturb thyroid function. Kelp products can contain very variable amounts."),
    DoseLimit("copper", BaseUnit.MG, maxDaily = 10.0, severity = MODERATE,
        note = "Upper limit is 10 mg a day."),
    DoseLimit("melatonin", BaseUnit.MG, maxDaily = 10.0, severity = MINOR,
        note = "Typical doses are 0.5-5 mg; higher doses rarely help more and increase next-day drowsiness."),
    DoseLimit("allopurinol", BaseUnit.MG, maxDaily = 900.0, maxSingle = 300.0, severity = MODERATE, singleDoseSeverity = MINOR,
        note = "Usual maximum is 900 mg a day. Daily doses above 300 mg should be split into smaller doses. Lower doses are used with kidney impairment."),
    DoseLimit("febuxostat", BaseUnit.MG, maxDaily = 120.0, severity = MODERATE,
        note = "Maximum is 120 mg once a day."),
    DoseLimit("colchicine", BaseUnit.MG, maxDaily = 2.0, severity = MAJOR,
        note = "For gout flares the usual dose is 500 mcg 2-4 times a day, no more than 6 mg per course. Toxic in overdose."),
    DoseLimit("codeine", BaseUnit.MG, maxDaily = 240.0, maxSingle = 60.0, minHoursBetweenDoses = 4.0, severity = MAJOR,
        note = "Adult maximum is 60 mg per dose and 240 mg a day, at least 4 hours apart."),
    DoseLimit("dihydrocodeine", BaseUnit.MG, maxDaily = 240.0, minHoursBetweenDoses = 4.0, severity = MAJOR,
        note = "Adult maximum is usually 240 mg a day, doses at least 4-6 hours apart."),
    DoseLimit("tramadol", BaseUnit.MG, maxDaily = 400.0, maxSingle = 100.0, minHoursBetweenDoses = 4.0, severity = MAJOR,
        note = "Adult maximum is 400 mg a day (immediate release 50-100 mg every 4-6 hours)."),
    DoseLimit("gabapentin", BaseUnit.MG, maxDaily = 3600.0, severity = MODERATE,
        note = "Maximum is 3600 mg a day, in 3 divided doses; lower with kidney impairment."),
    DoseLimit("pregabalin", BaseUnit.MG, maxDaily = 600.0, severity = MODERATE,
        note = "Maximum is 600 mg a day in 2-3 divided doses; lower with kidney impairment."),
    DoseLimit("mefenamic_acid", BaseUnit.MG, maxDaily = 1500.0, severity = MODERATE,
        note = "Usual maximum is 500 mg three times a day."),
    DoseLimit("cetirizine", BaseUnit.MG, maxDaily = 10.0, severity = MINOR,
        note = "Usual adult dose is 10 mg once a day."),
    DoseLimit("loratadine", BaseUnit.MG, maxDaily = 10.0, severity = MINOR,
        note = "Usual adult dose is 10 mg once a day."),
    DoseLimit("fexofenadine", BaseUnit.MG, maxDaily = 180.0, severity = MINOR,
        note = "Usual adult maximum is 180 mg a day."),
    DoseLimit("chlorphenamine", BaseUnit.MG, maxDaily = 24.0, severity = MINOR,
        note = "Adult maximum is 4 mg every 4-6 hours, up to 24 mg a day."),
    DoseLimit("loperamide", BaseUnit.MG, maxDaily = 16.0, severity = MODERATE,
        note = "Maximum is 16 mg a day (12 mg for over-the-counter use). High doses can cause heart rhythm problems."),
    DoseLimit("metoclopramide", BaseUnit.MG, maxDaily = 30.0, severity = MODERATE,
        note = "Maximum is 10 mg three times a day, normally for no more than 5 days."),
    DoseLimit("domperidone", BaseUnit.MG, maxDaily = 30.0, severity = MODERATE,
        note = "Maximum is 10 mg up to three times a day, normally for no more than a week."),
    DoseLimit("fish_oil", BaseUnit.MG, maxDaily = 5000.0, severity = MINOR,
        note = "Up to 5 g a day of omega-3 (EPA + DHA) is considered safe; above this bleeding risk may increase."),
)
