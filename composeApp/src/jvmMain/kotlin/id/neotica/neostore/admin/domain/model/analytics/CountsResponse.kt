package id.neotica.neostore.admin.domain.model.analytics

import kotlinx.serialization.Serializable

@Serializable
data class CountsResponse(
    val total: Int,
    val daily: List<DailyCount>
)

@Serializable
data class DailyCount(
    val date: String,
    val count: Int
)
