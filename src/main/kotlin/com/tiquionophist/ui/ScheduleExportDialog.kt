package com.tiquionophist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.rememberDialogState
import com.tiquionophist.io.SaveFileIO
import com.tiquionophist.ui.common.FilePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private sealed class ExportStatus {
    object NotStarted : ExportStatus()
    object InProgress : ExportStatus()
    class Error(val throwable: Throwable) : ExportStatus()
    object Success : ExportStatus()
}

@Composable
fun ScheduleExportDialog(
    computedSchedule: ComputedSchedule,
    onClose: () -> Unit,
) {
    Dialog(
        state = rememberDialogState(size = DpSize(width = Dp.Unspecified, height = Dp.Unspecified)),
        title = "Export schedule",
        onCloseRequest = onClose,
    ) {
        Surface(elevation = Dimens.TOOLTIP_ELEVATION) {
            var status by remember { mutableStateOf<ExportStatus>(ExportStatus.NotStarted) }

            Column(
                modifier = Modifier.padding(Dimens.SPACING_2),
                verticalArrangement = Arrangement.spacedBy(Dimens.SPACING_3),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)) {
                    Icon(
                        modifier = Modifier.size(Dimens.NOTIFICATION_ICON_SIZE),
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                    )

                    Text(
                        modifier = Modifier.widthIn(max = Dimens.Dialog.MAX_TEXT_WIDTH),
                        text = """
                        Copying this schedule to an existing save file is complicated and has a chance of corrupting the
                        data. As a precaution, the export will write to a copy of your game file without touching the
                        original. Choose an existing save file and a copy will be saved named
                        "${exportFilename(sourceFilename = "mySave", sourceExtension = "sav")}" with this schedule as
                        its timetable. Please report any issues you see with exporting.
                        """
                            .trim()
                            .replace("""\s+""".toRegex(), " "),
                    )
                }

                when (status) {
                    ExportStatus.NotStarted -> {
                        val coroutineScope = rememberCoroutineScope { Dispatchers.Default }
                        Button(
                            modifier = Modifier.align(Alignment.End),
                            onClick = {
                                FilePicker.load(fileFilter = FilePicker.gameSaveFileFilter)?.let { sourceFile ->
                                    status = ExportStatus.InProgress

                                    coroutineScope.launch {
                                        val result = runCatching {
                                            val destinationFile = sourceFile.resolveSibling(
                                                exportFilename(
                                                    sourceFilename = sourceFile.nameWithoutExtension,
                                                    sourceExtension = sourceFile.extension,
                                                )
                                            )

                                            SaveFileIO.write(
                                                schedule = computedSchedule,
                                                sourceFile = sourceFile,
                                                destinationFile = destinationFile,
                                            )
                                        }

                                        result.onSuccess { status = ExportStatus.Success }
                                        result.onFailure { throwable -> status = ExportStatus.Error(throwable) }
                                    }
                                }
                            }
                        ) {
                            Text("Choose save file")
                        }
                    }
                    ExportStatus.InProgress -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                    ExportStatus.Success -> {
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = Color.Green,
                            text = "??? Success!",
                        )
                    }
                    is ExportStatus.Error -> {
                        val throwable = (status as ExportStatus.Error).throwable
                        Text("Error: ${throwable.message}", color = MaterialTheme.colors.error)
                    }
                }
            }
        }
    }
}

private fun exportFilename(sourceFilename: String, sourceExtension: String): String {
    return "$sourceFilename-EXPORTED-SCHEDULE.$sourceExtension"
}
