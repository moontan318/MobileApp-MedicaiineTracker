# Medicine Tracker (Android)

An Android app for recording the medicines, vitamins, minerals and herbal supplements you take - with dose and time of day - and reviewing them for:

- **Interactions & contraindications** - e.g. warfarin + ibuprofen, sertraline + St John's wort, ACE inhibitor + potassium, sildenafil + nitrates, duplicate statins (including red yeast rice).
- **Timing problems** - products that block each other's absorption when taken together (levothyroxine vs calcium/iron/magnesium, antibiotics vs minerals, iron vs calcium, probiotics vs antibiotics), and medicines best taken at a particular time or with/without food (simvastatin in the evening, levothyroxine before breakfast, diuretics in the morning, fat-soluble vitamins with food...).
- **Dosage** - daily totals summed across all products (so paracetamol in two products is caught), single-dose limits (e.g. calcium > 500 mg per dose), minimum gaps between doses, and IU → mcg/mg conversion for vitamins A, D and E.

It then proposes a **revised daily schedule** that separates clashing products and moves items into their preferred time windows - moving supplements rather than prescribed medicines where possible - which you can apply with one tap.

> **Disclaimer:** this app is an information aid, not medical advice. Its built-in database covers ~130 common products and general adult guidance only. Always check with a doctor or pharmacist before changing or stopping a medicine.

## Screens

| Tab | What it does |
|---|---|
| **My list** | Everything you take, grouped by type, with a summary of issues. Tap **Add** or an item to edit. Names autocomplete from the database (brand names such as Nurofen or Eliquis are recognised). |
| **Schedule** | Your day in time order. Toggle between your current times and the suggested times, and apply the suggestions. |
| **Review** | Counts by severity (Major / Moderate / Minor / Info), suggested timing changes, and every finding with an explanation and "What to do". |

All data is stored locally on the device (`regimen.json` in app-private storage).

## Project structure

```
core/   Pure Kotlin - knowledge base and analysis engine (unit-tested, no Android dependency)
  knowledge/Substances.kt     substances, aliases & brand names, class tags
  knowledge/Interactions.kt   pairwise interaction and absorption-separation rules
  knowledge/Advice.kt         time-of-day / food advice and dose limits
  analysis/RegimenAnalyzer.kt produces findings
  analysis/ScheduleOptimizer.kt proposes new dose times
app/    Android app - Jetpack Compose + Material 3
```

Rules target *tags* (e.g. `nsaid`, `polyvalent_cation`) so adding a new product usually only means adding one line to `Substances.kt`.

## Building

Requirements: JDK 17+ and the Android SDK (API 35), e.g. via Android Studio.

```bash
./gradlew :core:test          # run the engine tests
./gradlew :app:assembleDebug  # build app/build/outputs/apk/debug/app-debug.apk
```

Install on a device with `adb install app/build/outputs/apk/debug/app-debug.apk`, or open the project in Android Studio and press Run.

Every push also builds the APK on GitHub Actions (see the **Android build** workflow); download it from the run's **Artifacts** section and sideload it (enable "Install unknown apps" for your browser/file manager).

Minimum Android version: 8.0 (API 26).
