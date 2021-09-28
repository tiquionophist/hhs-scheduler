package com.tiquionophist.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState

@Composable
fun ScheduleWindow(
    computedSchedule: ComputedSchedule,
    onClose: () -> Unit
) {
    Window(
        title = "Schedule ${computedSchedule.index + 1}",
        onCloseRequest = onClose,
        state = rememberWindowState(
            width = Dp.Unspecified,
            height = Dp.Unspecified,
            position = WindowPosition.Aligned(Alignment.Center),
        ),
    ) {
        Surface {
            Layout(
                content = {
                    val selectedClassIndexState = remember { mutableStateOf(0) }

                    Row {
                        repeat(computedSchedule.configuration.classes) { classIndex ->
                            val selected = selectedClassIndexState.value == classIndex
                            Button(
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                onClick = { selectedClassIndexState.value = classIndex },
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (selected) {
                                        MaterialTheme.colors.primary
                                    } else {
                                        MaterialTheme.colors.background
                                    }
                                )
                            ) {
                                Text("Class ${classIndex + 1}")
                            }
                        }
                    }

                    ScheduleTable(
                        configuration = computedSchedule.configuration,
                        schedule = computedSchedule.schedule,
                        classIndex = selectedClassIndexState.value,
                    )
                },
                measurePolicy = { measurables, constraints ->
                    val buttonRowMeasurable = measurables[0]
                    val tableMeasurable = measurables[1]

                    val tablePlaceable = tableMeasurable.measure(constraints)
                    val width = tablePlaceable.width

                    val buttonRowPlaceable = buttonRowMeasurable.measure(
                        constraints.copy(minWidth = width, maxWidth = width)
                    )

                    layout(width, tablePlaceable.height + buttonRowPlaceable.height) {
                        buttonRowPlaceable.place(0, 0)
                        tablePlaceable.place(0, buttonRowPlaceable.height)
                    }
                }
            )
        }
    }
}
