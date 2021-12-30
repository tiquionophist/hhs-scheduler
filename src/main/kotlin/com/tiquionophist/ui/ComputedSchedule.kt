package com.tiquionophist.ui

import com.tiquionophist.core.Schedule
import com.tiquionophist.core.ScheduleConfiguration

/**
 * Wraps a [schedule] computed from a certain [configuration], along with an [index] incremented each time a new object
 * is created.
 */
data class ComputedSchedule(
    val configuration: ScheduleConfiguration,
    val schedulerSettings: SchedulerSettings = SchedulerSettings(),
    val schedule: Schedule,
    val index: Int = indexCounter++
) {
    companion object {
        private var indexCounter: Int = 0
    }
}
