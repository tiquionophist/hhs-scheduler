package com.tiquionophist.ui

import androidx.compose.runtime.mutableStateOf
import java.util.prefs.Preferences
import kotlin.reflect.KProperty

/**
 * Stores application-wide preferences which should be persisted between runs of the application.
 */
object ApplicationPreferences {
    private val prefs = Preferences.userNodeForPackage(ApplicationPreferences::class.java)

    /**
     * Whether the application should be in light mode.
     */
    var lightMode by BooleanPropertyDelegate(prefs = prefs, propertyName = "light_mode", defaultValue = true)

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
}
