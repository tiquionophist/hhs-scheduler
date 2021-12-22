package com.tiquionophist.scheduler

import com.tiquionophist.core.Classroom
import com.tiquionophist.core.Lesson
import com.tiquionophist.core.Schedule
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Scheduler
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import org.optaplanner.core.api.domain.entity.PlanningEntity
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty
import org.optaplanner.core.api.domain.solution.PlanningScore
import org.optaplanner.core.api.domain.solution.PlanningSolution
import org.optaplanner.core.api.domain.solution.ProblemFactProperty
import org.optaplanner.core.api.domain.valuerange.CountableValueRange
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider
import org.optaplanner.core.api.domain.variable.PlanningVariable
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore
import org.optaplanner.core.api.score.stream.Constraint
import org.optaplanner.core.api.score.stream.ConstraintFactory
import org.optaplanner.core.api.score.stream.ConstraintProvider
import org.optaplanner.core.api.score.stream.Joiners
import org.optaplanner.core.api.solver.SolverFactory
import org.optaplanner.core.config.solver.SolverConfig
import org.optaplanner.core.config.solver.termination.TerminationConfig
import java.util.EnumMap

/**
 * Uses OptaPlanner (https://www.optaplanner.org/) to attempt to solve the scheduling problem.
 *
 * [timeoutSeconds] sets the maximum number of seconds that the scheduler will run for before aborting if no valid
 * schedule is found.
 *
 * TODO OptaScheduler does not allow cooperative cancelling of its schedule() function
 */
class OptaScheduler(private val timeoutSeconds: Int? = null) : Scheduler {
    override suspend fun schedule(configuration: ScheduleConfiguration): Schedule? {
        val subjectToTeacher: EnumMap<Subject, List<WrappedTeacher>> = Subject.values()
            .associateWith { subject ->
                configuration.teacherAssignments.filterValues { it.contains(subject) }.keys.map { WrappedTeacher(it) }
            }
            .plus(Subject.EMPTY to listOf(WrappedTeacher(null)))
            .let { EnumMap(it) }

        val initial = OptaSolution(
            configuration = configuration,
            lessons = (0 until configuration.classes).flatMap { classIndex ->
                var counter = 0
                configuration.subjectFrequency.flatMap { (subject, freq) ->
                    List(freq) {
                        OptaLesson(
                            teacherRange = subjectToTeacher[subject]!!,
                            subject = subject,
                            classIndex = classIndex,
                            id = counter++
                        )
                    }
                }
            }
        )

        val solution = SolverFactory.create<OptaSolution>(
            SolverConfig()
                .withSolutionClass(OptaSolution::class.java)
                .withEntityClasses(OptaLesson::class.java)
                .withConstraintProviderClass(ScheduleConstraintProvider::class.java)
                .withTerminationConfig(
                    TerminationConfig()
                        .apply {
                            if (timeoutSeconds != null) {
                                withSecondsSpentLimit(timeoutSeconds.toLong())
                            }
                        }
                        .withBestScoreLimit("0hard/0soft")
                )
        )
            .buildSolver()
            .solve(initial)

        return if (solution.score?.hardScore == 0) solution.schedule else null
    }

    /**
     * A single lesson in the Opta problem, with a fixed [subject] and [classIndex], whose [periodIndex], [teacher], and
     * [classroom] may be adjusted.
     *
     * This problem formulation always guarantees that the correct number of subjects are scheduled for each class, and
     * allows Opta to move them around between periods/teachers.
     */
    @PlanningEntity
    class OptaLesson(
        @ValueRangeProvider(id = "teachers")
        val teacherRange: List<WrappedTeacher> = listOf(),
        val subject: Subject = Subject.EMPTY,
        val classIndex: Int = 0,
        val id: Int = 0
    ) {
        @PlanningVariable(valueRangeProviderRefs = ["periods"])
        val periodIndex: Int? = null

        @PlanningVariable(valueRangeProviderRefs = ["teachers"])
        lateinit var teacher: WrappedTeacher

        @PlanningVariable(valueRangeProviderRefs = ["classrooms"])
        lateinit var classroom: WrappedClassroom

        @ValueRangeProvider(id = "classrooms")
        val classroomRange = subjectToClassrooms[subject]
    }

