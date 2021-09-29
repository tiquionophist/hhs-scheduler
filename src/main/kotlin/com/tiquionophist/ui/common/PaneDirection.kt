package com.tiquionophist.ui.common

/**
 * The relative direction of a [ContentWithPane] pane, e.g. [PaneDirection.TOP] places the pane above the content.
 */
enum class PaneDirection(val vertical: Boolean) {
    TOP(vertical = true),
    BOTTOM(vertical = true),
    LEFT(vertical = false),
    RIGHT(vertical = false),
}
