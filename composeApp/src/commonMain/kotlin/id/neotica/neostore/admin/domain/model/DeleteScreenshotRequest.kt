package id.neotica.neostore.admin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteScreenshotRequest(
    @SerialName("file_url")
    val fileUrl: String
)