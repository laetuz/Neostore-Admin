package id.neotica.neostore.admin.domain.model.category.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    @SerialName("parent_slug") val parentSlug: String? = null
)