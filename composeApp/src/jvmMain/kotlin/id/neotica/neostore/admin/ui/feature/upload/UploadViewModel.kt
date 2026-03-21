package id.neotica.neostore.admin.ui.feature.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL_BUCKET
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

    fun setPath(path: String) {
        _uiState.update {
            it.copy(filePath = path, statusMessage = "Ready to upload", uploadProgress = 0f)
        }
    }

    fun setApkFileFolder(folder: String) {
        _uiState.update { it.copy(apkFileFolder = folder) }
    }

    fun setVersionName(name: String) = _uiState.update { it.copy(versionName = name) }
    fun setVersionCode(code: String) = _uiState.update { it.copy(versionCode = code.filter { char -> char.isDigit() }) } // Force numbers only
    fun setChangelog(log: String) = _uiState.update { it.copy(changelog = log) }

    fun upload() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.filePath.isBlank()) return
        if (currentState.apkFileFolder.isBlank() || currentState.versionName.isBlank() || currentState.versionCode.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Please fill in all version details!") }
            return
        }

        val file = File(currentState.filePath)
        if (!file.exists()) {
            _uiState.update { it.copy(statusMessage = "File does not exist!") }
            return
        }

        _uiState.update {
            it.copy(isLoading = true, statusMessage = "Starting upload...", uploadProgress = 0f)
        }

        val bucketUrl = "$BASE_URL_BUCKET/neostore"

        viewModelScope.launch {
            val uploadResult = repository.uploadFile(file, bucketUrl, currentState.apkFileFolder, currentState.versionCode.toInt()) { progress ->
                _uiState.update { it.copy(uploadProgress = progress) }
            }

            uploadResult.onSuccess {
                val fileUrl = "/buckets/neostore/${currentState.apkFileFolder}/${currentState.versionCode}.apk"

                _uiState.update {
                    it.copy(statusMessage = "Upload Success! ✅", uploadProgress = 1f, isLoading = false)
                }

                val registerResult = repository.registerAppVersion(
                    packageName = currentState.apkFileFolder,
                    versionName = currentState.versionName,
                    versionCode = currentState.versionCode.toInt(),
                    fileUrl = fileUrl,
                    changelog = currentState.changelog
                )

                registerResult
                    .onSuccess { _uiState.update { it.copy(statusMessage = "Version published successfully! ✅", isLoading = false) } }
                    .onFailure { error -> _uiState.update { it.copy(statusMessage = "File uploaded, but registration failed: ${error.message} ❌", isLoading = false) } }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(statusMessage = "Error: ${e.message} ❌", isLoading = false)
                }
            }
        }
    }
}

enum class TargetUpload(val label: String) {
    NEOSTORE("neostore")
}