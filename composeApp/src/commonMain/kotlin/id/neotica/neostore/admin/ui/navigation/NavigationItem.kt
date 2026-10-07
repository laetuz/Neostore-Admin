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
    ENRICH,
    INFO
}

data class NavItem(
    val type: MainScreenType?,
    val label: String,
    val indicator: String,
    val children: List<NavItem> = emptyList(),
)

private val analyticsItem = NavItem(MainScreenType.ANALYTICS, "Analytics", "\uD83D\uDCCA")
private val enrichItem = NavItem(MainScreenType.ENRICH, "Enrich", "\u2728")
private val contributionsItem = NavItem(MainScreenType.CONTRIBUTIONS, "Contributions", "\uD83D\uDEE0\uFE0F")
private val contributorsItem = NavItem(MainScreenType.CONTRIBUTORS, "Contributors", "\uD83D\uDC65")

private val manageOwner = NavItem(
    type = null,
    label = "Manage",
    indicator = "\uD83D\uDDC2\uFE0F",
    children = listOf(analyticsItem, enrichItem, contributionsItem, contributorsItem),
)

private val manageContributor = NavItem(
    type = null,
    label = "Manage",
    indicator = "\uD83D\uDDC2\uFE0F",
    children = listOf(contributionsItem),
)

val navItems = listOf(
    NavItem(MainScreenType.UPLOADER, "Upload", "\uD83D\uDCE6"),
    NavItem(MainScreenType.FEEDS, "Feed", "\uD83D\uDCCB"),
    NavItem(MainScreenType.CATEGORIES, "Categories", "\uD83D\uDCC1"),
    manageOwner,
    NavItem(MainScreenType.INFO, "Info", "\u2139\uFE0F"),
)

fun navItemsFor(role: ContributorRole): List<NavItem> = when (role) {
    ContributorRole.OWNER -> navItems
    ContributorRole.CONTRIBUTOR -> listOf(
        NavItem(MainScreenType.UPLOADER, "Submit", "\uD83D\uDCE6"),
        manageContributor,
        NavItem(MainScreenType.INFO, "Info", "\u2139\uFE0F"),
    )
    ContributorRole.NONE -> emptyList()
}

fun List<NavItem>.flattenScreens(): List<MainScreenType> =
    flatMap { item ->
        val childTypes = item.children.mapNotNull { it.type }
        if (childTypes.isNotEmpty()) childTypes else listOfNotNull(item.type)
    }
