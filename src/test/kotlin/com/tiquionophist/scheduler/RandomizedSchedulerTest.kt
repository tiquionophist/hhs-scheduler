package com.tiquionophist.scheduler

import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

internal class RandomizedSchedulerTest {
    @Test
    fun testScheduleTrivial() {
        val configuration = ScheduleConfiguration(
            classes = 2,
            teacherAssignments = mapOf(
                Teacher("English", "Teacher") to setOf(Subject.ENGLISH),
                Teacher("Math", "Teacher") to setOf(Subject.MATH),
            ),
            subjectFrequency = mapOf(
                Subject.ENGLISH to 10,
                Subject.MATH to 10,
            )
        )

        val scheduler = RandomizedScheduler(attemptsPerRound = null, rounds = 1)

        val schedule = runBlocking { scheduler.schedule(configuration) }

        assertNotNull(schedule)
    }
}
