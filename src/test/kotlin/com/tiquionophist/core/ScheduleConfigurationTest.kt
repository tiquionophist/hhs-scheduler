package com.tiquionophist.core

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
        fun configurations(): List<ScheduleConfiguration> {
            return listOf(
                ScheduleConfiguration(
                    classes = 0,
                    teacherAssignments = mapOf(),
                    subjectFrequency = mapOf()
                ),

                ScheduleConfiguration(
                    daysPerWeek = 4,
                    periodsPerDay = 3,
                    classes = 2,
                    teacherAssignments = mapOf(
                        Teacher.SAMANTHA_KELLER to setOf(Subject.COMPUTER_SCIENCE, Subject.PRACTICAL_SEX_ED),
                        Teacher.BETH_MANILI to setOf(Subject.MATH),
                        Teacher.RONDA_BELLS to setOf(Subject.SCHOOL_SPORT, Subject.SWIMMING),
                    ),
                    subjectFrequency = mapOf(
                        Subject.COMPUTER_SCIENCE to 3,
                        Subject.PRACTICAL_SEX_ED to 3,
                        Subject.MATH to 8,
                        Subject.SCHOOL_SPORT to 3,
                        Subject.SWIMMING to 3,
                    )
                )
            )
        }
    }
}
