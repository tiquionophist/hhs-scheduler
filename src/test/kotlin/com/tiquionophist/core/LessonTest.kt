package com.tiquionophist.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class LessonTest {
    data class VerifyTestCase(val lesson: Lesson, val config: ScheduleConfiguration, val message: String?)

    @ParameterizedTest
    @MethodSource
    fun testVerify(testCase: VerifyTestCase) {
        try {
            testCase.lesson.verify(testCase.config)
            assertNull(testCase.message)
        } catch (ex: IllegalArgumentException) {
            assertEquals(testCase.message, ex.message)
        }
    }

    companion object {
        @JvmStatic
        fun testVerify(): List<VerifyTestCase> {
            val emptyConfig = ScheduleConfiguration(
                classes = 1,
                teacherAssignments = mapOf(),
                subjectFrequency = listOf(mapOf()),
            )

            val nonEmptyConfig = ScheduleConfiguration(
                classes = 1,
                teacherAssignments = mapOf(
                    Teacher.CARMEN_SMITH to setOf(Subject.ENGLISH),
                    Teacher.SAMANTHA_KELLER to setOf(Subject.COMPUTER_SCIENCE),
                ),
                subjectFrequency = listOf(mapOf()),
            )

            return listOf(
                VerifyTestCase(
                    lesson = Lesson(subject = Subject.EMPTY, teacher = null, assignedClassroom = null),
                    config = emptyConfig,
                    message = null
                ),
                VerifyTestCase(
                    lesson = Lesson(subject = Subject.EMPTY, teacher = Teacher.BETH_MANILI, assignedClassroom = null),
                    config = emptyConfig,
                    message = "empty subject must not have a teacher"
                ),
                VerifyTestCase(
                    lesson = Lesson(
                        subject = Subject.EMPTY,
                        teacher = null,
                        assignedClassroom = AssignedClassroom.NamedClassroom(Classroom.ART)
                    ),
                    config = emptyConfig,
                    message = "empty subject must not have a classroom"
                ),
                VerifyTestCase(
                    lesson = Lesson(
                        subject = Subject.ENGLISH,
                        teacher = null,
                        assignedClassroom = AssignedClassroom.NumberedClassroom(1)
                    ),
                    config = emptyConfig,
                    message = "no teachers assigned for English"
                ),
                VerifyTestCase(
                    lesson = Lesson(
                        subject = Subject.ENGLISH,
                        teacher = Teacher.CARMEN_SMITH,
                        assignedClassroom = AssignedClassroom.NumberedClassroom(1)
                    ),
                    config = nonEmptyConfig,
                    message = null
                ),
                VerifyTestCase(
                    lesson = Lesson(
                        subject = Subject.ENGLISH,
                        teacher = null,
                        assignedClassroom = AssignedClassroom.NumberedClassroom(1)
                    ),
                    config = nonEmptyConfig,
                    message = "no teacher provided for English"
                ),
                VerifyTestCase(
                    lesson = Lesson(
                        subject = Subject.ENGLISH,
                        teacher = Teacher.SAMANTHA_KELLER,
                        assignedClassroom = AssignedClassroom.NumberedClassroom(1)
                    ),
                    config = nonEmptyConfig,
                    message = "Samantha Keller not allowed to teach English"
                ),
                VerifyTestCase(
                    lesson = Lesson(
                        subject = Subject.ENGLISH,
                        teacher = Teacher.CARMEN_SMITH,
                        assignedClassroom = AssignedClassroom.NamedClassroom(Classroom.COMPUTER)
                    ),
                    config = nonEmptyConfig,
                    message = "English has no special classroom, but Computer Room was provided"
                ),
                VerifyTestCase(
                    lesson = Lesson(
                        subject = Subject.COMPUTER_SCIENCE,
                        teacher = Teacher.SAMANTHA_KELLER,
                        assignedClassroom = AssignedClassroom.NamedClassroom(Classroom.COMPUTER)
                    ),
                    config = nonEmptyConfig,
                    message = null
                ),
                VerifyTestCase(
                    lesson = Lesson(
                        subject = Subject.COMPUTER_SCIENCE,
                        teacher = Teacher.SAMANTHA_KELLER,
                        assignedClassroom = AssignedClassroom.NamedClassroom(Classroom.ART)
                    ),
                    config = nonEmptyConfig,
                    message = "Computer Science cannot be scheduled in Classroom Art"
                ),
            )
        }
    }
}
