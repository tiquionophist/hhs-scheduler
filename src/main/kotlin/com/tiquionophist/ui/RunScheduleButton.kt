package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import com.tiquionophist.Res
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.ic_stop
import com.tiquionophist.ui.common.ErrorDialog
import com.tiquionophist.ui.common.IconAndTextButton
import com.tiquionophist.ui.common.liveDurationState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.time.DurationUnit

private class ErrorDialogState(
    val title: String,
    val message: String,
    val throwable: Throwable?,
)

private const val SCHEDULE_NOT_FOUND_TITLE = "No schedule found"
private const val SCHEDULE_NOT_FOUND_MESSAGE =
    "No schedule could be found. This likely means that no schedule is possible for these settings, or that the " +
        "scheduler settings are too strict to find it."

private const val EXCEPTION_TITLE = "Exception thrown!"
private const val EXCEPTION_MESSAGE =
    "An exception was thrown. Consider reporting this (with the stack trace) on the project's GitHub page or try again."

@Composable
fun RunScheduleButton(
    schedulerSettings: SchedulerSettings,
    modifier: Modifier = Modifier,
    configuration: ScheduleConfiguration = GlobalState.scheduleConfiguration,
    enabled: Boolean = true,
    runImageVector: ImageVector = Icons.Default.PlayArrow,
    runText: String = "Run",
) {
    val coroutineScope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    val loading = job != null

    var errorDialogs by remember { mutableStateOf<Set<ErrorDialogState>>(emptySet()) }

    errorDialogs.forEach { errorDialogState ->
        ErrorDialog(
            title = errorDialogState.title,
            message = errorDialogState.message,
            throwable = errorDialogState.throwable,
            onClose = { errorDialogs -= errorDialogState }
        )
    }

    Row(modifier = modifier) {
        if (loading) {
            IconAndTextButton(
                text = "Cancel",
                iconRes = Res.drawable.ic_stop,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error),
                onClick = {
                    job?.cancel()
                    job = null
                }
            )
        }

        Button(
            enabled = enabled && !loading,
            onClick = {
                job = coroutineScope.launch {
                    val scheduler = schedulerSettings.create()

                    @Suppress("TooGenericExceptionCaught")
                    try {
                        val schedule = scheduler.schedule(configuration)?.also { it.verify(configuration) }
                        if (schedule == null) {
                            errorDialogs += ErrorDialogState(
                                title = SCHEDULE_NOT_FOUND_TITLE,
                                message = SCHEDULE_NOT_FOUND_MESSAGE,
                                throwable = null,
                            )
                        } else {
                            GlobalState.computedSchedules += ComputedSchedule(
                                configuration = configuration,
                                schedulerSettings = schedulerSettings,
                                schedule = schedule,
                            )
                        }
                    } catch (_: CancellationException) {
                        // no-op
                    } catch (throwable: Throwable) {
                        errorDialogs += ErrorDialogState(
                            title = EXCEPTION_TITLE,
                            message = EXCEPTION_MESSAGE,
                            throwable = throwable,
                        )
                    }
                }
                    .apply {
                        // can't use the job isCompleted since it would not refresh the composition
                        invokeOnCompletion { job = null }
                    }
            },
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(Dimens.SPACING_3),
                    strokeWidth = Dimens.PROGRESS_INDICATOR_STROKE_WIDTH,
                )

                Spacer(Modifier.width(Dimens.SPACING_2))

                val durationState = liveDurationState()
                val formattedDurationState = remember {
                    derivedStateOf { "%.1fs".format(durationState.value.toDouble(DurationUnit.SECONDS)) }
                }
                Text("Running for ${formattedDurationState.value}")
            } else {
                Image(
                    imageVector = runImageVector,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(LocalContentColor.current),
                    alpha = LocalContentAlpha.current,
                )

                Spacer(Modifier.width(Dimens.SPACING_2))

                Text(text = runText, maxLines = 2)
            }
        }
    }
}
