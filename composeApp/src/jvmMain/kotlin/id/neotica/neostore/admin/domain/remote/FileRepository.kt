package id.neotica.neostore.admin.domain.remote

import id.neotica.neostore.admin.domain.model.AppVersionResponse
import id.neotica.neostore.admin.domain.model.RegisterAppRequest
import id.neotica.neostore.admin.domain.model.UpdateAppRequest
import id.neotica.neostore.admin.domain.model.response.AppDetailResponse
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.domain.model.response.PaginationResponse
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
    suspend fun updateApp(packageName: String, updateAppRequest: UpdateAppRequest): Result<String>
    suspend fun getFeeds(
        page: Int = 1,
        limit: Int = 10,
        search: String? = null,
        category: String? = null
    ): Result<PaginationResponse<AppFeedItemResponse>>
    suspend fun getAppDetail(packageName: String): Result<AppDetailResponse>
}