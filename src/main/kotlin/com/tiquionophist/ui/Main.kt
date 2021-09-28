package com.tiquionophist.ui

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.common.ContentWithBottomPane

// TODO add option to clear schedule configuration
// TODO add scheduler option
// TODO add schedule process timeout
// TODO improve error states and help messages
// TODO show weekly stat effects for class
// TODO show weekly stat effects for each teacher
// TODO investigate text field focus (cursor remains after unfocused)
// TODO add window icon
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HHS+ Scheduler",
            state = rememberWindowState(placement = WindowPlacement.Maximized),
        ) {
            val scheduleConfigurationState = remember {
                mutableStateOf(ScheduleConfiguration(classes = 2))
            }

            val customTeachersState = remember {
                mutableStateOf(listOf<Teacher>())
            }
            val customTeacherDialogVisibleState = remember { mutableStateOf(false) }
            val computedSchedulesState = remember { mutableStateOf(listOf<ComputedSchedule>()) }

            MenuBar(
                scheduleConfigurationState = scheduleConfigurationState,
                showCustomTeacherDialog = {
                    customTeacherDialogVisibleState.value = true
                }
            )

            // TODO add toggle for light/dark theme
            DesktopMaterialTheme(colors = Colors.materialColors()) {
                Surface {
                    if (customTeacherDialogVisibleState.value) {
                        CustomTeacherDialog { teacher ->
                            customTeacherDialogVisibleState.value = false
                            teacher?.let {
                                // TODO handle duplicate teachers (since it is a data class)
                                customTeachersState.value = customTeachersState.value.plus(teacher)
                            }
                        }
                    }

                    computedSchedulesState.value.forEach { computedSchedule ->
                        ScheduleWindow(
                            computedSchedule = computedSchedule,
                            onClose = {
                                computedSchedulesState.value = computedSchedulesState.value
                                    .minus(computedSchedule)
                            }
                        )
                    }

                    ContentWithBottomPane(
                        content = {
                            // TODO find a way to have the table fill and still allow scrollbars
                            Box(contentAlignment = Alignment.Center) {
                                val verticalScrollState = rememberScrollState(0)
                                val horizontalScrollState = rememberScrollState(0)

                                Box(
                                    modifier = Modifier
                                        .verticalScroll(verticalScrollState)
                                        .horizontalScroll(horizontalScrollState)
                                ) {
                                    ScheduleConfigurationTable(
                                        scheduleConfigurationState = scheduleConfigurationState,
                                        customTeachers = customTeachersState.value,
                                    )
                                }

                                VerticalScrollbar(
                                    adapter = rememberScrollbarAdapter(verticalScrollState),
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                )

                                HorizontalScrollbar(
                                    adapter = rememberScrollbarAdapter(horizontalScrollState),
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                )
                            }
                        },
                        bottomPane = {
                            SettingsPane(
                                scheduleConfigurationState = scheduleConfigurationState,
                                onComputedSchedule = { computedSchedule ->
                                    computedSchedulesState.value = computedSchedulesState.value
                                        .plus(computedSchedule)
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
