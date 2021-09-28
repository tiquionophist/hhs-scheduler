package com.tiquionophist.ui

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.BoxWithTooltip
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.lightColors
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tiquionophist.core.Schedule
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Teacher
import com.tiquionophist.scheduler.RandomizedScheduler
import com.tiquionophist.ui.common.ContentWithBottomPane
import com.tiquionophist.ui.common.FilePicker
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.topBorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val MAX_CLASSES = 100

private val PRIMARY_COLOR = Color(42, 149, 232)

data class ComputedSchedule(
    val configuration: ScheduleConfiguration,
    val schedule: Schedule,
    val index: Int = indexCounter++
) {
    companion object {
        private var indexCounter: Int = 0
    }
}

@ExperimentalComposeUiApi
@ExperimentalFoundationApi
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HHS+ Scheduler",
            state = rememberWindowState(placement = WindowPlacement.Maximized)
        ) {
            val darkThemeState = remember { mutableStateOf(false) }

            val scheduleConfigurationState = remember {
                mutableStateOf(ScheduleConfiguration(classes = 2))
            }

            val customTeachersState = remember {
                mutableStateOf(listOf<Teacher>())
            }
            val customTeacherDialogVisibleState = remember { mutableStateOf(false) }
            val computedSchedulesState = remember { mutableStateOf(listOf<ComputedSchedule>()) }

            MenuBar {
                Menu("File") {
                    Item(
                        text = "Save configuration",
                        shortcut = KeyShortcut(Key.S, ctrl = true),
                        onClick = {
                            FilePicker.save()?.let { file ->
                                scheduleConfigurationState.value.save(file)
                                // TODO show success (or error) modal
                            }
                        }
                    )

                    Item(
                        text = "Load configuration",
                        shortcut = KeyShortcut(Key.O, ctrl = true),
                        onClick = {
                            FilePicker.load()?.let { file ->
                                ScheduleConfiguration.load(file)?.let {
                                    // TODO show error modal on null case
                                    scheduleConfigurationState.value = it
                                }
                            }
                        }
                    )
                }

                Menu("Edit") {
                    Item(
                        text = "Add custom teacher",
                        onClick = {
                            customTeacherDialogVisibleState.value = true
                        }
                    )
                }
            }

            val colors = if (darkThemeState.value) {
                darkColors(primary = PRIMARY_COLOR)
            } else {
                lightColors(primary = PRIMARY_COLOR)
            }

            DesktopMaterialTheme(colors = colors) {
                Surface {
                    CustomTeacherDialog(
                        visible = customTeacherDialogVisibleState.value,
                        onClose = { teacher ->
                            customTeacherDialogVisibleState.value = false
                            teacher?.let {
                                customTeachersState.value = customTeachersState.value.plus(teacher)
                            }
                        }
                    )

                    computedSchedulesState.value.forEach { computedSchedule ->
                        ScheduleWindow(
                            computedSchedule = computedSchedule,
                            onClose = {
                                computedSchedulesState.value = computedSchedulesState.value
                                    .minus(computedSchedule)
                            }
                        )
                    }

                    val verticalScrollState = rememberScrollState(0)
                    val horizontalScrollState = rememberScrollState(0)

                    ContentWithBottomPane(
                        content = {
                            // TODO find a way to have the table fill and still allow scrollbars
                            Box(contentAlignment = Alignment.Center) {
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
                            Row(
                                modifier = Modifier.fillMaxWidth().topBorder().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Classes:")

                                        NumberPicker(
                                            value = scheduleConfigurationState.value.classes,
                                            onValueChange = { newValue ->
                                                scheduleConfigurationState.value = scheduleConfigurationState.value.copy(
                                                    classes = newValue
                                                )
                                            },
                                            range = IntRange(1, MAX_CLASSES),
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    val validationError = runCatching { scheduleConfigurationState.value.verify() }
                                        .exceptionOrNull()
                                        ?.message

                                    if (validationError != null) {
                                        BoxWithTooltip(
                                            tooltip = {
                                                Surface(modifier = Modifier.shadow(4.dp)) {
                                                    Text(
                                                        text = validationError,
                                                        modifier = Modifier.padding(10.dp),
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color.Red,
                                            )
                                        }
                                    }

                                    val loading = remember { mutableStateOf(false) }
                                    val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
                                    Button(
                                        enabled = validationError == null && !loading.value,
                                        onClick = {
                                            loading.value = true
                                            coroutineScope.launch {
                                                val config = scheduleConfigurationState.value
                                                val scheduler = RandomizedScheduler(
                                                    attemptsPerRound = 1_000,
                                                    rounds = 1_000
                                                )

                                                val result = runCatching { scheduler.schedule(config) }
                                                if (result.isSuccess) {
                                                    val schedule = result.getOrThrow()
                                                    if (schedule == null) {
                                                        // TODO show as dialog
                                                        println("No schedule found")
                                                    } else {
                                                        val computedSchedule = ComputedSchedule(
                                                            configuration = config,
                                                            schedule = schedule
                                                        )

                                                        computedSchedulesState.value = computedSchedulesState.value
                                                            .plus(computedSchedule)
                                                    }
                                                } else {
                                                    // TODO show as dialog
                                                    val throwable = result.exceptionOrNull()
                                                    println("Error: $throwable")
                                                }

                                                loading.value = false
                                            }
                                        },
                                        content = {
                                            if (loading.value) {
                                                CircularProgressIndicator(Modifier.size(16.dp))
                                            } else {
                                                Text("Run")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
