package com.tiquionophist.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalLayoutDirection

/**
 * A simple column layout which attempts to match the widths of all its children by using the largest minimum intrinsic
 * width as the width of all the children.
 *
 * In theory [horizontalAlignment] should not be necessary, but in case any children refuse to conform to this
 * calculated width, it is used to align the children.
 */
@Composable
fun MatchingWidthColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    Layout(
        modifier = modifier,
        content = content,
        measurePolicy = { measurables, constraints ->
            val goalWidth = measurables.maxOf { it.minIntrinsicWidth(constraints.maxHeight) }
                .coerceAtLeast(constraints.minWidth)

            val placeables = measurables.map {
                it.measure(constraints.copy(minWidth = goalWidth, maxWidth = goalWidth))
            }

            val actualWidth = placeables.maxOf { it.width }

            layout(actualWidth, placeables.sumOf { it.height }) {
                var y = 0
                placeables.forEach {
                    it.place(
                        x = horizontalAlignment.align(
                            size = it.width,
                            space = actualWidth,
                            layoutDirection = layoutDirection,
                        ),
                        y = y,
                    )
                    y += it.height
                }
            }
        }
    )
}
