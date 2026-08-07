package id.neotica.neostore.admin.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import java.awt.datatransfer.DataFlavor
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun rememberPlatformFileDropTarget(
    onDragStarted: () -> Unit,
    onDragEnded: () -> Unit,
    onFilesDropped: (List<PlatformFile>) -> Unit,
): DragAndDropTarget {
    val start by rememberUpdatedState(onDragStarted)
    val end by rememberUpdatedState(onDragEnded)
    val dropped by rememberUpdatedState(onFilesDropped)

    return remember(start, end, dropped) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                super.onStarted(event)
                start()
            }
            override fun onEnded(event: DragAndDropEvent) {
                super.onEnded(event)
                end()
            }
            override fun onDrop(event: DragAndDropEvent): Boolean {
                end()
                val transferable = event.awtTransferable
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                    val validFiles = files.filterIsInstance<java.io.File>().map { platformFileFromJavaFile(it) }
                    if (validFiles.isNotEmpty()) {
                        dropped(validFiles)
                        return true
                    }
                }
                return false
            }
        }
    }
}

@Composable
actual fun rememberPlatformImagePicker(onImagePicked: (PlatformFile) -> Unit): () -> Unit {
    val picked by rememberUpdatedState(onImagePicked)
    return remember(picked) {
        {
            val chooser = JFileChooser()
            chooser.dialogTitle = "Select App Icon"
            chooser.fileFilter = FileNameExtensionFilter("Image files", "png", "jpg", "jpeg")
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                chooser.selectedFile?.let { picked(platformFileFromJavaFile(it)) }
            }
        }
    }
}

@Composable
actual fun rememberPlatformImagesPicker(onImagesPicked: (List<PlatformFile>) -> Unit): () -> Unit {
    val picked by rememberUpdatedState(onImagesPicked)
    return remember(picked) {
        {
            val chooser = JFileChooser()
            chooser.dialogTitle = "Select Screenshots"
            chooser.isMultiSelectionEnabled = true
            chooser.fileFilter = FileNameExtensionFilter("Image files", "png", "jpg", "jpeg")
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val files = chooser.selectedFiles.toList().ifEmpty { listOfNotNull(chooser.selectedFile) }
                picked(files.map { platformFileFromJavaFile(it) })
            }
        }
    }
}

@Composable
actual fun rememberPlatformFilePicker(onFilesPicked: (List<PlatformFile>) -> Unit): () -> Unit {
    val picked by rememberUpdatedState(onFilesPicked)
    return remember(picked) {
        {
            val chooser = JFileChooser()
            chooser.dialogTitle = "Select APK(s)"
            chooser.isMultiSelectionEnabled = true
            chooser.fileFilter = FileNameExtensionFilter("Android Package (.apk)", "apk")
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                val files = chooser.selectedFiles.toList().ifEmpty { listOfNotNull(chooser.selectedFile) }
                picked(files.map { platformFileFromJavaFile(it) })
            }
        }
    }
}