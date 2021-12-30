package com.tiquionophist.scheduler

import com.tiquionophist.core.Classroom
import com.tiquionophist.core.Lesson
import com.tiquionophist.core.Schedule
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Scheduler
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import kotlinx.coroutines.yield
import java.util.EnumMap
import java.util.EnumSet
import kotlin.random.Random

/**
 * Randomly schedules each lesson according to [fillOrder], restarting the search from scratch [rounds] times, each
 * round reaching an end state (when there are no options to schedule the next period) up to [attemptsPerRound] times
 * before starting the next round. Randomizing this process tends to shuffle the schedule in a way that is more likely
 * to find a valid schedule quickly.
 *
 * If [attemptsPerRound] is null, runs each round exhaustively, in which case [rounds] must be exactly 1. In this case a
 * solution is guaranteed to be found is one exists; otherwise this cannot be guaranteed.
 */
class RandomizedScheduler(
    private val fillOrder: ScheduleFillOrder = ScheduleFillOrder.CLASS_BY_CLASS,
    private val attemptsPerRound: Int?,
    private val rounds: Int,

    /**
     * Optional configuration for how the random seed for each round is generated. The input is the round index
     * (starting from 0), returning the random seed to be used, or null if it should not be randomized (to allow
     * [ExhaustiveScheduler] to reuse this algorithm).
     */
    private val randomSeed: (Int) -> Long? = { it.toLong() }
) : Scheduler {
    override suspend fun schedule(configuration: ScheduleConfiguration): Schedule? {
        repeat(rounds) { round ->
            yield() // yield computation after each round to allow cancellation

            scheduleRecursive(
                current = PartialSchedule(
                    configuration = configuration,
                    lessons = List(configuration.classes) {
                        List(configuration.periodsPerWeek) { null }
                    },
                    remainingSubjects = List(configuration.classes) {
                        configuration.subjectFrequency.takeIf { it.isNotEmpty() }?.let { EnumMap(it) }
                            ?: EnumMap(Subject::class.java)
                    }
                ),
                random = randomSeed(round)?.let { Random(it) },
                attempts = attemptsPerRound?.let { AttemptWrapper(it) }
            )?.let {
                return it
            }
        }

        return null
    }

    /**
     * One recursive step in the scheduling algorithm, which attempts to schedule the next period in [current] by adding
     * each of [PartialSchedule.scheduleOptions], shuffled by [random]. Returns null when there are no remaining options
     * (or [attempts]) or a completed schedule if one has been found.
     */
    private fun scheduleRecursive(
        current: PartialSchedule,
        random: Random? = null,
        attempts: AttemptWrapper? = null
    ): Schedule? {
        if (attempts?.remaining == 0) {
            return null
        }

        if (current.complete) return current.toCompletedSchedule()

        val options = if (random == null) current.scheduleOptions else current.scheduleOptions.shuffled(random)
        if (attempts != null && options.isEmpty()) {
            attempts.remaining -= 1
            return null
        }

        for (lesson in options) {
            scheduleRecursive(current = current.scheduleNext(lesson), random = random, attempts = attempts)
                ?.let { return it }
        }

        return null
    }

    /**
     * A simple mutable wrapper to hold the number of remaining attempts in the current round.
     */
    private class AttemptWrapper(var remaining: Int)

    /**
     * A single, immutable partial state where some but not all the lessons in the schedule have been assigned.
     */
    private inner class PartialSchedule(
        private val configuration: ScheduleConfiguration,

        // [classIndex -> [periodIndex -> (subject, teacher, classroom)]]
        private val lessons: List<List<Lesson?>>,

        // [classIndex -> { subject -> number of classes in that subject still to be assigned }]
        private val remainingSubjects: List<EnumMap<Subject, Int>>,

        private val nextClassIndex: Int = 0,
        private val nextPeriodIndex: Int = 0,
    ) {
        // whether all the classes and periods are assigned
        val complete: Boolean
            get() = nextClassIndex == -1 || nextPeriodIndex == -1

        // subjects that can be scheduled next: still have >0 times to be assigned, has a free teacher, and has a free
        // classroom
        val scheduleOptions: List<Lesson>
            get() = scheduleOptions(classIndex = nextClassIndex, periodIndex = nextPeriodIndex)

        /**
         * Generates the period indexes on the same day as [periodIndex].
         *
         * For example, if we have 4 periods per day, then [0, 1, 2, 3] is the result for input indexes 0, 1, 2, or 3;
         * [4, 5, 6, 7] is the result for input indexes 4, 5, 6, 7; etc.
         */
        private fun periodIndexesForDay(periodIndex: Int): IntRange {
            val dayIndex = periodIndex / configuration.periodsPerDay
            return IntRange(start = dayIndex, endInclusive = dayIndex + configuration.periodsPerDay)
        }

        /**
         * Generates the neighboring period indexes for [periodIndex], excluding periods on different days.
         *
         * For example, if we have 4 periods per day, then:
         * - 0 -> [1] since [-1] is not a valid period
         * - 1 -> [0, 2]
         * - 2 -> [1, 3]
         * - 3 -> [2] since 4 is on the next day
         * - 4 -> [5] since 3 is on the previous day
         * - 5 -> [4, 6]
         * etc
         */
        private fun subsequentPeriodIndexes(periodIndex: Int): List<Int> {
            val dayIndexes = periodIndexesForDay(periodIndex)
            val previous = periodIndex - 1
            val next = periodIndex + 1
            val list = mutableListOf<Int>()
            if (previous in dayIndexes) {
                list.add(previous)
            }
            if (next in dayIndexes) {
                list.add(next)
            }
            return list
        }

        // schedule options for a particular period and class
        // note: doesn't check that the class is currently unscheduled, wouldn't work if it wasn't
        private fun scheduleOptions(classIndex: Int, periodIndex: Int): List<Lesson> {
            val classOptions = mutableListOf<Lesson>()

            val busyTeachers = mutableSetOf<Teacher>()
            val busyClassrooms = EnumSet.noneOf(Classroom::class.java)
            val blockedSubjects = EnumSet.noneOf(Subject::class.java)

            for (classSchedule in lessons) {
                val lesson = classSchedule[periodIndex]
                lesson?.teacher?.let { busyTeachers.add(it) }
                lesson?.classroom?.let { busyClassrooms?.add(it) }
            }

            if (!configuration.allowSameDaySubjectRepeats) {
                for (periodIndexInDay in periodIndexesForDay(periodIndex = periodIndex)) {
                    val lesson = lessons[classIndex][periodIndexInDay]
                    lesson?.subject
                        ?.takeIf { it != Subject.EMPTY }
                        ?.let { blockedSubjects.add(it) }
                }
            }

            if (!configuration.allowSubsequentSubjects) {
                for (subsequentPeriodIndex in subsequentPeriodIndexes(periodIndex = periodIndex)) {
                    val lesson = lessons[classIndex][subsequentPeriodIndex]
                    lesson?.subject
                        ?.takeIf { it != Subject.EMPTY }
                        ?.let { blockedSubjects.add(it) }
                }
            }

            for ((subject, _) in remainingSubjects[classIndex].minus(blockedSubjects)) {
                if (subject == Subject.EMPTY) {
                    classOptions.add(Lesson(subject, null, null))
                } else {
                    val teachers = configuration.subjectAssignments[subject]!!
                    val classrooms = subject.classrooms

                    if (classrooms == null) {
                        for (teacher in teachers) {
                            if (teacher !in busyTeachers) {
                                classOptions.add(Lesson(subject = subject, teacher = teacher, classroom = null))
                            }
                        }
                    } else {
                        for (teacher in teachers) {
                            if (teacher !in busyTeachers) {
                                for (classroom in classrooms) {
                                    if (classroom !in busyClassrooms) {
                                        classOptions.add(Lesson(subject, teacher, classroom))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return classOptions
        }

        fun scheduleNext(next: Lesson): PartialSchedule {
            val last = nextClassIndex == lessons.lastIndex && nextPeriodIndex == lessons[nextClassIndex].lastIndex
            val indexOverflow = when (fillOrder) {
                ScheduleFillOrder.CLASS_BY_CLASS -> nextPeriodIndex == lessons[nextClassIndex].lastIndex
                ScheduleFillOrder.PERIOD_BY_PERIOD -> nextClassIndex == lessons.lastIndex
            }

            return PartialSchedule(
                configuration = configuration,
                lessons = lessons.mapIndexed { classIndex, classSchedule ->
                    classSchedule.mapIndexed { periodIndex, lesson ->
                        if (classIndex == nextClassIndex && periodIndex == nextPeriodIndex) {
                            Lesson(
                                subject = next.subject,
                                teacher = next.teacher,
                                classroom = next.classroom,
                            )
                        } else {
                            lesson
                        }
                    }
                },
                remainingSubjects = remainingSubjects.mapIndexed { index, subjectMap ->
                    if (index == nextClassIndex) {
                        val remaining = subjectMap.getValue(next.subject) - 1
                        if (remaining == 0) {
                            EnumMap(subjectMap).apply { remove(next.subject) }
                        } else {
                            EnumMap(subjectMap).apply { this[next.subject] = remaining }
                        }
                    } else {
                        subjectMap
                    }
                },
                nextClassIndex = when {
                    last -> -1

                    fillOrder == ScheduleFillOrder.CLASS_BY_CLASS && indexOverflow -> nextClassIndex + 1
                    fillOrder == ScheduleFillOrder.CLASS_BY_CLASS && !indexOverflow -> nextClassIndex

                    fillOrder == ScheduleFillOrder.PERIOD_BY_PERIOD && indexOverflow -> 0
                    fillOrder == ScheduleFillOrder.PERIOD_BY_PERIOD && !indexOverflow -> nextClassIndex + 1

                    else -> error("impossible")
                },
                nextPeriodIndex = when {
                    last -> -1

                    fillOrder == ScheduleFillOrder.CLASS_BY_CLASS && indexOverflow -> 0
                    fillOrder == ScheduleFillOrder.CLASS_BY_CLASS && !indexOverflow -> nextPeriodIndex + 1

                    fillOrder == ScheduleFillOrder.PERIOD_BY_PERIOD && indexOverflow -> nextPeriodIndex + 1
                    fillOrder == ScheduleFillOrder.PERIOD_BY_PERIOD && !indexOverflow -> nextPeriodIndex

                    else -> error("impossible")
                },
            )
        }

        fun toCompletedSchedule(): Schedule = Schedule.fromNullable(lessons)
    }

    /**
     * Determines the order in lessons are added to a schedule when searching for a solution.
     */
    enum class ScheduleFillOrder {
        /**
         * Fills out the complete week schedule each class before moving on to the next class.
         */
        CLASS_BY_CLASS,

        /**
         * Fills out every class lesson for each period before moving on to the next period.
         */
        PERIOD_BY_PERIOD
    }
}
