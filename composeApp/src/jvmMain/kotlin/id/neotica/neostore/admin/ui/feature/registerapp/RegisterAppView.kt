package id.neotica.neostore.admin.ui.feature.registerapp

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
import androidx.compose.foundation.layout.width
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
import androidx.lifecycle.viewmodel.compose.viewModel
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.NeoCard
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import id.neotica.neostore.admin.ui.feature.upload.ClearState
import org.koin.compose.viewmodel.koinViewModel
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterAppView(
    viewModel: RegisterAppViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var isDragging by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

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
                    val file = files.firstOrNull() as? File

                    if (file != null) {
//                        viewModel.setPath(file.absolutePath)
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
            text = "Welcome to Neostore App Registrar.",
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
                        text = "Target upload: Neostore",
                        color = DarkPrimary
                    )
                    Text(
                        text = if (dropdownExpanded) "⬆️" else "⬇️",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
//            TextField(
//                value = uiState.minSdk,
//                onValueChange = { viewModel.setMinSdk(it) },
//                label = { Text("Min Sdk") },
//                placeholder = { Text("7", color = PurpleGrey40) },
//                modifier = Modifier.weight(2f),
//                singleLine = true
//            )
//            TextField(
//                value = uiState.maxSdk,
//                onValueChange = { viewModel.setMaxSdk(it) },
//                label = { Text("Max Sdk") },
//                placeholder = { Text("21", color = PurpleGrey40) },
//                modifier = Modifier.weight(2f),
//                singleLine = true
//            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = uiState.packageName,
                onValueChange = { viewModel.setPackageName(it) },
                label = { Text("Package Name") },
                placeholder = { Text("com.something.app", color = PurpleGrey40) },
                singleLine = true
            )
            TextField(
                value = uiState.title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text("Title") },
                placeholder = { Text("com.something.app", color = PurpleGrey40) },
                singleLine = true
            )
        }
        TextField(
            value = uiState.category,
            onValueChange = { viewModel.setCategory(it) },
            label = { Text("Category") },
            placeholder = { Text("APPLICATION", color = PurpleGrey40) },
            singleLine = true
        )
        TextField(
            value = uiState.description,
            onValueChange = { viewModel.setDescription(it) },
            label = { Text("Description") },
            placeholder = { Text("This app was something.", color = PurpleGrey40) },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Drag and Drop Area
            NeoCard(
                isDragging = isDragging,
                dropTarget = dropTarget
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    Text(
                        text = if (uiState.filePath.isEmpty()) "Drag and drop a file here" else "File Selected",
                        color = if (isDragging) MaterialTheme.colorScheme.primary else Color.Gray
                    )

                    if (uiState.filePath.isNotEmpty()) {
                        Text(
                            text = uiState.filePath,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(200.dp)
                        )
                    }

//                    if (uiState.isLoading || uiState.uploadProgress > 0f) {
//                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                            LinearProgressIndicator(
//                                progress = { uiState.uploadProgress },
//                                modifier = Modifier.fillMaxWidth().height(8.dp),
//                            )
//                            Text(
//                                text = "${(uiState.uploadProgress * 100).toInt()}%",
//                                style = MaterialTheme.typography.labelSmall,
//                                modifier = Modifier.padding(top = 4.dp)
//                            )
//                        }
//                    }

//                    if (uiState.statusMessage.isNotEmpty()) {
//                        Text(
//                            text = uiState.statusMessage,
//                            color = MaterialTheme.colorScheme.secondary,
//                            style = MaterialTheme.typography.bodyMedium,
//                            modifier = Modifier.padding(vertical = 8.dp)
//                        )
//                    }

                    if (uiState.filePath.isNotEmpty() && !uiState.isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            ButtonBasic("Upload") {
//                                viewModel.upload()
                            }
                            ButtonBasic("Clear") {
//                                viewModel.clear(ClearState.UPLOAD)
                            }
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ButtonBasic("Clear All") { /*viewModel.clear(ClearState.ALL)*/ }
            }

        }
    }
}