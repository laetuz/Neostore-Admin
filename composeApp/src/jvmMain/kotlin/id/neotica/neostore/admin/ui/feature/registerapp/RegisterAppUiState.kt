package id.neotica.neostore.admin.ui.feature.registerapp

import id.neotica.neostore.admin.domain.model.category.response.Category

data class RegisterAppUiState(
    val isLoading: Boolean = false,
    val filePath: String = "",
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val categories: List<Category> = emptyList(),
    val categorySlug: String? = null,
    val iconUrl: String = "",
    val statusMessage: String = "",
    val iconByteArray: ByteArray? = null,
)
