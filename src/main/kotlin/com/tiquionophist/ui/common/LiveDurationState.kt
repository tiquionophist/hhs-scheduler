package com.tiquionophist.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Returns a [State] of the duration from the time this function was called, updated every [increment].
 */
@Composable
fun liveDurationState(increment: Duration = 25.milliseconds): State<Duration> {
    return produceState(initialValue = Duration.ZERO) {
        val start = TimeSource.Monotonic.markNow()
        while (true) {
            delay(increment.inWholeMilliseconds)
            value = start.elapsedNow().absoluteValue
        }
    }
}
