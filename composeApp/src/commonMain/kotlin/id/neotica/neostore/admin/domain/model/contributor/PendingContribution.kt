package id.neotica.neostore.admin.domain.model.contributor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ContributionStatus {
    PENDING,
    APPROVED,
    DECLINED
}

@Serializable
data class PendingContribution(
    val id: String = "",
    @SerialName("package_name")
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val developer: String = "",
    val category: String = "",
    val categories: List<String> = emptyList(),
    @SerialName("version_name")
    val versionName: String = "",
    @SerialName("version_code")
    val versionCode: Int = 0,
    val changelog: String = "",
    @SerialName("apk_url")
    val apkUrl: String = "",
    @SerialName("icon_url")
    val iconUrl: String = "",
    val screenshots: List<String> = emptyList(),
    val status: ContributionStatus = ContributionStatus.PENDING,
    @SerialName("submitted_by")
    val submittedBy: String = "",
    @SerialName("submitted_at")
    val submittedAt: Long = 0L,
    @SerialName("decided_by")
    val decidedBy: String = "",
    @SerialName("decided_at")
    val decidedAt: Long = 0L,
    @SerialName("decline_reason")
    val declineReason: String = ""
)