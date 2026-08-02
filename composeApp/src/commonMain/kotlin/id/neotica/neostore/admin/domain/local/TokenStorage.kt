package id.neotica.neostore.admin.domain.local

interface TokenStorage {
    fun saveToken(token: String, refreshToken: String)
    fun getToken(): String?
    fun clearToken()
}