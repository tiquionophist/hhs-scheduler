package com.tiquionophist.core

import com.tiquionophist.scheduler.ScheduleConfigurationFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

internal class ScheduleConfigurationTest {
    data class ValidationTestCase(val config: ScheduleConfiguration, val errors: List<String> = emptyList())

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
    fun testValidationErrors(testCase: ValidationTestCase) {
        assertEquals(testCase.errors, testCase.config.validationErrors())
    }

    companion object {
        @JvmStatic
        fun configurations() = ScheduleConfigurationFixtures.allConfigurations

        @JvmStatic
        fun testValidationErrors(): List<ValidationTestCase> {
            return listOf(
                ValidationTestCase(config = ScheduleConfiguration(classes = 2)),
                ValidationTestCase(
                    config = ScheduleConfiguration(classes = -1),
                    errors = listOf("Must have >0 classes."),
                ),
                ValidationTestCase(
                    config = ScheduleConfiguration(
                        daysPerWeek = 4,
                        periodsPerDay = 3,
                        classes = 2,
                        teacherAssignments = mapOf(
                            Teacher("Teacher", "1") to setOf(Subject.CHEMISTRY, Subject.PHYSICS),
                        ),
                        subjectFrequency = List(2) {
                            mapOf(
                                Subject.CHEMISTRY to 1,
                                Subject.EMPTY to 2,
                                Subject.PHYSICS to 1,
                            )
                        },
                    ),
                    errors = listOf(
                        "4 subjects assigned per week for class 1; must be 12.",
                        "4 subjects assigned per week for class 2; must be 12.",
                    )
                ),
                ValidationTestCase(
                    config = ScheduleConfiguration(
                        classes = 2,
                        teacherAssignments = mapOf(
                            Teacher("Teacher", "1") to setOf(Subject.CHEMISTRY),
                        ),
                        subjectFrequency = List(2) {
                            mapOf(
                                Subject.CHEMISTRY to 1,
                                Subject.EMPTY to 18,
                                Subject.PHYSICS to 1,
                            )
                        },
                    ),
                    errors = listOf("Physics is not taught by any teachers.")
                ),
                ValidationTestCase(
                    config = ScheduleConfiguration(
                        classes = 2,
                        teacherAssignments = mapOf(
                            Teacher("Teacher", "1") to setOf(Subject.PHILOSOPHY),
                        ),
                        subjectFrequency = List(2) {
                            mapOf(
                                Subject.PHILOSOPHY to 16,
                                Subject.EMPTY to 4,
                            )
                        },
                    ),
                    errors = listOf("Teacher 1 must teach at least 32 classes per week, which is impossible.")
                ),
                ValidationTestCase(
                    config = ScheduleConfiguration(
                        classes = 10,
                        teacherAssignments = mapOf(
                            Teacher("Teacher", "1") to setOf(Subject.CHEMISTRY, Subject.PHYSICS),
                            Teacher("Teacher", "2") to setOf(Subject.CHEMISTRY, Subject.PHYSICS),
                            Teacher("Teacher", "3") to setOf(Subject.CHEMISTRY, Subject.PHYSICS),
                        ),
                        subjectFrequency = List(10) {
                            mapOf(
                                Subject.CHEMISTRY to 2,
                                Subject.PHYSICS to 1,
                                Subject.EMPTY to 17,
                            )
                        },
                    ),
                    errors = listOf(
                        "Classroom Chemistry must be occupied at least 30 times per week (by Chemistry, Physics), " +
                            "which is impossible."
                    ),
                ),
            )
                .plus(ScheduleConfigurationFixtures.possibleConfigurations.map { ValidationTestCase(config = it) })
        }
    }
}
