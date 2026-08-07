package id.neotica.neostore.admin.ui.feature.updateapp

import id.neotica.neostore.admin.domain.model.category.response.Category

data class UpdateAppUiState(
    val isLoading: Boolean = false,
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val categories: List<Category> = emptyList(),
    val categorySlug: String? = null,
    val iconUrl: String = "",
    val developer: String = "",
    val githubRepo: String = "",
    val statusMessage: String = ""
)
