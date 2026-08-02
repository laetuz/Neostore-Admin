package id.neotica.neostore.admin.domain.model.category.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val slug: String,
    val name: String,
    @SerialName("parent_slug") val parentSlug: String? = null,
    val children: List<Category> = emptyList()
)