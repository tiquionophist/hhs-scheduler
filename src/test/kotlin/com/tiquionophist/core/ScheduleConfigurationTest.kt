package com.tiquionophist.core

import com.tiquionophist.scheduler.ScheduleConfigurationFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

internal class ScheduleConfigurationTest {
    data class VerifyTestCase(val config: ScheduleConfiguration, val errors: List<String> = emptyList())

    @ParameterizedTest
    @MethodSource("configurations")
    fun testSaveLoad(config: ScheduleConfiguration) {
        val file = File("test-config.json")
        file.deleteOnExit()

        config.save(file)
        val loaded = ScheduleConfiguration.loadOrNull(file)

        assertEquals(config, loaded)
    }

    @ParameterizedTest
    @MethodSource
    fun testVerify(testCase: VerifyTestCase) {
        assertEquals(testCase.errors, testCase.config.validationErrors())
    }

    companion object {
        @JvmStatic
        fun configurations() = ScheduleConfigurationFixtures.allConfigurations

        @JvmStatic
        fun testVerify(): List<VerifyTestCase> {
            // TODO cover other error cases (and more success cases)
            return listOf(
                VerifyTestCase(config = ScheduleConfiguration(classes = 2)),
                VerifyTestCase(
                    config = ScheduleConfiguration(classes = -1),
                    errors = listOf("Must have >0 classes"),
                ),
                VerifyTestCase(
                    config = ScheduleConfiguration(
                        classes = 10,
                        teacherAssignments = mapOf(
                            Teacher("Teacher", "1") to setOf(Subject.CHEMISTRY, Subject.PHYSICS),
                            Teacher("Teacher", "2") to setOf(Subject.CHEMISTRY, Subject.PHYSICS),
                            Teacher("Teacher", "3") to setOf(Subject.CHEMISTRY, Subject.PHYSICS),
                        ),
                        subjectFrequency = mapOf(
                            Subject.CHEMISTRY to 2,
                            Subject.PHYSICS to 1,
                            Subject.EMPTY to 17,
                        ),
                    ),
                    errors = listOf(
                        "Classroom Chemistry must be occupied at least 30 times per week (by Chemistry, Physics), " +
                                "which is impossible"
                    ),
                ),
            )
        }
    }
}
