package id.neotica.neostore.admin.data.remote

import id.neotica.neostore.admin.domain.model.analytics.AnalyticsEvent
import id.neotica.neostore.admin.domain.model.analytics.CountsResponse
import id.neotica.neostore.admin.domain.model.analytics.PaginatedResponse
import id.neotica.neostore.admin.domain.model.analytics.TrendingItem
import id.neotica.neostore.admin.domain.remote.AnalyticsRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class AnalyticsRepositoryImpl(
    private val client: HttpClient
) : AnalyticsRepository {

    override suspend fun getEvents(
        eventType: String?,
        sourceService: String?,
        userId: String?,
        from: String?,
        to: String?,
        page: Int,
        limit: Int
    ): Result<PaginatedResponse<AnalyticsEvent>> = try {
        val response = client.get("$BASE_URL/metrics/analytics/events") {
            eventType?.let { parameter("event_type", it) }
            sourceService?.let { parameter("source_service", it) }
            userId?.let { parameter("user_id", it) }
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
            parameter("page", page)
            parameter("limit", limit)
        }
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getCounts(
        eventType: String?,
        sourceService: String?,
        from: String?,
        to: String?
    ): Result<CountsResponse> = try {
        val response = client.get("$BASE_URL/metrics/analytics/counts") {
            eventType?.let { parameter("event_type", it) }
            sourceService?.let { parameter("source_service", it) }
            from?.let { parameter("from", it) }
            to?.let { parameter("to", it) }
        }
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getTrending(
        eventType: String?,
        sourceService: String?,
        limit: Int
    ): Result<List<TrendingItem>> = try {
        val response = client.get("$BASE_URL/metrics/trending") {
            eventType?.let { parameter("event_type", it) }
            sourceService?.let { parameter("source_service", it) }
            parameter("limit", limit)
        }
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getEventById(id: String): Result<AnalyticsEvent> = try {
        val response = client.get("$BASE_URL/metrics/analytics/events/$id")
        Result.success(response.body())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
