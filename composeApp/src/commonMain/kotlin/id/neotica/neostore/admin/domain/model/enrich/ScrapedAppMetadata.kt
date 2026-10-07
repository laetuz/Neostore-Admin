package id.neotica.neostore.admin.domain.model.enrich

enum class ScraperSource(val label: String) {
    WAYBACK("Wayback"),
    PLAY("Play (live)"),
    FDROID("F-Droid")
}

data class WaybackVersion(
    val timestamp: String,
    val date: String
)

data class ScrapedAppMetadata(
    val source: ScraperSource,
    val developer: String = "",
    val categoryName: String = "",
    val description: String = "",
    val screenshots: List<String> = emptyList(),
    val iconUrl: String = "",
    val resolvedSnapshot: String? = null
)
