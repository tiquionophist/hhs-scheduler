package com.tiquionophist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
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
        state = rememberDialogState(size = DpSize(width = 500.dp, height = 250.dp)),
        title = "Export schedule",
        onCloseRequest = onClose,
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var status by remember { mutableStateOf<ExportStatus>(ExportStatus.NotStarted) }

            Column(
                modifier = Modifier.padding(Dimens.SPACING_2),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SPACING_2)) {
                    Icon(
                        modifier = Modifier.size(50.dp),
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                    )

                    Text(
                        text = """
                        Copying this schedule to an existing save file is complicated and has a chance of corrupting the
                        data. As a precaution, this will write to a copy of your game file without touching the
                        original. Choose a save file and a copy will be saved named
                        "${exportFilename(sourceFilename = "mySave", sourceExtension = "sav")}" (overwriting it if it
                        exists) with this schedule as its timetable. Please report any issues you see with exporting.
                        """
                            .trim()
                            .replace("""\s+""".toRegex(), " ")
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
                            text = "✔ Success!",
                        )
                    }
                    is ExportStatus.Error -> {
                        val throwable = (status as ExportStatus.Error).throwable
                        throwable.printStackTrace()
                        Text("Error: ${throwable.message}")
                    }
                }
            }
        }
    }
}

private fun exportFilename(sourceFilename: String, sourceExtension: String): String {
    return "$sourceFilename-EXPORTED-SCHEDULE.$sourceExtension"
}
