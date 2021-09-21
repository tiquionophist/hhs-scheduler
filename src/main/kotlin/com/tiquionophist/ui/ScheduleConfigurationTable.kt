package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Subject
import com.tiquionophist.core.Teacher
import com.tiquionophist.util.prettyName
import org.jetbrains.skia.Image

private val subjects = Subject.values().filter { it != Subject.EMPTY }

private object SubjectIconColumn : ColumnWithHeader<Subject> {
    override val items = subjects

    @Composable
    override fun itemContent(value: Subject) {
        val imageBitmap = remember(value) {
            val classLoader = Thread.currentThread().contextClassLoader!!
            classLoader.getResourceAsStream(value.name + ".png")?.use {
                Image.makeFromEncoded(it.readAllBytes()).asImageBitmap()
            }
        }

        imageBitmap?.let {
            Image(
                painter = BitmapPainter(imageBitmap),
                contentDescription = value.prettyName,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

private object SubjectNameColumn : ColumnWithHeader<Subject> {
    override val items = subjects

    override val horizontalAlignment = Alignment.Start

    @Composable
    override fun itemContent(value: Subject) {
        Text(
            text = value.prettyName,
            modifier = Modifier.padding(8.dp),
        )
    }
}

private class SubjectTeacherAssignmentsColumn(
    private val teacher: Teacher,
    private val scheduleConfigurationState: MutableState<ScheduleConfiguration>
) : ColumnWithHeader<Subject> {
    override val items = subjects

    @Composable
    override fun header() {
        Text(
            text = teacher.prettyName.replace(' ', '\n'),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp),
        )
    }

    @Composable
    override fun itemContent(value: Subject) {
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
fun ScheduleConfigurationTable(scheduleConfigurationState: MutableState<ScheduleConfiguration>) {
    Table(
        columns = listOf(
            SubjectIconColumn,
            SubjectNameColumn,
        ).plus(
            Teacher.values().map { teacher ->
                SubjectTeacherAssignmentsColumn(teacher, scheduleConfigurationState)
            }
        )
    )
}
