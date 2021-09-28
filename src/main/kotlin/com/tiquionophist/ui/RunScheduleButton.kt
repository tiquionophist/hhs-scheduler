package com.tiquionophist.ui

import androidx.compose.foundation.BoxWithTooltip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.scheduler.RandomizedScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The button to run the scheduler (and the validation warning icon, etc) for the given [configuration].
 */
@ExperimentalComposeUiApi
@Composable
fun RunScheduleButton(
    configuration: ScheduleConfiguration,
    onComputedSchedule: (ComputedSchedule) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_3)
    ) {
        val validationError = remember(configuration) {
            runCatching { configuration.verify() }
                .exceptionOrNull()
                ?.message
        }

        if (validationError != null) {
            BoxWithTooltip(
                tooltip = {
                    Surface(modifier = Modifier.shadow(Dimens.SHADOW_ELEVATION)) {
                        Text(
                            text = validationError,
                            modifier = Modifier.padding(Dimens.SPACING_2),
                        )
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.Red,
                )
            }
        }

        val loading = remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
        Button(
            enabled = validationError == null && !loading.value,
            onClick = {
                loading.value = true
                coroutineScope.launch {
                    val scheduler = RandomizedScheduler(
                        attemptsPerRound = 1_000,
                        rounds = 1_000
                    )

                    val result = runCatching {
                        scheduler.schedule(configuration)?.also { it.verify(configuration) }
                    }
                    if (result.isSuccess) {
                        val schedule = result.getOrThrow()
                        if (schedule == null) {
                            // TODO show as dialog
                            println("No schedule found")
                        } else {
                            val computedSchedule = ComputedSchedule(
                                configuration = configuration,
                                schedule = schedule
                            )

                            onComputedSchedule(computedSchedule)
                        }
                    } else {
                        // TODO show as dialog
                        val throwable = result.exceptionOrNull()
                        println("Error: $throwable")
                    }

                    loading.value = false
                }
            },
            content = {
                // TODO improve loading state (show elapsed time, etc)
                if (loading.value) {
                    CircularProgressIndicator(Modifier.size(Dimens.SPACING_3))
                } else {
                    Text("Run")
                }
            }
        )
    }
}
