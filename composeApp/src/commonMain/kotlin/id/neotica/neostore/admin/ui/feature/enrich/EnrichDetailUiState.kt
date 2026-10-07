package id.neotica.neostore.admin.ui.feature.enrich

import id.neotica.neostore.admin.domain.model.category.response.Category
import id.neotica.neostore.admin.domain.model.enrich.ScraperSource
import id.neotica.neostore.admin.domain.model.enrich.WaybackVersion
import id.neotica.neostore.admin.domain.model.response.AppDetailResponse

data class ScrapedScreenshot(
    val url: String,
    val bytes: ByteArray? = null,
    val originalSize: Long = 0L,
    val isLoading: Boolean = true,
    val failed: Boolean = false,
    val included: Boolean = true,
    val compressedBytes: ByteArray? = null,
    val compressedSize: Long = 0L,
    val conversionFailed: Boolean = false
)

data class EnrichDetailUiState(
    val isLoading: Boolean = false,
    val app: AppDetailResponse? = null,
    val source: ScraperSource = ScraperSource.WAYBACK,
    val isScraping: Boolean = false,
    val hasScraped: Boolean = false,
    val developer: String = "",
    val description: String = "",
    val categorySlug: String? = null,
    val categories: List<Category> = emptyList(),
    val scrapedDeveloper: String = "",
    val scrapedDescription: String = "",
    val scrapedCategoryName: String = "",
    val scrapedCategorySlug: String? = null,
    val resolvedSnapshot: String? = null,
    val waybackVersions: List<WaybackVersion> = emptyList(),
    val selectedWaybackTimestamp: String? = null,
    val isLoadingVersions: Boolean = false,
    val screenshots: List<ScrapedScreenshot> = emptyList(),
    val quality: Int = 80,
    val isApplying: Boolean = false,
    val statusMessage: String = ""
)
