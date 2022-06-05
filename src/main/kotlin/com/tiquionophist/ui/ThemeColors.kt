package com.tiquionophist.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.enabledIf(enabled: Boolean) = if (enabled) this else alpha(ThemeColors.current.disabledAlpha)

private fun Color(all: Int) = Color(red = all, green = all, blue = all)

data class ThemeColors(
    private val materialColors: Colors,

    val primary: Color = Color(red = 42, green = 149, blue = 232),
    val selected: Color = Color(red = 22, green = 199, blue = 28),

    val divider: Color,
    val weakDivider: Color,

    // lowest elevation
    val surface1: Color = materialColors.surface,

    // medium
    val surface2: Color = materialColors.surface,

    // highest elevation
    val surface3: Color = materialColors.surface,

    val disabledAlpha: Float = 0.3f,

    val expStart: Color = Color.Red.copy(alpha = disabledAlpha),
    val expStop: Color = Color.Green.copy(alpha = disabledAlpha),
) {
    companion object {
        private val light = ThemeColors(
            materialColors = lightColors(),

            divider = Color(all = 220),
            weakDivider = Color(all = 235),

            surface1 = Color(all = 248),
            surface2 = Color(all = 234),
            surface3 = Color(all = 220),
        )
        private val dark = ThemeColors(
            materialColors = darkColors(),

            divider = Color(all = 64),
            weakDivider = Color(all = 42),

            surface1 = Color(all = 22),
            surface2 = Color(all = 36),
            surface3 = Color(all = 50),
        )

        val current: ThemeColors
            @Composable
            get() = provider.current

        private val provider = compositionLocalOf<ThemeColors> { error("no theme colors provided") }

        @Composable
        fun apply(light: Boolean, content: @Composable () -> Unit) {
            CompositionLocalProvider(provider provides if (light) this.light else dark) {
                val current = provider.current
                MaterialTheme(
                    colors = current.materialColors.copy(primary = current.primary, surface = current.surface1),
                    content = content,
                )
            }
        }
    }
}
