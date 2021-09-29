package com.tiquionophist.scheduler

import com.tiquionophist.core.ScheduleConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class OptaSchedulerTest {
    @ParameterizedTest
    @MethodSource("possibleConfiguration")
    fun testPossibleConfigurations(configuration: ScheduleConfiguration) {
        val scheduler = OptaScheduler(timeoutSeconds = 10)

        val schedule = runBlocking { scheduler.schedule(configuration) }

        assertNotNull(schedule)
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
