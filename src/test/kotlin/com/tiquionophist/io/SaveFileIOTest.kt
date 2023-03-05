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
                    Subject.PRACTICAL_SEX_EDUCATION to 2.9278853f,
                    Subject.SCHOOL_SPORT to 3.4683082f,
                    Subject.SWIMMING to 1.6509249f,
                    Subject.THEORETICAL_SEX_EDUCATION to 0.08692217f,
                ),
                Teacher.BETH_MANILI to mapOf(
                    Subject.MATH to 53.688625f,
                    Subject.ENGLISH to 3.7224066f,
                    Subject.GEOGRAPHY to 2.5592937f,
                    Subject.HISTORY to 1.3841027f,
                    Subject.PHILOSOPHY to 4.2076006f,
                    Subject.RELIGION to 1.8579054f,
                    Subject.COMPUTER_SCIENCE to 32.96095f,
                    Subject.PHYSICS to 24.353767f,
                    Subject.ANATOMY to 1.9031856f,
                    Subject.ART to 0.8315216f,
                    Subject.BIOLOGY to 2.5022929f,
                    Subject.BONDAGE to 2.4093869f,
                    Subject.CHEMISTRY to 3.98793f,
                    Subject.ECONOMICS to 0.86499137f,
                    Subject.EMPTY to 0.2670303f,
                    Subject.MUSIC to 4.9302945f,
                    Subject.PRACTICAL_SEX_EDUCATION to 2.4588776f,
                    Subject.SCHOOL_SPORT to 1.9717485f,
                    Subject.SWIMMING to 2.6453626f,
                    Subject.THEORETICAL_SEX_EDUCATION to 4.9956913f,
                ),
                Teacher.CARL_WALKER to mapOf(
                    Subject.MATH to 1.3501142f,
                    Subject.ENGLISH to 4.1952333f,
                    Subject.GEOGRAPHY to 4.285211f,
                    Subject.HISTORY to 14.520526f,
                    Subject.PHILOSOPHY to 4.7550473f,
                    Subject.RELIGION to 1.3372501f,
                    Subject.COMPUTER_SCIENCE to 0.36121923f,
                    Subject.PHYSICS to 4.654996f,
                    Subject.MUSIC to 51.281f,
                    Subject.SCHOOL_SPORT to 30.201674f,
                    Subject.SWIMMING to 23.24078f,
                    Subject.ANATOMY to 4.7899666f,
                    Subject.ART to 2.237848f,
                    Subject.BIOLOGY to 4.234518f,
                    Subject.BONDAGE to 2.5528135f,
                    Subject.CHEMISTRY to 4.150535f,
                    Subject.ECONOMICS to 4.359125f,
                    Subject.EMPTY to 0.2115368f,
                    Subject.PRACTICAL_SEX_EDUCATION to 4.693095f,
                    Subject.THEORETICAL_SEX_EDUCATION to 2.548563f,
                ),
                Teacher.CARMEN_SMITH to mapOf(
                    Subject.MATH to 3.002735f,
                    Subject.ENGLISH to 10.577222f,
                    Subject.GEOGRAPHY to 7.5449142f,
                    Subject.HISTORY to 1.0699799f,
                    Subject.PHILOSOPHY to 1.6552335f,
                    Subject.RELIGION to 1.5970033f,
                    Subject.COMPUTER_SCIENCE to 3.3092694f,
                    Subject.PHYSICS to 0.29695556f,
                    Subject.CHEMISTRY to 16.844318f,
                    Subject.BIOLOGY to 54.388397f,
                    Subject.ANATOMY to 40.56473f,
                    Subject.MUSIC to 0.82294536f,
                    Subject.SCHOOL_SPORT to 4.9494796f,
                    Subject.SWIMMING to 3.2588518f,
                    Subject.ART to 0.22259085f,
                    Subject.BONDAGE to 0.8190777f,
                    Subject.ECONOMICS to 0.5552097f,
                    Subject.EMPTY to 0.6647299f,
                    Subject.PRACTICAL_SEX_EDUCATION to 4.6653376f,
                    Subject.THEORETICAL_SEX_EDUCATION to 3.6267107f,
                ),
                Teacher.CLAIRE_FUZUSHI to mapOf(
                    Subject.MATH to 6.6562314f,
                    Subject.ENGLISH to 24.552605f,
                    Subject.ECONOMICS to 17.407629f,
                    Subject.GEOGRAPHY to 4.660671f,
                    Subject.HISTORY to 1.1216276f,
                    Subject.PHILOSOPHY to 0.09679982f,
                    Subject.RELIGION to 2.84945f,
                    Subject.COMPUTER_SCIENCE to 0.033988427f,
                    Subject.PHYSICS to 4.4309607f,
                    Subject.BIOLOGY to 1.0717967f,
                    Subject.ANATOMY to 13.601823f,
                    Subject.ART to 50.653454f,
                    Subject.MUSIC to 23.730968f,
                    Subject.SCHOOL_SPORT to 3.4154406f,
                    Subject.SWIMMING to 0.7084953f,
                    Subject.PRACTICAL_SEX_EDUCATION to 14.567376f,
                    Subject.BONDAGE to 9.437196f,
                    Subject.CHEMISTRY to 3.0387678f,
                    Subject.EMPTY to 0.17524704f,
                    Subject.THEORETICAL_SEX_EDUCATION to 0.84126574f,
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
                    Subject.THEORETICAL_SEX_EDUCATION to 38.119778f,
                    Subject.PRACTICAL_SEX_EDUCATION to 54.729317f,
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
                    Subject.THEORETICAL_SEX_EDUCATION to 7.1107483f,
                    Subject.BONDAGE to 0.34281868f,
                    Subject.CHEMISTRY to 2.4779608f,
                    Subject.EMPTY to 2.301376f,
                    Subject.PRACTICAL_SEX_EDUCATION to 1.6500497f,
                ),
                Teacher.NINA_PARKER to mapOf(
                    Subject.MATH to 0.7831031f,
                    Subject.ENGLISH to 53.573128f,
                    Subject.ECONOMICS to 4.4808583f,
                    Subject.GEOGRAPHY to 2.1330452f,
                    Subject.HISTORY to 14.281952f,
                    Subject.PHILOSOPHY to 31.924816f,
                    Subject.RELIGION to 10.152393f,
                    Subject.COMPUTER_SCIENCE to 2.1837964f,
                    Subject.PHYSICS to 0.39659402f,
                    Subject.BIOLOGY to 2.9355838f,
                    Subject.ANATOMY to 4.631415f,
                    Subject.ART to 6.292065f,
                    Subject.MUSIC to 3.1408975f,
                    Subject.SCHOOL_SPORT to 2.8602533f,
                    Subject.SWIMMING to 2.3914175f,
                    Subject.THEORETICAL_SEX_EDUCATION to 8.996942f,
                    Subject.BONDAGE to 0.6568422f,
                    Subject.CHEMISTRY to 3.3193383f,
                    Subject.EMPTY to 0.6834687f,
                    Subject.PRACTICAL_SEX_EDUCATION to 4.0367885f,
                ),
                Teacher.RONDA_BELLS to mapOf(
                    Subject.MATH to 10.880086f,
                    Subject.ENGLISH to 5.2743635f,
                    Subject.ECONOMICS to 4.974505f,
                    Subject.GEOGRAPHY to 4.870836f,
                    Subject.HISTORY to 2.5908334f,
                    Subject.PHILOSOPHY to 2.9880402f,
                    Subject.RELIGION to 1.1159879f,
                    Subject.COMPUTER_SCIENCE to 0.15751734f,
                    Subject.PHYSICS to 2.8461332f,
                    Subject.CHEMISTRY to 21.751734f,
                    Subject.BIOLOGY to 5.0844884f,
                    Subject.ANATOMY to 4.7833486f,
                    Subject.ART to 2.625545f,
                    Subject.MUSIC to 2.0245097f,
                    Subject.SCHOOL_SPORT to 53.21771f,
                    Subject.SWIMMING to 30.353296f,
                    Subject.THEORETICAL_SEX_EDUCATION to 0.69085795f,
                    Subject.BONDAGE to 2.7272987f,
                    Subject.EMPTY to 0.38161215f,
                    Subject.PRACTICAL_SEX_EDUCATION to 3.2439005f,
                ),
                Teacher.SAMANTHA_KELLER to mapOf(
                    Subject.MATH to 12.876606f,
                    Subject.ENGLISH to 2.383463f,
                    Subject.ECONOMICS to 9.224959f,
                    Subject.GEOGRAPHY to 3.6564312f,
                    Subject.HISTORY to 4.2590566f,
                    Subject.PHILOSOPHY to 3.9923308f,
                    Subject.RELIGION to 3.3354812f,
                    Subject.COMPUTER_SCIENCE to 29.811497f,
                    Subject.PHYSICS to 4.3176866f,
                    Subject.CHEMISTRY to 3.3045826f,
                    Subject.BIOLOGY to 2.6191232f,
                    Subject.ANATOMY to 9.579614f,
                    Subject.ART to 4.3164406f,
                    Subject.MUSIC to 6.395598f,
                    Subject.SCHOOL_SPORT to 50.591957f,
                    Subject.SWIMMING to 34.619534f,
                    Subject.THEORETICAL_SEX_EDUCATION to 39.749004f,
                    Subject.PRACTICAL_SEX_EDUCATION to 50.401413f,
                    Subject.BONDAGE to 43.418247f,
                    Subject.EMPTY to 0.0031140982f,
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
    }
}
