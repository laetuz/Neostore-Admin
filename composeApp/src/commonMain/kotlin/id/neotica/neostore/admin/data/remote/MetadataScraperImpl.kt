package id.neotica.neostore.admin.data.remote

import id.neotica.neostore.admin.data.ktorClient
import id.neotica.neostore.admin.domain.model.enrich.ScrapedAppMetadata
import id.neotica.neostore.admin.domain.model.enrich.ScraperSource
import id.neotica.neostore.admin.domain.model.enrich.WaybackVersion
import id.neotica.neostore.admin.domain.remote.MetadataScraper
import id.neotica.neostore.admin.platform.parseStoreHtml
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class MetadataScraperImpl : MetadataScraper {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val versionsCache = mutableMapOf<String, List<WaybackVersion>>()

    override suspend fun scrape(
        packageName: String,
        source: ScraperSource,
        waybackTimestamp: String?
    ): Result<ScrapedAppMetadata> = try {
        when (source) {
            ScraperSource.WAYBACK -> scrapeWayback(packageName, waybackTimestamp)
            ScraperSource.PLAY -> {
                val url = "https://play.google.com/store/apps/details?id=$packageName&hl=en&gl=US"
                fetchAndParse(source, url, packageName, null)
            }
            ScraperSource.FDROID -> {
                val url = "https://f-droid.org/en/packages/$packageName/"
                fetchAndParse(source, url, packageName, null)
            }
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun scrapeWayback(
        packageName: String,
        waybackTimestamp: String?
    ): Result<ScrapedAppMetadata> {
        val requested = waybackTimestamp ?: "0"
        val startUrl =
            "https://web.archive.org/web/${requested}id_/https://play.google.com/store/apps/details?id=$packageName"

        val response = ktorClient.get(startUrl) {
            header(HttpHeaders.UserAgent, USER_AGENT)
            header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
        }

        if (!response.status.isSuccess()) {
            return Result.failure(Exception("Wayback returned HTTP ${response.status.value}"))
        }

        val resolvedUrl = response.request.url.toString()
        val resolvedTs = Regex("/web/(\\d{14})").find(resolvedUrl)?.groupValues?.get(1)

        val html = response.bodyAsText()
        val parsed = withContext(Dispatchers.Default) {
            parseStoreHtml(ScraperSource.WAYBACK, html)
        } ?: return Result.failure(Exception("No metadata could be parsed from Wayback"))

        val snapshot = resolvedTs?.let { formatDate(it) } ?: waybackTimestamp?.let { formatDate(it) }
        return Result.success(parsed.copy(resolvedSnapshot = snapshot))
    }

    private suspend fun fetchAndParse(
        source: ScraperSource,
        url: String,
        packageName: String,
        snapshot: String?
    ): Result<ScrapedAppMetadata> {
        val response = ktorClient.get(url) {
            header(HttpHeaders.UserAgent, USER_AGENT)
            header(HttpHeaders.AcceptLanguage, "en-US,en;q=0.9")
        }
        if (!response.status.isSuccess()) {
            return Result.failure(Exception("${source.label} returned HTTP ${response.status.value}"))
        }
        val html = response.bodyAsText()
        val parsed = withContext(Dispatchers.Default) {
            parseStoreHtml(source, html)
        } ?: return Result.failure(Exception("No metadata could be parsed from ${source.label}"))
        return Result.success(parsed.copy(resolvedSnapshot = snapshot))
    }

    override suspend fun waybackVersions(packageName: String): Result<List<WaybackVersion>> {
        versionsCache[packageName]?.let { return Result.success(it) }
        return try {
            val target = "play.google.com/store/apps/details?id=$packageName".encodeURLParameter()
            val url = "https://web.archive.org/cdx/search/cdx" +
                "?url=$target&output=json&fl=timestamp&filter=statuscode:200&collapse=digest"

            val response = ktorClient.get(url) {
                header(HttpHeaders.UserAgent, USER_AGENT)
            }
            if (!response.status.isSuccess()) {
                return Result.failure(Exception("CDX returned HTTP ${response.status.value}"))
            }

            val rows: List<List<String>> = json.decodeFromString(response.bodyAsText())
            val versions = rows
                .mapNotNull { row -> row.firstOrNull() }
                .filter { it.length >= 8 && it.all { ch -> ch.isDigit() } }
                .map { WaybackVersion(timestamp = it, date = formatDate(it)) }

            versionsCache[packageName] = versions
            Result.success(versions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchBytes(url: String): Result<ByteArray> = try {
        val response = ktorClient.get(url) {
            header(HttpHeaders.UserAgent, USER_AGENT)
        }
        if (response.status.isSuccess()) {
            Result.success(response.readRawBytes())
        } else {
            Result.failure(Exception("HTTP ${response.status.value}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun formatDate(timestamp: String): String =
        if (timestamp.length >= 8) {
            "${timestamp.substring(0, 4)}-${timestamp.substring(4, 6)}-${timestamp.substring(6, 8)}"
        } else {
            timestamp
        }

    companion object {
        private const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    }
}
