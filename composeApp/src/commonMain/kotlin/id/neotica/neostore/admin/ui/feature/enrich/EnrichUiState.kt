package id.neotica.neostore.admin.ui.feature.enrich

import id.neotica.neostore.admin.domain.model.response.AppDetailResponse

data class EnrichUiState(
    val isLoading: Boolean = false,
    val apps: List<AppDetailResponse> = emptyList(),
    val onlyIncomplete: Boolean = true,
    val statusMessage: String = ""
)

fun AppDetailResponse.isIncomplete(): Boolean =
    developer.isNullOrBlank() ||
        category.isBlank() ||
        description.isBlank() ||
        screenshots.isEmpty()

fun AppDetailResponse.missingFields(): List<String> = buildList {
    if (developer.isNullOrBlank()) add("developer")
    if (category.isBlank()) add("category")
    if (description.isBlank()) add("description")
    if (screenshots.isEmpty()) add("screenshots")
}
