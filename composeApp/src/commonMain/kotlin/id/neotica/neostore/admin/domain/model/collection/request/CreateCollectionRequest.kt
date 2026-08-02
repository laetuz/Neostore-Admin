package id.neotica.neostore.admin.domain.model.collection.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCollectionRequest(
    val title: String,
    val description: String = "",
    @SerialName("custom_slug")
    val customSlug: String? = null
)