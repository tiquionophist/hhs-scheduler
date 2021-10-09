package com.tiquionophist.core

import com.tiquionophist.scheduler.ScheduleConfigurationFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

internal class ScheduleConfigurationTest {
    @ParameterizedTest
    @MethodSource("configurations")
    fun testSaveLoad(config: ScheduleConfiguration) {
        val file = File("test-config.json")
        file.deleteOnExit()

        config.save(file)
        val loaded = ScheduleConfiguration.loadOrNull(file)

        assertEquals(config, loaded)
    }

    companion object {
        @JvmStatic
        fun configurations() = ScheduleConfigurationFixtures.allConfigurations
    }
}
