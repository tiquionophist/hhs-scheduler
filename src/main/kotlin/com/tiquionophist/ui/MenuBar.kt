package com.tiquionophist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ui.common.FilePicker

/**
 * The application menu bar.
 */
@ExperimentalComposeUiApi
@Composable
fun FrameWindowScope.MenuBar(
    scheduleConfigurationState: MutableState<ScheduleConfiguration>,
    showCustomTeacherDialog: () -> Unit
) {
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
                },
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
    }
}
