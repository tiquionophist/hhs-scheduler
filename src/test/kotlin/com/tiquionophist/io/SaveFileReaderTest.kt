package com.tiquionophist.io

import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class SaveFileReaderTest {
    @Test
    fun test() {
        val file = File(this::class.java.classLoader.getResource("test-save-file.sav")!!.file)
        val gameVariables = SaveFileReader.read(file)

        val scheduleConfiguration = gameVariables.toScheduleConfiguration()
        assertEquals(saveFileConfiguration, scheduleConfiguration)
        assertTrue(scheduleConfiguration.validationErrors().isEmpty())
    }

    companion object {
        // expected ScheduleConfiguration in the test save file
        private val saveFileConfiguration = ScheduleConfiguration(
            classes = 2,
            teacherAssignments = mapOf(
                Teacher.APRIL_RAYMUND to setOf(Subject.PHILOSOPHY, Subject.RELIGION),
                Teacher.IRINA_JELABITCH to setOf(Subject.ENGLISH, Subject.GEOGRAPHY),
                Teacher.JESSICA_UNDERWOOD to setOf(Subject.HISTORY, Subject.SCHOOL_SPORT),
            ),
            subjectFrequency = mapOf(
                Subject.PHILOSOPHY to 3,
                Subject.RELIGION to 3,
                Subject.ENGLISH to 3,
                Subject.GEOGRAPHY to 3,
                Subject.HISTORY to 3,
                Subject.SCHOOL_SPORT to 3,
                Subject.EMPTY to 2,
            )
        )
    }
}
