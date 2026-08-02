package id.neotica.neostore.admin.domain.model.analytics

import kotlinx.serialization.Serializable

@Serializable
data class TrendingItem(
    val event_name: String,
    val count: Int
)
