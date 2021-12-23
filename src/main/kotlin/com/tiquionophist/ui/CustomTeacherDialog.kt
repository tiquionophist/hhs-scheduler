package com.tiquionophist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.common.withInitialFocus

/**
 * Wraps global handling of the custom teacher dialog state and content.
 */
object CustomTeacherDialogHandler {
    var visible by mutableStateOf(false)

    @Composable
    fun content() {
        if (visible) {
            CustomTeacherDialog { visible = false }
        }
    }
}

/**
 * Wraps a [Dialog] which allows the user to create a new [Teacher] with a custom first/last name.
 *
 * When the dialog is closed, [onClose] is invoked, with a non-null [Teacher] if it was submitted successfully, or null
 * if it was cancelled.
 */
@Composable
fun CustomTeacherDialog(onClose: () -> Unit) {
    val firstNameState = remember { mutableStateOf("") }
    val lastNameState = remember { mutableStateOf("") }

    // attempt to submit the dialog
    fun submit() {
        val firstName = firstNameState.value
        val lastName = lastNameState.value
        if (firstName.isNotEmpty() && lastName.isNotEmpty()) {
            val teacher = Teacher(firstName = firstName, lastName = lastName)
            GlobalState.customTeachers = GlobalState.customTeachers.plus(teacher)
            onClose()
        }
    }

    Dialog(
        title = "Add custom teacher",
        onCloseRequest = onClose,
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(width = Dp.Unspecified, height = Dp.Unspecified),
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
        Surface(elevation = Dimens.TOOLTIP_ELEVATION) {
            Column(
                modifier = Modifier.padding(Dimens.SPACING_2),
                verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_3),
            ) {
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
