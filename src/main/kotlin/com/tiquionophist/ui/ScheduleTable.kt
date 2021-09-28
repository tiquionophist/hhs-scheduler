package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import com.tiquionophist.core.Lesson
import com.tiquionophist.core.Schedule
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ui.common.ColumnWithHeader
import com.tiquionophist.ui.common.Table
import com.tiquionophist.ui.common.TableDivider
import com.tiquionophist.util.prettyName

private object PeriodNamesColumn : ColumnWithHeader<Int> {
    override fun horizontalAlignment(rowIndex: Int) = Alignment.End

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
    override val itemHorizontalAlignment = Alignment.Start

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

        Column(modifier = Modifier.padding(8.dp).size(width = 200.dp, height = 80.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val subject = lesson.subject
                subject.imageBitmap?.let { imageBitmap ->
                    Image(
                        painter = BitmapPainter(imageBitmap),
                        contentDescription = subject.prettyName,
                    )
                }

                Text(
                    text = subject.prettyName,
                    modifier = Modifier.padding(8.dp),
                )
            }

            lesson.teacher?.let { teacher ->
                Text("by ${teacher.fullName}")
            }

            lesson.classroom?.let { classroom ->
                Text("in ${classroom.prettyName}")
            }
        }
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

    val rows = remember(configuration.periodsPerDay) {
        listOf(null).plus(List(configuration.periodsPerDay) { it })
    }

    // TODO use color from theme
    val strongDividerColor = Color.Black
    val weakDividerColor = strongDividerColor.copy(alpha = 0.25f)

    Table(
        columns = columns,
        rows = rows,
        horizontalDividers = List(rows.size - 1) { rowIndex ->
            rowIndex + 1 to TableDivider(dividerColor = if (rowIndex == 0) strongDividerColor else weakDividerColor)
        }.toMap(),
        verticalDividers = List(columns.size - 1) { colIndex ->
            colIndex + 1 to TableDivider(dividerColor = if (colIndex == 0) strongDividerColor else weakDividerColor)
        }.toMap(),
    )
}
