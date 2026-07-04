package id.neotica.neostore.admin.ui.feature.detailapp

import id.neotica.neostore.admin.domain.model.AppVersionResponse

data class DetailAppUiState(
    val isLoading: Boolean = false,
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val iconUrl: String = "",
    val githubRepo: String = "",
    val lastGithubTag: String = "",
    val versions: List<AppVersionResponse> = emptyList(),
    val statusMessage: String = ""
)
