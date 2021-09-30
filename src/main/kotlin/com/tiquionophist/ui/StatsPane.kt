package com.tiquionophist.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiquionophist.core.ScheduleConfiguration
import com.tiquionophist.core.Stat
import com.tiquionophist.ui.common.Column
import com.tiquionophist.ui.common.Table
import com.tiquionophist.ui.common.TableDivider
import com.tiquionophist.ui.common.leftBorder
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
 * Shows the weekly stat effects of [configuration], placed at the right of the window.
 */
@Composable
fun StatsPane(configuration: ScheduleConfiguration) {
    Box(modifier = Modifier.fillMaxHeight().leftBorder()) {
        val scrollState = rememberScrollState()
        Column(Modifier.verticalScroll(scrollState)) {
            Text(
                maxLines = 1,
                text = "Weekly class effects",
                modifier = Modifier.padding(Dimens.SPACING_2).align(Alignment.CenterHorizontally)
            )

            val stats = remember(configuration) {
                configuration.classStats.stats
                    .toList()
                    .sortedBy { it.first.prettyName }
            }

            Table(
                columns = listOf(StatNameColumn, StatValueColumn),
                rows = stats,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalDividers = mapOf(
                    0 to TableDivider(dividerSize = 0.dp, paddingAfter = Dimens.SPACING_2),
                    1 to TableDivider(color = Colors.weakDivider),
                    2 to TableDivider(dividerSize = 0.dp, paddingAfter = Dimens.SPACING_2),
                ),
                horizontalDividers = List(stats.size + 1) { rowIndex ->
                    rowIndex to TableDivider(color = Colors.weakDivider)
                }.toMap()
            )
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}
