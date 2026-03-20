package id.neotica.neostore.admin.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val expirationTime: Long
)