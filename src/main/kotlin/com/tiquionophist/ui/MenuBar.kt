package com.tiquionophist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ui.common.ErrorDialog
import com.tiquionophist.ui.common.FilePicker
import com.tiquionophist.ui.common.Notification
import java.awt.Desktop
import java.net.URI

private const val GITHUB_URL = "https://github.com/tiquionophist/hhs-scheduler"
private const val APPLICATION_VERSION = "1.0" // TODO integrate with gradle

/**
 * The application menu bar.
 */
@ExperimentalComposeUiApi
@Composable
fun FrameWindowScope.MenuBar(
    scheduleConfigurationState: MutableState<ScheduleConfiguration>,
    notificationState: MutableState<Notification?>,
    showCustomTeacherDialog: () -> Unit
) {
    val throwableState = remember { mutableStateOf<Throwable?>(null) }

    throwableState.value?.let { throwable ->
        ErrorDialog(
            throwable = throwable,
            onClose = { throwableState.value = null }
        )
    }

    MenuBar {
        Menu("File") {
            Item(
                text = "Save configuration",
                shortcut = KeyShortcut(Key.S, ctrl = true),
                onClick = {
                    FilePicker.save()?.let { file ->
                        val result = runCatching { scheduleConfigurationState.value.save(file) }

                        if (result.isSuccess) {
                            notificationState.value = Notification(
                                title = "Configuration saved",
                                message = "Successfully saved configuration to ${file.canonicalPath}.",
                                iconFilename = "done",
                                iconTint = Colors.SELECTED,
                            )
                        } else {
                            throwableState.value = result.exceptionOrNull()
                        }
                    }
                },
            )

            Item(
                text = "Load configuration",
                shortcut = KeyShortcut(Key.O, ctrl = true),
                onClick = {
                    FilePicker.load()?.let { file ->
                        val result = runCatching { ScheduleConfiguration.loadOrError(file) }

                        if (result.isSuccess) {
                            scheduleConfigurationState.value = result.getOrThrow()
                        } else {
                            throwableState.value = result.exceptionOrNull()
                        }
                    }
                },
            )
        }

        Menu("Edit") {
            Item(
                text = "Add custom teacher",
                shortcut = KeyShortcut(Key.Equals, ctrl = true),
                onClick = showCustomTeacherDialog,
            )
        }

        Menu("About") {
            Item(
                text = "HHS+ Scheduler version $APPLICATION_VERSION",
                enabled = false,
                onClick = {},
            )

            Item(
                text = "View project on GitHub",
                enabled = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE),
                onClick = {
                    Desktop.getDesktop().browse(URI(GITHUB_URL))
                }
            )
        }
    }
}
