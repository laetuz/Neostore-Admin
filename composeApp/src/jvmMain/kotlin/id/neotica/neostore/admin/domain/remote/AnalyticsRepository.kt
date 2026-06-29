package id.neotica.neostore.admin.domain.remote

import id.neotica.neostore.admin.domain.model.analytics.CountsResponse
import id.neotica.neostore.admin.domain.model.analytics.PaginatedResponse
import id.neotica.neostore.admin.domain.model.analytics.TrendingItem
import id.neotica.neostore.admin.domain.model.analytics.AnalyticsEvent

interface AnalyticsRepository {
    suspend fun getEvents(
        eventType: String? = null,
        sourceService: String? = null,
        userId: String? = null,
        from: String? = null,
        to: String? = null,
        page: Int = 1,
        limit: Int = 10
    ): Result<PaginatedResponse<AnalyticsEvent>>

    suspend fun getCounts(
        eventType: String? = null,
        sourceService: String? = null,
        from: String? = null,
        to: String? = null
    ): Result<CountsResponse>

    suspend fun getTrending(
        eventType: String? = null,
        sourceService: String? = null,
        limit: Int = 10
    ): Result<List<TrendingItem>>
}
