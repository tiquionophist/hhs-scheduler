package com.tiquionophist.ui.common

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap

/**
 * Loads an [ImageBitmap] from the given [filename].
 *
 * Necessary (rather than useResource or painterResource) since default methods throw an exception if the file does not
 * exist. This method returns null instead.
 */
@OptIn(ExperimentalResourceApi::class)
fun loadImageBitmapOrNull(filename: String): ImageBitmap? {
    val classLoader = Thread.currentThread().contextClassLoader
    return classLoader.getResourceAsStream(filename)?.use {
        it.readAllBytes().decodeToImageBitmap()
    }
}
