package id.neotica.neostore.admin.domain.model.category.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val slug: String,
    @SerialName("parent_slug") val parentSlug: String? = null
)