package com.tiquionophist.ui

import androidx.compose.ui.unit.dp

/**
 * Dimension constants used throughout the application.
 */
object Dimens {
    /**
     * Width of borders and dividers.
     */
    val BORDER_WIDTH = 1.dp

    /**
     * Small spacing.
     */
    val SPACING_1 = 4.dp

    /**
     * Medium spacing.
     */
    val SPACING_2 = 8.dp

    /**
     * Large spacing.
     */
    val SPACING_3 = 16.dp

    /**
     * Standard elevation for drop shadows.
     */
    val SHADOW_ELEVATION = 4.dp

    /**
     * Standard rounding for corners.
     */
    val CORNER_ROUNDING = 4.dp

    /**
     * Stroke width of loading spinners.
     */
    val PROGRESS_INDICATOR_STROKE_WIDTH = 2.dp

    object NumberPicker {
        val BUTTON_WIDTH = 32.dp
        val TEXT_FIELD_WIDTH = 50.dp
    }

    object ScheduleConfigurationTable {
        val TEACHER_IMAGE_WIDTH = 50.dp
    }

    object ScheduleTable {
        val MIN_CELL_WIDTH = 250.dp
    }
}
