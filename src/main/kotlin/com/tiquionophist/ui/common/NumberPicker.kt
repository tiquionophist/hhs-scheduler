package com.tiquionophist.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tiquionophist.ui.Dimens
import java.lang.Integer.max
import java.lang.Integer.min

/**
 * A component allowing the user to select a numeric value, either by incrementing/decrementing buttons or by a text
 * field between them.
 *
 * TODO min/max intrinsic width don't match actual button widths, leading to discrepancies in table sizing
 */
@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    min: Int? = null,
    max: Int? = null,
    cornerRounding: Dp = Dimens.CORNER_ROUNDING,
    buttonIconSize: Dp = Dimens.NumberPicker.BUTTON_ICON_SIZE,
    textFieldWidth: Dp = Dimens.NumberPicker.TEXT_FIELD_WIDTH,
) {
    fun setValue(newValue: Int) {
        var adjusted = newValue
        min?.let { adjusted = max(adjusted, min) }
        max?.let { adjusted = min(adjusted, max) }

        onValueChange(adjusted)
    }

    // if the text field has been cleared, to avoid jankiness
    val clearedState = remember { mutableStateOf(false) }

    val buttonColors = ButtonDefaults.buttonColors()

    Layout(
        modifier = modifier,
        content = {
            Button(
                modifier = Modifier.widthIn(max = buttonIconSize + Dimens.SPACING_2 * 2),
                enabled = enabled && min?.let { value > it } != false,
                onClick = { setValue(value - 1) },
                colors = buttonColors,
                shape = AbsoluteRoundedCornerShape(topLeft = cornerRounding, bottomLeft = cornerRounding),
                contentPadding = PaddingValues(0.dp),
            ) {
                Image(
                    modifier = Modifier.size(buttonIconSize),
                    painter = painterResource("icons/minus.svg"),
                    contentDescription = "Minus",
                    colorFilter = ColorFilter.tint(LocalContentColor.current),
                    alpha = LocalContentAlpha.current,
                )
            }

            BasicTextField(
                modifier = Modifier
                    .widthIn(max = textFieldWidth)
                    .border(width = Dimens.BORDER_WIDTH, color = buttonColors.backgroundColor(enabled).value),
                value = if (clearedState.value) "" else value.toString(),
                singleLine = true,
                enabled = enabled,
                cursorBrush = SolidColor(LocalContentColor.current),
                textStyle = LocalTextStyle.current.merge(TextStyle(color = LocalContentColor.current)),
                onValueChange = { newValue ->
                    clearedState.value = newValue.isEmpty()
                    newValue.toIntOrNull()?.let { setValue(it) }
                },
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.padding(Dimens.SPACING_2),
                    ) {
                        innerTextField()
                    }
                }
            )

            Button(
                modifier = Modifier.widthIn(max = buttonIconSize + Dimens.SPACING_2 * 2),
                enabled = enabled && max?.let { value < it } != false,
                onClick = { setValue(value + 1) },
                colors = buttonColors,
                shape = AbsoluteRoundedCornerShape(topRight = cornerRounding, bottomRight = cornerRounding),
                contentPadding = PaddingValues(0.dp),
            ) {
                Image(
                    modifier = Modifier.size(buttonIconSize),
                    painter = painterResource("icons/plus.svg"),
                    contentDescription = "Plus",
                    colorFilter = ColorFilter.tint(LocalContentColor.current),
                    alpha = LocalContentAlpha.current,
                )
            }
        },
        measurePolicy = { measurables: List<Measurable>, constraints: Constraints ->
            val leftButtonMeasurable = measurables[0]
            val rightButtonMeasurable = measurables[2]
            val textFieldMeasurable = measurables[1]

            val height = minOf(
                leftButtonMeasurable.minIntrinsicHeight(constraints.maxWidth),
                rightButtonMeasurable.minIntrinsicHeight(constraints.maxWidth),
                textFieldMeasurable.minIntrinsicHeight(constraints.maxWidth),
            )

            val constraintsWithHeight = constraints.copy(minHeight = height, maxHeight = height)

            val leftButtonPlaceable = leftButtonMeasurable.measure(constraintsWithHeight)
            val rightButtonPlaceable = rightButtonMeasurable.measure(constraintsWithHeight)
            val textFieldPlaceable = textFieldMeasurable.measure(constraintsWithHeight)

            val totalWidth = leftButtonPlaceable.width + rightButtonPlaceable.width + textFieldPlaceable.width

            layout(totalWidth, height) {
                leftButtonPlaceable.place(0, 0)

                textFieldPlaceable.place(leftButtonPlaceable.width, 0)

                rightButtonPlaceable.place(leftButtonPlaceable.width + textFieldPlaceable.width, 0)
            }
        }
    )
}
