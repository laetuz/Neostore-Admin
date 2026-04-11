package id.neotica.neostore.admin.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterAppRequest(
    @SerialName("package_name")
    val packageName: String,
    val title: String,
    val description: String,
    val category: String = ""
)
