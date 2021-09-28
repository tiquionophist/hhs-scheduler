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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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

/**
 * Wraps a [schedule] computed from a certain [configuration], along with an [index] incremented each time a new object
 * is created.
 */
data class ComputedSchedule(
    val configuration: ScheduleConfiguration,
    val schedule: Schedule,
    val index: Int = indexCounter++
) {
    companion object {
        private var indexCounter: Int = 0
    }
}

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
                        shortcut = KeyShortcut(Key.Equals, ctrl = true),
                        onClick = {
                            customTeacherDialogVisibleState.value = true
                        }
                    )
                }
            }

            DesktopMaterialTheme(colors = Colors.materialColors(light = !darkThemeState.value)) {
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
                                modifier = Modifier.fillMaxWidth().topBorder().padding(Dimens.SPACING_2),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_3)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
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
                                            min = 1,
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_3)
                                ) {
                                    val validationError = runCatching { scheduleConfigurationState.value.verify() }
                                        .exceptionOrNull()
                                        ?.message

                                    if (validationError != null) {
                                        BoxWithTooltip(
                                            tooltip = {
                                                Surface(modifier = Modifier.shadow(Dimens.SHADOW_ELEVATION)) {
                                                    Text(
                                                        text = validationError,
                                                        modifier = Modifier.padding(Dimens.SPACING_2),
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

                                                val result = runCatching {
                                                    scheduler.schedule(config)?.also { it.verify(config) }
                                                }
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
                                                CircularProgressIndicator(Modifier.size(Dimens.SPACING_3))
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
