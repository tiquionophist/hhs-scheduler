package com.tiquionophist.scheduler

import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher

object ScheduleConfigurationFixtures {
    private val trivialConfiguration = ScheduleConfiguration(
        classes = 2,
        teacherAssignments = mapOf(
            Teacher("English", "Teacher") to setOf(Subject.ENGLISH),
            Teacher("Math", "Teacher") to setOf(Subject.MATH),
        ),
        subjectFrequency = List(2) {
            mapOf(
                Subject.ENGLISH to 10,
                Subject.MATH to 10,
            )
        }
    )

    private val easyConfiguration = ScheduleConfiguration(
        classes = 3,
        teacherAssignments = mapOf(
            Teacher.APRIL_RAYMUND to setOf(Subject.PHILOSOPHY, Subject.RELIGION),
            Teacher.BETH_MANILI to setOf(Subject.MATH),
            Teacher.CARMEN_SMITH to setOf(Subject.ART),
        ),
        subjectFrequency = List(3) {
            mapOf(
                Subject.ART to 4,
                Subject.MATH to 4,
                Subject.PHILOSOPHY to 3,
                Subject.RELIGION to 3,
                Subject.EMPTY to 6,
            )
        }
    )

    private val easyConfigurationDifferentClassFrequencies = ScheduleConfiguration(
        classes = 3,
        teacherAssignments = mapOf(
            Teacher.APRIL_RAYMUND to setOf(Subject.PHILOSOPHY, Subject.RELIGION),
            Teacher.BETH_MANILI to setOf(Subject.MATH),
            Teacher.CARMEN_SMITH to setOf(Subject.ART),
        ),
        subjectFrequency = listOf(
            mapOf(
                Subject.ART to 4,
                Subject.MATH to 4,
                Subject.PHILOSOPHY to 3,
                Subject.RELIGION to 3,
                Subject.EMPTY to 6,
            ),
            mapOf(
                Subject.ART to 4,
                Subject.MATH to 3,
                Subject.PHILOSOPHY to 4,
                Subject.RELIGION to 3,
                Subject.EMPTY to 6,
            ),
            mapOf(
                Subject.ART to 4,
                Subject.MATH to 4,
                Subject.PHILOSOPHY to 3,
                Subject.RELIGION to 4,
                Subject.EMPTY to 5,
            ),
        )
    )

    private val easyConfigurationNoSameDayRepeats = ScheduleConfiguration(
        classes = 2,
        teacherAssignments = mapOf(
            Teacher.APRIL_RAYMUND to setOf(Subject.ART, Subject.MATH, Subject.PHILOSOPHY, Subject.RELIGION),
            Teacher.BETH_MANILI to setOf(Subject.ART, Subject.MATH, Subject.PHILOSOPHY, Subject.RELIGION),
            Teacher.CARMEN_SMITH to setOf(Subject.ART, Subject.MATH, Subject.PHILOSOPHY, Subject.RELIGION),
        ),
        subjectFrequency = List(2) {
            mapOf(
                Subject.ART to 4,
                Subject.MATH to 4,
                Subject.PHILOSOPHY to 3,
                Subject.RELIGION to 3,
                Subject.EMPTY to 6,
            )
        },
        allowSameDaySubjectRepeats = false,
    )

    private val mediumConfiguration = ScheduleConfiguration(
        classes = 6,
        teacherAssignments = mapOf(
            Teacher.APRIL_RAYMUND to setOf(Subject.PHILOSOPHY, Subject.RELIGION),
            Teacher.BETH_MANILI to setOf(Subject.MATH),
            Teacher.CARL_WALKER to setOf(Subject.CHEMISTRY),
            Teacher.CARMEN_SMITH to setOf(Subject.ART),
            Teacher.CLAIRE_FUZUSHI to setOf(Subject.SCHOOL_SPORT, Subject.SWIMMING),
            Teacher.JESSICA_UNDERWOOD to setOf(Subject.ANATOMY, Subject.COMPUTER_SCIENCE),
            Teacher.NINA_PARKER to setOf(Subject.THEORETICAL_SEX_EDUCATION),
            Teacher.RONDA_BELLS to setOf(Subject.SCHOOL_SPORT, Subject.SWIMMING),
            Teacher.SAMANTHA_KELLER to setOf(
                Subject.BONDAGE,
                Subject.PRACTICAL_SEX_EDUCATION,
                Subject.THEORETICAL_SEX_EDUCATION,
            ),
        ),
        subjectFrequency = List(6) {
            mapOf(
                Subject.ANATOMY to 1,
                Subject.ART to 2,
                Subject.BONDAGE to 2,
                Subject.CHEMISTRY to 2,
                Subject.COMPUTER_SCIENCE to 2,
                Subject.MATH to 3,
                Subject.PHILOSOPHY to 2,
                Subject.PRACTICAL_SEX_EDUCATION to 1,
                Subject.RELIGION to 1,
                Subject.SCHOOL_SPORT to 1,
                Subject.SWIMMING to 2,
                Subject.THEORETICAL_SEX_EDUCATION to 1,
            )
        },
    )

