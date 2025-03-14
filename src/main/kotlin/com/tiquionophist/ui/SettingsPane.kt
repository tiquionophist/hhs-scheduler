package com.tiquionophist.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.tiquionophist.Res
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ic_arrow_drop_down
import com.tiquionophist.ic_dark_mode
import com.tiquionophist.ic_delete
import com.tiquionophist.ic_error
import com.tiquionophist.ic_light_mode
import com.tiquionophist.ic_settings
import com.tiquionophist.scheduler.ClassroomFillOrder
import com.tiquionophist.ui.common.CheckboxWithLabel
import com.tiquionophist.ui.common.ConfirmationDialog
import com.tiquionophist.ui.common.IconAndTextButton
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.Tooltip
import com.tiquionophist.ui.common.TooltipSurface
import com.tiquionophist.util.prettyName
import org.jetbrains.compose.resources.painterResource

/**
 * Row of scheduling-wide settings, placed at the bottom of the window.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsPane(modifier: Modifier = Modifier) {
    Surface(color = ThemeColors.current.surface3, modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.SPACING_3, vertical = Dimens.SPACING_2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_3)
            ) {
                val config = GlobalState.scheduleConfiguration

                IconButton(onClick = { ApplicationPreferences.lightMode = !ApplicationPreferences.lightMode }) {
                    Image(
                        painter = painterResource(
                            if (ApplicationPreferences.lightMode) {
                                Res.drawable.ic_light_mode
                            } else {
                                Res.drawable.ic_dark_mode
                            }
                        ),
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
                    iconRes = Res.drawable.ic_delete,
                    onClick = { confirmDialogVisible.value = true }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_1),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Classes:", maxLines = 2)

                    NumberPicker(
                        value = config.classes,
                        onValueChange = { newValue ->
                            // if we're adding new classes, we need to add new subject frequencies. Copy them from the
                            // currently selected class frequencies
                            val currentFrequency = config.subjectFrequency[GlobalState.currentClassIndex ?: 0]
                            val missingFrequencies = List((newValue - config.classes).coerceAtLeast(0)) {
                                currentFrequency
                            }

                            val droppedFrequencies = (config.classes - newValue).coerceAtLeast(0)

                            GlobalState.currentClassIndex = GlobalState.currentClassIndex?.coerceAtMost(newValue - 1)
                            GlobalState.scheduleConfiguration = config.copy(
                                classes = newValue,
                                subjectFrequency = config.subjectFrequency
                                    .dropLast(droppedFrequencies)
                                    .plus(missingFrequencies),
                            )
                        },
                        min = 1,
                    )
                }

                Column {
                    Tooltip(
                        "Whether to allow the same subject (excluding free periods) to be scheduled for the same " +
                            "class more than once on the same day."
                    ) {
                        CheckboxWithLabel(
                            checked = config.allowSameDaySubjectRepeats,
                            padding = PaddingValues(all = Dimens.SPACING_1),
                            onCheckedChange = {
                                GlobalState.scheduleConfiguration = config.copy(
                                    allowSameDaySubjectRepeats = !config.allowSameDaySubjectRepeats,
                                )
                            }
                        ) {
                            Text(text = "Allow same-day subject repeats", maxLines = 2)
                        }
                    }

                    Tooltip(
                        "Whether to allow the same subject (excluding free periods) to be scheduled for the same " +
                            "class back-to-back on the same day. Does not affect scheduling between the end of the " +
                            "previous day and the first period of the following day."
                    ) {
                        CheckboxWithLabel(
                            checked = config.allowSubsequentSubjectsRepeats && config.allowSameDaySubjectRepeats,
                            enabled = config.allowSameDaySubjectRepeats,
                            padding = PaddingValues(all = Dimens.SPACING_1),
                            onCheckedChange = {
                                GlobalState.scheduleConfiguration = config.copy(
                                    allowSubsequentSubjectsRepeats = !config.allowSubsequentSubjectsRepeats,
                                )
                            }
                        ) {
                            Text(text = "Allow subsequent subject repeats", maxLines = 2)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_1)) {
                    Text(text = "Classroom selection:", maxLines = 2)

                    val dropdownExpanded = remember { mutableStateOf(false) }
                    TextButton(
                        onClick = { dropdownExpanded.value = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current)
                    ) {
                        Text(text = config.classroomFillOrder.prettyName, maxLines = 2)

                        Image(
                            painter = painterResource(Res.drawable.ic_arrow_drop_down),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(LocalContentColor.current)
                        )

                        DropdownMenu(
                            expanded = dropdownExpanded.value,
                            onDismissRequest = { dropdownExpanded.value = false }
                        ) {
                            ClassroomFillOrder.entries.forEach { classroomFillOrder ->
                                DropdownMenuItem(
                                    onClick = {
                                        GlobalState.scheduleConfiguration = config.copy(
                                            classroomFillOrder = classroomFillOrder,
                                        )
                                        dropdownExpanded.value = false
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = Dimens.SPACING_2),
                                        verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_1),
                                    ) {
                                        Text(text = classroomFillOrder.prettyName)

                                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                                            Text(
                                                text = classroomFillOrder.description,
                                                fontSize = Dimens.FONT_SMALL,
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                        painter = painterResource(Res.drawable.ic_settings),
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
                            painter = painterResource(Res.drawable.ic_error),
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
