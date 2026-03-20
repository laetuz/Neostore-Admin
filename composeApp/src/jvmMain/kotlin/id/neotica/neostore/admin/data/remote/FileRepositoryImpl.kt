package id.neotica.neostore.admin.data.remote

import id.neotica.neostore.admin.data.ktorClient
import id.neotica.neostore.admin.domain.model.AppVersionRequest
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.utils.Constants.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
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
        onProgress: (Float) -> Unit
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

    override suspend fun registerAppVersion(
        packageName: String,
        versionName: String,
        versionCode: Int,
        fileUrl: String,
        changelog: String
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
                        changelog = changelog
                    )
                )
            }

            if (response.status.isSuccess()) {
                Result.success(response.bodyAsText())
            } else {
                Result.failure(Exception("Failed to register app version"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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