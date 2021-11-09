package com.tiquionophist.core

/**
 * The set of special classrooms in the game which can be occupied by particular subjects. Numbered classrooms (e.g.
 * Classroom 1, Classroom 2) are not included for simplicity; these can be auto-assigned.
 */
enum class Classroom {
    ART,
    BASEMENT,
    BIOLOGY,
    CHEMISTRY,
    COMPUTER,
    GYM,
    MUSIC,
    SPORTS_AREA,
    SWIMMING_POOL;

    /**
     * The canonical name of the classroom used in the game. This is similar to its [Classroom.prettyName], but not
     * always identical, since some have a prefix "Classroom XXX".
     */
    val canonicalName: String
        get() {
            return when (this) {
                ART -> "Classroom Art"
                BASEMENT -> "Basement"
                BIOLOGY -> "Classroom Biology"
                CHEMISTRY -> "Classroom Chemistry"
                COMPUTER -> "Computer Room"
                GYM -> "Gym"
                MUSIC -> "Classroom Music"
                SPORTS_AREA -> "Sports Area"
                SWIMMING_POOL -> "Swimming Pool"
            }
        }
}
