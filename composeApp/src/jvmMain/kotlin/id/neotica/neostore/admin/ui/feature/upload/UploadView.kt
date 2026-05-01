package id.neotica.neostore.admin.ui.feature.upload

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.NeoCard
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import org.koin.compose.viewmodel.koinViewModel
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UploadView(
    viewModel: UploadViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var isDragging by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var targetSite by remember { mutableStateOf(TargetUpload.NEOSTORE) }

    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                isDragging = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                isDragging = false
            }
            override fun onDrop(event: DragAndDropEvent): Boolean {
                isDragging = false
                println("Action at the target: ${event.action}")

                // Extract File from Drag Event (Desktop/AWT specific)
                val transferable = event.awtTransferable
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>

                    val validFiles = files?.filterIsInstance<File>() ?: emptyList()

                    if (validFiles.isNotEmpty()) {
                        viewModel.addFilesToQueue(validFiles)
                        return true
                    }
                }
                return false
            }
        }
    }

    Column(
        Modifier.padding(16.dp)
    ) {
        Text(
            text = "Welcome to Neotica Uploader.",
            modifier = Modifier
                .padding(bottom = 8.dp),
            color = DarkPrimary
        )

        // Target Selection Dropdown
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .clickable { dropdownExpanded = !dropdownExpanded }
                    .border(1.dp, MaterialTheme.colorScheme.primary)
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Target upload: ${targetSite.label}",
                        color = DarkPrimary
                    )
                    Text(
                        text = if (dropdownExpanded) "⬆️" else "⬇️",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            TextField(
                value = uiState.minSdk,
                onValueChange = { viewModel.setMinSdk(it) },
                label = { Text("Min Sdk") },
                placeholder = { Text("7", color = PurpleGrey40) },
                modifier = Modifier.weight(2f),
                singleLine = true
            )
            TextField(
                value = uiState.maxSdk,
                onValueChange = { viewModel.setMaxSdk(it) },
                label = { Text("Max Sdk") },
                placeholder = { Text("21", color = PurpleGrey40) },
                modifier = Modifier.weight(2f),
                singleLine = true
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = uiState.apkFileFolder,
                onValueChange = { viewModel.setApkFileFolder(it) },
                label = { Text("Package Name") },
                placeholder = { Text("id.neotica.neomart", color = PurpleGrey40) },
                modifier = Modifier.weight(2f),
                singleLine = true,
                trailingIcon = {
                    if (uiState.apkFileFolder.isNotEmpty()) {
                        Row {
                            ButtonBasic("Check") { viewModel.checkLatestVersion() }
                            Spacer(Modifier.padding(4.dp))
                        }
                    }
                }
            )

            TextField(
                value = uiState.versionName,
                onValueChange = { viewModel.setVersionName(it) },
                label = { Text("Version Name") },
                placeholder = { Text("0.0.20", color = PurpleGrey40) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            TextField(
                value = uiState.versionCode,
                onValueChange = { viewModel.setVersionCode(it) },
                label = { Text("Version Code") },
                placeholder = { Text("1", color = PurpleGrey40) },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = uiState.changelog,
            onValueChange = { viewModel.setChangelog(it) },
            label = { Text("Changelog") },
            placeholder = { Text("Initial release with checkout support!", color = PurpleGrey40) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column (
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Drag and Drop Area
            NeoCard(
                isDragging = isDragging,
                dropTarget = dropTarget
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Column {
                        if (uiState.uploadQueue.size > 1 || uiState.uploadQueue.any { it.status != FileStatus.PENDING }) {
                            Text(
                                text = "Bulk Queue: ${uiState.uploadQueue.size} files ready.",
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (uiState.isBulkProcessing) {
                                Text(
                                    text = "Processing File ${uiState.currentQueueIndex} of ${uiState.uploadQueue.size}",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            } else {
                                // Show a preview of the queued files
                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    uiState.uploadQueue.take(3).forEach { file ->
//                                    Text("• ${file.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    if (uiState.uploadQueue.size > 3) {
                                        Text("... and ${uiState.uploadQueue.size - 3} more.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (uiState.filePath.isEmpty()) "Drag and drop APK(s) here" else "File Selected",
                                color = if (isDragging) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }

                        if (uiState.filePath.isNotEmpty()) {
                            Text(
                                text = uiState.filePath,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(200.dp)
                            )
                        }

                        if (uiState.isLoading || uiState.uploadProgress > 0f) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                LinearProgressIndicator(
                                    progress = { uiState.uploadProgress },
                                    modifier = Modifier.fillMaxWidth().height(8.dp),
                                )
                                Text(
                                    text = "${(uiState.uploadProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        if (uiState.statusMessage.isNotEmpty()) {
                            Text(
                                text = uiState.statusMessage,
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }


                    }
                    if (!uiState.isLoading && !uiState.isBulkProcessing) {
                        Column (
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Row {
                                Column {
                                    if (uiState.uploadQueue.size > 1) {
                                        ButtonBasic("Start Bulk Upload") { viewModel.startBulkUpload() }
                                    } else if (uiState.filePath.isNotEmpty()) {
                                        ButtonBasic("Upload") { viewModel.upload() }
                                    }
                                }
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ButtonBasic("Clear All") { viewModel.clear(ClearState.ALL) }
                                }
                            }

                            if (uiState.uploadQueue.isNotEmpty() || uiState.filePath.isNotEmpty()) {
                                ButtonBasic("Clear") { viewModel.clear(ClearState.UPLOAD) }
                            }

                            LazyColumn {
                                items(uiState.uploadQueue) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ${it.file.name}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DarkPrimary,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Status Indicator
                                        when (it.status) {
                                            FileStatus.PENDING -> Text("Waiting", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                            FileStatus.PROCESSING -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            FileStatus.SUCCESS -> Text("✅ Success", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                                            FileStatus.FAILED -> Text("❌ ${it.errorMessage}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}