package com.tiquionophist.scheduler

import com.tiquionophist.core.Scheduler

/**
 * An exhaustive search of all possible schedules, filling out each schedule based on [fillOrder] until no possible
 * lesson can be added. Aborts after reaching an end state (when no possible lesson can be added) [maxAttempts] times,
 * or never if [maxAttempts] is null in which case the algorithm is guaranteed to find a schedule if one exists. Tends
 * to be very slow for large search spaces.
 */
class ExhaustiveScheduler(
    private val fillOrder: RandomizedScheduler.ScheduleFillOrder,
    private val maxAttempts: Int? = null,
) : Scheduler by RandomizedScheduler(
    fillOrder = fillOrder,
    attemptsPerRound = maxAttempts,
    rounds = 1,
    randomSeed = { null }
)
