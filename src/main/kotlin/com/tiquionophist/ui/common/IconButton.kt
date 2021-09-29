package com.tiquionophist.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.tiquionophist.ui.Dimens

/**
 * A simpler wrapper around [Button] with content that has an SVG icon and text.
 */
@Composable
fun IconAndTextButton(
    text: String,
    iconFilename: String,
    iconContentDescription: String? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit,
) {
    Button(colors = colors, onClick = onClick) {
        Image(
            painter = painterResource("icons/$iconFilename.svg"),
            contentDescription = iconContentDescription,
            colorFilter = ColorFilter.tint(LocalContentColor.current),
            alpha = LocalContentAlpha.current,
        )

        Spacer(Modifier.width(Dimens.SPACING_2))

        Text(text)
    }
}
