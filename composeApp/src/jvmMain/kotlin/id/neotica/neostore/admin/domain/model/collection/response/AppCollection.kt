package id.neotica.neostore.admin.domain.model.collection.response

import kotlinx.serialization.Serializable

@Serializable
data class AppCollection(
    val slug: String,
    val title: String,
    val description: String = ""
)