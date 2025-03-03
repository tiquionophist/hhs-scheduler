package com.tiquionophist.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tiquionophist.ui.common.CheckboxWithLabel
import com.tiquionophist.ui.common.MatchingWidthColumn
import com.tiquionophist.ui.common.StatsTable

/**
 * Shows the weekly stat effects of [GlobalState.scheduleConfiguration], placed at the right of the window.
 */
@Composable
fun StatsPane() {
    Surface(color = ThemeColors.current.surface2) {
        Box {
            val scrollState = rememberScrollState()
            MatchingWidthColumn(Modifier.verticalScroll(scrollState).widthIn(min = 300.dp)) {
                Text(
                    maxLines = 1,
                    text = "Weekly class effects",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(Dimens.SPACING_2),
                )

                val stats = remember(GlobalState.scheduleConfiguration, GlobalState.currentClassIndex) {
                    GlobalState.scheduleConfiguration.classStats(classIndex = GlobalState.currentClassIndex ?: 0)
                }

                StatsTable(stats = stats)

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
