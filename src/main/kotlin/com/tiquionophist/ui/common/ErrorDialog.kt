package com.tiquionophist.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.ui.Dimens

@Composable
fun ErrorDialog(
    throwable: Throwable?,
    title: String = "Error",
    message: String = if (throwable == null) "An error occurred" else "An exception was thrown",
    onClose: () -> Unit
) {
    Dialog(
        state = rememberDialogState(size = DpSize(width = Dp.Unspecified, height = Dp.Unspecified)),
        resizable = false,
        icon = rememberVectorPainter(Icons.Default.Warning),
        title = "Error",
        onCloseRequest = onClose,
    ) {
        Surface(elevation = Dimens.TOOLTIP_ELEVATION) {
            Column(
                modifier = Modifier.padding(Dimens.SPACING_2),
                verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_3),
            ) {
                Text(
                    text = title,
                    fontSize = Dimens.Dialog.TITLE_FONT_SIZE,
                    modifier = Modifier.widthIn(max = Dimens.Dialog.MAX_TEXT_WIDTH),
                )

                Text(text = message, modifier = Modifier.widthIn(max = Dimens.Dialog.MAX_TEXT_WIDTH))

                if (throwable != null) {
                    throwable.message?.let { message ->
                        Text(
                            modifier = Modifier.padding(horizontal = Dimens.SPACING_2),
                            text = message,
                            color = MaterialTheme.colors.error,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)
                ) {
                    if (throwable != null) {
                        CopyStackTraceButton(throwable)
                    }

                    Button(onClick = onClose) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
