package com.tiquionophist.ui.common

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Loads an [ImageBitmap] from the given [filename].
 *
 * Necessary (rather than useResource or painterResource) since default methods throw an exception if the file does not
 * exist. This method returns null instead.
 */
fun loadImageBitmapOrNull(filename: String): ImageBitmap? {
    val classLoader = Thread.currentThread().contextClassLoader
    return classLoader.getResourceAsStream(filename)?.use {
        androidx.compose.ui.res.loadImageBitmap(it)
    }
}
