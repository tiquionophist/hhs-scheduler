package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ui.common.CheckboxWithLabel
import com.tiquionophist.ui.common.ConfirmationDialog
import com.tiquionophist.ui.common.IconAndTextButton
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.TooltipSurface

/**
 * Row of scheduling-wide settings, placed at the bottom of the window.
 */
@Composable
fun SettingsPane() {
    Surface(color = ThemeColors.current.surface3) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Dimens.SPACING_2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_3)
            ) {
                IconButton(onClick = { ApplicationPreferences.lightMode = !ApplicationPreferences.lightMode }) {
                    val filename = if (ApplicationPreferences.lightMode) "light_mode" else "dark_mode"
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
                        prompt = "Reset schedule configuration?",
                        acceptText = "Clear",
                        onAccept = {
                            GlobalState.scheduleConfiguration = ScheduleConfiguration.EMPTY
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
                        value = GlobalState.scheduleConfiguration.classes,
                        onValueChange = { newValue ->
                            GlobalState.scheduleConfiguration = GlobalState.scheduleConfiguration.copy(
                                classes = newValue
                            )
                        },
                        min = 1,
                    )
                }

                TooltipArea(
                    tooltip = {
                        TooltipSurface {
                            Box(Modifier.padding(Dimens.SPACING_2).widthIn(max = Dimens.Dialog.MAX_TEXT_WIDTH)) {
                                Text(
                                    "Whether to allow the same subject (excluding free periods) to be scheduled for " +
                                            "the same class more than once on the same day."
                                )
                            }
                        }
                    }
                ) {
                    CheckboxWithLabel(
                        checked = GlobalState.scheduleConfiguration.allowSameDaySubjectRepeats,
                        onCheckedChange = {
                            GlobalState.scheduleConfiguration = GlobalState.scheduleConfiguration.copy(
                                allowSameDaySubjectRepeats =
                                !GlobalState.scheduleConfiguration.allowSameDaySubjectRepeats,
                            )
                        }
                    ) {
                        Text("Allow same-day subject repeats")
                    }
                }

                TooltipArea(
                    tooltip = {
                        TooltipSurface {
                            Box(Modifier.padding(Dimens.SPACING_2).widthIn(max = Dimens.Dialog.MAX_TEXT_WIDTH)) {
                                Text(
                                    "Whether to allow the same subject (excluding free periods) to be scheduled for " +
                                            "the same class back-to-back on the same day. Does not affect scheduling " +
                                            "between the end of the previous day and the first period of the " +
                                            "following day."
                                )
                            }
                        }
                    }
                ) {
                    CheckboxWithLabel(
                        checked = GlobalState.scheduleConfiguration.allowSubsequentSubjectsRepeats &&
                                GlobalState.scheduleConfiguration.allowSameDaySubjectRepeats,
                        enabled = GlobalState.scheduleConfiguration.allowSameDaySubjectRepeats,
                        onCheckedChange = {
                            GlobalState.scheduleConfiguration = GlobalState.scheduleConfiguration.copy(
                                allowSubsequentSubjectsRepeats =
                                !GlobalState.scheduleConfiguration.allowSubsequentSubjectsRepeats,
                            )
                        }
                    ) {
                        Text("Allow subsequent subject repeats")
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_3)
            ) {
                val schedulerSettingsState = remember { mutableStateOf(SchedulerSettings()) }

                val schedulerSettingsDialogVisibleState = remember { mutableStateOf(false) }
                IconButton(onClick = { schedulerSettingsDialogVisibleState.value = true }) {
                    Image(
                        painter = painterResource("icons/settings.svg"),
                        contentDescription = "Scheduler settings",
                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                        alpha = LocalContentAlpha.current,
                    )
                }

                if (schedulerSettingsDialogVisibleState.value) {
                    SchedulerSettingsDialog(initialSchedulerSettings = schedulerSettingsState.value) { newScheduler ->
                        newScheduler?.let { schedulerSettingsState.value = it }
                        schedulerSettingsDialogVisibleState.value = false
                    }
                }

                val validationErrors = remember(GlobalState.scheduleConfiguration) {
                    GlobalState.scheduleConfiguration.validationErrors()
                }

                if (validationErrors.isNotEmpty()) {
                    TooltipArea(
                        tooltip = {
                            TooltipSurface {
                                Column(
                                    modifier = Modifier.padding(Dimens.SPACING_2),
                                    verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
                                ) {
                                    validationErrors.forEach { error ->
                                        Text(error)
                                    }
                                }
                            }
                        },
                        tooltipPlacement = TooltipPlacement.CursorPoint(
                            offset = DpOffset(0.dp, Dimens.SPACING_3)
                        )
                    ) {
                        Icon(
                            painter = painterResource("icons/error.svg"),
                            contentDescription = "Error",
                            tint = MaterialTheme.colors.error,
                        )
                    }
                }

                RunScheduleButton(
                    enabled = validationErrors.isEmpty(),
                    schedulerSettings = schedulerSettingsState.value,
                )
            }
        }
    }
}
