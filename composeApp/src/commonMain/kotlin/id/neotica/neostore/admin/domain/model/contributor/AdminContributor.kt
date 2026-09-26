package id.neotica.neostore.admin.domain.model.contributor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdminContributor(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    val username: String = "",
    val role: ContributorRole = ContributorRole.CONTRIBUTOR,
    @SerialName("added_by")
    val addedBy: String = "",
    @SerialName("created_at")
    val createdAt: Long = 0L,
    val active: Boolean = true
)

@Serializable
data class UserSummary(
    val id: String = "",
    val username: String = ""
)