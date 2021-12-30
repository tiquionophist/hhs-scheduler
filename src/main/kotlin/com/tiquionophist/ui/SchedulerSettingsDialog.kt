package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.core.Scheduler
import com.tiquionophist.scheduler.ExhaustiveScheduler
import com.tiquionophist.scheduler.RandomizedScheduler
import com.tiquionophist.ui.common.CheckboxWithLabel
import com.tiquionophist.ui.common.ContentWithPane
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.PaneDirection
import com.tiquionophist.ui.common.topBorder
import com.tiquionophist.util.prettyName

private val DIALOG_WIDTH = 800.dp
private val DIALOG_HEIGHT = 700.dp

data class SchedulerSettings(
    val fillOrder: RandomizedScheduler.ScheduleFillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS,
    val exhaustive: Boolean = false,
    val limitAttemptsPerRound: Boolean = true,
    val attemptsPerRound: Int = 1_000,
    val rounds: Int = 1_000,
    val customRandomSeedEnabled: Boolean = false,
    val startingRandomSeed: Int = 0,
) {
    fun create(): Scheduler {
        return if (exhaustive) {
            ExhaustiveScheduler(fillOrder = fillOrder)
        } else {
            RandomizedScheduler(
                fillOrder = fillOrder,
                attemptsPerRound = attemptsPerRound.takeIf { limitAttemptsPerRound },
                rounds = if (limitAttemptsPerRound) rounds else 1,
                randomSeed = if (customRandomSeedEnabled) {
                    { (startingRandomSeed + it).toLong() }
                } else {
                    { null }
                },
            )
        }
    }
}

