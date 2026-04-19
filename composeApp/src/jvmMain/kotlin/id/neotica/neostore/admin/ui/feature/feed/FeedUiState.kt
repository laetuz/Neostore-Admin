package id.neotica.neostore.admin.ui.feature.feed

import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse

data class FeedUiState(
    val isLoading: Boolean = false,
    val isFetchingMore: Boolean = false,
    val apps: List<AppFeedItemResponse> = emptyList(),
    val searchQuery: String = "",
    val category: String = "",
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val errorMessage: String = ""
)
