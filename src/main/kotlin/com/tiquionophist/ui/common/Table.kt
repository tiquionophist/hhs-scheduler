package com.tiquionophist.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Represents a column in a [Table].
 */
interface Column<T> {
    /**
     * Specifies how the column width is determined.
     */
    val width: ColumnWidth
        get() = ColumnWidth.MatchContent

    /**
     * Specifies how the item at [rowIndex] is placed horizontally within the bounding box for its cell.
     */
    fun horizontalAlignment(rowIndex: Int): Alignment.Horizontal = Alignment.CenterHorizontally

    /**
     * Specifies how the item at [rowIndex] is placed vertically within the bounding box for its cell.
     */
    fun verticalAlignment(rowIndex: Int): Alignment.Vertical = Alignment.CenterVertically

    /**
     * The contents of the column, each of which is rendered by [content].
     */
    val rows: List<T>

    /**
     * Renders the content of the cell for the given row containing [value]. May be empty. Will automatically be aligned
     * within the cell by [horizontalAlignment] and [verticalAlignment].
     */
    @Composable
    fun content(value: T)
}

/**
 * A convenience wrapper for a [Column] which includes a special header row as the first row.
 */
interface ColumnWithHeader<T : Any> : Column<T?> {
    /**
     * The non-header items in the column.
     */
    val items: List<T>

    val headerHorizontalAlignment: Alignment.Horizontal
        get() = Alignment.CenterHorizontally

    val headerVerticalAlignment: Alignment.Vertical
        get() = Alignment.CenterVertically

    val itemHorizontalAlignment: Alignment.Horizontal
        get() = Alignment.CenterHorizontally

    val itemVerticalAlignment: Alignment.Vertical
        get() = Alignment.CenterVertically

    override fun verticalAlignment(rowIndex: Int): Alignment.Vertical {
        return if (rowIndex == 0) headerVerticalAlignment else itemVerticalAlignment
    }

    override fun horizontalAlignment(rowIndex: Int): Alignment.Horizontal {
        return if (rowIndex == 0) headerHorizontalAlignment else itemHorizontalAlignment
    }

    override val rows: List<T?>
        get() = listOf(null).plus(items)

    @Composable
    override fun content(value: T?) {
        if (value == null) header() else itemContent(value)
    }

    /**
     * Renders the header value for the column. May be empty. Will use the same [horizontalAlignment] and
     * [verticalAlignment] as other rows.
     */
    @Composable
    fun header() {}

    /**
     * Renders the content of a non-header row, from a row containing a [value].
     */
    @Composable
    fun itemContent(value: T)
}

sealed class ColumnWidth {
    /**
     * Fixed column width of [width].
     */
    data class Fixed(val width: Dp) : ColumnWidth()

    /**
     * Fill the maximum width of any row.
     */
    object MatchContent : ColumnWidth()

    /**
     * Fill all remaining space in the table. If multiple columns have [Fill] width, space will be distributed relative
     * to their [weight].
     */
    data class Fill(val weight: Int = 1) : ColumnWidth()
}

@Composable
fun Table(columns: List<Column<*>>, modifier: Modifier = Modifier) {
    require(columns.isNotEmpty()) { "Table() must have non-empty columns" }
    val numCols = columns.size
    val numRows = columns.first().rows.size
    require(numRows > 0) { "Table() must have non-empty rows" }
    require(columns.all { it.rows.size == numRows }) { "all columns must have the same number of rows" }

    // parameterized extension to avoid type erasure when calling content()
    @Composable
    fun <T> Column<T>.rowContent() {
        rows.forEach {
            Box { content(it) }
        }
    }

    val layoutDirection = LocalLayoutDirection.current

    Layout(
        content = {
            columns.forEach { column ->
                column.rowContent()
            }
        },
        modifier = modifier,
        measurePolicy = { measurables: List<Measurable>, constraints: Constraints ->
            // [colIndex -> [measurables in that column]]
            val measurablesByColumn = measurables.chunked(numRows)

            val placeables = Array(numCols) { arrayOfNulls<Placeable>(numRows) }
            val colWidths = arrayOfNulls<Int>(numCols)

            columns.forEachIndexed { colIndex, column ->
                val width = column.width
                if (width is ColumnWidth.Fixed) {
                    val fixedWidth = width.width.roundToPx()
                    colWidths[colIndex] = fixedWidth

                    val colMeasurables = measurablesByColumn[colIndex]
                    colMeasurables.forEachIndexed { rowIndex, measurable ->
                        val placeable = measurable.measure(Constraints(maxWidth = fixedWidth))
                        placeables[colIndex][rowIndex] = placeable
                    }
                } else if (width is ColumnWidth.MatchContent) {
                    var maxWidth = 0
                    val colMeasurables = measurablesByColumn[colIndex]
                    colMeasurables.forEachIndexed { rowIndex, measurable ->
                        val placeable = measurable.measure(Constraints())
                        placeables[colIndex][rowIndex] = placeable
                        maxWidth = max(maxWidth, placeable.width)
                    }

                    colWidths[colIndex] = maxWidth
                }
            }

            val fillColumns = columns.filter { it.width is ColumnWidth.Fill }
            if (fillColumns.isNotEmpty()) {
                val totalFillWeight = fillColumns.sumOf { (it.width as ColumnWidth.Fill).weight }
                val usedWidth = colWidths.sumOf { it ?: 0 }
                val remainingWidth = constraints.maxWidth - usedWidth

                columns.forEachIndexed { colIndex, column ->
                    if (column.width is ColumnWidth.Fill) {
                        val colWidth =
                            ((remainingWidth * (column.width as ColumnWidth.Fill).weight).toDouble() / totalFillWeight)
                                .roundToInt()

                        val colMeasurables = measurablesByColumn[colIndex]
                        colMeasurables.forEachIndexed { rowIndex, measurable ->
                            val placeable = measurable.measure(Constraints(maxWidth = colWidth))
                            placeables[colIndex][rowIndex] = placeable
                        }

                        colWidths[colIndex] = colWidth
                    }
                }
            }

            val rowHeights = Array(numRows) { rowIndex ->
                columns.indices.maxOf { colIndex ->
                    val placeable = requireNotNull(placeables[colIndex][rowIndex]) { "null placeable" }
                    placeable.height
                }
            }

            val totalWidth = colWidths.sumOf { it!! }
            val totalHeight = rowHeights.sum()

            layout(totalWidth, totalHeight) {
                var y = 0
                repeat(numRows) { rowIndex ->
                    val rowHeight = rowHeights[rowIndex]

                    var x = 0
                    repeat(numCols) { colIndex ->
                        val column = columns[colIndex]
                        val colWidth = requireNotNull(colWidths[colIndex]) { "null col width" }
                        val placeable = requireNotNull(placeables[colIndex][rowIndex]) { "null placeable" }

                        placeable.place(
                            x + column.horizontalAlignment(rowIndex = rowIndex).align(
                                size = placeable.width,
                                space = colWidth,
                                layoutDirection = layoutDirection
                            ),
                            y + column.verticalAlignment(rowIndex = rowIndex).align(
                                size = placeable.height,
                                space = rowHeight
                            ),
                        )
                        x += colWidth
                    }

                    y += rowHeight
                }
            }
        }
    )
}
