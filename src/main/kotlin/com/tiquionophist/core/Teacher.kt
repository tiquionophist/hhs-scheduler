package com.tiquionophist.core

import com.tiquionophist.Res
import com.tiquionophist.teacher_anna_miller
import com.tiquionophist.teacher_april_raymund
import com.tiquionophist.teacher_beth_manili
import com.tiquionophist.teacher_carl_walker
import com.tiquionophist.teacher_carmen_smith
import com.tiquionophist.teacher_claire_fuzushi
import com.tiquionophist.teacher_irina_jelabitch
import com.tiquionophist.teacher_jessica_underwood
import com.tiquionophist.teacher_lara_ellis
import com.tiquionophist.teacher_nina_parker
import com.tiquionophist.teacher_ronda_bells
import com.tiquionophist.teacher_samantha_keller
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.compose.resources.DrawableResource

@Serializable
data class Teacher(val firstName: String, val lastName: String) {
    @Transient
    val fullName: String = "$firstName $lastName"

    val imageRes: DrawableResource? by lazy {
        when (this) {
            ANNA_MILLER -> Res.drawable.teacher_anna_miller
            APRIL_RAYMUND -> Res.drawable.teacher_april_raymund
            BETH_MANILI -> Res.drawable.teacher_beth_manili
            CARL_WALKER -> Res.drawable.teacher_carl_walker
            CARMEN_SMITH -> Res.drawable.teacher_carmen_smith
            CLAIRE_FUZUSHI -> Res.drawable.teacher_claire_fuzushi
            IRINA_JELABITCH -> Res.drawable.teacher_irina_jelabitch
            JESSICA_UNDERWOOD -> Res.drawable.teacher_jessica_underwood
            LARA_ELLIS -> Res.drawable.teacher_lara_ellis
            NINA_PARKER -> Res.drawable.teacher_nina_parker
            RONDA_BELLS -> Res.drawable.teacher_ronda_bells
            SAMANTHA_KELLER -> Res.drawable.teacher_samantha_keller
            else -> null
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
    }
}
