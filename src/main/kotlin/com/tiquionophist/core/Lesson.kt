package com.tiquionophist.core

import com.tiquionophist.util.prettyName

/**
 * Represents a single lesson on the schedule, including
 * - the [subject] being taught
 * - the [teacher] teaching the class (or null for free periods)
 * - the [assignedClassroom] the class is being taught in (or null for free periods)
 */
data class Lesson(val subject: Subject, val teacher: Teacher?, val assignedClassroom: AssignedClassroom?) {
    /**
     * Verifies that this lesson is valid, throwing an [IllegalArgumentException] if it is not.
     */
    fun verify(config: ScheduleConfiguration) {
        if (subject == Subject.EMPTY) {
            require(teacher == null) { "empty subject must not have a teacher" }
            require(assignedClassroom == null) { "empty subject must not have a classroom" }
        } else {
            val allowedTeachers: Set<Teacher>? = config.subjectAssignments[subject]
            requireNotNull(allowedTeachers) { "no teachers assigned for ${subject.prettyName}" }
            requireNotNull(teacher) { "no teacher provided for ${subject.prettyName}" }
            require(allowedTeachers.contains(teacher)) {
                "${teacher.fullName} not allowed to teach ${subject.prettyName}"
            }
            requireNotNull(assignedClassroom) { "${subject.prettyName} was not assigned a classroom" }

            val classrooms = subject.classrooms
            if (classrooms == null) {
                require(assignedClassroom is AssignedClassroom.NumberedClassroom) {
                    "${subject.prettyName} has no special classroom, but $assignedClassroom was provided"
                }
            } else {
                require(assignedClassroom is AssignedClassroom.NamedClassroom) {
                    "${subject.prettyName} must be scheduled in $classrooms but was not scheduled in a named classroom"
                }
                require(classrooms.contains(assignedClassroom.classroom)) {
                    "${subject.prettyName} cannot be scheduled in ${assignedClassroom.classroom.canonicalName}"
                }
            }
        }
    }
}
