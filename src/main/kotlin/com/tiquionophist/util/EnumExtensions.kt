package com.tiquionophist.util

/**
 * A user-readable name for this [Enum], replacing underscores with spaces and title-casing each word.
 */
val Enum<*>.prettyName: String
    get() {
        return name
            .split("_")
            .joinToString(separator = " ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
    }
