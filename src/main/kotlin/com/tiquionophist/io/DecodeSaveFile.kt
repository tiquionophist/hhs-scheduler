package com.tiquionophist.io

import java.io.File

/**
 * A standalone program that decodes the encoded-and-gzipped game save file data to an XML file, for ease of inspecting.
 */
fun main(args: Array<String>) {
    val filename = requireNotNull(args.firstOrNull()) { "must provide an argument with the filename" }
    val sourceFile = File(filename)
    val destinationFile = args.getOrNull(1)?.let { File(it) }
        ?: sourceFile.resolveSibling("${sourceFile.nameWithoutExtension}-DECODED.xml")

    val saveFile = SaveFileIO.xmlMapper.readValue(sourceFile, SaveFile::class.java)
    EncodingUtil.decodeAndUnzip(saveFile.data).use { inputStream ->
        destinationFile.outputStream().use { outputStream ->
            inputStream.transferTo(outputStream)
        }
    }

    println("Decoded save file written to ${destinationFile.absolutePath}")
}
