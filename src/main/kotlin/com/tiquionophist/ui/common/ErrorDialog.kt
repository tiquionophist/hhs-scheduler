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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.ui.Dimens
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun ErrorDialog(
    throwable: Throwable?,
    title: String = "Error",
    message: String = if (throwable == null) "An error occurred" else "An exception was thrown",
    onClose: () -> Unit
) {
    Dialog(
        state = rememberDialogState(size = WindowSize(width = Dp.Unspecified, height = Dp.Unspecified)),
        resizable = false,
        icon = rememberVectorPainter(Icons.Default.Warning),
        title = "Error",
        onCloseRequest = onClose,
    ) {
        Surface {
            Column(
                modifier = Modifier
                    .padding(Dimens.SPACING_2)
                    .widthIn(min = Dimens.Dialog.MIN_WIDTH, max = Dimens.Dialog.MAX_WIDTH),
                verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_2),
            ) {
                Text(title, fontSize = Dimens.Dialog.TITLE_FONT_SIZE)

                Text(message)

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
                        Button(
                            onClick = {
                                val stringSelection = StringSelection(throwable.stackTraceToString())
                                Toolkit.getDefaultToolkit().systemClipboard.setContents(stringSelection, stringSelection)
                            }
                        ) {
                            Text("Copy stack trace")
                        }
                    }

                    Button(
                        onClick = onClose,
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
