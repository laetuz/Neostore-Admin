package id.neotica.neostore.admin.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ScreenshotsResponse(
    val screenshots: List<String> = emptyList()
)