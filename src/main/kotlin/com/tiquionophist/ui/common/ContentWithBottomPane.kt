package com.tiquionophist.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import java.lang.Integer.max

/**
 * A simple layout which places [bottomPane] below [content], giving [bottomPane] as much vertical space as it needs and
 * allocating the rest to [content].
 *
 * If one has a larger width than the other, [horizontalAlignment] is used to align the smaller pane.
 */
@Composable
fun ContentWithBottomPane(
    content: @Composable () -> Unit,
    bottomPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
) {
    val layoutDirection = LocalLayoutDirection.current
    Layout(
        modifier = modifier,
        content = {
            Box { content() }
            Box { bottomPane() }
        },
        measurePolicy = { measurables: List<Measurable>, constraints: Constraints ->
            val contentMeasurable = measurables[0]
            val bottomPaneMeasurable = measurables[1]

            val bottomPanePlaceable = bottomPaneMeasurable.measure(constraints)
            val contentPlaceable = contentMeasurable.measure(
                constraints.copy(maxHeight = constraints.maxHeight - bottomPanePlaceable.height)
            )

            val totalWidth = max(bottomPanePlaceable.width, contentPlaceable.width)
            val totalHeight = bottomPanePlaceable.height + contentPlaceable.height

            layout(width = totalWidth, height = totalHeight) {
                contentPlaceable.place(
                    x = horizontalAlignment.align(
                        size = contentPlaceable.width,
                        space = totalWidth,
                        layoutDirection = layoutDirection
                    ),
                    y = 0,
                )

                bottomPanePlaceable.place(
                    x = horizontalAlignment.align(
                        size = bottomPanePlaceable.width,
                        space = totalWidth,
                        layoutDirection = layoutDirection
                    ),
                    y = contentPlaceable.height
                )
            }
        }
    )

}
