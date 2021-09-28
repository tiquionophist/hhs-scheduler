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
     * A map from each [Teacher] in the schedule to the [StatSet] of stats gained by the teacher each week as a result
     * of the lessons they are teaching.
     */
    val teacherStats: Map<Teacher, StatSet> by lazy {
        lessons
            .flatten()
            .fold(mutableMapOf()) { acc, lesson ->
                lesson.teacher?.let { teacher ->
                    acc.compute(teacher) { _, stats ->
                        stats?.plus(lesson.subject.stats) ?: lesson.subject.stats
                    }
                }

                acc
            }
    }

    /**
     * Verifies that this schedule is valid, throwing an [IllegalArgumentException] if it is not.
     */
    fun verify(config: ScheduleConfiguration) {
        require(lessons.size == config.classes) { "wrong number of classes" }
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
                        "${classroom.prettyName} is overbooked for $classIndex | $periodIndex"
                    }
                    classrooms.add(classroom)
                }
            }
        }

        for (classSchedule in lessons) {
            val remainingSubjects = EnumMap(config.subjectFrequency)
            for (cls in classSchedule) {
                remainingSubjects.compute(cls.subject) { _, count -> (count ?: 0) - 1 }
            }

            val unfulfilled = remainingSubjects.filterValues { it > 0 }
            require(unfulfilled.isEmpty()) { "missing subjects: $unfulfilled" }
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
