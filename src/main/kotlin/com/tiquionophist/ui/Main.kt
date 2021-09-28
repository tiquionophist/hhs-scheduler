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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import com.tiquionophist.ui.common.ContentWithPane
import com.tiquionophist.ui.common.PaneDirection
import java.io.File

// TODO add option to clear schedule configuration
// TODO add scheduler option
// TODO add schedule process timeout
// TODO improve error states and help messages
// TODO show weekly stat effects for each teacher
// TODO investigate text field focus (cursor remains after unfocused)
// TODO add window icon
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
fun main() {
    // load from config.json by default on startup, if it exists, for convenience
    val initialConfiguration = ScheduleConfiguration.load(File("config.json")) ?: ScheduleConfiguration(classes = 2)

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HHS+ Scheduler",
            state = rememberWindowState(placement = WindowPlacement.Maximized),
        ) {
            val scheduleConfigurationState = remember { mutableStateOf(initialConfiguration) }
            val customTeachersState = remember { mutableStateOf(listOf<Teacher>()) }
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
                            teacher?.takeUnless { customTeachersState.value.contains(it) }?.let {
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
                }

                MainContent(
                    scheduleConfigurationState = scheduleConfigurationState,
                    customTeachers = customTeachersState.value,
                    onComputedSchedule = { computedSchedule ->
                        computedSchedulesState.value = computedSchedulesState.value
                            .plus(computedSchedule)
                    }
                )
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun MainContent(
    scheduleConfigurationState: MutableState<ScheduleConfiguration>,
    customTeachers: List<Teacher>,
    onComputedSchedule: (ComputedSchedule) -> Unit,
) {
    ContentWithPane(
        direction = PaneDirection.BOTTOM,
        content = {
            ContentWithPane(
                direction = PaneDirection.RIGHT,
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
                                customTeachers = customTeachers,
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
                pane = {
                    StatsPane(configuration = scheduleConfigurationState.value)
                }
            )
        },
        pane = {
            SettingsPane(
                scheduleConfigurationState = scheduleConfigurationState,
                onComputedSchedule = onComputedSchedule
            )
        }
    )
}
