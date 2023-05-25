package com.tiquionophist.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object EncodingUtil {
    /**
     * Returns an [InputStream] which contains the gunzipped stream of the contents of [inputStream].
     */
    fun unzip(inputStream: InputStream): InputStream {
        return GZIPInputStream(inputStream)
    }

    /**
     * Returns the gzipped data written to the [OutputStream] in [block].
     */
    fun zip(block: (OutputStream) -> Unit): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteArrayOutputStream).use(block)
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * Returns an [InputStream] which contains the Base64-decoded then gunzipped stream of the contents of [data].
     */
    fun decodeAndUnzip(data: String): InputStream {
        val decoded = Base64.getDecoder().decode(data)
        return unzip(ByteArrayInputStream(decoded))
    }

    /**
     * Returns the gzipped then Base64-encoded data written to the [OutputStream] in [block].
     */
    fun zipAndEncode(block: (OutputStream) -> Unit): String {
        val encoded = Base64.getEncoder().encode(zip(block))
        return encoded.decodeToString()
    }
}
