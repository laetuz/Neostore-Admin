package id.neotica.neostore.admin.domain.model.collection.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddToOrganizerRequest(
    @SerialName("collection_slug") val collectionSlug: String,
    @SerialName("sort_order") val sortOrder: Int,
)