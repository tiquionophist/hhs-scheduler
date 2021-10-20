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
        get() {
            // explicitly use containsKey to avoid calling loadImageBitmapOrNull() again if it returns null
            return if (imageCache.containsKey(this)) {
                imageCache[this]
            } else {
                loadImageBitmapOrNull(imageFilename).also { imageCache[this] = it }
            }
        }

    companion object {
        val APRIL_RAYMUND = Teacher("April", "Raymund")
        val BETH_MANILI = Teacher("Beth", "Manili")
        val CARL_WALKER = Teacher("Carl", "Walker")
        val CARMEN_SMITH = Teacher("Carmen", "Smith")
        val CLAIRE_FUZUSHI = Teacher("Claire", "Fuzushi")
        val IRINA_JELABITCH = Teacher("Irina", "Jelabitch")
        val JESSICA_UNDERWOOD = Teacher("Jessica", "Underwood")
        val NINA_PARKER = Teacher("Nina", "Parker")
        val RONDA_BELLS = Teacher("Ronda", "Bells")
        val SAMANTHA_KELLER = Teacher("Samantha", "Keller")

        val ANNA_MILLER = Teacher("Anna", "Miller")
        val LARA_ELLIS = Teacher("Lara", "Ellis")

        val DEFAULT_TEACHERS = setOf(
            APRIL_RAYMUND,
            BETH_MANILI,
            CARL_WALKER,
            CARMEN_SMITH,
            CLAIRE_FUZUSHI,
            IRINA_JELABITCH,
            JESSICA_UNDERWOOD,
            NINA_PARKER,
            RONDA_BELLS,
            SAMANTHA_KELLER,
        )

        val LEXVILLE_TEACHERS = setOf(
            ANNA_MILLER,
            LARA_ELLIS,
        )

        private val imageCache = mutableMapOf<Teacher, ImageBitmap?>()
    }
}
