package id.neotica.neostore.admin.domain.model.collection.request

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCollectionRequest(
    val title: String? = null,
    val description: String? = null
)