package com.tiquionophist.scheduler

import com.tiquionophist.core.Scheduler

/**
 * An exhaustive search of all possible schedules, filling out each schedule based on [fillOrder] until no possible
 * lesson can be added. Tends to be very slow for large search spaces.
 */
class ExhaustiveScheduler(
    private val fillOrder: RandomizedScheduler.ScheduleFillOrder,
) : Scheduler by RandomizedScheduler(
    fillOrder = fillOrder,
    attemptsPerRound = null,
    rounds = 1,
    randomSeed = null,
)
