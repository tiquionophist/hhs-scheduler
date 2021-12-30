package com.tiquionophist.core

import com.tiquionophist.util.prettyName
import java.util.EnumMap
import java.util.EnumSet

/**
 * A complete school schedule, represented as a two-dimensional list of [Lesson]s.
 *
 * The list is formatted as [classIndex -> [periodIndex -> lesson]], i.e. the class being taught for class index 1 and
 * period index 2 is classes[1][2].
 */
data class Schedule(val lessons: List<List<Lesson>>) {
    /**
     * Verifies that this schedule is valid, throwing an [IllegalArgumentException] if it is not.
     */
    fun verify(config: ScheduleConfiguration) {
        require(lessons.size == config.classes) { "wrong number of classes" }
        require(config.classes == config.subjectFrequency.size) { "wrong subject frequency size" }

        lessons.forEach { classSchedule ->
            require(classSchedule.size == config.periodsPerWeek) { "wrong number of periods" }

            classSchedule.forEach { lesson ->
                lesson.verify(config)
            }
        }

        for (periodIndex in 0 until config.periodsPerWeek) {
            val teachers = mutableSetOf<Teacher>()
            val classrooms = EnumSet.noneOf(Classroom::class.java)

            for (classIndex in 0 until config.classes) {
                val lesson = lessons[classIndex][periodIndex]

                lesson.teacher?.let { teacher ->
                    require(teacher !in teachers) {
                        "${teacher.fullName} is overbooked for $classIndex | $periodIndex"
                    }
                    teachers.add(teacher)
                }

                lesson.classroom?.let { classroom ->
                    require(classroom !in classrooms) {
                        "${classroom.canonicalName} is overbooked for $classIndex | $periodIndex"
                    }
                    classrooms.add(classroom)
                }
            }
        }

        for ((classSchedule, classFrequency) in lessons.zip(config.subjectFrequency)) {
            val remainingSubjects = EnumMap(classFrequency)
            for (cls in classSchedule) {
                remainingSubjects.compute(cls.subject) { _, count -> (count ?: 0) - 1 }
            }

            val unfulfilled = remainingSubjects.filterValues { it > 0 }
            require(unfulfilled.isEmpty()) { "missing subjects: $unfulfilled" }
        }

        if (!config.allowSubsequentSubjectsRepeats) {
            for (classLessons in lessons) {
                for (day in classLessons.chunked(size = config.periodsPerDay)) {
                    var prevSubject: Subject? = null
                    for (lesson in day) {
                        require(lesson.subject != prevSubject) { "${lesson.subject.prettyName} repeated subsequently" }

                        prevSubject = lesson.subject.takeIf { it != Subject.EMPTY }
                    }
                }
            }
        }

        if (!config.allowSameDaySubjectRepeats) {
            for (classLessons in lessons) {
                for (day in classLessons.chunked(size = config.periodsPerDay)) {
                    val subjects = day.map { it.subject }.filter { it != Subject.EMPTY }

                    val duplicates = subjects.minus(subjects.toSet())
                    require(duplicates.isEmpty()) {
                        "${duplicates.joinToString { it.prettyName }} repeated in the same day"
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Returns a [Schedule] from the given nullable list, throwing a [NullPointerException] if any of the values are
         * actually null.
         */
        fun fromNullable(lessons: List<List<Lesson?>>): Schedule {
            return Schedule(lessons.map { classSchedule -> classSchedule.map { it!! } })
        }
    }
}
