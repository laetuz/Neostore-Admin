package id.neotica.neostore.admin.domain.model.contributor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddContributorRequest(
    @SerialName("user_id")
    val userId: String? = null,
    val username: String? = null,
    val role: ContributorRole = ContributorRole.CONTRIBUTOR
)

@Serializable
data class UpdateContributorRoleRequest(
    val role: ContributorRole
)

@Serializable
data class DeclineContributionRequest(
    val reason: String? = null
)