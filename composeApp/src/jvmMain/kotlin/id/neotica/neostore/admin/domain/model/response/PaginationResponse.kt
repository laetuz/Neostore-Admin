package id.neotica.neostore.admin.domain.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaginationResponse<T>(
    val data: List<T>,
    val page: Int,
    val limit: Int,
    @SerialName("total_items")
    val totalItems: Int,
    @SerialName("total_pages")
    val totalPages: Int
)
