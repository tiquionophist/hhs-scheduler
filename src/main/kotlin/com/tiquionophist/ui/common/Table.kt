package com.tiquionophist.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tiquionophist.ui.Colors
import com.tiquionophist.ui.Dimens
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Represents a column in a [Table] whose rows have type [T], specifying how its contents are laid out and the [content]
 * of each row.
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
     * Whether the cell with the given row [value] should fill its space, i.e. have its max width and height determined
     * by the other values in the row/column. This means that there must be at least one value where this is false in
     * each row and column (to determine the column/row size).
     */
    fun fillCell(value: T): Boolean = false

    /**
     * Renders the content of the cell for the given row containing [value]. May be empty. Will automatically be aligned
     * within the cell by [horizontalAlignment] and [verticalAlignment].
     */
    @Composable
    fun content(value: T)
}

/**
 * A convenience wrapper for a [Column] which includes a special header row as the first row as a null value.
 */
interface ColumnWithHeader<T : Any> : Column<T?> {
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

    @Composable
    override fun content(value: T?) {
        if (value == null) header() else itemContent(value)
    }

    /**
     * Renders the header value for the column. May be empty. Will use the same [horizontalAlignment] and
     * [verticalAlignment] as other rows.
     */
    @Composable
    fun header() {
    }

    /**
     * Renders the content of a non-header row, from a row containing a [value].
     */
    @Composable
    fun itemContent(value: T)
}

/**
 * Specifies how the width of a column is determined.
 */
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
    data class Fill(val weight: Int = 1, val maxWidth: Dp? = null, val minWidth: Dp? = null) : ColumnWidth()
}

/**
 * Represents a horizontal or vertical divider between columns/rows in a [Table].
 */
data class TableDivider(
    val paddingBefore: Dp = 0.dp,
    val paddingAfter: Dp = 0.dp,
    val dividerSize: Dp = Dimens.BORDER_WIDTH,
    val color: Color? = null,
) {
    val totalSize = paddingBefore + paddingAfter + dividerSize
}

/**
 * A grid-based component which lays out a series of [columns], each taking values from [rows]. Each of [columns]
 * determines the content for each cell in the column and how it is laid out (e.g. how the column width is computed).
 *
 * [verticalDividers] and [horizontalDividers] may also be added between columns and rows, respectively. They are
 * specified as a map from the index of the column/row BEFORE which the divider should be placed to the [TableDivider]
 * value.
 *
 * TODO min intrinsic width/height are always a bit below what they should be, so content is cut off when scrolling
 */
