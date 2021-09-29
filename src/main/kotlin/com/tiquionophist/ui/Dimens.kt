package com.tiquionophist.ui

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Dimension constants used throughout the application.
 */
object Dimens {
    /**
     * Width of borders and dividers.
     */
    val BORDER_WIDTH = 1.dp

    val SPACING_1 = 4.dp
    val SPACING_2 = 8.dp
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

    object Dialog {
        val MIN_WIDTH = 200.dp
        val MAX_WIDTH = 500.dp
        val TITLE_FONT_SIZE = 20.sp
    }
}
