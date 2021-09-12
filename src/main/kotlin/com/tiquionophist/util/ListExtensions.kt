package com.tiquionophist.util

/**
 * Converts this two-dimensional list into a table. The list is expected to be in column-major order, i.e. this[0][0] is
 * the top-right elements, this[0][1] is the element below it, etc.
 *
 * [padding] denotes the number of spaces added before and after each string in the table.
 */
fun List<List<String>>.toTableString(padding: Int = 1): String {
    require(isNotEmpty())
    val height = first().size
    require(all { it.size == height }) // all columns must be the same height

    val mapped = map { col -> col.map { " ".repeat(padding) + it + " ".repeat(padding) } }

    val sb = StringBuilder()

    val colWidths = mapped.map { column -> column.maxOf { it.length } }
    val totalWidth = colWidths.sum()
    val totalWithWithPadding = totalWidth + size + 1

    val horizontalLine = "-".repeat(totalWithWithPadding)
    sb.appendLine(horizontalLine)

    for (row in 0 until height) {
        sb.append("|")

        mapped.forEachIndexed { colIndex, column ->
            val text = column[row].padEnd(length = colWidths[colIndex])
            sb.append(text)
            sb.append("|")
        }

        sb.appendLine()
        sb.appendLine(horizontalLine)
    }

    return sb.toString().trim()
}