@Composable
fun <T> Table(
    columns: List<Column<T>>,
    rows: List<T>,
    verticalDividers: Map<Int, TableDivider> = mapOf(),
    horizontalDividers: Map<Int, TableDivider> = mapOf(),
    fillMaxHeight: Boolean = false,
    modifier: Modifier = Modifier
) {
    val numCols = columns.size
    val numRows = rows.size

    val numHorizontalDividers = horizontalDividers.size
    val numVerticalDividers = verticalDividers.size

    require(numCols > 0) { "Table() must have non-empty columns" }
    require(numRows > 0) { "Table() must have non-empty rows" }

    val layoutDirection = LocalLayoutDirection.current

    Layout(
        content = {
            horizontalDividers.forEach { (_, divider) ->
                Box(
                    modifier = Modifier
                        .padding(top = divider.paddingBefore, bottom = divider.paddingAfter)
                        .fillMaxWidth()
                        .height(divider.dividerSize)
                        .background(divider.color ?: Colors.divider)
                )
            }

            verticalDividers.forEach { (_, divider) ->
                Box(
                    modifier = Modifier
                        .padding(start = divider.paddingBefore, end = divider.paddingAfter)
                        .fillMaxHeight()
                        .width(divider.dividerSize)
                        .background(divider.color ?: Colors.divider)
                )
            }

            columns.forEach { column ->
                rows.forEach { item ->
                    Box { column.content(item) }
                }
            }
        },
        modifier = modifier,
        measurePolicy = object : MeasurePolicy {
            override fun MeasureScope.measure(measurables: List<Measurable>, constraints: Constraints): MeasureResult {
                val horizontalDividerMeasurables = measurables.subList(fromIndex = 0, toIndex = numHorizontalDividers)
                val verticalDividerMeasurables = measurables.subList(
                    fromIndex = numHorizontalDividers,
                    toIndex = numHorizontalDividers + numVerticalDividers
                )
                val itemMeasurables = measurables.subList(
                    fromIndex = numHorizontalDividers + numVerticalDividers,
                    toIndex = measurables.size
                )

                // [rowIndex -> measurable for the divider for that row]
                val horizontalDividerMeasurablesByRow = horizontalDividers.keys
                    .withIndex()
                    .associate { (index, rowIndex) -> rowIndex to horizontalDividerMeasurables[index] }

                // [colIndex -> measurable for the divider for that col]
                val verticalDividerMeasurablesByCol = verticalDividers.keys
                    .withIndex()
                    .associate { (index, colIndex) -> colIndex to verticalDividerMeasurables[index] }

                // [colIndex -> [measurables in that column]]
                val measurablesByColumn = itemMeasurables.chunked(numRows)

                val placeables = Array(numCols) { arrayOfNulls<Placeable>(numRows) }
                val colWidths = arrayOfNulls<Int>(numCols)

                // set of cells that should fill their rows/columns as (colIndex, rowIndex)
                val filledCells = mutableSetOf<Pair<Int, Int>>()

                columns.forEachIndexed { colIndex, column ->
                    val width = column.width
                    if (width is ColumnWidth.Fixed) {
                        val fixedWidth = width.width.roundToPx()
                        colWidths[colIndex] = fixedWidth

                        val colMeasurables = measurablesByColumn[colIndex]
                        colMeasurables.forEachIndexed { rowIndex, measurable ->
                            if (column.fillCell(rows[rowIndex])) {
                                filledCells.add(Pair(colIndex, rowIndex))
                            } else {
                                val placeable = measurable.measure(Constraints(maxWidth = fixedWidth))
                                placeables[colIndex][rowIndex] = placeable
                            }
                        }
                    } else if (width is ColumnWidth.MatchContent) {
                        var maxWidth = 0
                        val colMeasurables = measurablesByColumn[colIndex]
                        colMeasurables.forEachIndexed { rowIndex, measurable ->
                            if (column.fillCell(rows[rowIndex])) {
                                filledCells.add(Pair(colIndex, rowIndex))
                            } else {
                                val placeable = measurable.measure(Constraints())
                                placeables[colIndex][rowIndex] = placeable
                                maxWidth = max(maxWidth, placeable.width)
                            }
                        }

                        colWidths[colIndex] = maxWidth
                    }
                }

                val fillColumns = columns.filter { it.width is ColumnWidth.Fill }
                if (fillColumns.isNotEmpty()) {
                    val totalFillWeight = fillColumns.sumOf { (it.width as ColumnWidth.Fill).weight }
                    val usedWidth =
                        colWidths.sumOf { it ?: 0 } + verticalDividers.values.sumOf { it.totalSize.roundToPx() }
                    val remainingWidth = constraints.maxWidth - usedWidth

                    columns.forEachIndexed { colIndex, column ->
                        val columnWidth = column.width
                        if (columnWidth is ColumnWidth.Fill) {
                            val minPx = columnWidth.minWidth?.roundToPx() ?: 0
                            val maxPx = columnWidth.maxWidth?.roundToPx() ?: Int.MAX_VALUE
                            val colWidth = ((remainingWidth * columnWidth.weight).toDouble() / totalFillWeight)
                                .roundToInt()
                                .coerceAtLeast(minPx)
                                .coerceAtMost(maxPx)

                            val colMeasurables = measurablesByColumn[colIndex]
                            colMeasurables.forEachIndexed { rowIndex, measurable ->
                                if (column.fillCell(rows[rowIndex])) {
                                    filledCells.add(Pair(colIndex, rowIndex))
                                } else {
                                    val placeable = measurable.measure(Constraints(maxWidth = colWidth))
                                    placeables[colIndex][rowIndex] = placeable
                                }
                            }

                            colWidths[colIndex] = colWidth
                        }
                    }
                }

                // [rowIndex -> height of the row in px]
                val rowHeights = Array(numRows) { rowIndex ->
                    columns.indices.maxOf { colIndex ->
                        placeables[colIndex][rowIndex]?.height ?: 0
                    }
                }

                // we need to compute these width/heights before measuring the dividers themselves so that we know the
                // total width and height
                val verticalDividerWidths = verticalDividers.mapValues { it.value.totalSize.roundToPx() }
                val horizontalDividerHeights = horizontalDividers.mapValues { it.value.totalSize.roundToPx() }

                if (fillMaxHeight) {
                    val totalRowHeight = rowHeights.sum()
                    val baseTotalHeight = totalRowHeight + horizontalDividerHeights.values.sum()
                    val missing = constraints.maxHeight - baseTotalHeight
                    if (missing > 0) {
                        // TODO not pixel-perfect: might under/over round some pixels
                        val ratio = (totalRowHeight + missing).toFloat() / totalRowHeight
                        repeat(numRows) { rowIndex ->
                            rowHeights[rowIndex] = (rowHeights[rowIndex] * ratio).roundToInt()
                        }
                    }
                }

                // place filled cells, which need the cell width and height to be pre-determined
                filledCells.forEach { (colIndex, rowIndex) ->
                    val measurable = measurablesByColumn[colIndex][rowIndex]
                    val colWidth = requireNotNull(colWidths[colIndex])
                    val rowHeight = rowHeights[rowIndex]
                    val placeable = measurable.measure(Constraints(maxWidth = colWidth, maxHeight = rowHeight))
                    placeables[colIndex][rowIndex] = placeable
                }

                val totalWidth = colWidths.sumOf { requireNotNull(it) { "null col width" } } +
                        verticalDividerWidths.values.sum()
                val totalHeight = (rowHeights.sum() + horizontalDividerHeights.values.sum())
                    .coerceAtLeast(0)

                val horizontalDividerPlaceables = horizontalDividerMeasurablesByRow.mapValues { (_, measurable) ->
                    measurable.measure(Constraints(maxWidth = totalWidth, maxHeight = totalHeight))
                }

                val verticalDividerPlaceables = verticalDividerMeasurablesByCol.mapValues { (_, measurable) ->
                    measurable.measure(Constraints(maxWidth = totalWidth, maxHeight = totalHeight))
                }

                return layout(totalWidth, totalHeight) {
                    var y = 0
                    repeat(numRows) { rowIndex ->
                        horizontalDividerPlaceables[rowIndex]?.place(x = 0, y = y)

                        horizontalDividerHeights[rowIndex]?.let { y += it }

                        val rowHeight = rowHeights[rowIndex]

                        var x = 0
                        repeat(numCols) { colIndex ->
                            if (rowIndex == 0) {
                                verticalDividerPlaceables[colIndex]?.place(x = x, y = 0)
                            }

                            verticalDividerWidths[colIndex]?.let { x += it }

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

            override fun IntrinsicMeasureScope.minIntrinsicWidth(
                measurables: List<IntrinsicMeasurable>,
                height: Int
            ): Int {
                var totalWidth = 0

                val measurablesByColumn = if (columns.any { it.width is ColumnWidth.MatchContent }) {
                    val itemMeasurables = measurables.subList(
                        fromIndex = numHorizontalDividers + numVerticalDividers,
                        toIndex = measurables.size
                    )
                    itemMeasurables.chunked(numRows)
                } else {
                    null
                }

                columns.forEachIndexed { colIndex, column ->
                    totalWidth += columnWidth(
                        column = column,
                        rows = rows,
                        columnMeasurables = if (column.width is ColumnWidth.MatchContent) {
                            measurablesByColumn!![colIndex]
                        } else {
                            null
                        },
                    )
                }

                totalWidth += verticalDividers.values.sumOf { it.totalSize.roundToPx() }

                return totalWidth
            }

            override fun IntrinsicMeasureScope.minIntrinsicHeight(
                measurables: List<IntrinsicMeasurable>,
                width: Int
            ): Int {
                var totalHeight = 0

                val itemMeasurables = measurables.subList(
                    fromIndex = numHorizontalDividers + numVerticalDividers,
                    toIndex = measurables.size
                )
                val measurablesByColumn = itemMeasurables.chunked(numRows)

                val columnWidths = Array(numCols) { colIndex ->
                    columnWidth(columns[colIndex], rows, measurablesByColumn[colIndex])
                }

                repeat(numRows) { rowIndex ->
                    var maxRowHeight = 0
                    repeat(numCols) { colIndex ->
                        if (!columns[colIndex].fillCell(rows[rowIndex])) {
                            maxRowHeight = max(
                                maxRowHeight,
                                measurablesByColumn[colIndex][rowIndex]
                                    .minIntrinsicHeight(width = columnWidths[colIndex])
                            )
                        }
                    }
                    totalHeight += maxRowHeight
                }

                totalHeight += horizontalDividers.values.sumOf { it.totalSize.roundToPx() }

                return totalHeight
            }

            private fun <T> Density.columnWidth(
                column: Column<T>,
                rows: List<T>,
                columnMeasurables: List<IntrinsicMeasurable>?,
            ): Int {
                return when (val width = column.width) {
                    is ColumnWidth.Fixed -> width.width.roundToPx()
                    is ColumnWidth.MatchContent -> {
                        var maxWidth = 0
                        repeat(rows.size) { rowIndex ->
                            if (!column.fillCell(rows[rowIndex])) {
                                maxWidth = max(
                                    columnMeasurables!![rowIndex].maxIntrinsicWidth(Constraints.Infinity),
                                    maxWidth
                                )
                            }
                        }
                        maxWidth
                    }
                    is ColumnWidth.Fill -> width.minWidth?.roundToPx() ?: 0
                }
            }
        }
    )
}
