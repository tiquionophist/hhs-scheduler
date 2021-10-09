package com.tiquionophist.scheduler

import com.tiquionophist.core.ScheduleConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ExhaustiveSchedulerTest {
    @ParameterizedTest
    @MethodSource("possibleConfiguration")
    fun testPossibleConfigurations(configuration: ScheduleConfiguration) {
        val scheduler = ExhaustiveScheduler(fillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS)

        val schedule = runBlocking { scheduler.schedule(configuration) }

        assertNotNull(schedule)
        assertDoesNotThrow { schedule!!.verify(configuration) }
    }

    @ParameterizedTest
    @MethodSource("impossibleConfiguration")
    fun testImpossibleConfigurations(configuration: ScheduleConfiguration) {
        val scheduler = ExhaustiveScheduler(fillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS)

        val schedule = runBlocking { scheduler.schedule(configuration) }

        assertNull(schedule)
    }

    companion object {
        @JvmStatic
        fun possibleConfiguration(): List<ScheduleConfiguration> {
            return listOf(
                ScheduleConfigurationFixtures.trivialConfiguration,
                ScheduleConfigurationFixtures.easyConfiguration,
            )
        }

        @JvmStatic
        fun impossibleConfiguration() = ScheduleConfigurationFixtures.impossibleConfigurations
    }
}
