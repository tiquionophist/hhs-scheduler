package com.tiquionophist.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiquionophist.core.Lesson
import com.tiquionophist.core.Schedule
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.util.prettyName

private class PeriodNamesColumn(periodsPerDay: Int) : ColumnWithHeader<Int> {
    override val items = List(periodsPerDay) { it }

    @Composable
    override fun itemContent(value: Int) {
        Text(
            text = "Period ${value + 1}",
            modifier = Modifier.padding(8.dp),
        )
    }
}

private class ScheduleDayColumn(private val dayName: String, lessons: List<Lesson>) : ColumnWithHeader<Lesson> {
    override val items = lessons

    @Composable
    override fun header() {
        Text(
            text = dayName,
            modifier = Modifier.padding(8.dp),
        )
    }

    @Composable
    override fun itemContent(value: Lesson) {
        Text(
            text = value.subject.prettyName,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
fun ScheduleTable(configuration: ScheduleConfiguration, schedule: Schedule, classIndex: Int) {
    val dayNames = remember(configuration.daysPerWeek) {
        if (configuration.daysPerWeek == 5) {
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        } else {
            List(configuration.daysPerWeek) { "Day ${it + 1}" }
        }
    }

    val columns = remember(configuration.periodsPerDay, configuration.daysPerWeek, classIndex) {
        val periodNamesColumn = PeriodNamesColumn(periodsPerDay = configuration.periodsPerDay)

        val chunkedLessons = schedule.lessons[classIndex].chunked(configuration.periodsPerDay)
        val scheduleDayColumns = List(configuration.daysPerWeek) { day ->
            ScheduleDayColumn(dayName = dayNames[day], lessons = chunkedLessons[day])
        }

        listOf(periodNamesColumn).plus(scheduleDayColumns)
    }

    Table(columns)
}
