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
    s("fluoxetine", "Fluoxetine", MEDICINE, listOf("fluoxetine", "prozac"), "ssri", "serotonergic", "cyp2d6_strong"),
    s("citalopram", "Citalopram", MEDICINE, listOf("citalopram", "celexa", "cipramil"), "ssri", "serotonergic", "qt"),
    s("escitalopram", "Escitalopram", MEDICINE, listOf("escitalopram", "lexapro", "cipralex"), "ssri", "serotonergic", "qt"),
    s("paroxetine", "Paroxetine", MEDICINE, listOf("paroxetine", "paxil", "seroxat"), "ssri", "serotonergic", "cyp2d6_strong"),
    s("venlafaxine", "Venlafaxine", MEDICINE, listOf("venlafaxine", "effexor"), "ssri", "serotonergic"),
    s("duloxetine", "Duloxetine", MEDICINE, listOf("duloxetine", "cymbalta"), "ssri", "serotonergic"),
    s("amitriptyline", "Amitriptyline", MEDICINE, listOf("amitriptyline"), "serotonergic", "cns_depressant", "anticholinergic", "sedating_night"),
    s("mirtazapine", "Mirtazapine", MEDICINE, listOf("mirtazapine", "zispin", "remeron"), "serotonergic", "cns_depressant", "sedating_night"),
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
    s("clarithromycin", "Clarithromycin", MEDICINE, listOf("clarithromycin", "klaricid", "biaxin"), "antibiotic", "macrolide", "cyp3a4_inhibitor", "qt"),

    // ---- Bone ----
    s("alendronate", "Alendronic acid", MEDICINE, listOf("alendronate", "alendronic acid", "fosamax"), "bisphosphonate"),
    s("risedronate", "Risedronate", MEDICINE, listOf("risedronate", "risedronic acid", "actonel"), "bisphosphonate"),
    s("ibandronate", "Ibandronic acid", MEDICINE, listOf("ibandronate", "ibandronic acid", "bonviva", "boniva"), "bisphosphonate"),

    // ---- Cholesterol ----
    s("simvastatin", "Simvastatin", MEDICINE, listOf("simvastatin", "zocor"), "statin", "statin_evening", "statin_3a4"),
    s("pravastatin", "Pravastatin", MEDICINE, listOf("pravastatin", "pravachol"), "statin", "statin_evening"),
    s("fluvastatin", "Fluvastatin", MEDICINE, listOf("fluvastatin", "lescol"), "statin", "statin_evening"),
    s("lovastatin", "Lovastatin", MEDICINE, listOf("lovastatin", "mevacor"), "statin", "statin_evening", "statin_3a4"),
    s("atorvastatin", "Atorvastatin", MEDICINE, listOf("atorvastatin", "lipitor"), "statin", "statin_3a4"),
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
    s("bisoprolol", "Bisoprolol", MEDICINE, listOf("bisoprolol", "cardicor", "concor"), "beta_blocker", "antihypertensive"),
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
    s("prednisolone", "Prednisolone", MEDICINE, listOf("prednisolone"), "corticosteroid", "steroid_morning"),
    s("prednisone", "Prednisone", MEDICINE, listOf("prednisone"), "corticosteroid", "steroid_morning"),
    s("levodopa", "Levodopa", MEDICINE, listOf("levodopa", "co careldopa", "co beneldopa", "sinemet", "madopar"), "levodopa"),
    s("contraceptive", "Hormonal contraceptive", MEDICINE,
        listOf("contraceptive", "contraceptive pill", "oral contraceptive", "the pill", "microgynon", "rigevidon",
            "yasmin", "levonorgestrel", "ethinylestradiol", "desogestrel", "cerazette"), "hormonal_contraceptive"),
    s("ciclosporin", "Ciclosporin", MEDICINE, listOf("ciclosporin", "cyclosporine", "neoral"), "immunosuppressant"),
    s("tacrolimus", "Tacrolimus", MEDICINE, listOf("tacrolimus", "prograf", "adoport"), "immunosuppressant"),
    s("isotretinoin", "Isotretinoin", MEDICINE, listOf("isotretinoin", "roaccutane", "accutane"), "retinoid"),

    // ---- Gout ----
    s("allopurinol", "Allopurinol", MEDICINE, listOf("allopurinol", "zyloric", "zyloprim", "uricto"), "xanthine_oxidase_inhibitor"),
    s("febuxostat", "Febuxostat", MEDICINE, listOf("febuxostat", "adenuric", "uloric"), "xanthine_oxidase_inhibitor"),
    s("colchicine", "Colchicine", MEDICINE, listOf("colchicine"), "colchicine"),

    // ---- Immunosuppressants & specialist ----
    s("azathioprine", "Azathioprine", MEDICINE, listOf("azathioprine", "imuran"), "thiopurine", "immunosuppressant"),
    s("mercaptopurine", "Mercaptopurine", MEDICINE, listOf("mercaptopurine", "xaluprine", "purinethol"), "thiopurine"),
    s("hydroxychloroquine", "Hydroxychloroquine", MEDICINE, listOf("hydroxychloroquine", "plaquenil"), "qt"),
    s("tamoxifen", "Tamoxifen", MEDICINE, listOf("tamoxifen", "nolvadex"), "tamoxifen"),
    s("hrt", "Hormone replacement therapy", MEDICINE,
        listOf("hrt", "estradiol", "oestradiol", "evorel", "elleste", "premarin", "conjugated oestrogens", "femoston"), "hormone_therapy"),

    // ---- Nerve pain & epilepsy ----
    s("gabapentin", "Gabapentin", MEDICINE, listOf("gabapentin", "neurontin"), "gabapentinoid", "cns_depressant"),
    s("pregabalin", "Pregabalin", MEDICINE, listOf("pregabalin", "lyrica", "alzain"), "gabapentinoid", "cns_depressant"),
    s("carbamazepine", "Carbamazepine", MEDICINE, listOf("carbamazepine", "tegretol"), "enzyme_inducer", "antiepileptic"),
    s("phenytoin", "Phenytoin", MEDICINE, listOf("phenytoin", "epanutin", "dilantin"), "enzyme_inducer", "antiepileptic"),
    s("valproate", "Sodium valproate", MEDICINE,
        listOf("valproate", "sodium valproate", "valproic acid", "epilim", "depakote", "divalproex"), "antiepileptic"),
    s("lamotrigine", "Lamotrigine", MEDICINE, listOf("lamotrigine", "lamictal"), "antiepileptic"),
    s("levetiracetam", "Levetiracetam", MEDICINE, listOf("levetiracetam", "keppra"), "antiepileptic"),

    // ---- More pain relief ----
    s("dihydrocodeine", "Dihydrocodeine", MEDICINE, listOf("dihydrocodeine", "df118", "co dydramol"), "opioid", "cns_depressant"),
    s("buprenorphine", "Buprenorphine", MEDICINE, listOf("buprenorphine", "butrans", "subutex", "transtec"), "opioid", "cns_depressant"),
    s("fentanyl", "Fentanyl", MEDICINE, listOf("fentanyl", "durogesic"), "opioid", "cns_depressant"),
    s("mefenamic_acid", "Mefenamic acid", MEDICINE, listOf("mefenamic acid", "ponstan"), "nsaid"),
    s("etoricoxib", "Etoricoxib", MEDICINE, listOf("etoricoxib", "arcoxia"), "nsaid"),
    s("meloxicam", "Meloxicam", MEDICINE, listOf("meloxicam", "mobic"), "nsaid"),

    // ---- More antibiotics & antifungals ----
    s("trimethoprim", "Trimethoprim", MEDICINE, listOf("trimethoprim"), "antibiotic", "raises_potassium"),
    s("nitrofurantoin", "Nitrofurantoin", MEDICINE, listOf("nitrofurantoin", "macrobid", "macrodantin"), "antibiotic"),
    s("flucloxacillin", "Flucloxacillin", MEDICINE, listOf("flucloxacillin", "floxapen"), "antibiotic", "penicillin_empty"),
    s("penicillin_v", "Phenoxymethylpenicillin", MEDICINE,
        listOf("phenoxymethylpenicillin", "penicillin v", "penicillin"), "antibiotic", "penicillin_empty"),
    s("cefalexin", "Cefalexin", MEDICINE, listOf("cefalexin", "cephalexin", "keflex"), "antibiotic"),
    s("erythromycin", "Erythromycin", MEDICINE, listOf("erythromycin", "erythrocin"), "antibiotic", "macrolide", "cyp3a4_inhibitor", "qt"),
    s("azithromycin", "Azithromycin", MEDICINE, listOf("azithromycin", "zithromax"), "antibiotic", "macrolide", "qt"),
    s("metronidazole", "Metronidazole", MEDICINE, listOf("metronidazole", "flagyl"), "antibiotic"),
    s("fluconazole", "Fluconazole", MEDICINE, listOf("fluconazole", "diflucan"), "cyp3a4_inhibitor", "qt"),

    // ---- More heart & blood pressure ----
    s("amiodarone", "Amiodarone", MEDICINE, listOf("amiodarone", "cordarone"), "cyp3a4_inhibitor", "qt"),
    s("verapamil", "Verapamil", MEDICINE, listOf("verapamil", "securon"), "antihypertensive", "cyp3a4_inhibitor", "rate_ccb"),
    s("diltiazem", "Diltiazem", MEDICINE, listOf("diltiazem", "tildiem", "adizem"), "antihypertensive", "cyp3a4_inhibitor", "rate_ccb"),
    s("nifedipine", "Nifedipine", MEDICINE, listOf("nifedipine", "adalat"), "antihypertensive"),
    s("atenolol", "Atenolol", MEDICINE, listOf("atenolol", "tenormin"), "beta_blocker", "antihypertensive"),
    s("propranolol", "Propranolol", MEDICINE, listOf("propranolol", "inderal"), "beta_blocker", "antihypertensive"),
    s("metoprolol", "Metoprolol", MEDICINE, listOf("metoprolol", "lopressor", "betaloc"), "beta_blocker", "antihypertensive"),
    s("doxazosin", "Doxazosin", MEDICINE, listOf("doxazosin", "cardura"), "alpha_blocker", "antihypertensive"),
    s("tamsulosin", "Tamsulosin", MEDICINE, listOf("tamsulosin", "flomax", "flomaxtra"), "alpha_blocker"),
    s("finasteride", "Finasteride", MEDICINE, listOf("finasteride", "proscar", "propecia"), "finasteride"),
    s("eplerenone", "Eplerenone", MEDICINE, listOf("eplerenone", "inspra"), "raises_potassium", "antihypertensive"),
    s("ticagrelor", "Ticagrelor", MEDICINE, listOf("ticagrelor", "brilique", "brilinta"), "antiplatelet"),
    s("prasugrel", "Prasugrel", MEDICINE, listOf("prasugrel", "efient"), "antiplatelet"),

    // ---- More diabetes ----
    s("dapagliflozin", "Dapagliflozin", MEDICINE, listOf("dapagliflozin", "forxiga", "farxiga"), "antidiabetic", "sglt2"),
    s("empagliflozin", "Empagliflozin", MEDICINE, listOf("empagliflozin", "jardiance"), "antidiabetic", "sglt2"),
    s("sitagliptin", "Sitagliptin", MEDICINE, listOf("sitagliptin", "januvia"), "antidiabetic"),
    s("semaglutide", "Semaglutide", MEDICINE, listOf("semaglutide", "ozempic", "wegovy", "rybelsus"), "antidiabetic"),

    // ---- Mental health & dementia ----
    s("quetiapine", "Quetiapine", MEDICINE, listOf("quetiapine", "seroquel"), "antipsychotic", "cns_depressant", "qt"),
    s("olanzapine", "Olanzapine", MEDICINE, listOf("olanzapine", "zyprexa"), "antipsychotic", "cns_depressant"),
    s("risperidone", "Risperidone", MEDICINE, listOf("risperidone", "risperdal"), "antipsychotic"),
    s("trazodone", "Trazodone", MEDICINE, listOf("trazodone", "molipaxin", "desyrel"), "serotonergic", "cns_depressant", "sedating_night"),
    s("nortriptyline", "Nortriptyline", MEDICINE, listOf("nortriptyline", "allegron", "pamelor"),
        "serotonergic", "cns_depressant", "anticholinergic", "sedating_night"),
    s("donepezil", "Donepezil", MEDICINE, listOf("donepezil", "aricept"), "cholinesterase_inhibitor"),
    s("memantine", "Memantine", MEDICINE, listOf("memantine", "ebixa", "namenda"), "memantine"),
    s("zolmitriptan", "Zolmitriptan", MEDICINE, listOf("zolmitriptan", "zomig"), "serotonergic"),
    s("rizatriptan", "Rizatriptan", MEDICINE, listOf("rizatriptan", "maxalt"), "serotonergic"),

    // ---- Allergy, breathing, bladder ----
    s("montelukast", "Montelukast", MEDICINE, listOf("montelukast", "singulair"), "montelukast"),
    s("salbutamol", "Salbutamol", MEDICINE, listOf("salbutamol", "albuterol", "ventolin"), "salbutamol"),
    s("theophylline", "Theophylline", MEDICINE, listOf("theophylline", "aminophylline", "uniphyllin"), "theophylline"),
    s("cetirizine", "Cetirizine", MEDICINE, listOf("cetirizine", "zirtek", "piriteze", "zyrtec"), "antihistamine"),
    s("loratadine", "Loratadine", MEDICINE, listOf("loratadine", "clarityn", "claritin"), "antihistamine"),
    s("fexofenadine", "Fexofenadine", MEDICINE, listOf("fexofenadine", "telfast", "allegra"), "antihistamine"),
    s("chlorphenamine", "Chlorphenamine", MEDICINE, listOf("chlorphenamine", "chlorpheniramine", "piriton"),
        "antihistamine", "cns_depressant", "anticholinergic"),
    s("promethazine", "Promethazine", MEDICINE, listOf("promethazine", "phenergan", "sominex"),
        "antihistamine", "cns_depressant", "anticholinergic", "sedating_night"),
    s("diphenhydramine", "Diphenhydramine", MEDICINE, listOf("diphenhydramine", "nytol", "benadryl"),
        "antihistamine", "cns_depressant", "anticholinergic"),
    s("oxybutynin", "Oxybutynin", MEDICINE, listOf("oxybutynin", "ditropan", "kentera"), "anticholinergic"),
    s("solifenacin", "Solifenacin", MEDICINE, listOf("solifenacin", "vesicare"), "anticholinergic"),
    s("tolterodine", "Tolterodine", MEDICINE, listOf("tolterodine", "detrusitol", "detrol"), "anticholinergic"),

    // ---- Stomach & bowel ----
    s("metoclopramide", "Metoclopramide", MEDICINE, listOf("metoclopramide", "maxolon", "reglan"), "metoclopramide"),
    s("domperidone", "Domperidone", MEDICINE, listOf("domperidone", "motilium"), "qt"),
    s("ondansetron", "Ondansetron", MEDICINE, listOf("ondansetron", "zofran"), "qt"),
    s("loperamide", "Loperamide", MEDICINE, listOf("loperamide", "imodium"), "loperamide"),
    s("famotidine", "Famotidine", MEDICINE, listOf("famotidine", "pepcid"), "h2_blocker"),
    s("senna", "Senna", MEDICINE, listOf("senna", "senokot", "sennosides"), "laxative"),
    s("hydrocortisone", "Hydrocortisone", MEDICINE, listOf("hydrocortisone"), "corticosteroid"),
    s("dexamethasone", "Dexamethasone", MEDICINE, listOf("dexamethasone"), "corticosteroid", "steroid_morning"),

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
    s("thiamine", "Thiamine (vitamin B1)", VITAMIN, listOf("thiamine", "vitamin b1", "b1"), "b_energising"),
    s("chondroitin", "Chondroitin", OTHER_SUPPLEMENT, listOf("chondroitin"), "chondroitin"),
    s("collagen", "Collagen", OTHER_SUPPLEMENT, listOf("collagen"), "collagen"),
    s("creatine", "Creatine", OTHER_SUPPLEMENT, listOf("creatine"), "creatine"),
    s("evening_primrose", "Evening primrose oil", HERBAL, listOf("evening primrose", "evening primrose oil"), "evening_primrose"),
    s("milk_thistle", "Milk thistle", HERBAL, listOf("milk thistle", "silymarin", "silybum"), "milk_thistle"),
    s("saw_palmetto", "Saw palmetto", HERBAL, listOf("saw palmetto"), "bleeding_supplement"),
    s("black_cohosh", "Black cohosh", HERBAL, listOf("black cohosh", "cimicifuga"), "liver_herb"),
    s("green_tea_extract", "Green tea extract", HERBAL, listOf("green tea extract", "egcg"), "liver_herb"),
    s("ginger", "Ginger", HERBAL, listOf("ginger", "ginger root"), "bleeding_supplement"),
    s("feverfew", "Feverfew", HERBAL, listOf("feverfew"), "bleeding_supplement"),
    s("dong_quai", "Dong quai", HERBAL, listOf("dong quai", "angelica sinensis"), "bleeding_supplement"),
    s("cbd", "CBD (cannabidiol)", OTHER_SUPPLEMENT, listOf("cbd", "cannabidiol", "cbd oil"), "sedative_herb"),
    s("grapefruit", "Grapefruit", OTHER_SUPPLEMENT, listOf("grapefruit", "grapefruit juice"), "grapefruit"),
    s("lemon_balm", "Lemon balm", HERBAL, listOf("lemon balm", "melissa"), "sedative_herb"),
    s("chamomile", "Chamomile", HERBAL, listOf("chamomile"), "sedative_herb"),
)
