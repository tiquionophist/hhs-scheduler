package com.tiquionophist.ui.common

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

/**
 * A [Modifier] which requests focus for this element once on the first composition.
 */
fun Modifier.withInitialFocus(requester: FocusRequester = FocusRequester()): Modifier {
    return composed {
        DisposableEffect(Unit) {
            requester.requestFocus()
            onDispose { }
        }

        focusRequester(requester)
    }
}
