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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import java.math.BigDecimal

@Composable
fun StatsTable(stats: StatSet, modifier: Modifier = Modifier) {
    val statsList: ImmutableList<Pair<Stat, StatValue>> = remember(stats) {
        stats.stats
            .map { it.key to StatValue(it.value) }
            .sortedBy { it.first.prettyName }
            .toImmutableList()
    }

    val horizontalDividers: ImmutableMap<Int, TableDivider> = remember(statsList.size) {
        List(statsList.size + 1) { rowIndex ->
            rowIndex to TableDivider(weak = rowIndex != 0 && rowIndex != statsList.size)
        }.toMap().toImmutableMap()
    }

    Table(
        columns = columns,
        rows = statsList,
        verticalDividers = verticalDividers,
        horizontalDividers = horizontalDividers,
        fillMaxWidth = true,
        modifier = modifier.width(IntrinsicSize.Min),
    )
}

@Immutable
private data class StatValue(val value: BigDecimal) {
    fun isGood(stat: Stat): Boolean = stat.positiveBetter == (value.signum() == 1)
}

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
            color = if (value.second.isGood(stat = value.first)) ThemeColors.current.good else ThemeColors.current.bad,
            modifier = Modifier.padding(Dimens.SPACING_2),
        )
    }
}

private val columns = persistentListOf(StatNameColumn, StatValueColumn)

private val verticalDividers = persistentMapOf(
    0 to TableDivider(dividerSize = 0.dp, paddingAfter = Dimens.SPACING_2),
    1 to TableDivider(weak = true),
    2 to TableDivider(dividerSize = 0.dp, paddingAfter = Dimens.SPACING_2),
)
