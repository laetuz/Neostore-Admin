package id.neotica.neostore.admin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppVersionResponse(
    val id: String,
    @SerialName("app_id")
    val appId: String,
    @SerialName("version_name")
    val versionName: String,
    @SerialName("version_code")
    val versionCode: Int,
    @SerialName("file_url")
    val fileUrl: String,
    val changelog: String,
    @SerialName("min_sdk")
    val minSdk: Int,
    @SerialName("max_sdk")
    val maxSdk: Int,
    @SerialName("created_at")
    val createdAt: Long
)
