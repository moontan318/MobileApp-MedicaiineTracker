package com.medtracker.core.knowledge

import com.medtracker.core.model.ItemType
import com.medtracker.core.model.ItemType.HERBAL
import com.medtracker.core.model.ItemType.MEDICINE
import com.medtracker.core.model.ItemType.MINERAL
import com.medtracker.core.model.ItemType.OTHER_SUPPLEMENT
import com.medtracker.core.model.ItemType.VITAMIN

private fun s(id: String, name: String, type: ItemType, aliases: List<String>, vararg tags: String) =
    Substance(id, name, type, aliases, tags.toSet())

/** Built-in list of common medicines and supplements. Aliases are matched as whole words. */
internal val SUBSTANCES: List<Substance> = listOf(
    // ---- Anticoagulants & antiplatelets ----
    s("warfarin", "Warfarin", MEDICINE, listOf("warfarin", "coumadin", "marevan", "jantoven"), "anticoagulant", "vka"),
    s("apixaban", "Apixaban", MEDICINE, listOf("apixaban", "eliquis"), "anticoagulant", "doac"),
    s("rivaroxaban", "Rivaroxaban", MEDICINE, listOf("rivaroxaban", "xarelto"), "anticoagulant", "doac"),
    s("dabigatran", "Dabigatran", MEDICINE, listOf("dabigatran", "pradaxa"), "anticoagulant", "doac"),
    s("edoxaban", "Edoxaban", MEDICINE, listOf("edoxaban", "lixiana", "savaysa"), "anticoagulant", "doac"),
    s("clopidogrel", "Clopidogrel", MEDICINE, listOf("clopidogrel", "plavix"), "antiplatelet"),
    s("aspirin", "Aspirin", MEDICINE, listOf("aspirin", "acetylsalicylic acid", "disprin", "nu seals"), "antiplatelet", "nsaid"),

    // ---- Pain relief ----
    s("ibuprofen", "Ibuprofen", MEDICINE, listOf("ibuprofen", "nurofen", "advil", "brufen", "motrin"), "nsaid"),
    s("naproxen", "Naproxen", MEDICINE, listOf("naproxen", "naprosyn", "aleve"), "nsaid"),
    s("diclofenac", "Diclofenac", MEDICINE, listOf("diclofenac", "voltarol", "voltaren"), "nsaid"),
    s("celecoxib", "Celecoxib", MEDICINE, listOf("celecoxib", "celebrex"), "nsaid"),
    s("paracetamol", "Paracetamol (acetaminophen)", MEDICINE,
        listOf("paracetamol", "acetaminophen", "tylenol", "panadol", "calpol", "co codamol", "cocodamol", "co dydramol"), "analgesic"),
    s("codeine", "Codeine", MEDICINE, listOf("codeine", "co codamol", "cocodamol"), "opioid", "cns_depressant"),
    s("tramadol", "Tramadol", MEDICINE, listOf("tramadol", "zydol", "ultram"), "opioid", "cns_depressant", "serotonergic"),
    s("morphine", "Morphine", MEDICINE, listOf("morphine", "oramorph", "zomorph"), "opioid", "cns_depressant"),
    s("oxycodone", "Oxycodone", MEDICINE, listOf("oxycodone", "oxycontin", "oxynorm"), "opioid", "cns_depressant"),

    // ---- Antidepressants, mood & migraine ----
    s("sertraline", "Sertraline", MEDICINE, listOf("sertraline", "zoloft", "lustral"), "ssri", "serotonergic"),
    s("fluoxetine", "Fluoxetine", MEDICINE, listOf("fluoxetine", "prozac"), "ssri", "serotonergic"),
    s("citalopram", "Citalopram", MEDICINE, listOf("citalopram", "celexa", "cipramil"), "ssri", "serotonergic"),
    s("escitalopram", "Escitalopram", MEDICINE, listOf("escitalopram", "lexapro", "cipralex"), "ssri", "serotonergic"),
    s("paroxetine", "Paroxetine", MEDICINE, listOf("paroxetine", "paxil", "seroxat"), "ssri", "serotonergic"),
    s("venlafaxine", "Venlafaxine", MEDICINE, listOf("venlafaxine", "effexor"), "ssri", "serotonergic"),
    s("duloxetine", "Duloxetine", MEDICINE, listOf("duloxetine", "cymbalta"), "ssri", "serotonergic"),
    s("amitriptyline", "Amitriptyline", MEDICINE, listOf("amitriptyline"), "serotonergic", "cns_depressant"),
    s("mirtazapine", "Mirtazapine", MEDICINE, listOf("mirtazapine", "zispin", "remeron"), "serotonergic", "cns_depressant"),
    s("sumatriptan", "Sumatriptan", MEDICINE, listOf("sumatriptan", "imigran", "imitrex"), "serotonergic"),
    s("lithium", "Lithium", MEDICINE, listOf("lithium", "priadel", "camcolit"), "lithium"),

    // ---- Sleep & anxiety ----
    s("diazepam", "Diazepam", MEDICINE, listOf("diazepam", "valium"), "benzo_z", "cns_depressant"),
    s("lorazepam", "Lorazepam", MEDICINE, listOf("lorazepam", "ativan"), "benzo_z", "cns_depressant"),
    s("alprazolam", "Alprazolam", MEDICINE, listOf("alprazolam", "xanax"), "benzo_z", "cns_depressant"),
    s("zopiclone", "Zopiclone", MEDICINE, listOf("zopiclone", "imovane"), "benzo_z", "cns_depressant"),
    s("zolpidem", "Zolpidem", MEDICINE, listOf("zolpidem", "ambien", "stilnoct"), "benzo_z", "cns_depressant"),

    // ---- Thyroid ----
    s("levothyroxine", "Levothyroxine", MEDICINE,
        listOf("levothyroxine", "thyroxine", "eltroxin", "synthroid", "euthyrox", "levoxyl", "tirosint"), "thyroid"),

    // ---- Antibiotics ----
    s("ciprofloxacin", "Ciprofloxacin", MEDICINE, listOf("ciprofloxacin", "cipro", "ciproxin"), "quinolone", "antibiotic"),
    s("levofloxacin", "Levofloxacin", MEDICINE, listOf("levofloxacin", "levaquin", "tavanic"), "quinolone", "antibiotic"),
    s("doxycycline", "Doxycycline", MEDICINE, listOf("doxycycline", "vibramycin"), "tetracycline", "antibiotic"),
    s("tetracycline", "Tetracycline", MEDICINE, listOf("tetracycline"), "tetracycline", "antibiotic"),
    s("minocycline", "Minocycline", MEDICINE, listOf("minocycline"), "tetracycline", "antibiotic"),
    s("amoxicillin", "Amoxicillin", MEDICINE, listOf("amoxicillin", "amoxycillin", "amoxil", "co amoxiclav", "augmentin"), "antibiotic"),
    s("clarithromycin", "Clarithromycin", MEDICINE, listOf("clarithromycin", "klaricid", "biaxin"), "antibiotic"),

    // ---- Bone ----
    s("alendronate", "Alendronic acid", MEDICINE, listOf("alendronate", "alendronic acid", "fosamax"), "bisphosphonate"),
    s("risedronate", "Risedronate", MEDICINE, listOf("risedronate", "risedronic acid", "actonel"), "bisphosphonate"),
    s("ibandronate", "Ibandronic acid", MEDICINE, listOf("ibandronate", "ibandronic acid", "bonviva", "boniva"), "bisphosphonate"),

    // ---- Cholesterol ----
    s("simvastatin", "Simvastatin", MEDICINE, listOf("simvastatin", "zocor"), "statin", "statin_evening"),
    s("pravastatin", "Pravastatin", MEDICINE, listOf("pravastatin", "pravachol"), "statin", "statin_evening"),
    s("fluvastatin", "Fluvastatin", MEDICINE, listOf("fluvastatin", "lescol"), "statin", "statin_evening"),
    s("lovastatin", "Lovastatin", MEDICINE, listOf("lovastatin", "mevacor"), "statin", "statin_evening"),
    s("atorvastatin", "Atorvastatin", MEDICINE, listOf("atorvastatin", "lipitor"), "statin"),
    s("rosuvastatin", "Rosuvastatin", MEDICINE, listOf("rosuvastatin", "crestor"), "statin"),

    // ---- Blood pressure & heart ----
    s("lisinopril", "Lisinopril", MEDICINE, listOf("lisinopril", "zestril", "prinivil"), "acei", "raises_potassium", "antihypertensive"),
    s("ramipril", "Ramipril", MEDICINE, listOf("ramipril", "tritace", "altace"), "acei", "raises_potassium", "antihypertensive"),
    s("enalapril", "Enalapril", MEDICINE, listOf("enalapril", "innovace", "vasotec"), "acei", "raises_potassium", "antihypertensive"),
    s("perindopril", "Perindopril", MEDICINE, listOf("perindopril", "coversyl"), "acei", "raises_potassium", "antihypertensive"),
    s("losartan", "Losartan", MEDICINE, listOf("losartan", "cozaar"), "arb", "raises_potassium", "antihypertensive"),
    s("candesartan", "Candesartan", MEDICINE, listOf("candesartan", "amias", "atacand"), "arb", "raises_potassium", "antihypertensive"),
    s("valsartan", "Valsartan", MEDICINE, listOf("valsartan", "diovan"), "arb", "raises_potassium", "antihypertensive"),
    s("irbesartan", "Irbesartan", MEDICINE, listOf("irbesartan", "aprovel", "avapro"), "arb", "raises_potassium", "antihypertensive"),
    s("spironolactone", "Spironolactone", MEDICINE, listOf("spironolactone", "aldactone"), "raises_potassium", "diuretic", "antihypertensive"),
    s("furosemide", "Furosemide", MEDICINE, listOf("furosemide", "frusemide", "lasix"), "diuretic", "lowers_potassium", "antihypertensive"),
    s("bendroflumethiazide", "Bendroflumethiazide", MEDICINE, listOf("bendroflumethiazide", "bendrofluazide"),
        "thiazide", "diuretic", "lowers_potassium", "antihypertensive"),
    s("hydrochlorothiazide", "Hydrochlorothiazide", MEDICINE, listOf("hydrochlorothiazide", "hctz"),
        "thiazide", "diuretic", "lowers_potassium", "antihypertensive"),
    s("indapamide", "Indapamide", MEDICINE, listOf("indapamide", "natrilix"), "thiazide", "diuretic", "lowers_potassium", "antihypertensive"),
    s("amlodipine", "Amlodipine", MEDICINE, listOf("amlodipine", "norvasc", "istin"), "antihypertensive"),
    s("bisoprolol", "Bisoprolol", MEDICINE, listOf("bisoprolol", "cardicor", "concor"), "antihypertensive"),
    s("digoxin", "Digoxin", MEDICINE, listOf("digoxin", "lanoxin"), "digoxin"),
    s("gtn", "Glyceryl trinitrate", MEDICINE, listOf("glyceryl trinitrate", "gtn", "nitroglycerin", "nitroglycerine"), "nitrate"),
    s("isosorbide", "Isosorbide mononitrate", MEDICINE,
        listOf("isosorbide", "isosorbide mononitrate", "isosorbide dinitrate", "ismn"), "nitrate"),
    s("sildenafil", "Sildenafil", MEDICINE, listOf("sildenafil", "viagra"), "pde5"),
    s("tadalafil", "Tadalafil", MEDICINE, listOf("tadalafil", "cialis"), "pde5"),

    // ---- Diabetes ----
    s("metformin", "Metformin", MEDICINE, listOf("metformin", "glucophage"), "antidiabetic"),
    s("gliclazide", "Gliclazide", MEDICINE, listOf("gliclazide", "diamicron"), "antidiabetic", "sulfonylurea"),
    s("glimepiride", "Glimepiride", MEDICINE, listOf("glimepiride", "amaryl"), "antidiabetic", "sulfonylurea"),
    s("insulin", "Insulin", MEDICINE,
        listOf("insulin", "novorapid", "lantus", "humalog", "levemir", "humulin", "tresiba", "abasaglar"), "antidiabetic"),

    // ---- Stomach ----
    s("omeprazole", "Omeprazole", MEDICINE, listOf("omeprazole", "losec", "prilosec"), "ppi", "ppi_cyp2c19"),
    s("esomeprazole", "Esomeprazole", MEDICINE, listOf("esomeprazole", "nexium"), "ppi", "ppi_cyp2c19"),
    s("lansoprazole", "Lansoprazole", MEDICINE, listOf("lansoprazole", "zoton", "prevacid"), "ppi"),
    s("pantoprazole", "Pantoprazole", MEDICINE, listOf("pantoprazole", "protonix", "protium"), "ppi"),
    s("antacid", "Antacid", MEDICINE, listOf("antacid", "gaviscon", "rennie", "tums", "maalox", "mylanta"), "antacid", "polyvalent_cation"),

    // ---- Other medicines ----
    s("methotrexate", "Methotrexate", MEDICINE, listOf("methotrexate"), "methotrexate"),
    s("prednisolone", "Prednisolone", MEDICINE, listOf("prednisolone"), "corticosteroid"),
    s("prednisone", "Prednisone", MEDICINE, listOf("prednisone"), "corticosteroid"),
    s("levodopa", "Levodopa", MEDICINE, listOf("levodopa", "co careldopa", "co beneldopa", "sinemet", "madopar"), "levodopa"),
    s("contraceptive", "Hormonal contraceptive", MEDICINE,
        listOf("contraceptive", "contraceptive pill", "oral contraceptive", "the pill", "microgynon", "rigevidon",
            "yasmin", "levonorgestrel", "ethinylestradiol", "desogestrel", "cerazette"), "hormonal_contraceptive"),
    s("ciclosporin", "Ciclosporin", MEDICINE, listOf("ciclosporin", "cyclosporine", "neoral"), "immunosuppressant"),
    s("tacrolimus", "Tacrolimus", MEDICINE, listOf("tacrolimus", "prograf", "adoport"), "immunosuppressant"),
    s("isotretinoin", "Isotretinoin", MEDICINE, listOf("isotretinoin", "roaccutane", "accutane"), "retinoid"),

    // ---- Vitamins ----
    s("vitamin_a", "Vitamin A", VITAMIN, listOf("vitamin a", "retinol", "retinyl palmitate", "retinyl acetate"), "fat_soluble"),
    s("vitamin_b12", "Vitamin B12", VITAMIN,
        listOf("vitamin b12", "b12", "cyanocobalamin", "methylcobalamin", "hydroxocobalamin", "cobalamin"), "b_energising"),
    s("vitamin_b6", "Vitamin B6", VITAMIN, listOf("vitamin b6", "b6", "pyridoxine", "pyridoxal"), "b_energising"),
    s("b_complex", "Vitamin B complex", VITAMIN, listOf("vitamin b complex", "b complex", "b vitamins", "b 50", "b 100"), "b_energising"),
    s("niacin", "Niacin (vitamin B3)", VITAMIN, listOf("niacin", "vitamin b3", "b3", "nicotinic acid"), "niacin"),
    s("folic_acid", "Folic acid", VITAMIN, listOf("folic acid", "folate", "vitamin b9", "methylfolate"), "folate"),
    s("biotin", "Biotin", VITAMIN, listOf("biotin", "vitamin b7", "vitamin h"), "biotin"),
    s("vitamin_c", "Vitamin C", VITAMIN, listOf("vitamin c", "ascorbic acid", "ascorbate"), "vitamin_c"),
    s("vitamin_d", "Vitamin D", VITAMIN,
        listOf("vitamin d", "vitamin d3", "vitamin d2", "d3", "cholecalciferol", "ergocalciferol", "colecalciferol"), "fat_soluble"),
    s("vitamin_e", "Vitamin E", VITAMIN, listOf("vitamin e", "tocopherol", "alpha tocopherol", "tocopheryl"),
        "fat_soluble", "bleeding_supplement"),
    s("vitamin_k", "Vitamin K", VITAMIN,
        listOf("vitamin k", "vitamin k1", "vitamin k2", "k2", "menaquinone", "phylloquinone", "mk 7", "mk7"), "fat_soluble"),
    s("multivitamin", "Multivitamin & mineral", VITAMIN,
        listOf("multivitamin", "multi vitamin", "multivitamins", "centrum", "sanatogen", "wellman", "wellwoman",
            "one a day", "a z multivitamin"), "polyvalent_cation"),

    // ---- Minerals ----
    s("calcium", "Calcium", MINERAL,
        listOf("calcium", "calcium carbonate", "calcium citrate", "adcal", "calcichew", "accrete"), "polyvalent_cation"),
    s("iron", "Iron", MINERAL,
        listOf("iron", "ferrous sulfate", "ferrous sulphate", "ferrous fumarate", "ferrous gluconate", "ferric",
            "feroglobin", "spatone", "iron bisglycinate"), "polyvalent_cation"),
    s("magnesium", "Magnesium", MINERAL,
        listOf("magnesium", "magnesium citrate", "magnesium glycinate", "magnesium oxide", "magnesium bisglycinate"), "polyvalent_cation"),
    s("zinc", "Zinc", MINERAL, listOf("zinc", "zinc gluconate", "zinc picolinate", "zinc citrate"), "polyvalent_cation"),
    s("potassium", "Potassium", MINERAL, listOf("potassium", "potassium chloride", "sando k", "slow k"), "potassium"),
    s("selenium", "Selenium", MINERAL, listOf("selenium", "selenomethionine"), "selenium"),
    s("iodine", "Iodine", MINERAL, listOf("iodine", "kelp", "potassium iodide"), "iodine"),
    s("copper", "Copper", MINERAL, listOf("copper"), "copper"),
    s("chromium", "Chromium", MINERAL, listOf("chromium", "chromium picolinate"), "glucose_lowering_supplement"),

    // ---- Herbal & other supplements ----
    s("st_johns_wort", "St John's wort", HERBAL,
        listOf("st john s wort", "st johns wort", "saint john s wort", "saint johns wort", "hypericum"), "serotonergic", "enzyme_inducer"),
    s("ginkgo", "Ginkgo biloba", HERBAL, listOf("ginkgo", "ginkgo biloba", "gingko"), "bleeding_supplement"),
    s("garlic", "Garlic", HERBAL, listOf("garlic", "allicin"), "bleeding_supplement"),
    s("fish_oil", "Fish oil / omega-3", OTHER_SUPPLEMENT,
        listOf("fish oil", "omega 3", "omega3", "cod liver oil", "krill oil", "epa", "dha"), "bleeding_supplement", "fat_soluble"),
    s("turmeric", "Turmeric / curcumin", HERBAL, listOf("turmeric", "curcumin"), "bleeding_supplement"),
    s("ginseng", "Ginseng", HERBAL, listOf("ginseng", "panax"), "stimulant_herb", "glucose_lowering_supplement"),
    s("rhodiola", "Rhodiola", HERBAL, listOf("rhodiola", "rhodiola rosea"), "stimulant_herb"),
    s("echinacea", "Echinacea", HERBAL, listOf("echinacea"), "immunostimulant"),
    s("valerian", "Valerian", HERBAL, listOf("valerian", "valerian root"), "sedative_herb"),
    s("kava", "Kava", HERBAL, listOf("kava", "kava kava"), "sedative_herb"),
    s("passionflower", "Passionflower", HERBAL, listOf("passionflower", "passion flower", "passiflora"), "sedative_herb"),
    s("melatonin", "Melatonin", OTHER_SUPPLEMENT, listOf("melatonin", "circadin"), "sedative_herb"),
    s("ashwagandha", "Ashwagandha", HERBAL, listOf("ashwagandha", "withania"), "sedative_herb", "thyroid_active"),
    s("red_yeast_rice", "Red yeast rice", HERBAL, listOf("red yeast rice", "monacolin"), "statin"),
    s("liquorice", "Liquorice root", HERBAL, listOf("liquorice", "licorice", "glycyrrhiza"), "liquorice"),
    s("berberine", "Berberine", HERBAL, listOf("berberine"), "glucose_lowering_supplement"),
    s("cranberry", "Cranberry", HERBAL, listOf("cranberry"), "cranberry"),
    s("glucosamine", "Glucosamine", OTHER_SUPPLEMENT, listOf("glucosamine"), "glucosamine"),
    s("coq10", "Coenzyme Q10", OTHER_SUPPLEMENT,
        listOf("coenzyme q10", "coq10", "co q10", "q10", "ubiquinol", "ubiquinone"), "coq10", "fat_soluble"),
    s("probiotic", "Probiotic", OTHER_SUPPLEMENT,
        listOf("probiotic", "probiotics", "lactobacillus", "bifidobacterium", "acidophilus", "saccharomyces", "yakult"), "probiotic"),
    s("five_htp", "5-HTP", OTHER_SUPPLEMENT, listOf("5 htp", "5htp", "5 hydroxytryptophan"), "serotonergic"),
)