    class ScheduleConstraintProvider : ConstraintProvider {
        override fun defineConstraints(constraintFactory: ConstraintFactory): Array<Constraint> {
            return arrayOf(
                periodConflict(constraintFactory),
                teacherConflict(constraintFactory),
                classroomConflict(constraintFactory),
            )
        }

        private fun periodConflict(constraintFactory: ConstraintFactory): Constraint {
            return constraintFactory.from(OptaLesson::class.java)
                .join(
                    OptaLesson::class.java,
                    Joiners.equal(OptaLesson::periodIndex),
                    Joiners.equal(OptaLesson::classIndex),
                    Joiners.lessThan(OptaLesson::id),
                )
                .penalize("Period conflict", HardSoftScore.ofHard(1))
        }

        private fun teacherConflict(constraintFactory: ConstraintFactory): Constraint {
            return constraintFactory.from(OptaLesson::class.java)
                .join(
                    OptaLesson::class.java,
                    Joiners.equal(OptaLesson::periodIndex),
                    Joiners.lessThan(OptaLesson::classIndex),
                    Joiners.filtering { t1, t2 ->
                        t1.teacher.teacher != null && t1.teacher.teacher == t2.teacher.teacher
                    },
                )
                .penalize("Teacher conflict", HardSoftScore.ofHard(1))
        }

        private fun classroomConflict(constraintFactory: ConstraintFactory): Constraint {
            return constraintFactory.from(OptaLesson::class.java)
                .join(
                    OptaLesson::class.java,
                    Joiners.equal(OptaLesson::periodIndex),
                    Joiners.lessThan(OptaLesson::classIndex),
                    Joiners.filtering { t1, t2 ->
                        t1.classroom.classroom != null && t1.classroom.classroom == t2.classroom.classroom
                    },
                )
                .penalize("Classroom conflict", HardSoftScore.ofHard(1))
        }
    }

    /**
     * The top-level solution in the Opta problem, with a set of [lessons] to be configured.
     */
    @PlanningSolution
    class OptaSolution(
        private val configuration: ScheduleConfiguration = ScheduleConfiguration(classes = 2),

        @PlanningEntityCollectionProperty
        val lessons: List<OptaLesson> = listOf()
    ) {
        @PlanningScore
        val score: HardSoftScore? = null

        @ValueRangeProvider(id = "periods")
        @ProblemFactProperty
        val periodRange: CountableValueRange<Int> =
            ValueRangeFactory.createIntValueRange(0, configuration.periodsPerWeek)

        // [classIndex -> [periodIndex -> class]]
        val schedule: Schedule
            get() {
                val lessons = List(configuration.classes) {
                    MutableList<Lesson?>(configuration.periodsPerWeek) { null }
                }

                for (cls in this.lessons) {
                    lessons[cls.classIndex][cls.periodIndex!!] = Lesson(
                        subject = cls.subject,
                        teacher = cls.teacher.teacher,
                        classroom = cls.classroom.classroom,
                    )
                }

                return Schedule.fromNullable(lessons)
            }
    }

    /**
     * A simple wrapper around [teacher] so that it may sometimes be null, to avoid Opta planning variable nullability.
     */
    data class WrappedTeacher(val teacher: Teacher?)

    /**
     * A simple wrapper around [classroom] so that it may sometimes be null, to avoid Opta planning variable
     * nullability.
     */
    data class WrappedClassroom(val classroom: Classroom?)

    companion object {
        private val subjectToClassrooms: EnumMap<Subject, List<WrappedClassroom>> = Subject.values()
            .associateWith { subject ->
                subject.classrooms?.map { WrappedClassroom(it) } ?: listOf(WrappedClassroom(null))
            }
            .let { EnumMap(it) }
    }
}
