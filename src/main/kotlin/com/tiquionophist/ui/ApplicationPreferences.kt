package com.tiquionophist.ui

import androidx.compose.runtime.mutableStateOf
import java.io.File
import java.util.prefs.Preferences
import kotlin.reflect.KProperty

/**
 * Stores application-wide preferences which should be persisted between runs of the application.
 */
object ApplicationPreferences {
    private val prefs = Preferences.userNodeForPackage(ApplicationPreferences::class.java)

    /**
     * Whether the application should be in light mode.
     *
     * TODO use isSystemInDarkTheme() as the default (non-trivial since it's @Composable)
     */
    var lightMode: Boolean by BooleanPropertyDelegate(prefs = prefs, propertyName = "light_mode", defaultValue = true)

    /**
     * The default directory to open when choosing a file, or null if it has not been set.
     */
    var fileChooserDirectory: File? by FilePropertyDelegate(prefs = prefs, propertyName = "file_chooser_directory")

    /**
     * Delegates a boolean preference from [prefs], also storing it in a [MutableState] so that changes trigger a
     * recomposition.
     */
    private class BooleanPropertyDelegate(val prefs: Preferences, val propertyName: String, defaultValue: Boolean) {
        private val state = mutableStateOf(prefs.getBoolean(propertyName, defaultValue))

        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            return state.value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            prefs.putBoolean(propertyName, value)
            state.value = value
        }
    }

    /**
     * Delegates a [File] preference from [prefs].
     */
    private class FilePropertyDelegate(val prefs: Preferences, val propertyName: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): File? {
            return prefs.get(propertyName, "")
                .takeIf { it.isNotEmpty() }
                ?.let { File(it) }
                ?.takeIf { it.exists() }
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: File?) {
            if (value == null) {
                prefs.remove(propertyName)
            } else {
                prefs.put(propertyName, value.normalize().absolutePath)
            }
        }
    }
}
