package id.neotica.neostore.admin.domain.model.contributor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContributorRole {
    OWNER,
    CONTRIBUTOR,
    NONE
}

@Serializable
data class ContributorMe(
    @SerialName("user_id")
    val userId: String = "",
    val username: String = "",
    val role: ContributorRole = ContributorRole.NONE
)