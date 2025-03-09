package com.tiquionophist.io

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
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
        return unzip(ByteArrayInputStream(Base64.decode(data)))
    }

    /**
     * Returns the gzipped then Base64-encoded data written to the [OutputStream] in [block].
     */
    fun zipAndEncode(block: (OutputStream) -> Unit): String {
        return Base64.encode(zip(block))
    }
}
