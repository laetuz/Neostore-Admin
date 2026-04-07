package id.neotica.neostore.admin.ui.feature.registerapp

import androidx.lifecycle.ViewModel
import id.neotica.neostore.admin.domain.remote.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterAppViewModel(
    private val repo: FileRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(RegisterAppUiState())
    val uiState = _uiState.asStateFlow()

    fun setPackageName(packageName: String) = _uiState.update { it.copy(packageName = packageName) }
    fun setTitle(title: String) = _uiState.update { it.copy(title = title) }
    fun setDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun setCategory(category: String) = _uiState.update { it.copy(category = category) }

    // todo: add function for upload, clear, and apk analyzer. Also icon extractor from the analyzer. Icon uploader too.
}