package com.tiquionophist.core

import com.tiquionophist.util.prettyName
import java.util.EnumSet

/**
 * Represents a single lesson on the schedule, including
 * - the [subject] being taught
 * - the [teacher] teaching the class (or null for free periods)
 * - the [classroom] the class is being taught in (or null for classes without a specific classroom requirement)
 */
data class Lesson(val subject: Subject, val teacher: Teacher?, val classroom: Classroom?) {
    /**
     * Verifies that this lesson is valid, throwing an [IllegalArgumentException] if it is not.
     */
    fun verify(config: ScheduleConfiguration) {
        if (subject == Subject.EMPTY) {
            require(teacher == null) { "empty subject must not have a teacher" }
            require(classroom == null) { "empty subject must not have a classroom" }
        } else {
            val allowedTeachers: EnumSet<Teacher>? = config.subjectAssignments[subject]
            requireNotNull(allowedTeachers) { "no teachers assigned for ${subject.prettyName}" }
            requireNotNull(teacher) { "no teacher provided for ${subject.prettyName}" }
            require(allowedTeachers.contains(teacher)) {
                "${teacher.prettyName} not allowed to teach ${subject.prettyName}"
            }

            val classrooms = subject.classrooms
            if (classrooms == null) {
                require(classroom == null) {
                    "${subject.classrooms} has no classroom, but ${classroom!!.prettyName} was provided"
                }
            } else {
                require(classrooms.contains(classroom)) {
                    "${subject.prettyName} cannot be scheduled in ${classroom?.prettyName}"
                }
            }
        }
    }
}
