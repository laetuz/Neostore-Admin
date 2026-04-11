package id.neotica.neostore.admin.data.remote

import id.neotica.neostore.admin.data.ktorClient
import id.neotica.neostore.admin.domain.model.AppVersionRequest
import id.neotica.neostore.admin.domain.model.AppVersionResponse
import id.neotica.neostore.admin.domain.model.RegisterAppRequest
import id.neotica.neostore.admin.domain.model.UpdateAppRequest
import id.neotica.neostore.admin.domain.model.response.AppDetailResponse
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toByteReadChannel
import java.io.File

class FileRepositoryImpl(
    private val httpClient: HttpClient,
): FileRepository {
    private val LARGE_FILE_THRESHOLD = 50 * 1024 * 1024L

    override suspend fun uploadFile(
        file: File,
        s3Path: String,
        apkPath: String,
        versionCode: Int,
        onProgress: (Float) -> Unit,
    ): Result<String> {
        val apkPathCheck = apkPath.ifEmpty { "" }
        return try {
            if (file.length() > LARGE_FILE_THRESHOLD) {
                uploadRaw(file, s3Path, onProgress)
            } else {
                uploadMultipart(
                    file = file,
                    s3Path = s3Path,
                    apkPath = apkPathCheck,
                    versionCode = versionCode,
                    onProgress = onProgress
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadIcon(
        file: File,
        s3Path: String,
        apkPath: String
    ): Result<String> {
        return try {
            val response = httpClient.post("$s3Path/upload/form") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("file", file.readBytes(), Headers.build {
                                append(HttpHeaders.ContentType, "image/png")

                                val fileName = "$apkPath/icon.jpg"
                                append(HttpHeaders.ContentDisposition, "form-data; name=\"file\"; filename=\"${fileName}\"")
                            })
                        }
                    )
                )
            }
            if (response.status == HttpStatusCode.OK) {
                Result.success(response.bodyAsText())
            } else Result.failure(Exception("Icon upload failed: ${response.status}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun publishApkVersion(
        packageName: String,
        versionName: String,
        versionCode: Int,
        fileUrl: String,
        changelog: String,
        minSdk: Int,
        maxSdk: Int,
    ): Result<String> {
        return try {
            val url = "$BASE_URL/neostore/admin/apps/$packageName/versions"

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    AppVersionRequest(
                        versionName = versionName,
                        versionCode = versionCode,
                        fileUrl = fileUrl,
                        changelog = changelog,
                        minSdk = minSdk,
                        maxSdk = maxSdk
                    )
                )
            }

            if (response.status.isSuccess()) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("Failed to publish app version"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkLatestVersion(packageName: String): Result<AppVersionResponse> = try {
        val url = "$BASE_URL/neostore/apps/$packageName/latest"
        val response = httpClient.get(url)

        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else Result.failure(Exception(""))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun registerApp(registerAppRequest: RegisterAppRequest): Result<String> {
        return try {
            val url = "$BASE_URL/neostore/admin/apps"

            val response = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(
                    registerAppRequest
                )
            }

            if (response.status.isSuccess()) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("Failed to register app."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateApp(packageName: String, updateAppRequest: UpdateAppRequest): Result<String> {
        return try {
            val url = "$BASE_URL/neostore/admin/apps/${packageName}"

            val response = httpClient.put(url) {
                contentType(ContentType.Application.Json)
                setBody(updateAppRequest)
            }

            if (response.status.isSuccess()) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("Failed to register app."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAppDetail(packageName: String): Result<AppDetailResponse> = try {
        val url = "$BASE_URL/neostore/apps/$packageName"
        val response = httpClient.get(url)

        if (response.status.isSuccess()) {
            Result.success(response.body())
        } else Result.failure(Exception(""))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uploadRaw(
        file: File,
        s3Path: String,
        onProgress: (Float) -> Unit
    ): Result<String> {
        val response = httpClient.post("$s3Path/raw") {

            // Stream directly from disk
            setBody(file.readChannel())

            onUpload { bytesSent, totalBytes ->
                if (totalBytes != null) {
                    println("Progress: ${bytesSent.toFloat() / totalBytes.toFloat()}")
                    onProgress(bytesSent.toFloat() / totalBytes.toFloat())
                } else return@onUpload
            }
        }

        return if (response.status == HttpStatusCode.OK) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Raw Upload Failed: ${response.status}"))
        }
    }

    suspend fun uploadMultipart(
        file: File,
        s3Path: String,
        apkPath: String,
        versionCode: Int,
        onProgress: (Float) -> Unit
    ): Result<String> {
        val response = ktorClient.post("$s3Path/upload/form") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("file", file.readBytes(), Headers.build {
                            append(HttpHeaders.ContentType, "application/octet-stream")

                            val fileName = "$apkPath/${versionCode}.apk"
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file\"; filename=\"${fileName}\""
                            )
                        })
                    }
                )
            )

            onUpload { bytesSent, totalBytes ->
                if (totalBytes != null) {
                    onProgress(bytesSent.toFloat() / totalBytes.toFloat())
                } else return@onUpload
            }
        }

        return if (response.status == HttpStatusCode.OK) {
            Result.success(response.bodyAsText())
        } else {
            Result.failure(Exception("Form Upload Failed: ${response.status}"))
        }
    }

    private fun File.readChannel(): ByteReadChannel = this.inputStream().toByteReadChannel()
}