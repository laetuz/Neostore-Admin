package id.neotica.neostore.admin.data.remote

import id.neotica.neostore.admin.domain.local.TokenStorage
import id.neotica.neostore.admin.domain.model.LoginResponse
import id.neotica.neostore.admin.domain.remote.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
    private val baseUrl: String
): AuthRepository {
    override suspend fun login(
        username: String,
        password: String
    ): Result<String> {
        try {
            val response = httpClient.get("${baseUrl}/auth/auth") {
                header("username", username)
                header("password", password)
            }

            if (response.status.isSuccess()) {
                val loginData: LoginResponse = response.body()

                tokenStorage.saveToken(loginData.token, loginData.refreshToken)
                return Result.success(loginData.token)
            } else {
                val errorBody = response.bodyAsText()
                return Result.failure(Exception("Login failed: ${response.status} | $errorBody"))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}