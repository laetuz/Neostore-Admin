package id.neotica.neostore.admin.ui.feature.upload

import id.neotica.neostore.admin.platform.PlatformFile

data class UploadUiState(
    val isLoading: Boolean = false,
    val filePath: String = "",
    val apkFileFolder: String = "", // Used as the package name (e.g., id.neotica.neomart)
    val versionName: String = "",
    val versionCode: String = "",
    val changelog: String = "",
    val installStatus: String = "",
    val statusMessage: String = "",
    val uploadProgress: Float = 0f,
    val minSdk: String = "",
    val maxSdk: String = "",
    // Added for Auto-Registration Fallback
    val title: String = "",
    val description: String = "",
    val category: String = "APPLICATION", // Defaulting to APPLICATION
    val iconByteArray: ByteArray? = null,

    val uploadQueue: List<QueuedFile> = emptyList(),
    val isBulkProcessing: Boolean = false,
    val currentQueueIndex: Int = 0
)

enum class FileStatus { PENDING, PROCESSING, SUCCESS, FAILED }

data class QueuedFile(
    val file: PlatformFile,
    val status: FileStatus = FileStatus.PENDING,
    val errorMessage: String? = null
)