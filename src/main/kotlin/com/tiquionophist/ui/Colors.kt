package com.tiquionophist.ui

import androidx.compose.material.Colors
import androidx.compose.material.LocalContentColor
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

/**
 * Color constants used throughout the application.
 */
object Colors {
    const val DISABLED_ALPHA = 0.3f

    private val PRIMARY = Color(42, 149, 232)
    val SELECTED = Color(22, 199, 28)

    /**
     * The color of dividers between elements.
     */
    val divider: Color
        @Composable
        get() = LocalContentColor.current.copy(alpha = 0.65f)

    /**
     * The color of weak dividers between elements.
     */
    val weakDivider: Color
        @Composable
        get() = divider.copy(alpha = 0.25f)

    /**
     * Applies an alpha to this element if [enabled] is false.
     */
    fun Modifier.enabledIf(enabled: Boolean) = if (enabled) this else alpha(DISABLED_ALPHA)

    /**
     * The set of material [Colors] to be applied application-wide; based on either the light theme if [light] is true
     * or the dark theme otherwise.
     */
    fun materialColors(light: Boolean = true): Colors {
        return if (light) {
            lightColors(primary = PRIMARY)
        } else {
            darkColors(primary = PRIMARY)
        }
    }
}
