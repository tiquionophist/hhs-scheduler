package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.style.TextAlign
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.Colors.enabledIf
import com.tiquionophist.ui.common.ColumnWidth
import com.tiquionophist.ui.common.ColumnWithHeader
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.Table
import com.tiquionophist.ui.common.TableDivider
import com.tiquionophist.util.pluralizedCount
import com.tiquionophist.util.prettyName

/**
 * Subjects displayed in the table, in order.
 */
private val subjects: List<Subject> = Subject.values()
    .filter { it != Subject.EMPTY }
    .sortedBy { it.prettyName }
    .plus(Subject.EMPTY)

/**
 * Whether [teacher] should be shown as enabled for this [ScheduleConfiguration], i.e. if it has any assignments.
 */
private fun ScheduleConfiguration.teacherEnabled(teacher: Teacher): Boolean {
    return teacherAssignments[teacher]?.isNotEmpty() == true
}

/**
 * Whether [subject] should be shown as enabled for this [ScheduleConfiguration], i.e. if it has non-zero frequency in
 * the schedule or has any teachers assigned to it.
 */
private fun ScheduleConfiguration.subjectEnabled(subject: Subject): Boolean {
    return subjectFrequency[subject]?.let { it > 0 } == true || subjectAssignments[subject]?.isNotEmpty() == true
}

/**
 * A column displaying the icon for each [Subject].
 */
private object SubjectIconColumn : ColumnWithHeader<Subject> {
    @Composable
    override fun itemContent(value: Subject) {
        value.imageBitmap?.let { imageBitmap ->
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = value.prettyName,
                modifier = Modifier
                    .padding(horizontal = Dimens.SPACING_2)
                    .size(Dimens.ScheduleConfigurationTable.SUBJECT_ICON_SIZE)
                    .enabledIf(GlobalState.scheduleConfiguration.subjectEnabled(value)),
            )
        }
    }
}

/**
 * A column displaying the name of each [Subject].
 */
private object SubjectNameColumn : ColumnWithHeader<Subject> {
    override val itemHorizontalAlignment = Alignment.Start

    @Composable
    override fun itemContent(value: Subject) {
        Text(
            text = value.prettyName,
            modifier = Modifier
                .padding(Dimens.SPACING_2)
                .enabledIf(GlobalState.scheduleConfiguration.subjectEnabled(value)),
        )
    }
}

/**
 * A column displaying a number picker for the frequency of each [Subject] in the schedule.
 */
private object SubjectFrequencyPickerColumn : ColumnWithHeader<Subject> {
    override val headerVerticalAlignment = Alignment.Bottom

    @Composable
    override fun header() {
        Text(
            modifier = Modifier.padding(vertical = Dimens.SPACING_2),
            text = "Times/week",
        )
    }

    @Composable
    override fun itemContent(value: Subject) {
        val config = GlobalState.scheduleConfiguration
        if (value == Subject.EMPTY) {
            Text(
                modifier = Modifier.padding(Dimens.SPACING_2),
                text = (config.subjectFrequency[value] ?: 0).toString(),
            )
        } else {
            NumberPicker(
                modifier = Modifier.padding(Dimens.SPACING_2),
                value = config.subjectFrequency[value] ?: 0,
                onValueChange = { newValue ->
                    GlobalState.scheduleConfiguration = GlobalState.scheduleConfiguration.copy(
                        subjectFrequency = ScheduleConfiguration.fillFreePeriods(
                            periodsPerWeek = config.periodsPerWeek,
                            subjectFrequency = config.subjectFrequency.plus(value to newValue),
                        )
                    )
                },
                min = 0,
                max = config.periodsPerWeek,
            )
        }
    }
}

/**
 * A column displaying the total number of teachers assigned to each [Subject].
 */
private object TotalTeacherAssignmentsColumn : ColumnWithHeader<Subject> {
    override val itemHorizontalAlignment = Alignment.Start

    @Composable
    override fun itemContent(value: Subject) {
        if (value == Subject.EMPTY) return

        val config = GlobalState.scheduleConfiguration
        val numTeachers = config.subjectAssignments[value]?.size ?: 0

        val frequency = config.subjectFrequency[value] ?: 0
        val error = (frequency == 0) != (numTeachers == 0)

        Text(
            text = "teacher".pluralizedCount(numTeachers),
            color = if (error) MaterialTheme.colors.error else Color.Unspecified,
            modifier = Modifier.padding(Dimens.SPACING_2).enabledIf(config.subjectEnabled(value)),
        )
    }
}

/**
 * A column displaying the subject assignments for each teacher, with a header showing the teacher image and name and a
 * checkbox toggling whether the teacher is assigned for each subject row.
 */
private class SubjectTeacherAssignmentsColumn(private val teacher: Teacher) : ColumnWithHeader<Subject> {
    override val headerVerticalAlignment = Alignment.Bottom

    override val width = ColumnWidth.Fill(
        minWidth = Dimens.ScheduleConfigurationTable.TEACHER_IMAGE_WIDTH + (Dimens.SPACING_4 * 2)
    )

    override fun fillCell(value: Subject?): Boolean {
        return value != null
    }

