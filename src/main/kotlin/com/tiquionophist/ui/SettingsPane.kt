package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.topBorder

/**
 * Row of scheduling-wide settings, placed at the bottom of the window.
 */
@ExperimentalComposeUiApi
@Composable
fun SettingsPane(
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
            // TODO add confirm dialog to clear configuration
            Button(
                onClick = {
                    scheduleConfigurationState.value = ScheduleConfiguration(classes = 2)
                }
            ) {
                Image(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Reset",
                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                )

                Text("Clear schedule")
            }

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
