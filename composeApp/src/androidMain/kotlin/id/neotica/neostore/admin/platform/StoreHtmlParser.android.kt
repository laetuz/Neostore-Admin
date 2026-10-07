package id.neotica.neostore.admin.platform

import id.neotica.neostore.admin.domain.model.enrich.ScrapedAppMetadata
import id.neotica.neostore.admin.domain.model.enrich.ScraperSource
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

actual fun parseStoreHtml(source: ScraperSource, html: String): ScrapedAppMetadata? {
    if (html.isBlank()) return null
    return try {
        val doc = Jsoup.parse(html)
        when (source) {
            ScraperSource.FDROID -> parseFdroid(source, doc)
            ScraperSource.PLAY, ScraperSource.WAYBACK -> parsePlay(source, doc)
        }
    } catch (e: Exception) {
        null
    }
}

private fun parsePlay(source: ScraperSource, doc: Document): ScrapedAppMetadata? {
    val developer = doc.selectFirst("a[href*=/store/apps/dev]")?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: doc.selectFirst("a.doc-header-link")?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: doc.selectFirst("a[href*=/store/apps/developer]")?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: ""

    val categorySlug = doc.selectFirst("[itemprop=genre] a[href*=/store/apps/category/]")?.attr("href")?.extractCategorySlug()
        ?: doc.select("a[href*=/store/apps/category/]").lastOrNull()?.attr("href")?.extractCategorySlug()
    val category = categorySlug?.let { titleCaseSlug(it) }
        ?: doc.selectFirst("[itemprop=genre]")?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: doc.select("a[href*=/store/apps/category/][aria-label]").lastOrNull()?.attr("aria-label")?.trim()?.takeIf { it.isNotBlank() }
        ?: ""

    val description = doc.selectFirst("div[data-g-id=description]")?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: doc.selectFirst("#doc-original-text")?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: doc.selectFirst("div.doc-description")?.text()?.trim()?.takeIf { it.isNotBlank() }
        ?: doc.select("meta[name]")
            .firstOrNull { it.attr("name").equals("description", ignoreCase = true) }
            ?.attr("content")?.trim()
            ?.takeIf { it.isNotBlank() }
        ?: ""

    val icon = doc.selectFirst("img[alt=\"Icon image\"]")?.attr("src")?.takeIf { it.isNotBlank() }
        ?: doc.selectFirst("img.doc-banner-icon")?.attr("src")?.takeIf { it.isNotBlank() }
        ?: ""

    var screenshots = doc.select("img[alt=\"Screenshot image\"]")
        .map { it.attr("src") }
        .filter { it.isNotBlank() }
    if (screenshots.isEmpty()) {
        screenshots = doc.select("img.doc-screenshot-img")
            .map { el -> el.attr("data-baseUrl").ifBlank { el.attr("src") } }
            .filter { it.isNotBlank() }
    }
    if (screenshots.isEmpty()) {
        screenshots = doc.select("img[src*=play-lh.googleusercontent.com]")
            .map { it.attr("src") }
            .filter { it.contains("w526-h296") }
    }

    if (developer.isBlank() && description.isBlank() && screenshots.isEmpty() && category.isBlank()) return null

    return ScrapedAppMetadata(
        source = source,
        developer = developer,
        categoryName = category,
        description = description,
        screenshots = screenshots.map { normalizeUrl(it, "https://play.google.com") }.distinct(),
        iconUrl = normalizeUrl(icon, "https://play.google.com")
    )
}

private fun parseFdroid(source: ScraperSource, doc: Document): ScrapedAppMetadata? {
    val developer = doc.selectFirst("li#author_name")?.text()
        ?.removePrefix("Author:")?.trim().orEmpty()
    val description = doc.selectFirst("div.package-description")?.wholeText()?.trim().orEmpty()
        .ifEmpty { doc.selectFirst("meta[name=description]")?.attr("content")?.trim().orEmpty() }
    val screenshots = doc.select("div#screenshots li.screenshot img")
        .map { it.attr("src") }
        .filter { it.isNotBlank() }
    val icon = doc.selectFirst("img.package-icon")?.attr("src").orEmpty()

    if (developer.isBlank() && description.isBlank() && screenshots.isEmpty()) return null

    return ScrapedAppMetadata(
        source = source,
        developer = developer,
        categoryName = "",
        description = description,
        screenshots = screenshots.map { normalizeUrl(it, "https://f-droid.org") }.distinct(),
        iconUrl = normalizeUrl(icon, "https://f-droid.org")
    )
}

private fun String.extractCategorySlug(): String? =
    Regex("/store/apps/category/([A-Za-z0-9_]+)").find(this)?.groupValues?.get(1)

private fun titleCaseSlug(slug: String): String =
    slug.split('_').joinToString(" ") { part ->
        part.lowercase().replaceFirstChar { it.uppercase() }
    }

private fun normalizeUrl(url: String, base: String): String = when {
    url.isBlank() -> ""
    url.startsWith("http") -> url
    url.startsWith("//") -> "https:$url"
    url.startsWith("/") -> "$base$url"
    else -> url
}
