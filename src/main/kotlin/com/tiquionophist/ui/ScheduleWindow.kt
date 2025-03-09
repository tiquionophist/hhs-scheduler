package com.tiquionophist.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import com.tiquionophist.Res
import com.tiquionophist.app_icon
import com.tiquionophist.ui.common.MatchingWidthColumn
import org.jetbrains.compose.resources.painterResource

/**
 * Wraps global handling of the computed schedule dialog windows.
 *
 * TODO revisit
 */
object ScheduleWindowHandler {
    @Composable
    fun content() {
        GlobalState.computedSchedules.forEach { computedSchedule ->
            ScheduleWindow(
                computedSchedule = computedSchedule,
                onClose = {
                    GlobalState.computedSchedules = GlobalState.computedSchedules
                        .filter { it.index != computedSchedule.index }
                }
            )
        }
    }
}

/**
 * A pop-up window which displays a [computedSchedule].
 */
@Composable
fun ScheduleWindow(
    computedSchedule: ComputedSchedule,
    onClose: () -> Unit
) {
    Window(
        title = "Schedule ${computedSchedule.index + 1}",
        icon = painterResource(Res.drawable.app_icon),
        onCloseRequest = onClose,
        resizable = false,
        state = rememberWindowState(
            width = Dp.Unspecified,
            height = Dp.Unspecified,
            position = WindowPosition.Aligned(Alignment.Center),
        ),
    ) {
        Surface {
            MatchingWidthColumn {
                var selectedClassIndex by remember { mutableIntStateOf(0) }

                Row {
                    repeat(computedSchedule.configuration.classes) { classIndex ->
                        val selected = selectedClassIndex == classIndex
                        Button(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            onClick = { selectedClassIndex = classIndex },
                            shape = RectangleShape,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = if (selected) {
                                    MaterialTheme.colors.primary
                                } else {
                                    MaterialTheme.colors.background
                                }
                            )
                        ) {
                            Text(
                                text = "Class ${classIndex + 1}",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                ScheduleTable(
                    configuration = computedSchedule.configuration,
                    schedule = computedSchedule.schedule,
                    classIndex = selectedClassIndex,
                )

                Divider(color = ThemeColors.current.divider)

                Spacer(Modifier.height(Dimens.SPACING_2))

                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2, Alignment.End)) {
                    RunScheduleButton(
                        schedulerSettings = computedSchedule.schedulerSettings,
                        configuration = computedSchedule.configuration,
                        runText = "Regenerate",
                        runImageVector = Icons.Default.Refresh,
                    )

                    var exporting by remember { mutableStateOf(false) }
                    Button(onClick = { exporting = true }) {
                        Image(imageVector = Icons.Default.Share, contentDescription = null)

                        Spacer(Modifier.width(Dimens.SPACING_2))

                        Text("Export to game save")
                    }

                    if (exporting) {
                        ScheduleExportDialog(
                            computedSchedule = computedSchedule,
                            onClose = { exporting = false },
                        )
                    }
                }
            }
        }
    }
}
