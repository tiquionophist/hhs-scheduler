package com.tiquionophist.scheduler

import com.tiquionophist.core.ScheduleConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class RandomizedSchedulerTest {
    @ParameterizedTest
    @MethodSource("possibleConfiguration")
    fun testPossibleConfigurations(configuration: ScheduleConfiguration) {
        val scheduler = RandomizedScheduler(attemptsPerRound = 1_000, rounds = 1_000)

        val schedule = runBlocking { scheduler.schedule(configuration) }

        assertNotNull(schedule)
        assertDoesNotThrow { schedule!!.verify(configuration) }
    }

    @ParameterizedTest
    @MethodSource("impossibleConfiguration")
    fun testImpossibleConfigurations(configuration: ScheduleConfiguration) {
        val scheduler = RandomizedScheduler(attemptsPerRound = 1_000, rounds = 1_000)

        val schedule = runBlocking { scheduler.schedule(configuration) }

        assertNull(schedule)
    }

    companion object {
        @JvmStatic
        fun possibleConfiguration() = ScheduleConfigurationFixtures.possibleConfigurations

        @JvmStatic
        fun impossibleConfiguration() = ScheduleConfigurationFixtures.impossibleConfigurations
    }
}
