package com.tiquionophist.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class EnumExtensionsTest {
    enum class TestEnum(val expectedPrettyName: String) {
        FIRST_VALUE("First Value"), SECOND_VALUE("Second Value"), VALUE_3("Value 3"), ANOTHER("Another")
    }

    @ParameterizedTest
    @EnumSource(TestEnum::class)
    fun testPrettyName(enum: TestEnum) {
        assertEquals(enum.expectedPrettyName, enum.prettyName)
    }
}
