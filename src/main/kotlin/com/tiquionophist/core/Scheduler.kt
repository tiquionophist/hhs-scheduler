package com.tiquionophist.core

interface Scheduler {
    /**
     * Attempts to generate a schedule based on [configuration], returning null if no schedule could be found.
     */
    suspend fun schedule(configuration: ScheduleConfiguration): Schedule?
}
