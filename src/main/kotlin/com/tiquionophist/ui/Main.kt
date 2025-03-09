package com.tiquionophist.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tiquionophist.Res
import com.tiquionophist.app_icon
import com.tiquionophist.ui.common.NotificationContainer
import com.tiquionophist.ui.common.fillMaxHeightVerticalScroll
import com.tiquionophist.ui.common.fillMaxWidthHorizontalScroll
import org.jetbrains.compose.resources.painterResource

// TODO investigate text field focus (cursor remains after unfocused)
@ExperimentalMaterialApi
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HHS+ Scheduler",
            icon = painterResource(Res.drawable.app_icon),
            state = rememberWindowState(placement = WindowPlacement.Maximized),
        ) {
            ThemeColors.apply(light = ApplicationPreferences.lightMode) {
                Dimens.apply {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                        var addingCustomTeacher by remember { mutableStateOf(false) }

                        MenuBar(
                            onAddCustomTeacher = { addingCustomTeacher = true },
                        )

                        if (addingCustomTeacher) {
                            AddCustomTeacherDialog(
                                onClose = { addingCustomTeacher = false },
                            )
                        }

                        ScheduleWindowHandler.content()

                        Surface {
                            MainContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainContent() {
    Column {
        Row(modifier = Modifier.weight(1f)) {
            NotificationContainer(modifier = Modifier.weight(1f)) {
                Box(contentAlignment = Alignment.Center) {
                    val verticalScrollState = rememberScrollState()
                    val horizontalScrollState = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .fillMaxHeightVerticalScroll(verticalScrollState)
                            .fillMaxWidthHorizontalScroll(horizontalScrollState),
                    ) {
                        ScheduleConfigurationTable()
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
            }

            StatsPane()
        }

        SettingsPane()
    }
}
