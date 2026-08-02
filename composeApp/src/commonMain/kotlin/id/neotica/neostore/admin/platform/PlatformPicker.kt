package id.neotica.neostore.admin.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.draganddrop.DragAndDropTarget

@Composable
expect fun rememberPlatformFileDropTarget(
    onDragStarted: () -> Unit,
    onDragEnded: () -> Unit,
    onFilesDropped: (List<PlatformFile>) -> Unit,
): DragAndDropTarget

@Composable
expect fun rememberPlatformImagePicker(onImagePicked: (PlatformFile) -> Unit): () -> Unit

@Composable
expect fun rememberPlatformFilePicker(onFilesPicked: (List<PlatformFile>) -> Unit): () -> Unit