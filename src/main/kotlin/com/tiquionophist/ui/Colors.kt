package com.tiquionophist.ui

import androidx.compose.material.Colors
import androidx.compose.material.LocalContentColor
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Colors {
    private val PRIMARY = Color(42, 149, 232)

    val divider: Color
        @Composable
        get() = LocalContentColor.current.copy(alpha = 0.65f)

    val weakDivider: Color
        @Composable
        get() = divider.copy(alpha = 0.25f)

    fun materialColors(light: Boolean): Colors {
        return if (light) {
            lightColors(primary = PRIMARY)
        } else {
            darkColors(primary = PRIMARY)
        }
    }
}
