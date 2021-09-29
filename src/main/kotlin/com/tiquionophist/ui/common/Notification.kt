package com.tiquionophist.ui.common

import androidx.compose.ui.graphics.Color
import kotlin.time.Duration

/**
 * A notification which can be displayed by [NotificationContainer].
 */
data class Notification(
    val title: String,
    val message: String,
    val iconFilename: String? = null,
    val iconTint: Color? = null,
    val duration: Duration? = @Suppress("MagicNumber") Duration.seconds(5),
)
