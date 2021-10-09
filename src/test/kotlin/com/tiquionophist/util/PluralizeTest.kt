package com.tiquionophist.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PluralizeTest {
    @Test
    fun testPluralize() {
        assertEquals("units", "unit".pluralize(0))
        assertEquals("unit", "unit".pluralize(1))
        assertEquals("units", "unit".pluralize(2))
    }

    @Test
    fun testPluralizedCount() {
        assertEquals("0 units", "unit".pluralizedCount(0))
        assertEquals("1 unit", "unit".pluralizedCount(1))
        assertEquals("2 units", "unit".pluralizedCount(2))
    }
}
