package com.tiquionophist.core

import java.math.BigDecimal
import java.util.EnumMap

/**
 * The set of stats that can be gained (or lost) by students/teachers as a result of the classes they attend/teach.
 */
enum class Stat {
    AROUSAL,
    AUTHORITY,
    CHARISMA,
    CORRUPTION,
    EDUCATION,
    ENERGY,
    HAPPINESS,
    INHIBITION,
    INTELLIGENCE,
    LOYALTY,
    LUST,
    STAMINA,
    WILLPOWER,
}

/**
 * A collection of [Stat]s mapped to [BigDecimal] values (to avoid floating point addition).
 */
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