@Composable
fun SchedulerSettingsDialog(
    initialSchedulerSettings: SchedulerSettings,
    onClose: (SchedulerSettings?) -> Unit
) {
    Dialog(
        state = rememberDialogState(
            size = DpSize(width = DIALOG_WIDTH, height = DIALOG_HEIGHT)
        ),
        title = "Scheduler settings",
        onCloseRequest = { onClose(null) },
    ) {
        Surface(elevation = Dimens.TOOLTIP_ELEVATION) {
            val settingsState = remember { mutableStateOf(initialSchedulerSettings) }

            ContentWithPane(
                direction = PaneDirection.BOTTOM,
                content = {
                    val scrollState = rememberScrollState()
                    Box {
                        Box(modifier = Modifier.verticalScroll(scrollState)) {
                            Column(
                                modifier = Modifier.padding(Dimens.SPACING_2),
                                verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
                            ) {
                                Text(
                                    "Scheduler settings control the algorithm used to generate a schedule. This " +
                                            "should generally work out of the box, but allows for some tuning for " +
                                            "advanced users."
                                )

                                Divider()

                                SchedulerSettings(settingsState)
                            }
                        }

                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scrollState),
                        )
                    }
                },
                pane = {
                    Row(
                        modifier = Modifier.topBorder().padding(Dimens.SPACING_2).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Button(
                            enabled = settingsState.value != SchedulerSettings(),
                            onClick = { settingsState.value = SchedulerSettings() },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                            )

                            Spacer(Modifier.width(Dimens.SPACING_2))

                            Text("Reset to defaults")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)) {
                            TextButton(onClick = { onClose(null) }) {
                                Text("Cancel")
                            }

                            Button(onClick = { onClose(settingsState.value) }) {
                                Text("Apply")
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun SchedulerSettings(settingsState: MutableState<SchedulerSettings>) {
    val settings = settingsState.value

    FillOrderSelector(
        fillOrder = settings.fillOrder,
        onFillOrderSelected = {
            settingsState.value = settings.copy(fillOrder = it)
        }
    )

    Divider()

    SettingWithDescription(
        description = "Exhaustive scheduling disables randomness and guarantees that a solution will be found if one " +
                "exists. Tends to run much more slowly than a non-exhaustive search and produce schedules that are " +
                "more uniform (i.e. scheduling the same subject back-to-back before moving on to the next subject)."
    ) {
        CheckboxWithLabel(
            checked = settings.exhaustive,
            onCheckedChange = {
                settingsState.value = settings.copy(exhaustive = it)
            }
        ) {
            Text("Exhaustive")
        }
    }

    if (!settings.exhaustive) {
        RandomizedSettings(settingsState)
    }
}

@Composable
private fun RandomizedSettings(settingsState: MutableState<SchedulerSettings>) {
    val settings = settingsState.value

    Divider()

    SettingWithDescription(
        description = "The RNG seed used to randomly choose the next lesson assignment. Unlikely to have a " +
                "noticeable effect on performance. If disabled, uses the system time so that re-runs will have a " +
                "new seed."
    ) {
        val enabled = settings.customRandomSeedEnabled

        CheckboxWithLabel(
            checked = enabled,
            onCheckedChange = { settingsState.value = settings.copy(customRandomSeedEnabled = it) }
        ) {
            Text("Custom random seed")
        }

        NumberPicker(
            value = settings.startingRandomSeed,
            enabled = enabled,
            textFieldWidth = Dimens.NumberPicker.LARGE_TEXT_FIELD_WIDTH,
            onValueChange = {
                settingsState.value = settings.copy(startingRandomSeed = it)
            }
        )
    }

    Divider()

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_1)) {
        CheckboxWithLabel(
            checked = settings.limitAttemptsPerRound,
            onCheckedChange = { settingsState.value = settings.copy(limitAttemptsPerRound = it) }
        ) {
            Text("Limit attempts per round")
        }

        StandardRow {
            Text("Attempts per round:")

            NumberPicker(
                value = settings.attemptsPerRound,
                enabled = settings.limitAttemptsPerRound,
                textFieldWidth = Dimens.NumberPicker.LARGE_TEXT_FIELD_WIDTH,
                min = 1,
                onValueChange = { settingsState.value = settings.copy(attemptsPerRound = it) }
            )
        }

        StandardRow {
            Text("Rounds:")

            NumberPicker(
                value = settings.rounds,
                enabled = settings.limitAttemptsPerRound,
                textFieldWidth = Dimens.NumberPicker.LARGE_TEXT_FIELD_WIDTH,
                min = 1,
                onValueChange = { settingsState.value = settings.copy(rounds = it) }
            )
        }

        Text(
            "Limit the number of end states (when no more lessons can be added to the schedule) reached before the " +
                    "algorithm starts again from scratch with the next random seed. Limiting attempts per round is " +
                    "very effective in avoiding local maxima (i.e. when we have all but the last period scheduled). " +
                    "Limiting the total number of rounds prevents the algorithm from running forever; if many runs " +
                    "fail to find a solution it is likely that one does not exist."
        )
    }
}

@Composable
private fun FillOrderSelector(
    fillOrder: RandomizedScheduler.ScheduleFillOrder,
    onFillOrderSelected: (RandomizedScheduler.ScheduleFillOrder) -> Unit
) {
    SettingWithDescription(
        description = "The fill order determines whether each class is filled out for each period in the week before " +
                "moving onto the next class, or whether each period is filled out for each class before moving onto " +
                "the next period. This has complicated implications on performance of the scheduler."
    ) {
        Text("Fill order:")

        Box {
            val dropdownExpanded = remember { mutableStateOf(false) }
            TextButton(
                onClick = { dropdownExpanded.value = true },
                colors = ButtonDefaults.textButtonColors(contentColor = LocalContentColor.current)
            ) {
                Text(fillOrder.prettyName)

                Image(
                    painter = painterResource("icons/arrow_drop_down.svg"),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(LocalContentColor.current)
                )
            }

            DropdownMenu(
                expanded = dropdownExpanded.value,
                onDismissRequest = { dropdownExpanded.value = false }
            ) {
                RandomizedScheduler.ScheduleFillOrder.values().forEach { fillOrder ->
                    DropdownMenuItem(
                        onClick = {
                            onFillOrderSelected(fillOrder)
                            dropdownExpanded.value = false
                        }
                    ) {
                        Text(fillOrder.prettyName)
                    }
                }
            }
        }
    }
}

@Composable
private fun StandardRow(content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
        content = content
    )
}

@Composable
private fun SettingWithDescription(
    description: String,
    content: @Composable RowScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_1)) {
        StandardRow(content)
        Text(description)
    }
}
