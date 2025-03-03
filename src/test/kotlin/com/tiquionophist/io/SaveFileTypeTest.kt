package com.tiquionophist.io

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class SaveFileTypeTest {
    @ParameterizedTest
    @MethodSource("testSaveFiles")
    fun testOfFile(testSaveFile: SaveFileIOTest.TestSaveFile) {
        assertEquals(testSaveFile.type, SaveFileType.ofFile(testSaveFile.file))
    }

    companion object {
        @JvmStatic
        fun testSaveFiles() = SaveFileIOTest.testSaveFiles()
    }
}
