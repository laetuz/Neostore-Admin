package id.neotica.neostore.admin.data.remote

import id.neotica.neostore.admin.domain.model.contributor.AddContributorRequest
import id.neotica.neostore.admin.domain.model.contributor.AdminContributor
import id.neotica.neostore.admin.domain.model.contributor.ContributionStatus
import id.neotica.neostore.admin.domain.model.contributor.ContributorMe
import id.neotica.neostore.admin.domain.model.contributor.ContributorRole
import id.neotica.neostore.admin.domain.model.contributor.DeclineContributionRequest
import id.neotica.neostore.admin.domain.model.contributor.PendingContribution
import id.neotica.neostore.admin.domain.model.contributor.UpdateContributorRoleRequest
import id.neotica.neostore.admin.domain.model.contributor.UserSummary
import id.neotica.neostore.admin.domain.remote.ContributorsRepository
import id.neotica.neostore.admin.platform.PlatformFile
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class ContributorsRepositoryImpl(
    private val httpClient: HttpClient
) : ContributorsRepository {

    private val base = "$BASE_URL/neostore/admin"

    override suspend fun getMyRole(): Result<ContributorMe> = try {
        val response = httpClient.get("$base/contributors/me")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to resolve role: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun listContributors(): Result<List<AdminContributor>> = try {
        val response = httpClient.get("$base/contributors")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to list contributors: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun searchUsers(query: String): Result<List<UserSummary>> = try {
        val response = httpClient.get("$base/contributors/users") {
            parameter("q", query)
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to search users: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addContributor(
        userId: String?,
        username: String?,
        role: ContributorRole
    ): Result<AdminContributor> = try {
        val response = httpClient.post("$base/contributors") {
            contentType(ContentType.Application.Json)
            setBody(AddContributorRequest(userId = userId, username = username, role = role))
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to add contributor: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateContributorRole(
        userId: String,
        role: ContributorRole
    ): Result<String> = try {
        val response = httpClient.put("$base/contributors/$userId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateContributorRoleRequest(role = role))
        }
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to update role: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeContributor(userId: String): Result<String> = try {
        val response = httpClient.delete("$base/contributors/$userId")
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to remove contributor: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun listContributions(
        status: ContributionStatus?
    ): Result<List<PendingContribution>> = try {
        val response = httpClient.get("$base/contributions") {
            if (status != null) parameter("status", status.name)
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to list contributions: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getContribution(id: String): Result<PendingContribution> = try {
        val response = httpClient.get("$base/contributions/$id")
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to get contribution: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun submitContribution(
        apk: PlatformFile,
        icon: ByteArray?,
        screenshots: List<PlatformFile>,
        packageName: String,
        versionName: String,
        versionCode: String,
        title: String,
        description: String,
        developer: String,
        category: String,
        categories: List<String>
    ): Result<PendingContribution> = try {
        val response = httpClient.post("$base/contributions") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", apk.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "application/vnd.android.package-archive")
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file\"; filename=\"${apk.name}\""
                            )
                        })
                        append("package_name", packageName)
                        append("version_name", versionName)
                        append("version_code", versionCode)
                        if (title.isNotBlank()) append("title", title)
                        if (description.isNotBlank()) append("description", description)
                        if (developer.isNotBlank()) append("developer", developer)
                        if (category.isNotBlank()) append("category", category)
                        if (categories.isNotEmpty()) append("categories", categories.joinToString(","))
                        if (icon != null) {
                            append("icon", icon, Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "form-data; name=\"icon\"; filename=\"icon.png\""
                                )
                            })
                        }
                        screenshots.forEach { file ->
                            append("screenshots", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "form-data; name=\"screenshots\"; filename=\"${file.name}\""
                                )
                            })
                        }
                    }
                )
            )
        }
        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else {
            Result.failure(Exception("Failed to submit contribution: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun downloadContribution(id: String): Result<ByteArray> = try {
        val response = httpClient.get("$base/contributions/$id/download")
        if (response.status.isSuccess()) {
            Result.success(response.readRawBytes())
        } else {
            Result.failure(Exception("Failed to download contribution: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun addContributionScreenshots(
        id: String,
        files: List<PlatformFile>
    ): Result<Unit> = try {
        val response = httpClient.post("$base/contributions/$id/screenshots") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        files.forEach { file ->
                            append("screenshots", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "image/png")
                                append(
                                    HttpHeaders.ContentDisposition,
                                    "form-data; name=\"screenshots\"; filename=\"${file.name}\""
                                )
                            })
                        }
                    }
                )
            )
        }
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to add screenshots: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun removeContributionScreenshot(id: String, index: Int): Result<Unit> = try {
        val response = httpClient.delete("$base/contributions/$id/screenshots/$index")
        if (response.status.isSuccess()) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to remove screenshot: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun purgeContribution(id: String): Result<String> = try {
        val response = httpClient.delete("$base/contributions/$id/declined")
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to purge contribution: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun approveContribution(id: String): Result<String> = try {
        val response = httpClient.put("$base/contributions/$id/approve")
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to approve contribution: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun declineContribution(id: String, reason: String?): Result<String> = try {
        val response = httpClient.put("$base/contributions/$id/decline") {
            contentType(ContentType.Application.Json)
            setBody(DeclineContributionRequest(reason = reason))
        }
        if (response.status.isSuccess()) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Failed to decline contribution: ${response.status}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}