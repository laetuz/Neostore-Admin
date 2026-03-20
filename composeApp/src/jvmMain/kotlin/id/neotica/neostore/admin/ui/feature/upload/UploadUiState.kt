package id.neotica.neostore.admin.ui.feature.upload

data class UploadUiState(
    val isLoading: Boolean = false,
    val filePath: String = "",
    val apkFileFolder: String = "", // Used as the package name (e.g., id.neotica.neomart)
    val versionName: String = "",
    val versionCode: String = "",
    val changelog: String = "",
    val installStatus: String = "",
    val statusMessage: String = "",
    val uploadProgress: Float = 0f
)
