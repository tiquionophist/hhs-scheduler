package com.tiquionophist.core

import androidx.compose.ui.graphics.ImageBitmap
import com.tiquionophist.ui.common.loadImageBitmapOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.Locale

@Serializable
data class Teacher(val firstName: String, val lastName: String) {
    @Transient
    val fullName: String = "$firstName $lastName"

    @Transient
    val imageFilename = "teachers/${firstName.uppercase(Locale.US)}_${lastName.uppercase(Locale.US)}.png"

    val imageBitmap: ImageBitmap?
        get() = imageCache.getOrPut(this) { loadImageBitmapOrNull(imageFilename) }

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

        private val imageCache = mutableMapOf<Teacher, ImageBitmap?>()
    }
}
