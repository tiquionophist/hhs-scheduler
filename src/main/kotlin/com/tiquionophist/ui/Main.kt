package com.tiquionophist.ui

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
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
import androidx.compose.material.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.scheduler.RandomizedScheduler
import com.tiquionophist.ui.common.ContentWithBottomPane
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HHS+ Scheduler",
            state = rememberWindowState(placement = WindowPlacement.Maximized)
        ) {
            DesktopMaterialTheme {
                val scheduleConfigurationState = remember {
                    mutableStateOf(
                        ScheduleConfiguration(
                            classes = 2,
                            teacherAssignments = emptyMap(),
                            subjectFrequency = emptyMap()
                        )
                    )
                }

                val verticalScrollState = rememberScrollState(0)
                val horizontalScrollState = rememberScrollState(0)

                ContentWithBottomPane(
                    content = {
                        Box {
                            Box(Modifier.verticalScroll(verticalScrollState).horizontalScroll(horizontalScrollState)) {
                                ScheduleConfigurationTable(scheduleConfigurationState)
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
                            modifier = Modifier.fillMaxWidth().background(Color.Gray).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${scheduleConfigurationState.value.classes} classes")

                            val loading = remember { mutableStateOf(false) }
                            val coroutineScope = rememberCoroutineScope()
                            Button(
                                enabled = !loading.value,
                                onClick = {
                                    loading.value = true
                                    coroutineScope.launch {
                                        val config = scheduleConfigurationState.value
                                        val scheduler = RandomizedScheduler(attemptsPerRound = 1_000, rounds = 1_000)

                                        val result = runCatching { scheduler.schedule(config) }
                                        if (result.isSuccess) {
                                            val schedule = result.getOrThrow()
                                            println("Scheduled: $schedule")
                                        } else {
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
                )
            }
        }
    }
}
