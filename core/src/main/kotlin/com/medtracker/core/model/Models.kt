package com.medtracker.core.model

/** The kind of product a user is taking. */
enum class ItemType(val label: String) {
    MEDICINE("Medicine"),
    VITAMIN("Vitamin"),
    MINERAL("Mineral"),
    HERBAL("Herbal"),
    OTHER_SUPPLEMENT("Other supplement"),
}

/** Units a single dose can be entered in. Only mass units and IU can be checked against dose limits. */
enum class DoseUnit(val label: String) {
    MG("mg"),
    MCG("mcg"),
    G("g"),
    IU("IU"),
    ML("ml"),
    TABLET("tablet(s)"),
    CAPSULE("capsule(s)"),
    DROP("drop(s)"),
    SACHET("sachet(s)"),
    PUFF("puff(s)"),
}

/** How a dose is taken relative to food. */
enum class FoodRelation(val label: String) {
    ANY("No preference"),
    WITH_FOOD("With food"),
    EMPTY_STOMACH("Empty stomach"),
}

/** A clock time within the day, stored as minutes after midnight. */
@JvmInline
value class DoseTime(val minuteOfDay: Int) : Comparable<DoseTime> {
    init {
        require(minuteOfDay in 0 until MINUTES_PER_DAY) { "minuteOfDay out of range: $minuteOfDay" }
    }

    val hour: Int get() = minuteOfDay / 60
    val minute: Int get() = minuteOfDay % 60

    override fun compareTo(other: DoseTime): Int = minuteOfDay.compareTo(other.minuteOfDay)

    /** 24-hour "HH:mm" representation. */
    fun format(): String = "%02d:%02d".format(hour, minute)

    override fun toString(): String = format()

    companion object {
        const val MINUTES_PER_DAY = 24 * 60

        fun of(hour: Int, minute: Int = 0): DoseTime = DoseTime(hour * 60 + minute)

        /** Parses "HH:mm"; returns null for malformed input. */
        fun parse(text: String): DoseTime? {
            val parts = text.trim().split(":")
            if (parts.size != 2) return null
            val h = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            if (h !in 0..23 || m !in 0..59) return null
            return of(h, m)
        }

        /** Shortest distance in minutes between two clock times, wrapping around midnight. */
        fun circularDistance(a: DoseTime, b: DoseTime): Int {
            val d = kotlin.math.abs(a.minuteOfDay - b.minuteOfDay)
            return minOf(d, MINUTES_PER_DAY - d)
        }
    }
}

/** One medicine or supplement in the user's regimen. */
data class RegimenItem(
    val id: String,
    val name: String,
    val type: ItemType,
    val doseAmount: Double,
    val doseUnit: DoseUnit,
    val times: List<DoseTime>,
    val food: FoodRelation = FoodRelation.ANY,
    val notes: String = "",
) {
    val sortedTimes: List<DoseTime> get() = times.sorted()

    /** Human readable dose, e.g. "500 mg" or "1.5 tablet(s)". */
    fun doseLabel(): String = "${formatAmount(doseAmount)} ${doseUnit.label}"

    companion object {
        fun formatAmount(amount: Double): String =
            if (amount == Math.floor(amount) && !amount.isInfinite()) amount.toLong().toString()
            else "%.2f".format(java.util.Locale.ROOT, amount).trimEnd('0').trimEnd('.')
    }
}
