package com.tiquionophist.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.tiquionophist.ui.Dimens
import com.tiquionophist.ui.ThemeColors

/**
 * Adds a border line to the top of this element.
 */
@Composable
fun Modifier.topBorder(width: Dp = Dimens.BORDER_WIDTH, color: Color = ThemeColors.current.divider): Modifier {
    val widthPx = with(LocalDensity.current) { width.toPx() }
    return this
        .border(
            width = width,
            color = color,
            shape = GenericShape { size, _ ->
                addRect(Rect(left = 0f, top = 0f, right = size.width, bottom = widthPx))
            }
        )
        .padding(top = width)
}