    @Composable
    override fun header() {
        val config = GlobalState.scheduleConfiguration

        Column(
            modifier = Modifier.padding(Dimens.SPACING_2).enabledIf(config.teacherEnabled(teacher)),
            verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            teacher.imageBitmap?.let { imageBitmap ->
                Image(
                    painter = BitmapPainter(imageBitmap),
                    contentDescription = teacher.fullName,
                    modifier = Modifier
                        .width(Dimens.ScheduleConfigurationTable.TEACHER_IMAGE_WIDTH)
                        // set the aspect ratio so that the image has a minimum intrinsic height
                        .aspectRatio(imageBitmap.width.toFloat() / imageBitmap.height)
                )
            }

            Text(
                text = "${teacher.firstName}\n${teacher.lastName}",
                textAlign = TextAlign.Center,
            )

            val numAssignments = config.teacherAssignments[teacher]?.size ?: 0
            Text(text = "subject".pluralizedCount(numAssignments))

            if (numAssignments != 0) {
                val minClasses = config.minClassesTaughtPerTeacher[teacher] ?: 0
                val maxClasses = config.maxClassesTaughtPerTeacher[teacher] ?: 0

                val range = if (minClasses == maxClasses) minClasses.toString() else "$minClasses - $maxClasses"

                Text(text = "($range classes/week)", textAlign = TextAlign.Center)
            } else {
                Text("")
            }
        }
    }

    @Composable
    override fun itemContent(value: Subject) {
        if (value == Subject.EMPTY) return

        val config = GlobalState.scheduleConfiguration
        val currentAssignments = remember(config, teacher) {
            config.teacherAssignments.getOrDefault(teacher, emptySet())
        }
        val contains = remember(currentAssignments) { currentAssignments.contains(value) }

        Surface(
            modifier = Modifier
                .clickable {
                    val newAssignments = if (contains) {
                        currentAssignments.minus(value)
                    } else {
                        currentAssignments.plus(value)
                    }

                    GlobalState.scheduleConfiguration = GlobalState.scheduleConfiguration.copy(
                        teacherAssignments = config.teacherAssignments
                            .plus(teacher to newAssignments)
                            .filterValues { it.isNotEmpty() }
                    )
                }
                .fillMaxSize(),
            color = if (contains) {
                Colors.SELECTED.copy(alpha = Colors.DISABLED_ALPHA)
            } else {
                Color.Unspecified
            },
        ) {
            Checkbox(
                checked = contains,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = Colors.SELECTED)
            )
        }
    }
}

/**
 * A grid-based view of the teacher/subject assignments and frequencies in [GlobalState.scheduleConfiguration].
 */
@Composable
fun ScheduleConfigurationTable() {
    val configuration = GlobalState.scheduleConfiguration
    val scheduledTeachers = configuration.teacherAssignments.keys

    val teachers = remember(
        scheduledTeachers,
        GlobalState.customTeachers,
        GlobalState.showLexvilleTeachers,
        GlobalState.showUnusedTeachers,
    ) {
        Teacher.DEFAULT_TEACHERS
            .plus(scheduledTeachers)
            .plus(GlobalState.customTeachers)
            .plus(if (GlobalState.showLexvilleTeachers) Teacher.LEXVILLE_TEACHERS else emptySet())
            .let { teachers ->
                if (GlobalState.showUnusedTeachers) {
                    teachers
                } else {
                    teachers.filter { configuration.teacherEnabled(it) }
                        .takeUnless { it.isEmpty() } ?: teachers // don't allow empty teachers
                }
            }
            .sortedBy { it.fullName }
    }

    val fixedColumns = listOf(
        SubjectIconColumn,
        SubjectNameColumn,
        SubjectFrequencyPickerColumn,
        TotalTeacherAssignmentsColumn,
    )

    val fixedRows = listOf(null)

    val subjectRows = remember(GlobalState.showUnusedSubjects, configuration) {
        if (GlobalState.showUnusedSubjects) {
            subjects
        } else {
            subjects.filter { configuration.subjectEnabled(it) }
                .takeUnless { it.minus(Subject.EMPTY).isEmpty() } ?: subjects // don't allow empty subjects
        }
    }

    Table(
        columns = fixedColumns
            .plus(
                teachers.map { teacher ->
                    SubjectTeacherAssignmentsColumn(teacher)
                }
            ),
        rows = fixedRows.plus(subjectRows),
        fillMaxHeight = true,
        verticalDividers = mapOf(
            // strong divider after fixed columns
            Pair(
                fixedColumns.size,
                TableDivider(
                    paddingBefore = Dimens.SPACING_2,
                    color = Colors.divider
                )
            ),
        ).plus(
            // weak dividers between each teacher column
            List(teachers.size - 1) { teacherIndex ->
                Pair(teacherIndex + fixedColumns.size + 1, TableDivider(color = Colors.weakDivider))
            }
        ),
        horizontalDividers = mapOf(
            // strong divider after header row
            fixedRows.size to TableDivider(
                paddingBefore = Dimens.SPACING_2,
                color = Colors.divider
            ),
        ).plus(
            // weak dividers between each subject row
            List(subjectRows.size - 1) { subjectIndex ->
                Pair(subjectIndex + fixedRows.size + 1, TableDivider(color = Colors.weakDivider))
            }
        ),
    )
}
