package id.neotica.neostore.admin.ui.feature.analytics

import id.neotica.neostore.admin.domain.model.analytics.CountsResponse
import id.neotica.neostore.admin.domain.model.analytics.TrendingItem

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val accessGranted: Boolean? = null,
    val counts: CountsResponse? = null,
    val trending: List<TrendingItem> = emptyList(),
    val errorMessage: String? = null
)
