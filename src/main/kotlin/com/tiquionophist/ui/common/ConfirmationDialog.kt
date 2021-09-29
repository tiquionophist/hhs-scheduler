package com.tiquionophist.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.ui.Dimens

@Composable
fun ConfirmationDialog(
    title: String? = null,
    windowTitle: String? = title,
    message: String? = null,
    cancelText: String = "Cancel",
    acceptText: String = "OK",
    onAccept: () -> Unit = {},
    onDecline: () -> Unit = {},
) {
    Dialog(
        state = rememberDialogState(size = WindowSize(width = Dp.Unspecified, height = Dp.Unspecified)),
        resizable = false,
        title = windowTitle ?: "Confirmation",
        onCloseRequest = onDecline,
    ) {
        Surface {
            Column(
                modifier = Modifier
                    .padding(Dimens.SPACING_2)
                    .widthIn(min = Dimens.Dialog.MIN_WIDTH, max = Dimens.Dialog.MAX_WIDTH),
                verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
            ) {
                title?.let { Text(title, fontSize = Dimens.Dialog.TITLE_FONT_SIZE) }

                message?.let { Text(message) }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)
                ) {
                    Button(
                        onClick = onDecline
                    ) {
                        Text(cancelText)
                    }

                    Button(
                        onClick = onAccept,
                    ) {
                        Text(acceptText)
                    }
                }
            }
        }
    }
}
