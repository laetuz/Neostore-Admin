package id.neotica.neostore.admin.ui.navigation

enum class MainScreenType {
    UPLOADER,
    FEEDS,
    DETAIL,
    CATEGORIES,
    ANALYTICS,
    INFO
}

data class NavItem(
    val type: MainScreenType,
    val label: String,
    val indicator: String,
)

val navItems = listOf(
    NavItem(MainScreenType.UPLOADER, "Upload", "📦"),
    NavItem(MainScreenType.FEEDS, "Feed", "📋"),
    NavItem(MainScreenType.CATEGORIES, "Categories", "📁"),
    NavItem(MainScreenType.ANALYTICS, "Analytics", "📊"),
    NavItem(MainScreenType.INFO, "Info", "ℹ️"),
)