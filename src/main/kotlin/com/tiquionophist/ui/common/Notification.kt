package com.tiquionophist.ui.common

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A notification which can be displayed by [NotificationContainer].
 */
data class Notification(
    val title: String,
    val message: String,
    val iconRes: DrawableResource? = null,
    val iconTint: Color? = null,
    val duration: Duration? = 5.seconds,
    val progress: Double? = null,
)
