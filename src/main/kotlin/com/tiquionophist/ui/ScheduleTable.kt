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
import com.tiquionophist.ui.common.ColumnWithHeader
import com.tiquionophist.ui.common.Table
import com.tiquionophist.util.prettyName

private object PeriodNamesColumn : ColumnWithHeader<Int> {
    @Composable
    override fun itemContent(value: Int) {
        Text(
            text = "Period ${value + 1}",
            modifier = Modifier.padding(8.dp),
        )
    }
}

private class ScheduleDayColumn(
    private val dayName: String,
    private val lessons: List<Lesson>
) : ColumnWithHeader<Int> {
    @Composable
    override fun header() {
        Text(
            text = dayName,
            modifier = Modifier.padding(8.dp),
        )
    }

    @Composable
    override fun itemContent(value: Int) {
        val lesson = lessons[value]

        Text(
            text = lesson.subject.prettyName,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
fun ScheduleTable(configuration: ScheduleConfiguration, schedule: Schedule, classIndex: Int) {
    val dayNames = remember(configuration.daysPerWeek) {
        @Suppress("MagicNumber")
        if (configuration.daysPerWeek == 5) {
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
        } else {
            List(configuration.daysPerWeek) { "Day ${it + 1}" }
        }
    }

    val columns = remember(configuration.periodsPerDay, configuration.daysPerWeek, classIndex) {
        val periodNamesColumn = PeriodNamesColumn

        val chunkedLessons = schedule.lessons[classIndex].chunked(configuration.periodsPerDay)
        val scheduleDayColumns = List(configuration.daysPerWeek) { day ->
            ScheduleDayColumn(dayName = dayNames[day], lessons = chunkedLessons[day])
        }

        listOf(periodNamesColumn).plus(scheduleDayColumns)
    }

    Table(
        columns = columns,
        rows = listOf(null).plus(List(configuration.periodsPerDay) { it })
    )
}
