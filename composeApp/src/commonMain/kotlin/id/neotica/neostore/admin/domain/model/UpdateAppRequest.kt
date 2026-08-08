package id.neotica.neostore.admin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateAppRequest(
    val title: String,
    val description: String,
    val category: String = "",
    val categories: List<String> = emptyList(),
    @SerialName("icon_url")
    val iconUrl: String = "",
    val developer: String? = null,
    @SerialName("github_repo")
    val githubRepo: String? = null
)