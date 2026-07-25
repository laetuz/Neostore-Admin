package id.neotica.neostore.admin.ui.feature.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.remote.CollectionsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CollectionsViewModel(
    private val repo: CollectionsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState = _uiState.asStateFlow()

    init { loadCollections() }

    fun loadCollections() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, statusMessage = "") }
        repo.listCollections().onSuccess { list ->
            _uiState.update { it.copy(isLoading = false, collections = list) }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${e.message}") }
        }
    }

    fun selectCollection(slug: String) = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, statusMessage = "") }
        repo.getCollection(slug).onSuccess { col ->
            _uiState.update {
                it.copy(isLoading = false, selectedCollection = col,
                    editTitle = col.title, editDescription = col.description,
                    viewFeed = emptyList())
            }
            loadFeed(col.slug)
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${e.message}") }
        }
    }

    fun deselectCollection() = _uiState.update {
        it.copy(selectedCollection = null, editTitle = "", editDescription = "",
            viewFeed = emptyList(), showDeleteConfirm = false, statusMessage = "")
    }

    fun setEditTitle(v: String) = _uiState.update { it.copy(editTitle = v) }
    fun setEditDescription(v: String) = _uiState.update { it.copy(editDescription = v) }
    fun setCreateTitle(v: String) = _uiState.update { it.copy(createTitle = v) }
    fun setCreateDescription(v: String) = _uiState.update { it.copy(createDescription = v) }
    fun setCreateSlug(v: String) = _uiState.update { it.copy(createSlug = v) }
    fun setAddPackageName(v: String) = _uiState.update { it.copy(addPackageName = v) }
    fun setAddSortOrder(v: String) = _uiState.update { it.copy(addSortOrder = v.filter { it.isDigit() }) }

    fun createCollection() {
        val s = _uiState.value
        if (s.createTitle.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Title is required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Creating...") }
            repo.createCollection(s.createTitle, s.createDescription, s.createSlug.ifBlank { null })
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, createTitle = "", createDescription = "", createSlug = "",
                            statusMessage = "Collection created.")
                    }
                    loadCollections()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${e.message}") }
                }
        }
    }

    fun updateCollection() {
        val slug = _uiState.value.selectedCollection?.slug ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Updating...") }
            repo.updateCollection(slug, _uiState.value.editTitle.ifBlank { null },
                _uiState.value.editDescription.ifBlank { null }).onSuccess {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Updated.") }
                selectCollection(slug)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${e.message}") }
            }
        }
    }

    fun requestDelete() = _uiState.update { it.copy(showDeleteConfirm = true) }
    fun cancelDelete() = _uiState.update { it.copy(showDeleteConfirm = false) }

    fun deleteCollection() {
        val slug = _uiState.value.selectedCollection?.slug ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Deleting...") }
            repo.deleteCollection(slug).onSuccess {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Deleted.", showDeleteConfirm = false) }
                deselectCollection()
                loadCollections()
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, showDeleteConfirm = false, statusMessage = "Failed: ${e.message}") }
            }
        }
    }

    fun addAppToCollection() {
        val slug = _uiState.value.selectedCollection?.slug ?: return
        val s = _uiState.value
        if (s.addPackageName.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Package name is required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Adding...") }
            repo.addAppToCollection(slug, s.addPackageName, s.addSortOrder.toIntOrNull() ?: 0)
                .onSuccess {
                    _uiState.update {
                        it.copy(isLoading = false, addPackageName = "", addSortOrder = "0",
                            statusMessage = "App added.")
                    }
                    loadFeed(slug)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${e.message}") }
                }
        }
    }

    fun removeAppFromCollection(packageName: String) {
        val slug = _uiState.value.selectedCollection?.slug ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "Removing...") }
            repo.removeAppFromCollection(slug, packageName).onSuccess {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Removed.") }
                loadFeed(slug)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${e.message}") }
            }
        }
    }

    private fun loadFeed(slug: String) = viewModelScope.launch {
        repo.getCollectionFeed(slug, 1, 50).onSuccess { feed ->
            _uiState.update {
                it.copy(viewFeed = feed.data, viewPage = feed.page,
                    viewTotal = feed.totalItems, viewPages = feed.totalPages)
            }
        }
    }
}