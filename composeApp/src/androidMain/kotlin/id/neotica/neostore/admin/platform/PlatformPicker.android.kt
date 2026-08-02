package id.neotica.neostore.admin.platform

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun rememberPlatformFileDropTarget(
    onDragStarted: () -> Unit,
    onDragEnded: () -> Unit,
    onFilesDropped: (List<PlatformFile>) -> Unit,
): DragAndDropTarget = remember {
    object : DragAndDropTarget {
        override fun onStarted(event: DragAndDropEvent) {
            super.onStarted(event)
        }
        override fun onEnded(event: DragAndDropEvent) {
            super.onEnded(event)
        }
        override fun onDrop(event: DragAndDropEvent): Boolean = false
    }
}

@Composable
actual fun rememberPlatformImagePicker(onImagePicked: (PlatformFile) -> Unit): () -> Unit {
    val picked by rememberUpdatedState(onImagePicked)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { picked(platformFileFromUri(it)) }
    }
    return remember {
        { launcher.launch(arrayOf("image/*")) }
    }
}

@Composable
actual fun rememberPlatformFilePicker(onFilesPicked: (List<PlatformFile>) -> Unit): () -> Unit {
    val picked by rememberUpdatedState(onFilesPicked)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) picked(uris.map { platformFileFromUri(it) })
    }
    return remember {
        { launcher.launch(arrayOf("application/vnd.android.package-archive")) }
    }
}