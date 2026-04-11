package id.neotica.neostore.admin.domain.model.response

import id.neotica.neostore.admin.domain.model.AppVersionResponse
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppDetailResponse(
    val id: String,
    @SerialName("package_name") val packageName: String,
    val title: String,
    val description: String,
    val category: String,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("icon_url") val iconUrl: String? = null,
    val versions: List<AppVersionResponse> = emptyList()
)