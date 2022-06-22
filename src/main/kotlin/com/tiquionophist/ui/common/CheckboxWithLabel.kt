package com.tiquionophist.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxColors
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.tiquionophist.ui.Dimens
import com.tiquionophist.ui.ThemeColors

@Composable
fun CheckboxWithLabel(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    padding: PaddingValues = PaddingValues(all = Dimens.SPACING_2),
    checkboxPadding: Dp = Dimens.SPACING_2,
    colors: CheckboxColors = CheckboxDefaults.colors(checkedColor = ThemeColors.current.selected),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.clickable(enabled = enabled) { onCheckedChange(!checked) }.padding(padding),
        verticalAlignment = verticalAlignment,
    ) {
        Checkbox(checked = checked, enabled = enabled, onCheckedChange = null, colors = colors)

        Spacer(Modifier.width(checkboxPadding))

        if (enabled) {
            content()
        } else {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled, content = content)
        }
    }
}
