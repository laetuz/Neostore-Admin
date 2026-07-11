package id.neotica.neostore.admin.ui.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.remote.CategoriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val repo: CategoriesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, statusMessage = "") }
        repo.getCategories().onSuccess { cats ->
            _uiState.update { it.copy(isLoading = false, categories = cats) }
        }.onFailure { e ->
            _uiState.update { it.copy(isLoading = false, statusMessage = "Failed: ${e.message}") }
        }
    }

    fun setName(v: String) = _uiState.update { it.copy(name = v) }
    fun setSlug(v: String) = _uiState.update { it.copy(slug = v) }
    fun setParentSlug(v: String?) = _uiState.update { it.copy(parentSlug = v) }

    fun createCategory() {
        val s = _uiState.value
        if (s.name.isBlank() || s.slug.isBlank()) {
            _uiState.update { it.copy(statusMessage = "Name and slug are required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Creating...") }
            repo.createCategory(s.name, s.slug, s.parentSlug).onSuccess {
                _uiState.update {
                    it.copy(name = "", slug = "", parentSlug = null, statusMessage = "Category created.")
                }
                load()
            }.onFailure { e ->
                _uiState.update { it.copy(statusMessage = "Failed: ${e.message}") }
            }
        }
    }

    fun startEdit(slug: String) {
        val cat = _uiState.value.categories.find { it.slug == slug } ?: return
        _uiState.update {
            it.copy(editingSlug = slug, editingName = cat.name, editingParentSlug = cat.parentSlug)
        }
    }

    fun cancelEdit() = _uiState.update {
        it.copy(editingSlug = null, editingName = "", editingParentSlug = null)
    }

    fun setEditingName(v: String) = _uiState.update { it.copy(editingName = v) }
    fun setEditingParentSlug(v: String?) = _uiState.update { it.copy(editingParentSlug = v) }

    fun saveEdit() {
        val s = _uiState.value
        val slug = s.editingSlug ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Updating...") }
            repo.updateCategory(slug, s.editingName.ifBlank { null }, s.editingParentSlug).onSuccess {
                cancelEdit()
                _uiState.update { it.copy(statusMessage = "Updated.") }
                load()
            }.onFailure { e ->
                _uiState.update { it.copy(statusMessage = "Failed: ${e.message}") }
            }
        }
    }

    fun toggleExpand(slug: String) {
        _uiState.update {
            if (it.editingSlug == slug) it
            else if (it.expandedSlug == slug) it.copy(expandedSlug = null)
            else it.copy(expandedSlug = slug)
        }
    }

    fun deleteCategory(slug: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Deleting...") }
            repo.deleteCategory(slug).onSuccess {
                _uiState.update { it.copy(statusMessage = "Deleted.") }
                load()
            }.onFailure { e ->
                _uiState.update { it.copy(statusMessage = "Failed: ${e.message}") }
            }
        }
    }
}