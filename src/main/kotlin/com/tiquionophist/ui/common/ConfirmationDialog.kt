package com.tiquionophist.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.ui.Dimens

@Composable
fun ConfirmationDialog(
    windowTitle: String,
    prompt: String,
    cancelText: String? = "Cancel",
    acceptText: String = "OK",
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
) {
    Dialog(
        state = rememberDialogState(size = DpSize(width = Dp.Unspecified, height = Dp.Unspecified)),
        title = windowTitle,
        resizable = false,
        onCloseRequest = onDecline,
    ) {
        Surface(elevation = Dimens.TOOLTIP_ELEVATION) {
            Column(
                modifier = Modifier.padding(Dimens.SPACING_2),
                verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_3),
            ) {
                Text(modifier = Modifier.widthIn(max = Dimens.Dialog.MAX_TEXT_WIDTH), text = prompt)

                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
                ) {
                    if (cancelText != null) {
                        TextButton(onClick = onDecline) {
                            Text(cancelText)
                        }
                    }

                    Button(onClick = onAccept) {
                        Text(acceptText)
                    }
                }
            }
        }
    }
}
