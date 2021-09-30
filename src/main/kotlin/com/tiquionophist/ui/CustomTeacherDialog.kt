package com.tiquionophist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.common.withInitialFocus

/**
 * Wraps a [Dialog] which allows the user to create a new [Teacher] with a custom first/last name.
 *
 * When the dialog is closed, [onClose] is invoked, with a non-null [Teacher] if it was submitted successfully, or null
 * if it was cancelled.
 */
@Composable
fun CustomTeacherDialog(onClose: (Teacher?) -> Unit) {
    val firstNameState = remember { mutableStateOf("") }
    val lastNameState = remember { mutableStateOf("") }

    // attempt to submit the dialog
    fun submit() {
        val firstName = firstNameState.value
        val lastName = lastNameState.value
        if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
            onClose(Teacher(firstName = firstName, lastName = lastName))
        }
    }

    Dialog(
        title = "Add custom teacher",
        onCloseRequest = { onClose(null) },
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = WindowSize(width = Dp.Unspecified, height = Dp.Unspecified),
        ),
        onKeyEvent = {
            if (it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                submit()
                true
            } else {
                false
            }
        },
        resizable = false,
    ) {
        Surface {
            Column(Modifier.padding(Dimens.SPACING_2), verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)) {
                OutlinedTextField(
                    modifier = Modifier.withInitialFocus(),
                    value = firstNameState.value,
                    onValueChange = { firstNameState.value = it },
                    singleLine = true,
                    placeholder = { Text("First name") }
                )

                OutlinedTextField(
                    value = lastNameState.value,
                    onValueChange = { lastNameState.value = it },
                    singleLine = true,
                    placeholder = { Text("Last name") }
                )

                Button(
                    modifier = Modifier.align(Alignment.End),
                    enabled = firstNameState.value.isNotEmpty() && lastNameState.value.isNotEmpty(),
                    onClick = { submit() }
                ) {
                    Text("Add")
                }
            }
        }
    }
}
