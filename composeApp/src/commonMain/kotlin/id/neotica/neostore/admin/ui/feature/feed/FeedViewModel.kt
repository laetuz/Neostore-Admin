package id.neotica.neostore.admin.ui.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.remote.CategoriesRepository
import id.neotica.neostore.admin.domain.remote.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: FileRepository,
    private val categoriesRepository: CategoriesRepository,
): ViewModel() {
    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchFeeds(page = 1)
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoriesRepository.getCategories().onSuccess { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun setSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }

    fun setCategory(category: String?) {
        _uiState.update {
            it.copy(category = category ?: "")
        }
        fetchFeeds(page = 1)
    }

    // Now accepts a specific page to load
    fun fetchFeeds(page: Int = 1) {
        val currentState = _uiState.value
        if (currentState.isLoading) return

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = "",
                currentPage = page,
                apps = emptyList()
            )
        }

        viewModelScope.launch {
            val result = repository.getFeeds(
                page = page,
                limit = 12,
                search = currentState.searchQuery,
                category = currentState.category
            )

            result.onSuccess { response ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        apps = response.data,
                        currentPage = response.page,
                        totalPages = response.totalPages
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unknown error."
                    )
                }
            }
        }
    }
}