package id.neotica.neostore.admin.ui.navigation

import id.neotica.neostore.admin.domain.model.contributor.ContributorRole

enum class MainScreenType {
    UPLOADER,
    FEEDS,
    DETAIL,
    CATEGORIES,
    ANALYTICS,
    CONTRIBUTIONS,
    CONTRIBUTORS,
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
    NavItem(MainScreenType.CONTRIBUTIONS, "Contributions", "🛠️"),
    NavItem(MainScreenType.CONTRIBUTORS, "Contributors", "👥"),
    NavItem(MainScreenType.INFO, "Info", "ℹ️"),
)

fun navItemsFor(role: ContributorRole): List<NavItem> = when (role) {
    ContributorRole.OWNER -> navItems
    ContributorRole.CONTRIBUTOR -> listOf(
        NavItem(MainScreenType.UPLOADER, "Submit", "📦"),
        NavItem(MainScreenType.CONTRIBUTIONS, "My Submissions", "🛠️"),
        NavItem(MainScreenType.INFO, "Info", "ℹ️"),
    )
    ContributorRole.NONE -> emptyList()
}