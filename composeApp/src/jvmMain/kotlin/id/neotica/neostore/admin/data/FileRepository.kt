package id.neotica.neostore.admin.data

import java.io.File

interface FileRepository {
    suspend fun uploadFile(
        file: File, s3Path: String, apkPath: String, onProgress: (Float) -> Unit
    ): Result<String>
}