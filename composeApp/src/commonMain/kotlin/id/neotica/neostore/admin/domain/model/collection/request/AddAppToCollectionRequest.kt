package id.neotica.neostore.admin.domain.model.collection.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddAppToCollectionRequest(
    @SerialName("package_name")
    val packageName: String,
    @SerialName("sort_order")
    val sortOrder: Int
)