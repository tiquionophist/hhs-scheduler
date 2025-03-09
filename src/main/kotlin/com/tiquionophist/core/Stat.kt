package com.tiquionophist.core

import androidx.compose.runtime.Immutable
import java.math.BigDecimal
import java.text.DecimalFormat
import java.util.EnumMap

/**
 * The set of stats that can be gained (or lost) by students/teachers as a result of the classes they attend/teach.
 */
enum class Stat(val positiveBetter: Boolean) {
    AROUSAL(positiveBetter = true),
    AUTHORITY(positiveBetter = true), // unsure what effects student authority have
    CHARISMA(positiveBetter = true),
    CORRUPTION(positiveBetter = true),
    EDUCATION(positiveBetter = true),
    ENERGY(positiveBetter = true),
    HAPPINESS(positiveBetter = true),
    INHIBITION(positiveBetter = false),
    INTELLIGENCE(positiveBetter = true),
    LOYALTY(positiveBetter = true),
    LUST(positiveBetter = true),
    STAMINA(positiveBetter = true),
    WILLPOWER(positiveBetter = false);

    companion object {
        val format = DecimalFormat("0.00").apply {
            positivePrefix = "+"
        }
    }
}

/**
 * A collection of [Stat]s mapped to [BigDecimal] values (to avoid floating point addition).
 */
@Immutable
data class StatSet(val stats: EnumMap<Stat, BigDecimal>) {
    /**
     * A convenience constructor which converts [String]s into [BigDecimal]s. Repeated [Stat]s in the arguments are
     * ignored.
     */
    constructor(vararg stats: Pair<Stat, String>) : this(
        stats
            .takeIf { it.isNotEmpty() }
            ?.let { EnumMap(it.associate { (stat, string) -> Pair(stat, BigDecimal(string)) }) }
            ?: EnumMap(Stat::class.java)
    )

    /**
     * Combines this [StatSet] and the given one, summing their values.
     */
    operator fun plus(other: StatSet): StatSet {
        val map = EnumMap(stats)
        other.stats.forEach { (stat, value) ->
            map.compute(stat) { _, currentValue ->
                (currentValue?.plus(value) ?: value).takeIf { it != BigDecimal.ZERO }
            }
        }

        return StatSet(stats = map)
    }
}
