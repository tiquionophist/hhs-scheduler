package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.core.Scheduler
import com.tiquionophist.scheduler.ExhaustiveScheduler
import com.tiquionophist.scheduler.OptaScheduler
import com.tiquionophist.scheduler.RandomizedScheduler
import com.tiquionophist.ui.common.ContentWithPane
import com.tiquionophist.ui.common.NumberPicker
import com.tiquionophist.ui.common.PaneDirection
import com.tiquionophist.ui.common.topBorder
import com.tiquionophist.util.prettyName

private val DIALOG_WIDTH = 800.dp
private val DIALOG_HEIGHT = 600.dp

sealed class SchedulerSettings(val type: SchedulerType) {
    abstract fun create(): Scheduler

    data class Exhaustive(
        val fillOrder: RandomizedScheduler.ScheduleFillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS,
        val maxAttemptsEnabled: Boolean = false,
        val maxAttempts: Int = 1_000,
    ) : SchedulerSettings(type = SchedulerType.EXHAUSTIVE) {
        override fun create(): Scheduler {
            return ExhaustiveScheduler(
                fillOrder = fillOrder,
                maxAttempts = maxAttempts.takeIf { maxAttemptsEnabled }
            )
        }
    }

    data class Randomized(
        val fillOrder: RandomizedScheduler.ScheduleFillOrder = RandomizedScheduler.ScheduleFillOrder.CLASS_BY_CLASS,
        val limitAttemptsPerRound: Boolean = true,
        val attemptsPerRound: Int = 1_000,
        val rounds: Int = 1_000,
        val startingRandomSeed: Int = 0,
    ) : SchedulerSettings(type = SchedulerType.RANDOMIZED) {
        override fun create(): Scheduler {
            return RandomizedScheduler(
                fillOrder = fillOrder,
                attemptsPerRound = attemptsPerRound.takeIf { limitAttemptsPerRound },
                rounds = if (limitAttemptsPerRound) rounds else 1,
                randomSeed = { (startingRandomSeed + it).toLong() }
            )
        }
    }

    data class Opta(
        val timeoutSeconds: Int = 10,
        val timeoutSecondsEnabled: Boolean = false
    ) : SchedulerSettings(type = SchedulerType.OPTA) {
        override fun create(): Scheduler {
            return OptaScheduler(timeoutSeconds = timeoutSeconds.takeIf { timeoutSecondsEnabled })
        }
    }

    companion object {
        val default: SchedulerSettings = defaultSettingsFor(SchedulerType.RANDOMIZED)

        fun defaultSettingsFor(type: SchedulerType): SchedulerSettings {
            return when (type) {
                SchedulerType.EXHAUSTIVE -> Exhaustive()
                SchedulerType.RANDOMIZED -> Randomized()
                SchedulerType.OPTA -> Opta()
            }
        }
    }
}

