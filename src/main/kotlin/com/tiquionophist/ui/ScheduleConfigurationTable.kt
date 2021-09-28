package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.common.ColumnWithHeader
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.Table
import com.tiquionophist.ui.common.TableDivider
import com.tiquionophist.util.prettyName

private val subjects = Subject.values()
    .filter { it != Subject.EMPTY }
    .sortedBy { it.prettyName }
    .plus(Subject.EMPTY)

private object SubjectIconColumn : ColumnWithHeader<Subject> {
    @Composable
    override fun itemContent(value: Subject) {
        value.imageBitmap?.let { imageBitmap ->
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = value.prettyName,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

private object SubjectNameColumn : ColumnWithHeader<Subject> {
    override val itemHorizontalAlignment = Alignment.Start

    @Composable
    override fun itemContent(value: Subject) {
        Text(
            text = value.prettyName,
            modifier = Modifier.padding(8.dp),
        )
    }
}

private class SubjectFrequencyPickerColumn(
    private val scheduleConfigurationState: MutableState<ScheduleConfiguration>
) : ColumnWithHeader<Subject> {
    override val headerVerticalAlignment = Alignment.Bottom

    @Composable
    override fun header() {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = "Times/week",
        )
    }

    @Composable
    override fun itemContent(value: Subject) {
        val config = scheduleConfigurationState.value
        if (value == Subject.EMPTY) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = (config.subjectFrequency[value] ?: 0).toString(),
            )
        } else {
            NumberPicker(
                modifier = Modifier.padding(8.dp),
                value = config.subjectFrequency[value] ?: 0,
                onValueChange = { newValue ->
                    scheduleConfigurationState.value = config.copy(
                        subjectFrequency = ScheduleConfiguration.fillFreePeriods(
                            periodsPerWeek = config.periodsPerWeek,
                            subjectFrequency = config.subjectFrequency.plus(value to newValue),
                        )
                    )
                },
                range = IntRange(0, config.periodsPerWeek)
            )
        }
    }
}

private class TotalTeacherAssignmentsColumn(val configuration: ScheduleConfiguration) : ColumnWithHeader<Subject> {
    override val itemHorizontalAlignment = Alignment.Start

    @Composable
    override fun itemContent(value: Subject) {
        if (value == Subject.EMPTY) return

        val numTeachers = configuration.subjectAssignments[value]?.size ?: 0
        val teachersPlural = numTeachers != 1

        val frequency = configuration.subjectFrequency[value] ?: 0
        val error = (frequency == 0) != (numTeachers == 0)

        Text(
            text = "$numTeachers teacher${if (teachersPlural) "s" else ""}",
            modifier = Modifier.padding(8.dp),
            color = if (error) Color.Red else Color.Unspecified,
        )
    }
}

private class SubjectTeacherAssignmentsColumn(
    private val teacher: Teacher,
    private val scheduleConfigurationState: MutableState<ScheduleConfiguration>
) : ColumnWithHeader<Subject> {
    override val headerVerticalAlignment = Alignment.Bottom

    @Composable
    override fun header() {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            teacher.imageBitmap?.let { imageBitmap ->
                Image(
                    painter = BitmapPainter(imageBitmap),
                    contentDescription = teacher.fullName,
                    modifier = Modifier.width(50.dp),
                )
            }

            Text(
                text = "${teacher.firstName}\n${teacher.lastName}",
                textAlign = TextAlign.Center,
            )

            val config = scheduleConfigurationState.value
            val numAssignments = config.teacherAssignments[teacher]?.size ?: 0
            Text(text = "$numAssignments subjects")
        }
    }

    @Composable
    override fun itemContent(value: Subject) {
        if (value == Subject.EMPTY) return

        val config = scheduleConfigurationState.value
        val currentAssignments = config.teacherAssignments.getOrDefault(teacher, emptySet())
        Checkbox(
            checked = currentAssignments.contains(value),
            onCheckedChange = { checked ->
                val newAssignments = if (checked) currentAssignments.plus(value) else currentAssignments.minus(value)
                scheduleConfigurationState.value = config.copy(
                    teacherAssignments = config.teacherAssignments
                        .plus(teacher to newAssignments)
                        .filterValues { it.isNotEmpty() }
                )
            }
        )
    }
}

/**
 * A grid-based view of the teacher/subject assignments and frequencies in [scheduleConfigurationState].
 */
@Composable
fun ScheduleConfigurationTable(
    scheduleConfigurationState: MutableState<ScheduleConfiguration>,
    customTeachers: List<Teacher>
) {
    val teachers = remember(customTeachers) {
        Teacher.DEFAULT_TEACHERS
            .plus(Teacher.LEXVILLE_TEACHERS)
            .plus(customTeachers)
            .sortedBy { it.fullName }
    }

    val strongDividerColor = LocalContentColor.current
    val weakDividerColor = strongDividerColor.copy(alpha = 0.25f)

    val fixedColumns = listOf(
        SubjectIconColumn,
        SubjectNameColumn,
        SubjectFrequencyPickerColumn(scheduleConfigurationState),
    )

    val fixedRows = listOf(null)

    Table(
        columns = fixedColumns
            .plus(
                teachers.map { teacher ->
                    SubjectTeacherAssignmentsColumn(teacher, scheduleConfigurationState)
                }
            )
            .plus(
                TotalTeacherAssignmentsColumn(scheduleConfigurationState.value)
            ),
        rows = fixedRows.plus(subjects),
        verticalDividers = mapOf(
            // strong divider after fixed columns
            Pair(
                fixedColumns.size,
                TableDivider(paddingBefore = 8.dp, paddingAfter = 4.dp, color = strongDividerColor)
            ),
            // strong divider before total teacher assignments column
            Pair(
                fixedColumns.size + teachers.size,
                TableDivider(paddingBefore = 8.dp, paddingAfter = 8.dp, color = strongDividerColor)
            ),
        ).plus(
            // weak dividers between each teacher column
            List(teachers.size - 1) { teacherIndex ->
                Pair(teacherIndex + fixedColumns.size + 1, TableDivider(color = weakDividerColor))
            }
        ),
        horizontalDividers = mapOf(
            // strong divider after header row
            fixedRows.size to TableDivider(paddingBefore = 8.dp, paddingAfter = 4.dp, color = strongDividerColor),
        ).plus(
            // weak dividers between each subject row
            List(subjects.size) { subjectIndex ->
                Pair(subjectIndex + fixedRows.size + 1, TableDivider(color = weakDividerColor))
            }
        ),
    )
}
