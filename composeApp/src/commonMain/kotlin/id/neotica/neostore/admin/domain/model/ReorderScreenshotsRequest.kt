package id.neotica.neostore.admin.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReorderScreenshotsRequest(
    val order: List<String>
)