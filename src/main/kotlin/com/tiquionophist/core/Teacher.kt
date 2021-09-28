package com.tiquionophist.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Teacher(val firstName: String, val lastName: String) {
    @Transient
    val fullName: String = "$firstName $lastName"

    companion object {
        // a few hardcoded teachers are used in unit tests
        val BETH_MANILI = Teacher("Beth", "Manili")
        val CARMEN_SMITH = Teacher("Carmen", "Smith")
        val RONDA_BELLS = Teacher("Ronda", "Bells")
        val SAMANTHA_KELLER = Teacher("Samantha", "Keller")

        val DEFAULT_TEACHERS = listOf(
            Teacher("April", "Raymund"),
            BETH_MANILI,
            Teacher("Carl", "Walker"),
            CARMEN_SMITH,
            Teacher("Claire", "Fuzushi"),
            Teacher("Jessica", "Underwood"),
            Teacher("Nina", "Parker"),
            RONDA_BELLS,
            SAMANTHA_KELLER,
        )

        val LEXVILLE_TEACHERS = listOf(
            Teacher("Anna", "Miller"),
            Teacher("Lara", "Ellis"),
        )
    }
}
