package com.tiquionophist.io

import com.tiquionophist.io.SaveFileIOTest.Companion.testFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SaveFileTypeTest {
    @ParameterizedTest
    @EnumSource(SaveFileType::class)
    fun testOfFile(saveFileType: SaveFileType) {
        assertEquals(saveFileType, SaveFileType.ofFile(saveFileType.testFile))
    }
}
