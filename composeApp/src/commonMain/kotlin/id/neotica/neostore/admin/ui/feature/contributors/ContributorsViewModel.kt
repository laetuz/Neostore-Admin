package id.neotica.neostore.admin.ui.feature.contributors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.model.contributor.ContributorRole
import id.neotica.neostore.admin.domain.model.contributor.UserSummary
import id.neotica.neostore.admin.domain.remote.ContributorsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContributorsViewModel(
    private val repo: ContributorsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContributorsUiState())
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "") }
            repo.listContributors()
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, contributors = list) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed to load: ${error.message}") }
                }
        }
    }

    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun search() {
        val query = _uiState.value.searchQuery
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchResults = emptyList(), statusMessage = "") }
            repo.searchUsers(query)
                .onSuccess { users -> _uiState.update { it.copy(isSearching = false, searchResults = users) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isSearching = false, statusMessage = "Search failed: ${error.message}") }
                }
        }
    }

    fun selectUser(user: UserSummary) = _uiState.update {
        it.copy(
            selectedUserId = user.id,
            selectedUsername = user.username,
            searchResults = emptyList(),
            searchQuery = user.username,
        )
    }

    fun addSelected() {
        val state = _uiState.value
        if (state.selectedUserId.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Pick a user first.") }
            return
        }
        viewModelScope.launch {
            repo.addContributor(
                userId = state.selectedUserId,
                username = state.selectedUsername,
                role = ContributorRole.CONTRIBUTOR,
            )
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            statusMessage = "Contributor added.",
                            selectedUserId = "",
                            selectedUsername = "",
                            searchQuery = "",
                        )
                    }
                    load()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(statusMessage = "Failed to add: ${error.message}") }
                }
        }
    }

    fun remove(userId: String) {
        viewModelScope.launch {
            repo.removeContributor(userId)
                .onSuccess { load() }
                .onFailure { error ->
                    _uiState.update { it.copy(statusMessage = "Failed to remove: ${error.message}") }
                }
        }
    }
}