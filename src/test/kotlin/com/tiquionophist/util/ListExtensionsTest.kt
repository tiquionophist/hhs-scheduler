package com.tiquionophist.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ListExtensionsTest {
    @ParameterizedTest
    @MethodSource
    fun testToTableString(data: Pair<List<List<String>>, String>) {
        assertEquals(data.second, data.first.toTableString())
    }

    companion object {
        @JvmStatic
        fun testToTableString(): List<Pair<List<List<String>>, String>> {
            return listOf(
                Pair(
                    listOf(listOf("a")),
                    """
                    -----
                    | a |
                    -----
                    """.trimIndent()
                ),
                Pair(
                    listOf(listOf("a", "bb"), listOf("ccc", "d")),
                    """
                    ------------
                    | a  | ccc |
                    ------------
                    | bb | d   |
                    ------------
                    """.trimIndent()
                )
            )
        }
    }
}
