package com.tiquionophist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import java.util.Properties

private val appProperties: Properties? by lazy {
    ApplicationPreferences::class.java.classLoader.getResourceAsStream("app.properties")
        ?.use { Properties().apply { load(it) } }
}

private val githubUrl: String? by lazy { appProperties?.getProperty("github") }
private val appVersion: String? by lazy { appProperties?.getProperty("version") }

/**
 * The application menu bar.
 */
@Composable
fun FrameWindowScope.MenuBar(
    scheduleConfigurationState: MutableState<ScheduleConfiguration>,
    notificationState: MutableState<Notification?>,
    showLexvilleTeachersState: MutableState<Boolean>,
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
            CheckboxItem(
                text = "Include Lexville teachers",
                checked = showLexvilleTeachersState.value,
                onCheckedChange = { checked -> showLexvilleTeachersState.value = checked }
            )

            Item(
                text = "Add custom teacher",
                shortcut = KeyShortcut(Key.Equals, ctrl = true),
                onClick = showCustomTeacherDialog,
            )
        }

        Menu("About") {
            Item(
                text = "HHS+ Scheduler version ${appVersion ?: "unknown"}",
                enabled = false,
                onClick = {},
            )

            Item(
                text = "View project on GitHub",
                enabled = Desktop.isDesktopSupported() &&
                        Desktop.getDesktop().isSupported(Desktop.Action.BROWSE) &&
                        githubUrl != null,
                onClick = {
                    githubUrl?.let { Desktop.getDesktop().browse(URI(it)) }
                }
            )
        }
    }
}
