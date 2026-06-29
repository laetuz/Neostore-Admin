package id.neotica.neostore.admin.domain.model.analytics

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val page: Int,
    val limit: Int,
    val total_items: Int,
    val total_pages: Int
)
