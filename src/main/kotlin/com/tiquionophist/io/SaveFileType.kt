package com.tiquionophist.io

import com.tiquionophist.io.SaveFileType.SQL
import com.tiquionophist.io.SaveFileType.XML
import java.io.File

/**
 * Lists the types of top-level save files; [XML] is the legacy type with [SQL] introduced in HHS+ 1.10.5.
 */
enum class SaveFileType {
    XML, SQL;

    companion object {
        private const val SQL_HEADER_BYTES = 16

        /**
         * Determines the [SaveFileType] of the given [file].
         *
         * This is necessary because both formats use the same ".sav" extension. Instead, as a workaround, we check for
         * the SQLite database header: https://sqlite.org/fileformat2.html#database_header.
         */
        fun ofFile(file: File): SaveFileType {
            val header = file.inputStream()
                .use { it.readNBytes(SQL_HEADER_BYTES) }
                .decodeToString()

            return if (header == "SQLite format 3\u0000") SQL else XML
        }
    }
}
