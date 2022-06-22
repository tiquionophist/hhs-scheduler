package com.tiquionophist.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Teacher
import com.tiquionophist.ui.common.Notification

/**
 * Stores the global state, which does not need to be attached to a particular point in the composition and can be
 * conveniently referenced from anywhere.
 *
 * TODO handle error dialogs here as well
 */
object GlobalState {
    private val initialConfiguration = ScheduleConfiguration.EMPTY

    var scheduleConfiguration by mutableStateOf(initialConfiguration)
    var customTeachers by mutableStateOf(emptySet<Teacher>())
    var computedSchedules by mutableStateOf(listOf<ComputedSchedule>())

    // null when all classes are the same, otherwise the index of the class being controlled now
    var currentClassIndex by mutableStateOf<Int?>(null)

    var showLexvilleTeachers by mutableStateOf(false)
    var showUnusedSubjects by mutableStateOf(true)
    var showLockedSubjects by mutableStateOf(false)
    var showUnusedTeachers by mutableStateOf(true)
    var showTeacherExp by mutableStateOf(false)

    var currentNotification by mutableStateOf<Notification?>(null)
}
