package com.tiquionophist.ui.common

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

/**
 * A [Modifier] which requests focus for this element once on the first composition.
 */
fun Modifier.requestInitialFocus(): Modifier {
    return composed {
        val requester = remember { FocusRequester() }
        LaunchedEffect(Unit) { requester.requestFocus() }
        focusRequester(requester)
    }
}
