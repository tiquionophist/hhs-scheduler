package com.tiquionophist.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tiquionophist.core.Stat
import com.tiquionophist.ui.common.CheckboxWithLabel
import com.tiquionophist.ui.common.Column
import com.tiquionophist.ui.common.MatchingWidthColumn
import com.tiquionophist.ui.common.Table
import com.tiquionophist.ui.common.TableDivider
import com.tiquionophist.util.prettyName
import java.math.BigDecimal
import java.math.RoundingMode

private object StatNameColumn : Column<Pair<Stat, BigDecimal>> {
    override fun horizontalAlignment(rowIndex: Int) = Alignment.Start

    @Composable
    override fun content(value: Pair<Stat, BigDecimal>) {
        Text(
            text = value.first.prettyName,
            modifier = Modifier.padding(Dimens.SPACING_2),
        )
    }
}

private object StatValueColumn : Column<Pair<Stat, BigDecimal>> {
    override fun horizontalAlignment(rowIndex: Int) = Alignment.End

    @Composable
    override fun content(value: Pair<Stat, BigDecimal>) {
        Text(
            text = value.second.setScale(2, RoundingMode.HALF_EVEN).toPlainString(),
            modifier = Modifier.padding(Dimens.SPACING_2),
        )
    }
}

/**
 * Shows the weekly stat effects of [GlobalState.scheduleConfiguration], placed at the right of the window.
 */
@Composable
fun StatsPane(classIndex: Int?) {
    Surface(color = ThemeColors.current.surface2) {
        Box(modifier = Modifier.fillMaxHeight()) {
            val scrollState = rememberScrollState()
            MatchingWidthColumn(Modifier.verticalScroll(scrollState), min = false) {
                Text(
                    maxLines = 1,
                    text = "Weekly class effects",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(Dimens.SPACING_2),
                )

                val stats = remember(GlobalState.scheduleConfiguration, classIndex) {
                    GlobalState.scheduleConfiguration.classStats(classIndex = classIndex ?: 0)
                        .stats
                        .toList()
                        .sortedBy { it.first.prettyName }
                }

                // TODO use new column width which fills remaining space but has minimum of ColumnWidth.MatchContent
                Table(
                    columns = listOf(StatNameColumn, StatValueColumn),
                    rows = stats,
                    verticalDividers = mapOf(
                        0 to TableDivider(dividerSize = 0.dp, paddingAfter = Dimens.SPACING_2),
                        1 to TableDivider(color = ThemeColors.current.weakDivider),
                        2 to TableDivider(dividerSize = 0.dp, paddingAfter = Dimens.SPACING_2),
                    ),
                    horizontalDividers = List(stats.size + 1) { rowIndex ->
                        rowIndex to TableDivider(
                            color = if (rowIndex == 0 || rowIndex == stats.size) {
                                ThemeColors.current.divider
                            } else {
                                ThemeColors.current.weakDivider
                            }
                        )
                    }.toMap()
                )

                Spacer(Modifier.height(Dimens.SPACING_3))

                CheckboxWithLabel(
                    checked = GlobalState.showUnusedSubjects,
                    onCheckedChange = { GlobalState.showUnusedSubjects = it },
                ) {
                    Text(text = "Show unused subjects", maxLines = 1)
                }

                CheckboxWithLabel(
                    checked = GlobalState.showUnusedTeachers,
                    onCheckedChange = { GlobalState.showUnusedTeachers = it },
                ) {
                    Text(text = "Show unused teachers", maxLines = 1)
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}
