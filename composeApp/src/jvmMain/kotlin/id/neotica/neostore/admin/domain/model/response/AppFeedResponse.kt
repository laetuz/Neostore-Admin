package id.neotica.neostore.admin.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppFeedItemResponse(
    @SerialName("package_name")
    val packageName: String,
    val title: String,
    val description: String? = null,
    val category: String,
    @SerialName("icon_url")
    val iconUrl: String? = null
)