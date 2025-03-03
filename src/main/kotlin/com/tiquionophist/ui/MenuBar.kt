package com.tiquionophist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.tiquionophist.Res
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ic_done
import com.tiquionophist.io.SaveFileIO
import com.tiquionophist.ui.common.ConfirmationDialog
import com.tiquionophist.ui.common.ErrorDialog
import com.tiquionophist.ui.common.FilePicker
import com.tiquionophist.ui.common.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val confirmConfigurationState = remember { mutableStateOf(false) }

    throwableState.value?.let { throwable ->
        ErrorDialog(
            throwable = throwable,
            onClose = { throwableState.value = null }
        )
    }

    if (confirmConfigurationState.value) {
        ConfirmationDialog(
            windowTitle = "Confirm imported schedule configuration",
            prompt = "The imported schedule has different subject frequencies across classes so per-class " +
                "scheduling has been enabled. Disable it to have subjects taught the same amount across all classes.",
            cancelText = null,
            onAccept = { confirmConfigurationState.value = false },
            onDecline = { confirmConfigurationState.value = false },
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
                                iconRes = Res.drawable.ic_done,
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
                        GlobalState.currentNotification = Notification(
                            title = "Configuration loaded",
                            message = "Successfully loaded configuration from ${file.canonicalPath}.",
                            iconRes = Res.drawable.ic_done,
                            iconTint = tint,
                        )

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

            val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
            Item(
                text = "Import configuration from saved game",
                onClick = {
                    FilePicker.load(fileFilter = FilePicker.gameSaveFileFilter)?.let { file ->
                        val notification = Notification(
                            title = "Importing save file",
                            message = "Reading save file ${file.canonicalPath}...",
                            progress = 0.0,
                            duration = null,
                        )

                        GlobalState.currentNotification = notification

                        coroutineScope.launch {
                            val result = runCatching {
                                val saveData = SaveFileIO.read(
                                    file = file,
                                    onReadSaveFile = {
                                        GlobalState.currentNotification = notification.copy(
                                            progress = 0.25,
                                            message = "Decoding and unzipping save data..."
                                        )
                                    },
                                    onDecodeAndUnzip = {
                                        GlobalState.currentNotification = notification.copy(
                                            progress = 0.5,
                                            message = "Parsing save data..."
                                        )
                                    }
                                )

                                GlobalState.currentNotification = notification.copy(
                                    progress = 0.75,
                                    message = "Importing save data..."
                                )

                                saveData.toScheduleConfiguration()
                            }

                            if (result.isSuccess) {
                                GlobalState.currentNotification = Notification(
                                    title = "Configuration imported",
                                    message = "Successfully imported configuration from ${file.canonicalPath}.",
                                    iconRes = Res.drawable.ic_done,
                                    iconTint = tint,
                                )

                                val configuration = result.getOrThrow()
                                val sameSubjectFrequencyInAllClasses = configuration.subjectFrequency.toSet().size == 1

                                GlobalState.scheduleConfiguration = configuration
                                GlobalState.currentClassIndex = if (sameSubjectFrequencyInAllClasses) null else 0

                                if (!sameSubjectFrequencyInAllClasses) {
                                    confirmConfigurationState.value = true
                                }
                            } else {
                                throwableState.value = result.exceptionOrNull()
                            }
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
