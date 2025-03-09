package com.tiquionophist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.common.requestInitialFocus

/**
 * Wraps a [Dialog] which allows the user to create a new [Teacher] with a custom first/last name.
 *
 * When the dialog is closed, [onClose] is invoked, with a non-null [Teacher] if it was submitted successfully, or null
 * if it was cancelled.
 */
@Composable
fun AddCustomTeacherDialog(onClose: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    // attempt to submit the dialog
    fun submit() {
        if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
            val teacher = Teacher(firstName = firstName, lastName = lastName)
            GlobalState.customTeachers = GlobalState.customTeachers.plus(teacher)
            onClose()
        }
    }

    DialogWindow(
        title = "Add custom teacher",
        onCloseRequest = onClose,
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = Dp.Unspecified, height = Dp.Unspecified),
        ),
        resizable = false,
    ) {
        Surface(elevation = Dimens.TOOLTIP_ELEVATION) {
            Column(
                modifier = Modifier.padding(Dimens.SPACING_2),
                verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_3),
            ) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = { Text("First name") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions { submit() },
                    singleLine = true,
                    modifier = Modifier.requestInitialFocus(),
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = { Text("Last name") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions { submit() },
                    singleLine = true,
                )

                Button(
                    onClick = ::submit,
                    enabled = firstName.isNotEmpty() && lastName.isNotEmpty(),
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Add")
                }
            }
        }
    }
}
