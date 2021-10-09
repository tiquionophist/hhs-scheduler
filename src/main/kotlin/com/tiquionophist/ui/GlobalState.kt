package com.tiquionophist.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.common.Notification
import java.io.File

/**
 * Stores the global state, which does not need to be attached to a particular point in the composition and can be
 * conveniently referenced from anywhere.
 *
 * TODO handle error dialogs here as well
 */
object GlobalState {
    private val initialConfiguration = ScheduleConfiguration.loadOrNull(File("config.json"))
        ?: ScheduleConfiguration.EMPTY

    var scheduleConfiguration by mutableStateOf(initialConfiguration)
    var showLexvilleTeachers by mutableStateOf(false)
    var customTeachers by mutableStateOf(emptySet<Teacher>())
    var computedSchedules by mutableStateOf(listOf<ComputedSchedule>())

    var currentNotification by mutableStateOf<Notification?>(null)
}