enum class SchedulerType(val buttonName: String, val description: String) {
    EXHAUSTIVE(
        buttonName = "Exhaustive",
        description = "An exhaustive search of all possible schedules, filling out each schedule based on the given " +
                "fill order until no possible lesson can be added. Tends to be very slow for large search spaces."
    ),
    RANDOMIZED(
        buttonName = "Randomized",
        description = "A randomized search through all possible schedules, filling out each schedule with a random " +
                "valid lesson in the given fill order until no more lessons can be added. Tends to have best overall " +
                "performance when attempts per round are limited."
    ),
    OPTA(
        buttonName = "OptaPlanner",
        description = "Uses commercial constraint satisfaction solver OptaPlanner. May scale the best to extremely " +
                "large problems or the most flexibly to add soft constraints in the future but struggles to find a " +
                "perfect solution for even medium-size problems."
    ),
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
        Surface {
            val settingsState = remember { mutableStateOf(initialSchedulerSettings) }
            val currentSettings = settingsState.value

            ContentWithPane(
                direction = PaneDirection.BOTTOM,
                content = {
                    Column {
                        Row(Modifier.fillMaxWidth()) {
                            SchedulerType.values().forEach { type ->
                                Button(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    onClick = { settingsState.value = SchedulerSettings.defaultSettingsFor(type) },
                                    shape = RectangleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (type == currentSettings.type) {
                                            MaterialTheme.colors.primary
                                        } else {
                                            MaterialTheme.colors.background
                                        }
                                    )
                                ) {
                                    Text(type.buttonName)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.padding(Dimens.SPACING_2),
                            verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)
                        ) {
                            Text(currentSettings.type.description)

                            Spacer(Modifier.height(Dimens.SPACING_2))

                            Divider()

                            when (currentSettings) {
                                is SchedulerSettings.Exhaustive -> ExhaustiveSettings(currentSettings, settingsState)
                                is SchedulerSettings.Randomized -> RandomizedSettings(currentSettings, settingsState)
                                is SchedulerSettings.Opta -> OptaSettings(currentSettings, settingsState)
                            }
                        }
                    }
                },
                pane = {
                    Row(
                        modifier = Modifier.topBorder().padding(Dimens.SPACING_2).fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { onClose(settingsState.value) }) {
                            Text("Apply")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun ExhaustiveSettings(
    settings: SchedulerSettings.Exhaustive,
    settingsState: MutableState<SchedulerSettings>
) {
    FillOrderSelector(
        fillOrder = settings.fillOrder,
        onFillOrderSelected = {
            settingsState.value = settings.copy(fillOrder = it)
        }
    )

    Divider()

    SettingWithDescription(
        description = "Max attempts limits the number of end states (when adding the next lesson is impossible) that " +
                "the scheduler will reach before aborting. If unchecked, the scheduler runs until it has exhausted " +
                "all possible end states, and is guaranteed to find a solution if one exists."
    ) {
        Checkbox(
            checked = settings.maxAttemptsEnabled,
            onCheckedChange = {
                settingsState.value = settings.copy(maxAttemptsEnabled = it)
            }
        )

        Text("Max attempts:")

        NumberPicker(
            value = settings.maxAttempts,
            enabled = settings.maxAttemptsEnabled,
            textFieldWidth = Dimens.NumberPicker.LARGE_TEXT_FIELD_WIDTH,
            onValueChange = {
                settingsState.value = settings.copy(maxAttempts = it)
            }
        )
    }
}

@Composable
private fun RandomizedSettings(
    settings: SchedulerSettings.Randomized,
    settingsState: MutableState<SchedulerSettings>
) {
    FillOrderSelector(
        fillOrder = settings.fillOrder,
        onFillOrderSelected = {
            settingsState.value = settings.copy(fillOrder = it)
        }
    )

    Divider()

    SettingWithDescription(
        description = "The RNG seed used to randomly choose the next lesson assignment. Unlikely to have a " +
                "noticeable effect on performance. Each new round of processing uses the next random seed (e.g. 0, " +
                "1, 2, 3, ...)."
    ) {
        Text("Random seed:")

        NumberPicker(
            value = settings.startingRandomSeed,
            textFieldWidth = Dimens.NumberPicker.LARGE_TEXT_FIELD_WIDTH,
            onValueChange = {
                settingsState.value = settings.copy(startingRandomSeed = it)
            }
        )
    }

    Divider()

    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_1)) {
        StandardRow {
            Checkbox(
                checked = settings.limitAttemptsPerRound,
                onCheckedChange = { settingsState.value = settings.copy(limitAttemptsPerRound = it) }
            )

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
                    "algorithm starts again from scratch with the next random seed. This is very effective in " +
                    "avoiding local maxima (i.e. when we have all but the last period scheduled), but enabling it " +
                    "does mean it is possible that the algorithm is not guaranteed to find a solution if one exists."
        )
    }
}

@Composable
private fun OptaSettings(
    settings: SchedulerSettings.Opta,
    settingsState: MutableState<SchedulerSettings>
) {
    SettingWithDescription(
        description = "A maximum time limit on the Opta runtime."
    ) {
        Checkbox(
            checked = settings.timeoutSecondsEnabled,
            onCheckedChange = {
                settingsState.value = settings.copy(timeoutSecondsEnabled = it)
            }
        )

        Text("Timeout (seconds):")

        NumberPicker(
            value = settings.timeoutSeconds,
            enabled = settings.timeoutSecondsEnabled,
            textFieldWidth = Dimens.NumberPicker.LARGE_TEXT_FIELD_WIDTH,
            min = 1,
            onValueChange = {
                settingsState.value = settings.copy(timeoutSeconds = it)
            }
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
                    contentDescription = null
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
