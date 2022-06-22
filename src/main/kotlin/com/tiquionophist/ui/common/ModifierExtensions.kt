package com.tiquionophist.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout

/**
 * Causes this element to always use its parent's width for the layout.
 */
fun Modifier.fillParent(): Modifier {
    return layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(width = constraints.maxWidth, height = placeable.height) {
            placeable.place(0, 0)
        }
    }
}
