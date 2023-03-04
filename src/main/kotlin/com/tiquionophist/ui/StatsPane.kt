package com.tiquionophist.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
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
import com.tiquionophist.ui.common.fillParent
import com.tiquionophist.util.prettyName
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import java.math.BigDecimal
import java.text.DecimalFormat

@Immutable
data class StatValue(val value: BigDecimal)

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
    private val format = DecimalFormat("0.00").apply {
        positivePrefix = "+"
    }

    override fun horizontalAlignment(rowIndex: Int) = Alignment.End

    @Composable
    override fun content(value: Pair<Stat, StatValue>) {
        Text(
            text = format.format(value.second.value),
            modifier = Modifier.padding(Dimens.SPACING_2),
        )
    }
}

/**
 * Shows the weekly stat effects of [GlobalState.scheduleConfiguration], placed at the right of the window.
 */
@Composable
fun StatsPane() {
    Surface(color = ThemeColors.current.surface2) {
        Box {
            val scrollState = rememberScrollState()
            MatchingWidthColumn(Modifier.verticalScroll(scrollState).widthIn(min = 300.dp), min = false) {
                Text(
                    maxLines = 1,
                    text = "Weekly class effects",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(Dimens.SPACING_2),
                )

                val stats = remember(GlobalState.scheduleConfiguration, GlobalState.currentClassIndex) {
                    GlobalState.scheduleConfiguration.classStats(classIndex = GlobalState.currentClassIndex ?: 0)
                        .stats
                        .mapValues { StatValue(it.value) }
                        .toList()
                        .sortedBy { it.first.prettyName }
                        .toImmutableList()
                }

                Table(
                    columns = persistentListOf(StatNameColumn, StatValueColumn),
                    rows = stats,
                    verticalDividers = persistentMapOf(
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
                    }.toMap().toImmutableMap(),
                    fillMaxWidth = true,
                    modifier = Modifier.width(IntrinsicSize.Min),
                )

                Spacer(Modifier.height(Dimens.SPACING_3))

                CheckboxWithLabel(
                    checked = GlobalState.showUnusedSubjects,
                    onCheckedChange = { GlobalState.showUnusedSubjects = it },
                ) {
                    Text(text = "Show unused subjects", maxLines = 1)
                }

                Spacer(Modifier.height(Dimens.SPACING_2))

                CheckboxWithLabel(
                    checked = GlobalState.showUnusedTeachers,
                    onCheckedChange = { GlobalState.showUnusedTeachers = it },
                ) {
                    Text(text = "Show unused teachers", maxLines = 1)
                }

                Spacer(Modifier.height(Dimens.SPACING_2))

                CheckboxWithLabel(
                    checked = GlobalState.showLockedSubjects,
                    onCheckedChange = { GlobalState.showLockedSubjects = it },
                    enabled = GlobalState.scheduleConfiguration.allowedSubjects != null,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_1)) {
                        Text(text = "Show locked subjects", maxLines = 1)

                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                            Text(
                                text = "Whether to show subjects which have not been unlocked in the school; " +
                                    "only available after importing from a game save file.",
                                fontSize = Dimens.FONT_SMALL,
                                modifier = Modifier.fillParent(),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(Dimens.SPACING_2))

                CheckboxWithLabel(
                    checked = GlobalState.showTeacherExp,
                    onCheckedChange = { GlobalState.showTeacherExp = it },
                    enabled = GlobalState.scheduleConfiguration.teacherExperience != null,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_1)) {
                        Text(text = "Show teacher experience", maxLines = 1)

                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                            Text(
                                text = "Whether to display the subject experience each teacher has in the scheduling " +
                                    "table; only available after importing from a game save file.",
                                fontSize = Dimens.FONT_SMALL,
                                modifier = Modifier.fillParent(),
                            )
                        }
                    }
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}
