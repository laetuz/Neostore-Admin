package id.neotica.neostore.admin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppVersionRequest(
    @SerialName("version_name")
    val versionName: String,
    @SerialName("version_code")
    val versionCode: Int,
    @SerialName("file_url")
    val fileUrl: String,
    val changelog: String
)
