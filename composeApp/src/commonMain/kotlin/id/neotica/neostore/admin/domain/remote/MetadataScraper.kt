package id.neotica.neostore.admin.domain.remote

import id.neotica.neostore.admin.domain.model.enrich.ScrapedAppMetadata
import id.neotica.neostore.admin.domain.model.enrich.ScraperSource
import id.neotica.neostore.admin.domain.model.enrich.WaybackVersion

interface MetadataScraper {
    suspend fun scrape(
        packageName: String,
        source: ScraperSource,
        waybackTimestamp: String? = null
    ): Result<ScrapedAppMetadata>

    suspend fun waybackVersions(packageName: String): Result<List<WaybackVersion>>

    suspend fun fetchBytes(url: String): Result<ByteArray>
}
