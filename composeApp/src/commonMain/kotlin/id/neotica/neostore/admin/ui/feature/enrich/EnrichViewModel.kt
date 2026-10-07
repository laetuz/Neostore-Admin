package id.neotica.neostore.admin.ui.feature.enrich

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.remote.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EnrichViewModel(
    private val repo: FileRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EnrichUiState())
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "") }
            repo.listApps()
                .onSuccess { apps ->
                    _uiState.update { it.copy(isLoading = false, apps = apps) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed to load apps: ${error.message}") }
                }
        }
    }

    fun toggleOnlyIncomplete() = _uiState.update { it.copy(onlyIncomplete = !it.onlyIncomplete) }
}
