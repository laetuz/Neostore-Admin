package id.neotica.neostore.admin.ui.navigation

enum class MainScreenType {
    UPLOADER,
    REGISTRAR,
    UPDATER,
    FEEDS,
    DETAIL,
    ANALYTICS
}

data class NavItem(
    val type: MainScreenType,
    val label: String,
    val indicator: String,
)

val navItems = listOf(
    NavItem(MainScreenType.UPLOADER, "Upload", "📦"),
    NavItem(MainScreenType.REGISTRAR, "Register", "🖌️"),
    NavItem(MainScreenType.UPDATER, "Update", "🔄"),
    NavItem(MainScreenType.FEEDS, "Feed", "📋"),
    NavItem(MainScreenType.ANALYTICS, "Analytics", "📊"),
)