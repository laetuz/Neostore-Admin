package id.neotica.neostore.admin.ui.feature.detailapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.model.UpdateAppRequest
import id.neotica.neostore.admin.domain.remote.FileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailAppViewModel(
    private val repo: FileRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(DetailAppUiState())
    val uiState = _uiState.asStateFlow()

    fun setPackageName(packageName: String) = _uiState.update { it.copy(packageName = packageName) }
    fun setTitle(title: String) = _uiState.update { it.copy(title = title) }
    fun setDescription(description: String) = _uiState.update { it.copy(description = description) }
    fun setCategory(category: String) = _uiState.update { it.copy(category = category) }
    fun setIconUrl(iconUrl: String) = _uiState.update { it.copy(iconUrl = iconUrl) }

    fun clear() = _uiState.update { DetailAppUiState() }

    fun getAppDetail() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        delay(100)
        val response = repo.getAppDetail(_uiState.value.packageName)

        response.onSuccess { data ->
            _uiState.update { it.copy(
                isLoading = false,
                title = data.title,
                description = data.description,
                category = data.category,
                iconUrl = data.iconUrl ?: ""
            ) }
        }
            .onFailure { error ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: $error") }
            }
    }

    fun updateApp() {
        val currentState = _uiState.value

        if (currentState.isLoading || currentState.packageName.isBlank() || currentState.title.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Please fill in Package Name and Title!") }
            return
        }

        _uiState.update { it.copy(statusMessage = "Updating app...") }

        viewModelScope.launch {
            val updateAppRequest = UpdateAppRequest(
                title = currentState.title,
                description = currentState.description,
                category = currentState.category,
                iconUrl = currentState.iconUrl
            )

            val updateResult = repo.updateApp(currentState.packageName, updateAppRequest)

            updateResult.onSuccess {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Updated.") }
            }.onFailure { error -> _uiState.update { it.copy(isLoading = false, statusMessage = "Failed updating app: $error") } }
        }
    }
}