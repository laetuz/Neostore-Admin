package id.neotica.neostore.admin.domain.remote

import id.neotica.neostore.admin.domain.model.contributor.AdminContributor
import id.neotica.neostore.admin.domain.model.contributor.ContributionStatus
import id.neotica.neostore.admin.domain.model.contributor.ContributorMe
import id.neotica.neostore.admin.domain.model.contributor.ContributorRole
import id.neotica.neostore.admin.domain.model.contributor.PendingContribution
import id.neotica.neostore.admin.domain.model.contributor.UserSummary
import id.neotica.neostore.admin.platform.PlatformFile

interface ContributorsRepository {
    suspend fun getMyRole(): Result<ContributorMe>

    suspend fun listContributors(): Result<List<AdminContributor>>
    suspend fun searchUsers(query: String): Result<List<UserSummary>>
    suspend fun addContributor(
        userId: String?,
        username: String?,
        role: ContributorRole
    ): Result<AdminContributor>
    suspend fun updateContributorRole(userId: String, role: ContributorRole): Result<String>
    suspend fun removeContributor(userId: String): Result<String>

    suspend fun listContributions(status: ContributionStatus? = null): Result<List<PendingContribution>>
    suspend fun getContribution(id: String): Result<PendingContribution>
    suspend fun submitContribution(
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
    ): Result<PendingContribution>
    suspend fun downloadContribution(id: String): Result<ByteArray>
    suspend fun addContributionScreenshots(id: String, files: List<PlatformFile>): Result<Unit>
    suspend fun removeContributionScreenshot(id: String, index: Int): Result<Unit>
    suspend fun purgeContribution(id: String): Result<String>
    suspend fun approveContribution(id: String): Result<String>
    suspend fun declineContribution(id: String, reason: String?): Result<String>
}