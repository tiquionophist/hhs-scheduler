package com.tiquionophist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.core.Teacher

@Composable
fun CustomTeacherDialog(visible: Boolean, onClose: (Teacher?) -> Unit) {
    Dialog(
        visible = visible,
        title = "Add custom teacher",
        onCloseRequest = { onClose(null) },
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = WindowSize(width = Dp.Unspecified, height = Dp.Unspecified),
        ),
        resizable = false,
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val firstNameState = remember { mutableStateOf("") }
            val lastNameState = remember { mutableStateOf("") }

            OutlinedTextField(
                value = firstNameState.value,
                onValueChange = { firstNameState.value = it },
                placeholder = { Text("First name") }
            )

            OutlinedTextField(
                value = lastNameState.value,
                onValueChange = { lastNameState.value = it },
                placeholder = { Text("Last name") }
            )

            Button(
                modifier = Modifier.align(Alignment.End),
                enabled = firstNameState.value.isNotEmpty() && lastNameState.value.isNotEmpty(),
                onClick = {
                    onClose(Teacher(firstName = firstNameState.value, lastName = lastNameState.value))
                }
            ) {
                Text("Add")
            }
        }
    }
}
