package com.tiquionophist.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.lang.Integer.max

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange? = null,
    cornerRounding: Dp = 4.dp,
    buttonWidth: Dp = 32.dp,
    textFieldWidth: Dp = 50.dp,
) {
    fun setValue(newValue: Int) {
        onValueChange(range?.let { newValue.coerceIn(it) } ?: newValue)
    }

    // if the text field has been cleared, to avoid jankiness
    val clearedState = remember { mutableStateOf(false) }

    Layout(
        modifier = modifier,
        content = {
            Button(
                modifier = Modifier.widthIn(max = buttonWidth),
                enabled = range?.let { value > it.first } != false,
                onClick = { setValue(value - 1) },
                shape = AbsoluteRoundedCornerShape(topLeft = cornerRounding, bottomLeft = cornerRounding),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text("-")
            }

            BasicTextField(
                modifier = Modifier
                    .widthIn(max = textFieldWidth)
                    .border(width = 1.dp, color = MaterialTheme.colors.primary),
                value = if (clearedState.value) "" else value.toString(),
                singleLine = true,
                onValueChange = { newValue ->
                    clearedState.value = newValue.isEmpty()
                    newValue.toIntOrNull()?.let { setValue(it) }
                },
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = 8.dp)) {
                        innerTextField()
                    }
                }
            )

            Button(
                modifier = Modifier.widthIn(max = buttonWidth),
                enabled = range?.let { value < it.last } != false,
                onClick = { setValue(value + 1) },
                shape = AbsoluteRoundedCornerShape(topRight = cornerRounding, bottomRight = cornerRounding),
                contentPadding = PaddingValues(0.dp),
            ) {
                Text("+")
            }
        },
        measurePolicy = { measurables: List<Measurable>, constraints: Constraints ->
            val leftButtonMeasurable = measurables[0]
            val rightButtonMeasurable = measurables[2]
            val textFieldMeasurable = measurables[1]

            val leftButtonPlaceable = leftButtonMeasurable.measure(constraints)
            val rightButtonPlaceable = rightButtonMeasurable.measure(constraints)

            val height = max(leftButtonPlaceable.height, rightButtonPlaceable.height)

            val textFieldPlaceable = textFieldMeasurable.measure(
                constraints.copy(minHeight = height, maxHeight = height)
            )

            val totalWidth = leftButtonPlaceable.width + rightButtonPlaceable.width + textFieldPlaceable.width

            layout(totalWidth, height) {
                leftButtonPlaceable.place(0, 0)

                textFieldPlaceable.place(leftButtonPlaceable.width, 0)

                rightButtonPlaceable.place(leftButtonPlaceable.width + textFieldPlaceable.width, 0)
            }
        }
    )
}
