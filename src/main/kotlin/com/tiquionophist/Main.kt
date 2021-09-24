package com.tiquionophist

import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.StatSet
import com.tiquionophist.scheduler.RandomizedScheduler
import com.tiquionophist.util.prettyName
import java.io.File
import kotlin.time.measureTimedValue

private const val PRINT_TEACHER_IN_GRID = false
private const val PRINT_CLASSROOM_IN_GRID = true

private val configFile = File("config.json").apply {
    require(isFile) { "config file $absoluteFile not found" }
}
private val scheduler = RandomizedScheduler(attemptsPerRound = 1_000, rounds = 1_000)

fun main() {
    val config = requireNotNull(ScheduleConfiguration.load(configFile))

    config.verify()

    config.teacherAssignments.forEach { (teacher, _) ->
        val min = config.minClassesTaughtPerTeacher[teacher]
        val max = config.maxClassesTaughtPerTeacher[teacher]
        println("${teacher.fullName} teachers at least $min and at most $max classes per week")
    }
    println()

    println("Weekly stats from schedule:")
    config.classStats.stats.forEach { (statName, statValue) ->
        println("  ${statName.prettyName} : $statValue")
    }
    println()

    val (schedule, duration) = measureTimedValue {
        scheduler.schedule(config)
    }

    println("Done in $duration!")
    println()
    if (schedule == null) {
        println("No schedule found.")
        return
    }

    println("Schedule found!")
    schedule.toTableString(
        config = config,
        includeTeacher = PRINT_TEACHER_IN_GRID,
        includeClassroom = PRINT_CLASSROOM_IN_GRID
    ).forEachIndexed { index, table ->
        println("Class ${index + 1}:")
        println(table)
        println()
    }

    config.teacherAssignments.keys.map { teacher ->
        val lessons = schedule.lessons.flatten().filter { it.teacher == teacher }

        val teacherStats = schedule.teacherStats[teacher] ?: StatSet()

        println("${teacher.fullName} teaches ${lessons.size} classes per week, gaining the stats:")
        teacherStats.stats.forEach { (statName, statValue) ->
            println("  $statName : $statValue")
        }
        println()
    }

    schedule.verify(config)
    println("Schedule verified!")
}
