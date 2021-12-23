package com.tiquionophist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.io.SaveFileIO
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
fun FrameWindowScope.MenuBar() {
    val throwableState = remember { mutableStateOf<Throwable?>(null) }

    throwableState.value?.let { throwable ->
        ErrorDialog(
            throwable = throwable,
            onClose = { throwableState.value = null }
        )
    }

    MenuBar {
        Menu("File") {
            val tint = ThemeColors.current.selected
            Item(
                text = "Save configuration",
                shortcut = KeyShortcut(Key.S, ctrl = true),
                onClick = {
                    FilePicker.save()?.let { file ->
                        val result = runCatching { GlobalState.scheduleConfiguration.save(file) }

                        if (result.isSuccess) {
                            GlobalState.currentNotification = Notification(
                                title = "Configuration saved",
                                message = "Successfully saved configuration to ${file.canonicalPath}.",
                                iconFilename = "done",
                                iconTint = tint,
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
                            GlobalState.scheduleConfiguration = result.getOrThrow()
                        } else {
                            throwableState.value = result.exceptionOrNull()
                        }
                    }
                },
            )

            Separator()

            Item(
                text = "Import configuration from saved game",
                onClick = {
                    FilePicker.load(fileFilter = FilePicker.gameSaveFileFilter)?.let { file ->
                        // TODO add loading dialog while the file is being read and parsed
                        val result = runCatching { SaveFileIO.read(file).toScheduleConfiguration() }

                        if (result.isSuccess) {
                            GlobalState.scheduleConfiguration = result.getOrThrow()
                        } else {
                            throwableState.value = result.exceptionOrNull()
                        }
                    }
                }
            )
        }

        Menu("Edit") {
            CheckboxItem(
                text = "Include Lexville teachers",
                checked = GlobalState.showLexvilleTeachers,
                onCheckedChange = { GlobalState.showLexvilleTeachers = it }
            )

            Item(
                text = "Add custom teacher",
                shortcut = KeyShortcut(Key.Equals, ctrl = true),
                onClick = { CustomTeacherDialogHandler.visible = true },
            )
        }

        Menu("About") {
            Item(
                text = "HHS+ Scheduler version ${appVersion ?: "unknown"}",
                enabled = false,
                onClick = {},
            )

            // remember since this is unlikely to change between recompositions
            val browseSupported = remember {
                Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
            }

            Item(
                text = "View project on GitHub",
                enabled = browseSupported && githubUrl != null,
                onClick = {
                    githubUrl?.let { Desktop.getDesktop().browse(URI(it)) }
                }
            )
        }
    }
}
