package id.neotica.neostore.admin.ui.feature.categories

import id.neotica.neostore.admin.domain.model.category.response.Category

data class CategoriesUiState(
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val name: String = "",
    val slug: String = "",
    val parentSlug: String? = null,
    val editingSlug: String? = null,
    val editingName: String = "",
    val editingParentSlug: String? = null,
    val expandedSlug: String? = null,
    val statusMessage: String = ""
)
