package id.neotica.neostore.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.contributor.ContributorRole
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.feature.analytics.AnalyticsView
import id.neotica.neostore.admin.ui.feature.categories.CategoriesView
import id.neotica.neostore.admin.ui.feature.contributions.ContributionsView
import id.neotica.neostore.admin.ui.feature.contributors.ContributorsView
import id.neotica.neostore.admin.ui.feature.feed.FeedView
import id.neotica.neostore.admin.ui.feature.info.InfoView
import id.neotica.neostore.admin.ui.feature.upload.UploadView
import id.neotica.neostore.admin.ui.navigation.AppNavigationBar
import id.neotica.neostore.admin.ui.navigation.AppNavigationRail
import id.neotica.neostore.admin.ui.navigation.MainScreenType
import id.neotica.neostore.admin.ui.navigation.Screen
import id.neotica.neostore.admin.ui.navigation.navItemsFor
import id.neotica.neostore.admin.ui.navigation.toMainScreenType

@Composable
fun MainView(
    screen: Screen,
    role: ContributorRole,
    myUserId: String,
    onNavigateTab: (MainScreenType) -> Unit,
    onNavigateToDetail: (AppFeedItemResponse) -> Unit,
    onNavigateToContribution: (String) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val screenType = screen.toMainScreenType() ?: MainScreenType.FEEDS
    val items = navItemsFor(role)

    MaterialTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxWidth < 600.dp

            if (isCompact) {
                Scaffold(
                    topBar = {
                        MainTopBar(onLogout = onLogout)
                    },
                    bottomBar = {
                        if (screenType != MainScreenType.DETAIL) {
                            AppNavigationBar(
                                currentScreen = screenType,
                                onNavigate = onNavigateTab,
                                items = items,
                            )
                        }
                    },
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                            .padding(paddingValues)
                    ) {
                        MainContent(
                            screenType = screenType,
                            role = role,
                            myUserId = myUserId,
                            onNavigateToDetail = onNavigateToDetail,
                            onNavigateToContribution = onNavigateToContribution,
                        )
                    }
                }
            } else {
                Scaffold(
                    topBar = {
                        MainTopBar(onLogout = onLogout)
                    },
                ) { paddingValues ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(DarkBackground)
                            .padding(paddingValues)
                    ) {
                        AppNavigationRail(
                            currentScreen = screenType,
                            onNavigate = onNavigateTab,
                            items = items,
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 1.dp)
                        ) {
                            MainContent(
                                screenType = screenType,
                                role = role,
                                myUserId = myUserId,
                                onNavigateToDetail = onNavigateToDetail,
                                onNavigateToContribution = onNavigateToContribution,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(onLogout: () -> Unit) {
    var moreDropdownExpanded by remember { mutableStateOf(false) }

    Column {
        TopAppBar(
            title = {
                Text(
                    text = "HoloMarket Console",
                    color = DarkPrimary
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground),
            actions = {
                Box(
                    Modifier
                        .border(1.dp, DarkPrimary)
                        .clickable { moreDropdownExpanded = !moreDropdownExpanded }
                ) {
                    Text(
                        text = "More \u25BE",
                        color = DarkPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                    DropdownMenu(
                        expanded = moreDropdownExpanded,
                        onDismissRequest = { moreDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                moreDropdownExpanded = false
                                onLogout()
                            }
                        )
                    }
                }
            }
        )
        HorizontalDivider(thickness = 2.dp, color = DarkPrimary)
    }
}

@Composable
private fun MainContent(
    screenType: MainScreenType,
    role: ContributorRole,
    myUserId: String,
    onNavigateToDetail: (AppFeedItemResponse) -> Unit,
    onNavigateToContribution: (String) -> Unit,
) {
    when (screenType) {
        MainScreenType.UPLOADER -> UploadView(role = role)
        MainScreenType.FEEDS -> FeedView(onNavigateToUpdater = onNavigateToDetail)
        MainScreenType.DETAIL -> Unit
        MainScreenType.ANALYTICS -> AnalyticsView()
        MainScreenType.CATEGORIES -> CategoriesView()
        MainScreenType.CONTRIBUTIONS -> ContributionsView(
            role = role,
            myUserId = myUserId,
            onNavigateToDetail = onNavigateToContribution,
        )
        MainScreenType.CONTRIBUTORS -> ContributorsView()
        MainScreenType.INFO -> InfoView()
    }
}
