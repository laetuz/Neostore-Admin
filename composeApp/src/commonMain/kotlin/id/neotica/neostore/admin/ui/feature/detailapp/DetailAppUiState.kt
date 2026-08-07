package id.neotica.neostore.admin.ui.feature.detailapp

import id.neotica.neostore.admin.domain.model.AppVersionResponse
import id.neotica.neostore.admin.domain.model.category.response.Category

data class DetailAppUiState(
    val isLoading: Boolean = false,
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val categories: List<Category> = emptyList(),
    val categorySlug: String? = null,
    val iconUrl: String = "",
    val developer: String = "",
    val githubRepo: String = "",
    val lastGithubTag: String = "",
    val versions: List<AppVersionResponse> = emptyList(),
    val screenshots: List<String> = emptyList(),
    val isUploadingScreenshots: Boolean = false,
    val screenshotToDelete: String? = null,
    val isUploadingIcon: Boolean = false,
    val showUnregisterConfirm: Boolean = false,
    val statusMessage: String = ""
)
