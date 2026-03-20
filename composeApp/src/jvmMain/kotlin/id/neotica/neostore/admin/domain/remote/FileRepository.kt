package id.neotica.neostore.admin.domain.remote

import java.io.File

interface FileRepository {
    suspend fun uploadFile(
        file: File,
        s3Path: String,
        apkPath: String,
        versionCode: Int,
        onProgress: (Float) -> Unit
    ): Result<String>
    suspend fun registerAppVersion(
        packageName: String,
        versionName: String,
        versionCode: Int,
        fileUrl: String,
        changelog: String
    ): Result<String>
}