package com.tiquionophist.io

import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import com.tiquionophist.scheduler.ExhaustiveScheduler
import com.tiquionophist.scheduler.RandomizedScheduler
import com.tiquionophist.ui.ComputedSchedule
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

internal class SaveFileIOTest {
    @Test
    fun testRead() {
        val gameVariables = SaveFileIO.read(testSaveFile)

        val scheduleConfiguration = gameVariables.toScheduleConfiguration()
        assertEquals(saveFileConfiguration, scheduleConfiguration)
        assertTrue(scheduleConfiguration.validationErrors().isEmpty())
    }

    @Test
    fun testWrite() {
        val scheduler = ExhaustiveScheduler(fillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS)
        val schedule = runBlocking { scheduler.schedule(saveFileConfiguration)!! }

        val outFile = resourcesDir.resolve("test-save-file-modified.sav")
        outFile.deleteOnExit()

        SaveFileIO.write(
            schedule = ComputedSchedule(
                configuration = saveFileConfiguration,
                schedule = schedule,
            ),
            sourceFile = testSaveFile,
            destinationFile = outFile,
        )

        assertEquals(saveFileConfiguration, SaveFileIO.read(outFile).toScheduleConfiguration())
    }

    companion object {
        private val resourcesDir = File("src/test/resources/")
        private val testSaveFile = resourcesDir.resolve("test-save-file.sav")

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
