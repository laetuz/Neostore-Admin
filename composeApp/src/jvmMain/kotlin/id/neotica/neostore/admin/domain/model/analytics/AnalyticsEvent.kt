package id.neotica.neostore.admin.domain.model.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsEvent(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("event_type") val eventType: String,
    @SerialName("event_name") val eventName: String,
    val properties: Map<String, String> = emptyMap(),
    @SerialName("source_service") val sourceService: String,
    @SerialName("ip_address") val ipAddress: String? = null,
    @SerialName("user_agent") val userAgent: String? = null,
    @SerialName("created_at") val createdAt: String
)
