package id.neotica.neostore.admin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAppRequest(
    val title: String,
    val description: String,
    val category: String = "",
    @SerialName("icon_url")
    val iconUrl: String = ""
)
