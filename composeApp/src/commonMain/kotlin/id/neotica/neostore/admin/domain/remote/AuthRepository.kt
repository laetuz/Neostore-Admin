package id.neotica.neostore.admin.domain.remote

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<String>
}