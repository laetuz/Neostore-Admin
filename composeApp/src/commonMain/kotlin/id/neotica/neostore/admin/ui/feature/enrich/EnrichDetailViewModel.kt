package id.neotica.neostore.admin.ui.feature.enrich

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.neotica.neostore.admin.domain.model.UpdateAppRequest
import id.neotica.neostore.admin.domain.model.category.response.Category
import id.neotica.neostore.admin.domain.model.enrich.ScrapedAppMetadata
import id.neotica.neostore.admin.domain.model.enrich.ScraperSource
import id.neotica.neostore.admin.domain.remote.CategoriesRepository
import id.neotica.neostore.admin.domain.remote.FileRepository
import id.neotica.neostore.admin.domain.remote.MetadataScraper
import id.neotica.neostore.admin.platform.compressJpeg
import id.neotica.neostore.admin.platform.platformFileFromBytes
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class EnrichDetailViewModel(
    private val repo: FileRepository,
    private val scraper: MetadataScraper,
    private val categoriesRepo: CategoriesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EnrichDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var packageName: String = ""
    private var compressJob: Job? = null

    fun load(pkg: String) {
        packageName = pkg
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = "") }

            categoriesRepo.getCategories().onSuccess { cats ->
                _uiState.update { it.copy(categories = cats) }
            }

            repo.getAppDetail(pkg)
                .onSuccess { app ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            app = app,
                            developer = app.developer ?: "",
                            description = app.description,
                            categorySlug = app.category.ifBlank { null },
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, statusMessage = "Failed to load: ${error.message}") }
                }

            loadWaybackVersions()
        }
    }

    fun setSource(source: ScraperSource) {
        _uiState.update { it.copy(source = source) }
        if (source == ScraperSource.WAYBACK) loadWaybackVersions()
    }

    fun setDeveloper(value: String) = _uiState.update { it.copy(developer = value) }
    fun setDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun setCategorySlug(slug: String?) = _uiState.update { it.copy(categorySlug = slug) }

    fun useScrapedDeveloper() = _uiState.update {
        if (it.scrapedDeveloper.isBlank()) it else it.copy(developer = it.scrapedDeveloper)
    }

    fun useScrapedDescription() = _uiState.update {
        if (it.scrapedDescription.isBlank()) it else it.copy(description = it.scrapedDescription)
    }

    fun useScrapedCategory() = _uiState.update {
        it.scrapedCategorySlug?.let { slug -> it.copy(categorySlug = slug) } ?: it
    }

    fun setWaybackVersion(timestamp: String) {
        _uiState.update { it.copy(selectedWaybackTimestamp = timestamp) }
        scrape()
    }

    fun nextNewerVersion() {
        val versions = _uiState.value.waybackVersions
        val index = versions.indexOfFirst { it.timestamp == _uiState.value.selectedWaybackTimestamp }
        if (index in 0 until versions.lastIndex) setWaybackVersion(versions[index + 1].timestamp)
    }

    fun olderVersion() {
        val versions = _uiState.value.waybackVersions
        val index = versions.indexOfFirst { it.timestamp == _uiState.value.selectedWaybackTimestamp }
        if (index > 0) setWaybackVersion(versions[index - 1].timestamp)
    }

    private fun loadWaybackVersions() {
        val pkg = packageName
        if (pkg.isBlank() || _uiState.value.waybackVersions.isNotEmpty() || _uiState.value.isLoadingVersions) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingVersions = true) }
            scraper.waybackVersions(pkg)
                .onSuccess { versions ->
                    _uiState.update { state ->
                        state.copy(
                            isLoadingVersions = false,
                            waybackVersions = versions,
                            selectedWaybackTimestamp = state.selectedWaybackTimestamp ?: versions.firstOrNull()?.timestamp,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoadingVersions = false, statusMessage = "Version list unavailable: ${error.message}")
                    }
                }
        }
    }

    fun scrape() {
        val pkg = packageName
        if (pkg.isBlank()) return
        viewModelScope.launch {
            val source = _uiState.value.source
            val isWayback = source == ScraperSource.WAYBACK
            val maxAttempts = if (isWayback) 3 else 1

            var timestamp = if (isWayback) _uiState.value.selectedWaybackTimestamp else null
            var lastError: String? = null
            var attempt = 0

            _uiState.update { it.copy(isScraping = true, statusMessage = "Scraping ${source.label}...") }

            while (attempt < maxAttempts) {
                attempt++
                val result = scraper.scrape(pkg, source, timestamp)
                if (result.isSuccess) {
                    applyScraped(result.getOrThrow())
                    return@launch
                }
                lastError = result.exceptionOrNull()?.message

                val versions = _uiState.value.waybackVersions
                val index = versions.indexOfFirst { it.timestamp == timestamp }
                val next = if (isWayback && index in 0 until versions.lastIndex) versions[index + 1].timestamp else null
                if (next == null) break

                timestamp = next
                _uiState.update {
                    it.copy(
                        selectedWaybackTimestamp = next,
                        statusMessage = "No data on that version, trying newer (${versions[index + 1].date})...",
                    )
                }
            }

            _uiState.update {
                it.copy(isScraping = false, statusMessage = "Scrape failed: ${lastError ?: "no data"}")
            }
        }
    }

    private fun applyScraped(meta: ScrapedAppMetadata) {
        val app = _uiState.value.app
        val resolvedSlug = resolveCategorySlug(meta.categoryName, _uiState.value.categories)
        _uiState.update { state ->
            state.copy(
                isScraping = false,
                hasScraped = true,
                developer = state.developer.ifBlank { meta.developer },
                description = state.description.ifBlank { meta.description },
                categorySlug = state.categorySlug ?: resolvedSlug,
                scrapedDeveloper = meta.developer,
                scrapedDescription = meta.description,
                scrapedCategoryName = meta.categoryName,
                scrapedCategorySlug = resolvedSlug,
                resolvedSnapshot = meta.resolvedSnapshot,
                screenshots = meta.screenshots.map {
                    ScrapedScreenshot(url = it, included = app?.screenshots?.isEmpty() ?: true)
                },
                statusMessage = "Scraped ${meta.source.label}" +
                    (meta.resolvedSnapshot?.let { " ($it)" } ?: "") +
                    ": ${meta.screenshots.size} screenshot(s).",
            )
        }
        fetchScreenshots()
    }

    private fun fetchScreenshots() {
        val urls = _uiState.value.screenshots.map { it.url }
        if (urls.isEmpty()) return
        viewModelScope.launch {
            val semaphore = Semaphore(4)
            coroutineScope {
                urls.forEachIndexed { index, url ->
                    launch {
                        semaphore.withPermit {
                            scraper.fetchBytes(url)
                                .onSuccess { bytes ->
                                    updateScreenshot(index) {
                                        it.copy(bytes = bytes, originalSize = bytes.size.toLong(), isLoading = false, failed = false)
                                    }
                                }
                                .onFailure {
                                    updateScreenshot(index) { it.copy(isLoading = false, failed = true) }
                                }
                        }
                    }
                }
            }
            recomputeCompressed()
        }
    }

    private fun updateScreenshot(index: Int, transform: (ScrapedScreenshot) -> ScrapedScreenshot) {
        _uiState.update { state ->
            val list = state.screenshots.toMutableList()
            if (index in list.indices) list[index] = transform(list[index])
            state.copy(screenshots = list)
        }
    }

    fun toggleInclude(index: Int) {
        updateScreenshot(index) { it.copy(included = !it.included) }
        recomputeCompressed()
    }

    fun setQuality(quality: Int) {
        _uiState.update { it.copy(quality = quality.coerceIn(1, 100)) }
        compressJob?.cancel()
        compressJob = viewModelScope.launch {
            delay(250)
            recomputeCompressed()
        }
    }

    private fun recomputeCompressed() {
        val quality = _uiState.value.quality
        _uiState.value.screenshots.forEachIndexed { index, shot ->
            val bytes = shot.bytes
            if (!shot.included || bytes == null) return@forEachIndexed
            val compressed = compressJpeg(bytes, quality)
            updateScreenshot(index) {
                if (compressed != null) {
                    it.copy(
                        compressedBytes = compressed,
                        compressedSize = compressed.size.toLong(),
                        conversionFailed = false,
                    )
                } else {
                    it.copy(
                        compressedBytes = null,
                        compressedSize = 0L,
                        conversionFailed = true,
                        included = false,
                    )
                }
            }
        }
    }

    fun apply() {
        val pkg = packageName
        val app = _uiState.value.app ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isApplying = true, statusMessage = "Updating metadata...") }

            val request = UpdateAppRequest(
                title = app.title,
                description = _uiState.value.description,
                category = _uiState.value.categorySlug ?: app.category,
                categories = app.categories,
                iconUrl = app.iconUrl ?: "",
                developer = _uiState.value.developer.ifBlank { null },
                githubRepo = app.githubRepo,
            )

            val updateResult = repo.updateApp(pkg, request)
            if (updateResult.isFailure) {
                _uiState.update {
                    it.copy(isApplying = false, statusMessage = "Update failed: ${updateResult.exceptionOrNull()?.message}")
                }
                return@launch
            }

            val convertible = _uiState.value.screenshots.filter { it.included && it.compressedBytes != null }
            val skipped = _uiState.value.screenshots.count { it.included && it.compressedBytes == null }
            val files = convertible.mapIndexed { index, shot ->
                platformFileFromBytes("screenshot-$index.jpg", shot.compressedBytes!!)
            }

            if (files.isNotEmpty()) {
                _uiState.update { it.copy(statusMessage = "Uploading ${files.size} screenshot(s)...") }
                val uploadResult = repo.uploadScreenshots(pkg, files)
                if (uploadResult.isFailure) {
                    _uiState.update {
                        it.copy(
                            isApplying = false,
                            statusMessage = "Metadata saved, screenshots failed: ${uploadResult.exceptionOrNull()?.message}"
                        )
                    }
                    return@launch
                }
            }

            val message = if (skipped > 0) {
                "Applied successfully. Skipped $skipped screenshot(s) that could not be converted to JPEG."
            } else {
                "Applied successfully."
            }
            _uiState.update { it.copy(isApplying = false, statusMessage = message) }
        }
    }

    private fun resolveCategorySlug(name: String, categories: List<Category>): String? {
        if (name.isBlank()) return null
        for (root in categories) {
            if (root.name.equals(name, ignoreCase = true)) return root.slug
            root.children.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it.slug }
        }
        return null
    }
}
