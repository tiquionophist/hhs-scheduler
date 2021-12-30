package com.tiquionophist.ui

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.tiquionophist.ui.common.ContentWithPane
import com.tiquionophist.ui.common.NotificationContainer
import com.tiquionophist.ui.common.PaneDirection
import com.tiquionophist.ui.common.fillMaxHeightVerticalScroll
import com.tiquionophist.ui.common.fillMaxWidthHorizontalScroll

// TODO investigate text field focus (cursor remains after unfocused)
@ExperimentalMaterialApi
fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "HHS+ Scheduler",
            icon = painterResource("app_icon.png"),
            state = rememberWindowState(placement = WindowPlacement.Maximized),
        ) {
            ThemeColors.apply(light = ApplicationPreferences.lightMode) {
                Dimens.apply {
                    CompositionLocalProvider(
                        LocalMinimumTouchTargetEnforcement provides false
                    ) {
                        MenuBar()
                        CustomTeacherDialogHandler.content()
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
    // null when all classes are the same, otherwise the index of the class being controlled now
    val classIndexState = remember { mutableStateOf<Int?>(null) }

    ContentWithPane(
        direction = PaneDirection.BOTTOM,
        content = {
            ContentWithPane(
                direction = PaneDirection.RIGHT,
                content = {
                    NotificationContainer {
                        Box(contentAlignment = Alignment.Center) {
                            val verticalScrollState = rememberScrollState()
                            val horizontalScrollState = rememberScrollState()

                            Box(
                                modifier = Modifier
                                    .fillMaxHeightVerticalScroll(verticalScrollState)
                                    .fillMaxWidthHorizontalScroll(horizontalScrollState)
                            ) {
                                ScheduleConfigurationTable(classIndexState = classIndexState)
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
                },
                pane = { StatsPane(classIndex = classIndexState.value) }
            )
        },
        pane = { SettingsPane(classIndexState = classIndexState) }
    )
}
