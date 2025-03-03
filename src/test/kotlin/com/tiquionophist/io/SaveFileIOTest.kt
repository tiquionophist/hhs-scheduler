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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File

internal class SaveFileIOTest {
    data class TestSaveFile(val file: File, val type: SaveFileType)

    private val scheduler = ExhaustiveScheduler(fillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS)

    @ParameterizedTest
    @MethodSource("testSaveFiles")
    fun testRead(testSaveFile: TestSaveFile) {
        val saveData = SaveFileIO.read(testSaveFile.file)

        val scheduleConfiguration = saveData.toScheduleConfiguration()
        assertEquals(saveFileConfiguration.normalize(), scheduleConfiguration.normalize())
        assertTrue(scheduleConfiguration.validationErrors().isEmpty())
    }

    @ParameterizedTest
    @MethodSource("testSaveFiles")
    fun testWrite(testSaveFile: TestSaveFile) {
        val schedule = runBlocking { scheduler.schedule(saveFileConfiguration)!! }

        SaveFileIO.write(
            schedule = ComputedSchedule(configuration = saveFileConfiguration, schedule = schedule),
            sourceFile = testSaveFile.file,
            destinationFile = outFile,
        )

        assertEquals(saveFileConfiguration.normalize(), SaveFileIO.read(outFile).toScheduleConfiguration().normalize())
    }

    @ParameterizedTest
    @MethodSource("testSaveFiles")
    fun testWriteMoreClassesXml(testSaveFile: TestSaveFile) {
        val configuration = saveFileConfiguration.copy(
            classes = saveFileConfiguration.classes + 1,
            // copy the first class subject frequency to the end so we have the right number
            subjectFrequency = saveFileConfiguration.subjectFrequency
                .plus(saveFileConfiguration.subjectFrequency.first().toMap()),
        )
        val schedule = runBlocking { scheduler.schedule(configuration)!! }

        SaveFileIO.write(
            schedule = ComputedSchedule(configuration = configuration, schedule = schedule),
            sourceFile = testSaveFile.file,
            destinationFile = outFile,
        )

        assertEquals(configuration.normalize(), SaveFileIO.read(outFile).toScheduleConfiguration().normalize())
    }

    @ParameterizedTest
    @MethodSource("testSaveFiles")
    fun testWriteFewerClasses(testSaveFile: TestSaveFile) {
        val configuration = saveFileConfiguration.copy(classes = saveFileConfiguration.classes - 1)
        val schedule = runBlocking { scheduler.schedule(configuration)!! }

        val exception = assertThrows<IllegalStateException> {
            SaveFileIO.write(
                schedule = ComputedSchedule(configuration = configuration, schedule = schedule),
                sourceFile = testSaveFile.file,
                destinationFile = outFile,
            )
        }

        assertEquals("Save file has 2 classes; cannot write 1 since this would lose students.", exception.message)
    }

    companion object {
        private val resourcesDir = File("src/test/resources/")

        private val outFile: File by lazy {
            resourcesDir.resolve("test-save-file-modified.sav").also {
                it.deleteOnExit()
            }
        }

        // expected ScheduleConfiguration in the save files
        private val saveFileConfiguration = ScheduleConfiguration(
            classes = 2,
            teacherAssignments = mapOf(
                Teacher.APRIL_RAYMUND to setOf(Subject.PHILOSOPHY, Subject.RELIGION),
                Teacher.IRINA_JELABITCH to setOf(Subject.ENGLISH, Subject.GEOGRAPHY),
                Teacher.JESSICA_UNDERWOOD to setOf(Subject.HISTORY, Subject.SCHOOL_SPORT),
            ),
            teacherExperience = emptyMap(),
            allowedSubjects = mapOf(
                Subject.ANATOMY to false,
                Subject.ART to false,
                Subject.BIOLOGY to false,
                Subject.BONDAGE to false,
                Subject.CHEMISTRY to false,
                Subject.COMPUTER_SCIENCE to false,
                Subject.ECONOMICS to true,
                Subject.ENGLISH to true,
                Subject.GEOGRAPHY to true,
                Subject.HISTORY to true,
                Subject.MATH to true,
                Subject.MUSIC to false,
                Subject.PHILOSOPHY to true,
                Subject.PHYSICS to false,
                Subject.PRACTICAL_SEX_EDUCATION to false,
                Subject.RELIGION to true,
                Subject.SCHOOL_SPORT to true,
                Subject.SWIMMING to false,
                Subject.THEORETICAL_SEX_EDUCATION to false,
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

        /**
         * Normalizes this [ScheduleConfiguration], removing its [ScheduleConfiguration.teacherExperience] which is
         * arbitrary and different between the test files.
         */
        private fun ScheduleConfiguration.normalize() = copy(teacherExperience = emptyMap())

        @JvmStatic
        fun testSaveFiles(): List<TestSaveFile> {
            return listOf(
                TestSaveFile(resourcesDir.resolve("test-save-file-xml.sav"), SaveFileType.XML),
                TestSaveFile(resourcesDir.resolve("test-save-file-sql.sav"), SaveFileType.SQL),
                TestSaveFile(resourcesDir.resolve("test-save-file-sql-1.11.sav"), SaveFileType.SQL),
            )
        }
    }
}
