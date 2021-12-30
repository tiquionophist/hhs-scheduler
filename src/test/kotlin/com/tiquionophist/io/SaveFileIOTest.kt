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
import org.junit.jupiter.api.assertThrows
import java.io.File

internal class SaveFileIOTest {
    private val scheduler = ExhaustiveScheduler(fillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS)

    private val outFile: File by lazy {
        resourcesDir.resolve("test-save-file-modified.sav").also {
            it.deleteOnExit()
        }
    }

    @Test
    fun testRead() {
        val gameVariables = SaveFileIO.read(testSaveFile)

        val scheduleConfiguration = gameVariables.toScheduleConfiguration()
        assertEquals(saveFileConfiguration, scheduleConfiguration)
        assertTrue(scheduleConfiguration.validationErrors().isEmpty())
    }

    @Test
    fun testWrite() {
        val schedule = runBlocking { scheduler.schedule(saveFileConfiguration)!! }

        SaveFileIO.write(
            schedule = ComputedSchedule(configuration = saveFileConfiguration, schedule = schedule),
            sourceFile = testSaveFile,
            destinationFile = outFile,
        )

        assertEquals(saveFileConfiguration, SaveFileIO.read(outFile).toScheduleConfiguration())
    }

    @Test
    fun testWriteMoreClasses() {
        val configuration = saveFileConfiguration.copy(
            classes = saveFileConfiguration.classes + 1,
            // copy the first class subject frequency to the end so we have the right number
            subjectFrequency = saveFileConfiguration.subjectFrequency
                .plus(saveFileConfiguration.subjectFrequency.first().toMap()),
        )
        val schedule = runBlocking { scheduler.schedule(configuration)!! }

        SaveFileIO.write(
            schedule = ComputedSchedule(configuration = configuration, schedule = schedule),
            sourceFile = testSaveFile,
            destinationFile = outFile,
        )

        assertEquals(configuration, SaveFileIO.read(outFile).toScheduleConfiguration())
    }

    @Test
    fun testWriteFewerClasses() {
        val configuration = saveFileConfiguration.copy(classes = saveFileConfiguration.classes - 1)
        val schedule = runBlocking { scheduler.schedule(configuration)!! }

        val exception = assertThrows<IllegalStateException> {
            SaveFileIO.write(
                schedule = ComputedSchedule(configuration = configuration, schedule = schedule),
                sourceFile = testSaveFile,
                destinationFile = outFile,
            )
        }

        assertEquals("Save file has 2 classes; cannot write 1 since this would lose students.", exception.message)
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
            subjectFrequency = List(2) {
                mapOf(
                    Subject.PHILOSOPHY to 3,
                    Subject.RELIGION to 3,
                    Subject.ENGLISH to 3,
                    Subject.GEOGRAPHY to 3,
                    Subject.HISTORY to 3,
                    Subject.SCHOOL_SPORT to 3,
                    Subject.EMPTY to 2,
                )
            }
        )
    }
}
