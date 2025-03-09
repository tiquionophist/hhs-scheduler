package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tiquionophist.core.Lesson
import com.tiquionophist.core.Schedule
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.ui.common.ColumnWidth
import com.tiquionophist.ui.common.ColumnWithHeader
import com.tiquionophist.ui.common.Table
import com.tiquionophist.ui.common.TableDivider
import com.tiquionophist.util.prettyName
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import org.jetbrains.compose.resources.painterResource

/**
 * A column which displays the period number for each period index row.
 */
private object PeriodNamesColumn : ColumnWithHeader<Int> {
    override fun horizontalAlignment(rowIndex: Int) = Alignment.End

    @Composable
    override fun itemContent(value: Int) {
        Text(
            text = "Period ${value + 1}",
            modifier = Modifier.padding(Dimens.SPACING_2),
        )
    }
}

/**
 * A column which displays the set of [lessons] on a certain day.
 */
private class ScheduleDayColumn(
    private val dayName: String,
    private val lessons: List<Lesson>
) : ColumnWithHeader<Int> {
    override val itemHorizontalAlignment = Alignment.Start
    override val itemVerticalAlignment = Alignment.Top

    override val width = ColumnWidth.Fixed(width = Dimens.ScheduleTable.CELL_WIDTH)

    override fun fillCell(value: Int?): Boolean {
        return value != null && lessons[value].subject == Subject.EMPTY
    }

    @Composable
    override fun header() {
        Text(
            text = dayName,
            modifier = Modifier.padding(Dimens.SPACING_2),
        )
    }

    @Composable
    override fun itemContent(value: Int) {
        val lesson = lessons[value]
        val subject = lesson.subject

        if (subject == Subject.EMPTY) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ScheduleTable.CELL_HEIGHT)
                    .background(LocalContentColor.current.copy(alpha = 0.05f))
            ) {
                Text(
                    text = subject.prettyName,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            Row(
                modifier = Modifier.height(Dimens.ScheduleTable.CELL_HEIGHT).padding(Dimens.SPACING_2),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)
            ) {
                subject.imageRes?.let { imageRes ->
                    Image(
                        painter = painterResource(imageRes),
                        contentDescription = subject.prettyName,
                    )
                }

                Column {
                    Text(text = subject.prettyName, fontSize = Dimens.FONT_LARGE)

                    lesson.teacher?.let { teacher ->
                        Text(text = "by ${teacher.fullName}", fontSize = Dimens.FONT_SMALL)
                    }

                    lesson.assignedClassroom?.let { classroom ->
                        Text(text = "in $classroom", fontSize = Dimens.FONT_SMALL)
                    }
                }
            }
        }
    }
}

/**
 * A grid-based view of the lessons in a [schedule] for the class at [classIndex].
 */
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

        listOf(periodNamesColumn).plus(scheduleDayColumns).toImmutableList()
    }

    val rows = remember(configuration.periodsPerDay) {
        listOf(null).plus(List(configuration.periodsPerDay) { it }).toImmutableList()
    }

    Table(
        columns = columns,
        rows = rows,
        // strong divider after the header row, weak dividers between each period row
        horizontalDividers = List(rows.size - 1) { rowIndex ->
            rowIndex + 1 to TableDivider(
                color = if (rowIndex == 0) ThemeColors.current.divider else ThemeColors.current.weakDivider
            )
        }.toMap().toImmutableMap(),
        // strong divider after the period name column, weak dividers between each day column
        verticalDividers = List(columns.size - 1) { colIndex ->
            colIndex + 1 to TableDivider(
                color = if (colIndex == 0) ThemeColors.current.divider else ThemeColors.current.weakDivider
            )
        }.toMap().toImmutableMap(),
    )
}
