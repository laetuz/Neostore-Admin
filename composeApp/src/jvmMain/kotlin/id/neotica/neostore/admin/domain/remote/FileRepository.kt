package id.neotica.neostore.admin.domain.remote

import id.neotica.neostore.admin.domain.model.AppVersionResponse
import id.neotica.neostore.admin.domain.model.RegisterAppRequest
import java.io.File

interface FileRepository {
    suspend fun uploadFile(
        file: File,
        s3Path: String,
        apkPath: String,
        versionCode: Int,
        onProgress: (Float) -> Unit,
    ): Result<String>
    suspend fun uploadIcon(file: File, s3Path: String, apkPath: String): Result<String>
    suspend fun publishApkVersion(
        packageName: String,
        versionName: String,
        versionCode: Int,
        fileUrl: String,
        changelog: String,
        minSdk: Int,
        maxSdk: Int,
    ): Result<String>
    suspend fun checkLatestVersion(packageName: String): Result<AppVersionResponse>
    suspend fun registerApp(registerAppRequest: RegisterAppRequest): Result<String>
}