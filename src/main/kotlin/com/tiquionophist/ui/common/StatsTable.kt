package com.tiquionophist.ui.common

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiquionophist.core.Stat
import com.tiquionophist.core.StatSet
import com.tiquionophist.ui.Dimens
import com.tiquionophist.ui.ThemeColors
import com.tiquionophist.util.prettyName
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import java.math.BigDecimal

@Immutable
private data class StatValue(val value: BigDecimal)

private object StatNameColumn : Column<Pair<Stat, StatValue>> {
    override fun horizontalAlignment(rowIndex: Int) = Alignment.Start

    @Composable
    override fun content(value: Pair<Stat, StatValue>) {
        Text(
            text = value.first.prettyName,
            modifier = Modifier.padding(Dimens.SPACING_2),
        )
    }
}

private object StatValueColumn : Column<Pair<Stat, StatValue>> {
    override fun horizontalAlignment(rowIndex: Int) = Alignment.End

    @Composable
    override fun content(value: Pair<Stat, StatValue>) {
        Text(
            text = Stat.format.format(value.second.value),
            modifier = Modifier.padding(Dimens.SPACING_2),
        )
    }
}

@Composable
fun StatsTable(stats: StatSet) {
    val statsList = remember(stats) {
        stats
            .stats
            .mapValues { StatValue(it.value) }
            .toList()
            .sortedBy { it.first.prettyName }
            .toImmutableList()
    }

    Table(
        columns = persistentListOf(StatNameColumn, StatValueColumn),
        rows = statsList,
        verticalDividers = persistentMapOf(
            0 to TableDivider(dividerSize = 0.dp, paddingAfter = Dimens.SPACING_2),
            1 to TableDivider(color = ThemeColors.current.weakDivider),
            2 to TableDivider(dividerSize = 0.dp, paddingAfter = Dimens.SPACING_2),
        ),
        horizontalDividers = List(statsList.size + 1) { rowIndex ->
            rowIndex to TableDivider(
                color = if (rowIndex == 0 || rowIndex == statsList.size) {
                    ThemeColors.current.divider
                } else {
                    ThemeColors.current.weakDivider
                }
            )
        }.toMap().toImmutableMap(),
        fillMaxWidth = true,
        modifier = Modifier.width(IntrinsicSize.Min),
    )
}
