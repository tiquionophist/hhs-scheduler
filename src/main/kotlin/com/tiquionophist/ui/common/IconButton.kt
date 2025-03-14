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
import com.tiquionophist.ui.Dimens
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * A simpler wrapper around [Button] with content that has an SVG icon and text.
 */
@Composable
fun IconAndTextButton(
    text: String,
    iconRes: DrawableResource,
    iconContentDescription: String? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    onClick: () -> Unit,
) {
    Button(colors = colors, onClick = onClick) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = iconContentDescription,
            colorFilter = ColorFilter.tint(LocalContentColor.current),
            alpha = LocalContentAlpha.current,
        )

        Spacer(Modifier.width(Dimens.SPACING_2))

        Text(text = text, maxLines = 2)
    }
}
