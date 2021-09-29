package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ui.common.ConfirmationDialog
import com.tiquionophist.ui.common.IconAndTextButton
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.topBorder

/**
 * Row of scheduling-wide settings, placed at the bottom of the window.
 */
@Composable
fun SettingsPane(
    lightModeState: MutableState<Boolean>,
    scheduleConfigurationState: MutableState<ScheduleConfiguration>,
    onComputedSchedule: (ComputedSchedule) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().topBorder().padding(Dimens.SPACING_2),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_3)
        ) {
            IconButton(onClick = { lightModeState.value = !lightModeState.value }) {
                val filename = if (lightModeState.value) "light_mode" else "dark_mode"
                Image(
                    painter = painterResource("icons/$filename.svg"),
                    contentDescription = "Light/dark mode",
                    colorFilter = ColorFilter.tint(LocalContentColor.current),
                    alpha = LocalContentAlpha.current,
                )
            }

            val confirmDialogVisible = remember { mutableStateOf(false) }
            if (confirmDialogVisible.value) {
                ConfirmationDialog(
                    windowTitle = "Clear schedule",
                    message = "Reset schedule configuration?",
                    acceptText = "Clear",
                    onAccept = {
                        scheduleConfigurationState.value = ScheduleConfiguration.EMPTY
                        confirmDialogVisible.value = false
                    },
                    onDecline = {
                        confirmDialogVisible.value = false
                    }
                )
            }

            IconAndTextButton(
                text = "Clear schedule",
                iconFilename = "delete",
                onClick = { confirmDialogVisible.value = true }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_1),
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

        RunScheduleButton(
            configuration = scheduleConfigurationState.value,
            onComputedSchedule = onComputedSchedule
        )
    }
}
