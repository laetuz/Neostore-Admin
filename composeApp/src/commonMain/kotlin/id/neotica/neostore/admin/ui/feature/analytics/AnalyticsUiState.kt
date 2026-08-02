package id.neotica.neostore.admin.ui.feature.analytics

import id.neotica.neostore.admin.domain.model.analytics.AnalyticsEvent
import id.neotica.neostore.admin.domain.model.analytics.CountsResponse
import id.neotica.neostore.admin.domain.model.analytics.TrendingItem

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val accessGranted: Boolean? = null,
    val counts: CountsResponse? = null,
    val trending: List<TrendingItem> = emptyList(),
    val errorMessage: String? = null,
    val selectedDate: String? = null,
    val events: List<AnalyticsEvent> = emptyList(),
    val isLoadingEvents: Boolean = false,
    val errorEvents: String? = null,
    val eventsPage: Int = 1,
    val eventsTotalPages: Int = 1,
    val selectedEvent: AnalyticsEvent? = null,
    val isLoadingEventDetail: Boolean = false,
    val eventDetailError: String? = null
) {
    val isShowingDetail: Boolean get() = selectedDate != null
    val isShowingEventDetail: Boolean get() = selectedEvent != null
}
