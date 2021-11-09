package com.tiquionophist.ui.common

import com.tiquionophist.ui.ApplicationPreferences
import java.io.File
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Wraps file chooser operations.
 */
object FilePicker {
    private val jsonFileFilter = FileNameExtensionFilter("*.json", "json")
    val gameSaveFileFilter = FileNameExtensionFilter("*.sav", "sav")

    /**
     * Opens a file chooser to save a file, starting in [startingDirectory], and returns the chosen file if the user
     * selected one.
     */
    fun save(
        startingDirectory: File? = ApplicationPreferences.fileChooserDirectory ?: File("."),
        confirmOverwrite: Boolean = true
    ): File? {
        val fc = object : JFileChooser(startingDirectory) {
            override fun approveSelection() {
                val file = selectedFile.withJsonExtensionIfEmpty()
                if (confirmOverwrite && file.exists()) {
                    val result = JOptionPane.showConfirmDialog(
                        this,
                        "Overwrite existing file?",
                        "File exists",
                        JOptionPane.OK_CANCEL_OPTION
                    )

                    if (result == JOptionPane.OK_OPTION) {
                        super.approveSelection()
                    }
                } else {
                    super.approveSelection()
                }
            }
        }

        fc.addChoosableFileFilter(jsonFileFilter)
        fc.fileFilter = jsonFileFilter

        val result = fc.showSaveDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            fc.selectedFile.withJsonExtensionIfEmpty()
                .also {
                    ApplicationPreferences.fileChooserDirectory = it.parentFile
                }
        } else {
            null
        }
    }

    /**
     * Opens a file chooser to load a file, starting in [startingDirectory], and returns the chosen file if the user
     * selected one.
     */
    fun load(
        startingDirectory: File? = ApplicationPreferences.fileChooserDirectory ?: File("."),
        fileFilter: FileFilter = jsonFileFilter
    ): File? {
        val fc = JFileChooser(startingDirectory)

        fc.addChoosableFileFilter(fileFilter)
        fc.fileFilter = fileFilter

        val result = fc.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            fc.selectedFile
                .takeIf { it.exists() }
                ?.also {
                    ApplicationPreferences.fileChooserDirectory = it.parentFile
                }
        } else {
            null
        }
    }

    /**
     * Returns this [File] if it has an extension, otherwise returns a new File with ".json" as the extension.
     */
    private fun File.withJsonExtensionIfEmpty(): File {
        return if (extension.isEmpty()) {
            File(parentFile, "$nameWithoutExtension.json")
        } else {
            this
        }
    }
}
