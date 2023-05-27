package com.tiquionophist.ui.common

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun CopyStackTraceButton(throwable: Throwable) {
    TextButton(
        onClick = {
            val stringSelection = StringSelection(throwable.stackTraceToString())
            Toolkit.getDefaultToolkit()
                .systemClipboard
                .setContents(stringSelection, stringSelection)
        }
    ) {
        Text("Copy stack trace")
    }
}
