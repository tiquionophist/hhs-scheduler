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
            teacherExperience = mapOf(
                Teacher.APRIL_RAYMUND to mapOf(
                    Subject.ECONOMICS to 5.489621f,
                    Subject.ENGLISH to 13.378371f,
                    Subject.GEOGRAPHY to 8.228421f,
                    Subject.HISTORY to 11.997896f,
                    Subject.PHILOSOPHY to 24.930988f,
                    Subject.RELIGION to 54.974483f,
                    Subject.ANATOMY to 0.524617f,
                    Subject.ART to 3.839543f,
                    Subject.BIOLOGY to 2.6110609f,
                    Subject.BONDAGE to 2.2287104f,
                    Subject.CHEMISTRY to 0.5095114f,
                    Subject.COMPUTER_SCIENCE to 1.4297221f,
                    Subject.EMPTY to 3.1108048f,
                    Subject.MATH to 0.48550412f,
                    Subject.MUSIC to 4.8723307f,
                    Subject.PHYSICS to 0.003791973f,
                    Subject.PRACTICAL_SEX_ED to 2.9278853f,
                    Subject.SCHOOL_SPORT to 3.4683082f,
                    Subject.SWIMMING to 1.6509249f,
                    Subject.THEORETICAL_SEX_ED to 0.08692217f,
                ),
                Teacher.IRINA_JELABITCH to mapOf(
                    Subject.MATH to 4.525236f,
                    Subject.ENGLISH to 48.17377f,
                    Subject.ECONOMICS to 2.4014919f,
                    Subject.GEOGRAPHY to 34.914528f,
                    Subject.HISTORY to 2.1467862f,
                    Subject.PHILOSOPHY to 4.039312f,
                    Subject.RELIGION to 3.729238f,
                    Subject.COMPUTER_SCIENCE to 4.8010235f,
                    Subject.PHYSICS to 3.892136f,
                    Subject.CHEMISTRY to 22.246025f,
                    Subject.BIOLOGY to 0.2270325f,
                    Subject.ANATOMY to 17.58227f,
                    Subject.ART to 3.717764f,
                    Subject.MUSIC to 0.79336834f,
                    Subject.SCHOOL_SPORT to 31.259333f,
                    Subject.SWIMMING to 2.3035102f,
                    Subject.THEORETICAL_SEX_ED to 38.119778f,
                    Subject.PRACTICAL_SEX_ED to 54.729317f,
                    Subject.BONDAGE to 33.801483f,
                    Subject.EMPTY to 3.728512f,
                ),
                Teacher.JESSICA_UNDERWOOD to mapOf(
                    Subject.MATH to 9.705796f,
                    Subject.ENGLISH to 4.194445f,
                    Subject.ECONOMICS to 17.325157f,
                    Subject.GEOGRAPHY to 22.420483f,
                    Subject.HISTORY to 24.274952f,
                    Subject.PHILOSOPHY to 1.9886432f,
                    Subject.RELIGION to 2.5870707f,
                    Subject.COMPUTER_SCIENCE to 14.394654f,
                    Subject.PHYSICS to 1.389176f,
                    Subject.BIOLOGY to 2.5464318f,
                    Subject.ANATOMY to 0.31299818f,
                    Subject.ART to 3.91764f,
                    Subject.MUSIC to 0.3803725f,
                    Subject.SCHOOL_SPORT to 10.188173f,
                    Subject.SWIMMING to 53.323532f,
                    Subject.THEORETICAL_SEX_ED to 7.1107483f,
                    Subject.BONDAGE to 0.34281868f,
                    Subject.CHEMISTRY to 2.4779608f,
                    Subject.EMPTY to 2.301376f,
                    Subject.PRACTICAL_SEX_ED to 1.6500497f,
                ),
            ),
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
                Subject.PRACTICAL_SEX_ED to false,
                Subject.RELIGION to true,
                Subject.SCHOOL_SPORT to true,
                Subject.SWIMMING to false,
                Subject.THEORETICAL_SEX_ED to false,
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
