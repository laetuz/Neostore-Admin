package id.neotica.neostore.admin.ui.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.data.FileRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class UploadViewModel(
    private val repository: FileRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState = _uiState.asStateFlow()

    fun clear() {
        _uiState.update { it.copy(filePath = "", statusMessage =  "", uploadProgress =  0f) }
    }

    fun clearStatus() {
        _uiState.update { it.copy(installStatus = "") }
    }

    fun setPath(path: String) {
        _uiState.update {
            it.copy(filePath = path, statusMessage = "Ready to upload", uploadProgress = 0f)
        }
    }

    fun setApkFileFolder(folder: String) {
        _uiState.update { it.copy(apkFileFolder = folder) }
    }

    fun upload(target: TargetUpload) {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.filePath.isBlank()) return

        val file = File(currentState.filePath)
        if (!file.exists()) {
            _uiState.update { it.copy(statusMessage = "File does not exist!") }
            return
        }

        _uiState.update {
            it.copy(isLoading = true, statusMessage = "Starting upload...", uploadProgress = 0f)
        }

        val baseUrl = "$BASE_URL/${target.label}"

        viewModelScope.launch {
            val result = repository.uploadFile(file, baseUrl, currentState.apkFileFolder) { progress ->
                _uiState.update { it.copy(uploadProgress = progress) }
            }

            result.onSuccess {
                _uiState.update {
                    it.copy(statusMessage = "Upload Success! ✅", uploadProgress = 1f, isLoading = false)
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(statusMessage = "Error: ${e.message} ❌", isLoading = false)
                }
            }
        }
    }
}

data class UploadUiState(
    val isLoading: Boolean = false,
    val filePath: String = "",
    val apkFileFolder: String = "",
    val installStatus: String = "",
    val statusMessage: String = "",
    val uploadProgress: Float = 0f
)

enum class TargetUpload(val label: String) {
    NEOTICA_ASSETS("neotica-assets"),
    NEOMART("neomart"),
    NEOSTORE("neostore")
}