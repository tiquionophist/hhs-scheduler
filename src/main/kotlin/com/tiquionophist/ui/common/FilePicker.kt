package com.tiquionophist.ui.common

import java.io.File
import javax.swing.JFileChooser

/**
 * Wraps file chooser operations.
 */
object FilePicker {
    /**
     * Opens a file chooser to save a file, starting in [startingDirectory], and returns the chosen file if the user
     * selected one.
     *
     * TODO polish (add confirm dialog on overwrite, etc)
     */
    fun save(startingDirectory: File? = File(".")): File? {
        val fc = JFileChooser(startingDirectory)
        val result = fc.showSaveDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            fc.selectedFile
        } else {
            null
        }
    }

    /**
     * Opens a file chooser to load a file, starting in [startingDirectory], and returns the chosen file if the user
     * selected one.
     *
     * TODO polish (add confirm dialog on overwrite, etc)
     */
    fun load(startingDirectory: File? = File(".")): File? {
        val fc = JFileChooser(startingDirectory)
        val result = fc.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            fc.selectedFile
        } else {
            null
        }
    }
}
