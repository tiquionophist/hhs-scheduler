package com.tiquionophist.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import java.lang.Integer.max

/**
 * Wraps [verticalScroll] while allowing content to fill max height. [verticalScroll] sets the maximum height to
 * infinity; this modifier instead uses the larger of the content's minimum intrinsic height and the actual constrained
 * maximum height. Thus, it will scroll when it is beyond its minimum intrinsic height and fill the maximum space
 * otherwise.
 */
fun Modifier.fillMaxHeightVerticalScroll(scrollState: ScrollState): Modifier {
    return composed {
        val maxHeight = remember { mutableStateOf(0) }

        this
            .layout { measurable, constraints ->
                maxHeight.value = constraints.maxHeight
                measureAndPlace(measurable, constraints)
            }
            .verticalScroll(scrollState)
            .layout { measurable, constraints ->
                measureAndPlace(
                    measurable,
                    constraints.copy(
                        maxHeight = max(measurable.minIntrinsicHeight(constraints.maxWidth), maxHeight.value)
                    )
                )
            }
    }
}

/**
 * Wraps [horizontalScroll] while allowing content to fill max width. [horizontalScroll] sets the maximum width to
 * infinity; this modifier instead uses the larger of the content's minimum intrinsic width and the actual constrained
 * maximum width. Thus, it will scroll when it is beyond its minimum intrinsic width and fill the maximum space
 * otherwise.
 */
fun Modifier.fillMaxWidthHorizontalScroll(scrollState: ScrollState): Modifier {
    return composed {
        val width = remember { mutableStateOf(0) }

        this
            .layout { measurable, constraints ->
                width.value = constraints.maxWidth
                measureAndPlace(measurable, constraints)
            }
            .horizontalScroll(scrollState)
            .layout { measurable, constraints ->
                measureAndPlace(
                    measurable,
                    constraints.copy(
                        maxWidth = max(measurable.minIntrinsicWidth(constraints.maxHeight), width.value)
                    )
                )
            }
    }
}

/**
 * Convenience function to measure [measurable] with [constraints] then place it at (0,0).
 */
private fun MeasureScope.measureAndPlace(measurable: Measurable, constraints: Constraints): MeasureResult {
    val placeable = measurable.measure(constraints)
    return layout(placeable.width, placeable.height) {
        placeable.place(0, 0)
    }
}
