package com.tiquionophist.scheduler

import com.tiquionophist.core.ScheduleConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ExhaustiveSchedulerTest {
    @ParameterizedTest
    @MethodSource("possibleConfiguration")
    fun testPossibleConfigurations(configuration: ScheduleConfiguration) {
        val scheduler = ExhaustiveScheduler(fillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS)

        val schedule = runBlocking { scheduler.schedule(configuration) }

        Assertions.assertNotNull(schedule)
        assertDoesNotThrow { schedule!!.verify(configuration) }
    }

    companion object {
        @JvmStatic
        fun possibleConfiguration(): List<ScheduleConfiguration> {
            return listOf(
                ScheduleConfigurationFixtures.trivialConfiguration,
                ScheduleConfigurationFixtures.easyConfiguration,
            )
        }
    }
}
