package id.neotica.neostore.admin.ui.feature.upload

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.NeoCard
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import org.koin.compose.viewmodel.koinViewModel
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UploadView(
    viewModel: UploadViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var isDragging by remember { mutableStateOf(false) }
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
                val transferable = event.awtTransferable
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    val validFiles = files.filterIsInstance<File>()
                    if (validFiles.isNotEmpty()) {
                        viewModel.addFilesToQueue(validFiles)
                        return true
                    }
                }
                return false
            }
        }
    }

    UploadViewContent(
        uiState = uiState,
        targetSite = targetSite,
        isDragging = isDragging,
        dropTarget = dropTarget,
        onApkFileFolderChange = viewModel::setApkFileFolder,
        onVersionNameChange = viewModel::setVersionName,
        onVersionCodeChange = viewModel::setVersionCode,
        onChangelogChange = viewModel::setChangelog,
        onMinSdkChange = viewModel::setMinSdk,
        onMaxSdkChange = viewModel::setMaxSdk,
        onCheckLatest = viewModel::checkLatestVersion,
        onUpload = viewModel::upload,
        onStartBulkUpload = viewModel::startBulkUpload,
        onClearAll = { viewModel.clear(ClearState.ALL) },
        onClearUpload = { viewModel.clear(ClearState.UPLOAD) },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun UploadViewContent(
    uiState: UploadUiState,
    targetSite: TargetUpload,
    isDragging: Boolean,
    dropTarget: DragAndDropTarget,
    onApkFileFolderChange: (String) -> Unit,
    onVersionNameChange: (String) -> Unit,
    onVersionCodeChange: (String) -> Unit,
    onChangelogChange: (String) -> Unit,
    onMinSdkChange: (String) -> Unit,
    onMaxSdkChange: (String) -> Unit,
    onCheckLatest: () -> Unit,
    onUpload: () -> Unit,
    onStartBulkUpload: () -> Unit,
    onClearAll: () -> Unit,
    onClearUpload: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Upload APK",
            style = MaterialTheme.typography.headlineSmall,
            color = DarkPrimary,
        )

        if (uiState.statusMessage.isNotEmpty()) {
            val isError = uiState.statusMessage.contains("Error", ignoreCase = true)
                    || uiState.statusMessage.contains("Failed", ignoreCase = true)
            Text(
                text = uiState.statusMessage,
                color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
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
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "APK Metadata",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextField(
                        value = uiState.apkFileFolder,
                        onValueChange = onApkFileFolderChange,
                        label = { Text("Package Name") },
                        placeholder = { Text("id.neotica.neomart", color = PurpleGrey40) },
                        singleLine = true,
                        modifier = Modifier.weight(2f),
                        trailingIcon = {
                            if (uiState.apkFileFolder.isNotEmpty()) {
                                ButtonBasic("Check") { onCheckLatest() }
                            }
                        },
                    )
                    TextField(
                        value = uiState.versionName,
                        onValueChange = onVersionNameChange,
                        label = { Text("Version Name") },
                        placeholder = { Text("0.0.20", color = PurpleGrey40) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    TextField(
                        value = uiState.versionCode,
                        onValueChange = onVersionCodeChange,
                        label = { Text("Version Code") },
                        placeholder = { Text("1", color = PurpleGrey40) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }

                TextField(
                    value = uiState.changelog,
                    onValueChange = onChangelogChange,
                    label = { Text("Changelog") },
                    placeholder = { Text("Initial release with checkout support!", color = PurpleGrey40) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                )
            }
        }

        NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Version Info",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextField(
                        value = uiState.minSdk,
                        onValueChange = onMinSdkChange,
                        label = { Text("Min SDK") },
                        placeholder = { Text("7", color = PurpleGrey40) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    TextField(
                        value = uiState.maxSdk,
                        onValueChange = onMaxSdkChange,
                        label = { Text("Max SDK") },
                        placeholder = { Text("21", color = PurpleGrey40) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            }
        }

        NeoCard(
            isDragging = isDragging,
            dropTarget = dropTarget,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (uiState.uploadQueue.isNotEmpty()) 280.dp else 200.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Drop Zone",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                )

                if (uiState.uploadQueue.isEmpty()) {
                    Text(
                        text = if (uiState.filePath.isEmpty()) "Drag and drop APK(s) here"
                        else "File Selected",
                        color = if (isDragging) MaterialTheme.colorScheme.primary else Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (uiState.filePath.isNotEmpty()) {
                        Text(
                            text = uiState.filePath,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White,
                            maxLines = 1,
                        )
                    }
                } else {
                    Text(
                        text = "Queue: ${uiState.uploadQueue.size} file(s)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (uiState.isBulkProcessing) {
                        Text(
                            text = "Processing ${uiState.currentQueueIndex} of ${uiState.uploadQueue.size}",
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.uploadQueue) { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = entry.file.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            when (entry.status) {
                                FileStatus.PENDING -> Text("Waiting", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                FileStatus.PROCESSING -> CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                FileStatus.SUCCESS -> Text("Success", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                                FileStatus.FAILED -> Text("${entry.errorMessage ?: "Failed"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                if (!uiState.isLoading && !uiState.isBulkProcessing && uiState.uploadQueue.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (uiState.uploadQueue.size > 1) {
                            ButtonBasic("Start Bulk Upload", onStartBulkUpload)
                        } else {
                            ButtonBasic("Upload", onUpload)
                        }
                        Spacer(Modifier.weight(1f))
                        ButtonBasic("Clear Upload", onClearUpload)
                        ButtonBasic("Clear All", onClearAll)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun UploadViewPreview() {
    val fakeDropTarget = object : DragAndDropTarget {
        override fun onDrop(event: DragAndDropEvent) = false
    }

    UploadViewContent(
        uiState = UploadUiState(
            apkFileFolder = "id.neotica.neomart",
            versionName = "0.0.20",
            versionCode = "2",
            changelog = "Fixed checkout crash on Android 2.3.",
            minSdk = "7",
            maxSdk = "21",
            statusMessage = "",
        ),
        targetSite = TargetUpload.NEOSTORE,
        isDragging = false,
        dropTarget = fakeDropTarget,
        onApkFileFolderChange = {},
        onVersionNameChange = {},
        onVersionCodeChange = {},
        onChangelogChange = {},
        onMinSdkChange = {},
        onMaxSdkChange = {},
        onCheckLatest = {},
        onUpload = {},
        onStartBulkUpload = {},
        onClearAll = {},
        onClearUpload = {},
    )
}
