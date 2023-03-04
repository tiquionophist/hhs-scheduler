package com.tiquionophist.core

import androidx.compose.runtime.Immutable
import com.tiquionophist.core.AssignedClassroom.NamedClassroom
import com.tiquionophist.core.AssignedClassroom.NumberedClassroom

/**
 * Represents a classroom in the schedule; either a [NamedClassroom] for a special classroom in the game or a
 * [NumberedClassroom] for a generic classroom with just a number.
 */
@Immutable
sealed interface AssignedClassroom {
    data class NamedClassroom(val classroom: Classroom) : AssignedClassroom {
        override fun toString() = classroom.canonicalName
    }

    data class NumberedClassroom(val number: Int) : AssignedClassroom {
        override fun toString() = "Classroom $number"
    }
}
