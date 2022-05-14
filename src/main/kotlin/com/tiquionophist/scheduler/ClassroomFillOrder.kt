package com.tiquionophist.scheduler

import com.tiquionophist.core.AssignedClassroom
import com.tiquionophist.core.Schedule
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher

/**
 * Specifies the strategy by which classrooms are assigned to each lesson.
 *
 * This is just the marker enum for which strategy is used; it can then [make] a corresponding
 * [ClassroomFillOrderInstance] which is the stateful object that does actual classroom assignments.
 */
enum class ClassroomFillOrder(val description: String) {
    BY_TEACHER("Teachers are always assigned to the same classroom; each class comes to them."),

    BY_CLASS("Classes are always assigned to the same classroom; each teacher comes to them."),

    BY_SUBJECT("Subjects are always assigned to the same classroom; each teacher and class comes to them."),

    ARBITRARY("Classrooms are assigned arbitrarily.");

    /**
     * Returns a new [ClassroomFillOrderInstance] based on this strategy which can assign classrooms for lessons.
     */
    fun make(): ClassroomFillOrderInstance {
        return when (this) {
            BY_TEACHER -> ClassroomFillOrderInstance.ByTeacher()
            BY_CLASS -> ClassroomFillOrderInstance.ByClass
            BY_SUBJECT -> ClassroomFillOrderInstance.BySubject()
            ARBITRARY -> ClassroomFillOrderInstance.Arbitrary
        }
    }

    /**
     * Verifies that [schedule] has the correct classroom assignments according to this [ClassroomFillOrder].
     */
    fun verify(schedule: Schedule) {
        when (this) {
            BY_TEACHER -> {
                val teacherClassroomNumbers = mutableMapOf<Teacher, Int>()
                for (cls in schedule.lessons) {
                    for (lesson in cls) {
                        val teacher = lesson.teacher
                        val classroom = lesson.assignedClassroom
                        if (teacher != null && classroom is AssignedClassroom.NumberedClassroom) {
                            val expectedNumber = teacherClassroomNumbers[teacher]
                            if (expectedNumber == null) {
                                teacherClassroomNumbers[teacher] = classroom.number
                            } else {
                                require(expectedNumber == classroom.number) {
                                    "$teacher teaches in both $expectedNumber and ${classroom.number}"
                                }
                            }
                        }
                    }
                }
            }

            BY_CLASS -> {
                for ((classIndex, cls) in schedule.lessons.withIndex()) {
                    for (lesson in cls) {
                        val classroom = lesson.assignedClassroom
                        if (classroom is AssignedClassroom.NumberedClassroom) {
                            require(classroom.number == classIndex + 1) {
                                "class ${classIndex + 1} was taught in classroom ${classroom.number}"
                            }
                        }
                    }
                }
            }

            BY_SUBJECT -> {
                val subjectClassroomNumbers = mutableMapOf<Subject, Int>()
                for (cls in schedule.lessons) {
                    for (lesson in cls) {
                        val subject = lesson.subject
                        val classroom = lesson.assignedClassroom
                        if (classroom is AssignedClassroom.NumberedClassroom) {
                            val expectedNumber = subjectClassroomNumbers[subject]
                            if (expectedNumber == null) {
                                subjectClassroomNumbers[subject] = classroom.number
                            } else {
                                require(expectedNumber == classroom.number) {
                                    "$subject is taught in both $expectedNumber and ${classroom.number}"
                                }
                            }
                        }
                    }
                }
            }

            ARBITRARY -> Unit // no-op: arbitrary
        }
    }
}

/**
 * The possibly stateful implementation of a [ClassroomFillOrder], which can produce valid classrooms for a lesson via
 * [assignNext].
 */
sealed interface ClassroomFillOrderInstance {
    /**
     * Returns a set of [AssignedClassroom]s which can be used for the lesson with the given parameters. If the
     * [subject] must be taught in particular [AssignedClassroom.NamedClassroom]s, those will be returned. Otherwise, a
     * single [AssignedClassroom.NumberedClassroom] is returned in which the class should take place.
     */
    fun assignNext(
        classIndex: Int,
        subject: Subject,
        teacher: Teacher,
        occupiedClassrooms: Set<AssignedClassroom>,
    ): Set<AssignedClassroom>

    /**
     * Convenience function mapping [Subject.classrooms] to their [AssignedClassroom.NamedClassroom]s.
     */
    val Subject.assignedClassrooms: Set<AssignedClassroom.NamedClassroom>?
        get() {
            return classrooms?.let { subjectClassrooms ->
                subjectClassrooms.mapTo(mutableSetOf()) { AssignedClassroom.NamedClassroom(it) }
            }
        }

    class ByTeacher : ClassroomFillOrderInstance {
        private var nextClassroomNumber: Int = 1
        private val teacherClassrooms = mutableMapOf<Teacher, AssignedClassroom.NumberedClassroom>()

        override fun assignNext(
            classIndex: Int,
            subject: Subject,
            teacher: Teacher,
            occupiedClassrooms: Set<AssignedClassroom>,
        ): Set<AssignedClassroom> {
            subject.assignedClassrooms?.let { return it }

            return setOf(
                teacherClassrooms.computeIfAbsent(teacher) {
                    AssignedClassroom.NumberedClassroom(nextClassroomNumber++)
                }
            )
        }
    }

    object ByClass : ClassroomFillOrderInstance {
        override fun assignNext(
            classIndex: Int,
            subject: Subject,
            teacher: Teacher,
            occupiedClassrooms: Set<AssignedClassroom>,
        ): Set<AssignedClassroom> {
            subject.assignedClassrooms?.let { return it }

            return setOf(AssignedClassroom.NumberedClassroom(number = classIndex + 1))
        }
    }

    class BySubject : ClassroomFillOrderInstance {
        private var nextClassroomNumber: Int = 1
        private val subjectClassrooms = mutableMapOf<Subject, AssignedClassroom.NumberedClassroom>()

        override fun assignNext(
            classIndex: Int,
            subject: Subject,
            teacher: Teacher,
            occupiedClassrooms: Set<AssignedClassroom>,
        ): Set<AssignedClassroom> {
            subject.assignedClassrooms?.let { return it }

            return setOf(
                subjectClassrooms.computeIfAbsent(subject) {
                    AssignedClassroom.NumberedClassroom(nextClassroomNumber++)
                }
            )
        }
    }

    object Arbitrary : ClassroomFillOrderInstance {
        override fun assignNext(
            classIndex: Int,
            subject: Subject,
            teacher: Teacher,
            occupiedClassrooms: Set<AssignedClassroom>,
        ): Set<AssignedClassroom> {
            subject.assignedClassrooms?.let { return it }

            val maxOccupiedNumber = occupiedClassrooms
                .maxOfOrNull { (it as? AssignedClassroom.NumberedClassroom)?.number ?: 0 }
                ?: 0

            return setOf(AssignedClassroom.NumberedClassroom(number = maxOccupiedNumber + 1))
        }
    }
}
