package com.tiquionophist.ui.common

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Returns a [State] of the duration from the time this function was called, updated every [increment].
 *
 * Runs on the given [coroutineScope], which should be attached to a specific point in the composition to make sure the
 * coroutine is cleaned up when it is no longer needed.
 */
fun LiveDurationState(
    coroutineScope: CoroutineScope,
    increment: Duration = Duration.milliseconds(100)
): State<Duration> {
    val state = mutableStateOf(Duration.ZERO)
    val start = TimeSource.Monotonic.markNow()

    coroutineScope.launch {
        while (true) {
            delay(increment.inWholeMilliseconds)
            state.value = start.elapsedNow().absoluteValue
        }
    }

    return state
}
