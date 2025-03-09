package com.tiquionophist.ui.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tiquionophist.ui.Dimens

/**
 * A simple [Surface] which applies standard corner rounding and elevation.
 */
@Composable
fun TooltipSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Dimens.CORNER_ROUNDING),
        elevation = Dimens.TOOLTIP_ELEVATION,
        modifier = modifier,
        content = content,
    )
}
