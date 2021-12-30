package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.tiquionophist.ui.common.ErrorDialog
import com.tiquionophist.ui.common.IconAndTextButton
import com.tiquionophist.ui.common.TooltipSurface
import com.tiquionophist.ui.common.liveDurationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit

private class ErrorDialogState(
    val title: String,
    val message: String,
    val throwable: Throwable?
)

private const val SCHEDULE_NOT_FOUND_TITLE = "No schedule found"
private const val SCHEDULE_NOT_FOUND_MESSAGE =
    "No schedule could be found. This could mean that no schedule is possible for these settings, or that the " +
            "scheduler could not find it under the provided settings."

private const val EXCEPTION_TITLE = "Exception thrown!"
private const val EXCEPTION_MESSAGE =
    "An exception was thrown. Consider reporting this (with the stack trace) on the project's GitHub page or try again."

/**
 * The button to run the scheduler (and the validation warning icon, etc.) for the current
 * [GlobalState.scheduleConfiguration].
 */
@Composable
fun RunScheduleButton() {
    val dialogState = remember { mutableStateOf<Set<ErrorDialogState>>(emptySet()) }
    dialogState.value.forEach { errorDialogState ->
        ErrorDialog(
            title = errorDialogState.title,
            message = errorDialogState.message,
            throwable = errorDialogState.throwable,
            onClose = {
                dialogState.value = dialogState.value.minus(errorDialogState)
            }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_3)
    ) {
        val schedulerSettingsState = remember { mutableStateOf(SchedulerSettings.default) }

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

        val jobState = remember { mutableStateOf<Job?>(null) }
        val loading = jobState.value != null

        if (loading) {
            IconAndTextButton(
                text = "Cancel",
                iconFilename = "stop",
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
                onClick = {
                    jobState.value?.cancel()
                    jobState.value = null
                }
            )
        }

        val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
        Button(
            enabled = validationErrors.isEmpty() && !loading,
            onClick = {
                jobState.value = coroutineScope.launch {
                    val scheduler = schedulerSettingsState.value.create()

                    val configuration = GlobalState.scheduleConfiguration
                    val result = runCatching {
                        scheduler.schedule(configuration)?.also { it.verify(configuration) }
                    }

                    if (isActive) {
                        if (result.isSuccess) {
                            val schedule = result.getOrThrow()
                            if (schedule == null) {
                                val errorDialogState = ErrorDialogState(
                                    title = SCHEDULE_NOT_FOUND_TITLE,
                                    message = SCHEDULE_NOT_FOUND_MESSAGE,
                                    throwable = null,
                                )
                                dialogState.value = dialogState.value.plus(errorDialogState)
                            } else {
                                val computedSchedule = ComputedSchedule(
                                    configuration = configuration,
                                    schedule = schedule
                                )

                                GlobalState.computedSchedules = GlobalState.computedSchedules.plus(computedSchedule)
                            }
                        } else {
                            val errorDialogState = ErrorDialogState(
                                title = EXCEPTION_TITLE,
                                message = EXCEPTION_MESSAGE,
                                throwable = result.exceptionOrNull(),
                            )
                            dialogState.value = dialogState.value.plus(errorDialogState)
                        }
                    }

                    jobState.value = null // can't use the job isCompleted since it would not refresh the composition
                }
            },
            content = {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimens.SPACING_3),
                        strokeWidth = Dimens.PROGRESS_INDICATOR_STROKE_WIDTH,
                    )

                    Spacer(Modifier.width(Dimens.SPACING_2))

                    val loadingCoroutineScope = rememberCoroutineScope { Dispatchers.Main }
                    val durationState = remember { liveDurationState(loadingCoroutineScope) }
                    val seconds = durationState.value.toDouble(DurationUnit.SECONDS)
                    Text("Running for %.1fs".format(seconds))
                } else {
                    Image(
                        painter = painterResource("icons/play.svg"),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(LocalContentColor.current),
                        alpha = LocalContentAlpha.current,
                    )

                    Spacer(Modifier.width(Dimens.SPACING_2))

                    Text("Run")
                }
            }
        )
    }
}