    private val hardConfiguration = ScheduleConfiguration(
        classes = 8,
        teacherAssignments = mapOf(
            Teacher.ANNA_MILLER to setOf(Subject.ENGLISH, Subject.GEOGRAPHY, Subject.HISTORY),
            Teacher.APRIL_RAYMUND to setOf(Subject.PHILOSOPHY, Subject.RELIGION),
            Teacher.BETH_MANILI to setOf(Subject.MATH),
            Teacher.CARL_WALKER to setOf(Subject.CHEMISTRY),
            Teacher.CARMEN_SMITH to setOf(Subject.ART),
            Teacher.CLAIRE_FUZUSHI to setOf(Subject.SCHOOL_SPORT, Subject.SWIMMING),
            Teacher.JESSICA_UNDERWOOD to setOf(Subject.ANATOMY, Subject.COMPUTER_SCIENCE),
            Teacher.LARA_ELLIS to setOf(Subject.ENGLISH, Subject.GEOGRAPHY, Subject.HISTORY),
            Teacher.NINA_PARKER to setOf(
                Subject.BONDAGE,
                Subject.PRACTICAL_SEX_EDUCATION,
                Subject.THEORETICAL_SEX_EDUCATION,
            ),
            Teacher.RONDA_BELLS to setOf(Subject.SCHOOL_SPORT, Subject.SWIMMING),
            Teacher.SAMANTHA_KELLER to setOf(
                Subject.BONDAGE,
                Subject.PRACTICAL_SEX_EDUCATION,
                Subject.THEORETICAL_SEX_EDUCATION,
            ),
        ),
        subjectFrequency = List(8) {
            mapOf(
                Subject.ANATOMY to 1,
                Subject.ART to 2,
                Subject.BONDAGE to 1,
                Subject.CHEMISTRY to 2,
                Subject.COMPUTER_SCIENCE to 1,
                Subject.ENGLISH to 1,
                Subject.GEOGRAPHY to 1,
                Subject.HISTORY to 1,
                Subject.MATH to 2,
                Subject.PHILOSOPHY to 1,
                Subject.PRACTICAL_SEX_EDUCATION to 1,
                Subject.RELIGION to 1,
                Subject.SCHOOL_SPORT to 1,
                Subject.SWIMMING to 2,
                Subject.THEORETICAL_SEX_EDUCATION to 1,
                Subject.EMPTY to 1,
            )
        },
    )

    private val trivialImpossibleConfiguration = ScheduleConfiguration(
        classes = 3,
        teacherAssignments = mapOf(Teacher.ANNA_MILLER to setOf(Subject.ENGLISH)),
        subjectFrequency = List(3) { mapOf(Subject.ENGLISH to 20) }
    )

    private val impossibleConfigurationFromSameDayConstraint = ScheduleConfiguration(
        classes = 2,
        teacherAssignments = mapOf(Teacher.ANNA_MILLER to setOf(Subject.ENGLISH)),
        subjectFrequency = List(2) { mapOf(Subject.ENGLISH to 20) },
        allowSameDaySubjectRepeats = false,
    )

    private val impossibleConfigurationFromSubsequentConstraint = ScheduleConfiguration(
        classes = 2,
        teacherAssignments = mapOf(Teacher.ANNA_MILLER to setOf(Subject.ENGLISH)),
        subjectFrequency = List(2) { mapOf(Subject.ENGLISH to 20) },
        allowSubsequentSubjectsRepeats = false,
    )

    val trivialOrEasyConfigurations = listOf(
        trivialConfiguration,
        trivialConfiguration.copy(allowSubsequentSubjectsRepeats = false),
        easyConfiguration,
        easyConfigurationDifferentClassFrequencies,
    )

    val possibleConfigurations = listOf(
        trivialConfiguration,
        trivialConfiguration.copy(allowSubsequentSubjectsRepeats = false),
        easyConfiguration.copy(classroomFillOrder = ClassroomFillOrder.BY_TEACHER),
        easyConfiguration.copy(classroomFillOrder = ClassroomFillOrder.BY_SUBJECT),
        easyConfiguration.copy(classroomFillOrder = ClassroomFillOrder.BY_CLASS),
        easyConfiguration.copy(classroomFillOrder = ClassroomFillOrder.ARBITRARY),
        easyConfigurationNoSameDayRepeats,
        easyConfigurationDifferentClassFrequencies,
        mediumConfiguration,
        mediumConfiguration.copy(allowSubsequentSubjectsRepeats = false),
        mediumConfiguration.copy(allowSameDaySubjectRepeats = false),
        hardConfiguration,
    )

    val impossibleConfigurations = listOf(
        trivialImpossibleConfiguration,
        impossibleConfigurationFromSameDayConstraint,
        impossibleConfigurationFromSubsequentConstraint,
    )

    val allConfigurations = possibleConfigurations.plus(impossibleConfigurations)
}
