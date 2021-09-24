package com.tiquionophist.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window

@Composable
fun ScheduleWindow(
    computedSchedule: ComputedSchedule,
    onClose: () -> Unit
) {
    Window(
        title = "Schedule ${computedSchedule.index}",
        onCloseRequest = onClose,
    ) {
        val selectedClassIndexState = remember { mutableStateOf(0) }

        ScheduleTable(
            configuration = computedSchedule.configuration,
            schedule = computedSchedule.schedule,
            classIndex = selectedClassIndexState.value,
        )
    }
}
