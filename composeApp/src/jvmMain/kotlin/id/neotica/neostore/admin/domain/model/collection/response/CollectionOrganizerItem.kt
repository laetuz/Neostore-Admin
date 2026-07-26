package id.neotica.neostore.admin.domain.model.collection.response

import kotlinx.serialization.Serializable

@Serializable
data class CollectionOrganizerItem(
    val index: Int,
    val slug: String,
    val title: String,
    val description: String,
)