package com.tiquionophist.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.unit.Constraints

/**
 * A simple layout which places [pane] relative to [content], allocating [pane] as much width/height as it needs
 * (depending on [direction]) and the rest to [content].
 */
@Composable
fun ContentWithPane(
    direction: PaneDirection,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    pane: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            Box { content() }
            Box { pane() }
        },
        measurePolicy = { measurables: List<Measurable>, constraints: Constraints ->
            val contentMeasurable = measurables[0]
            val paneMeasurable = measurables[1]

            val panePlaceable = paneMeasurable.measure(constraints)
            val contentPlaceable = contentMeasurable.measure(
                constraints.copy(
                    maxHeight = if (direction.vertical) {
                        constraints.maxHeight - panePlaceable.height
                    } else {
                        constraints.maxHeight
                    },
                    maxWidth = if (direction.vertical) {
                        constraints.maxWidth
                    } else {
                        constraints.maxWidth - panePlaceable.width
                    }
                )
            )

            layout(width = constraints.maxWidth, height = constraints.maxHeight) {
                when (direction) {
                    PaneDirection.TOP -> {
                        panePlaceable.place(x = 0, y = 0)
                        contentPlaceable.place(x = 0, y = panePlaceable.height)
                    }

                    PaneDirection.BOTTOM -> {
                        contentPlaceable.place(x = 0, y = 0)
                        panePlaceable.place(x = 0, y = constraints.maxHeight - panePlaceable.height)
                    }

                    PaneDirection.LEFT -> {
                        panePlaceable.place(x = 0, y = 0)
                        contentPlaceable.place(x = panePlaceable.width, y = 0)
                    }

                    PaneDirection.RIGHT -> {
                        contentPlaceable.place(x = 0, y = 0)
                        panePlaceable.place(x = constraints.maxWidth - panePlaceable.width, y = 0)
                    }
                }
            }
        }
    )
}
