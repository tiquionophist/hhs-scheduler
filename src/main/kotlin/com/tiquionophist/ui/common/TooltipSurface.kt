package com.tiquionophist.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import com.tiquionophist.ui.Dimens

/**
 * A simple [Surface] which applies standard corner rounding and elevation.
 */
@Composable
fun TooltipSurface(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Dimens.CORNER_ROUNDING),
        elevation = Dimens.TOOLTIP_ELEVATION,
        content = content,
    )
}
