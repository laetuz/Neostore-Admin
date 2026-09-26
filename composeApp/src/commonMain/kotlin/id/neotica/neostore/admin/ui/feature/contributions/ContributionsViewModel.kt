package id.neotica.neostore.admin.ui.feature.contributions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.model.contributor.ContributionStatus
import id.neotica.neostore.admin.domain.remote.ContributorsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContributionsViewModel(
    private val repo: ContributorsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContributionsUiState())
    val uiState = _uiState.asStateFlow()

    fun setMyUserId(userId: String, onlyMine: Boolean) {
        _uiState.update { it.copy(myUserId = userId, onlyMine = onlyMine) }
    }

    fun setFilter(status: ContributionStatus?) {
        _uiState.update { it.copy(statusFilter = status) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "") }
            val status = _uiState.value.statusFilter
            repo.listContributions(status)
                .onSuccess { items ->
                    _uiState.update { it.copy(isLoading = false, contributions = items) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed to load: ${error.message}") }
                }
        }
    }
}