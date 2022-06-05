package com.tiquionophist.ui.common

import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tiquionophist.ui.Dimens

@Composable
fun Tooltip(
    text: String,
    content: @Composable () -> Unit,
) {
    TooltipArea(
        tooltip = {
            TooltipSurface {
                Box(Modifier.padding(Dimens.SPACING_2).widthIn(max = Dimens.Dialog.MAX_TEXT_WIDTH)) {
                    Text(text)
                }
            }
        },
        content = content,
    )
}
