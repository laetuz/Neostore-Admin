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
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.feature.analytics.AnalyticsView
import id.neotica.neostore.admin.ui.feature.categories.CategoriesView
import id.neotica.neostore.admin.ui.feature.feed.FeedView
import id.neotica.neostore.admin.ui.feature.info.InfoView
import id.neotica.neostore.admin.ui.feature.upload.UploadView
import id.neotica.neostore.admin.ui.navigation.AppNavigationBar
import id.neotica.neostore.admin.ui.navigation.AppNavigationRail
import id.neotica.neostore.admin.ui.navigation.MainScreenType
import id.neotica.neostore.admin.ui.navigation.Screen
import id.neotica.neostore.admin.ui.navigation.toMainScreenType

@Composable
fun MainView(
    screen: Screen,
    onNavigateTab: (MainScreenType) -> Unit,
    onNavigateToDetail: (AppFeedItemResponse) -> Unit,
    onLogout: () -> Unit = {},
) {
    val screenType = screen.toMainScreenType() ?: MainScreenType.FEEDS

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
                            onNavigateToDetail = onNavigateToDetail,
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
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 1.dp)
                        ) {
                            MainContent(
                                screenType = screenType,
                                onNavigateToDetail = onNavigateToDetail,
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
                    text = "Neostore Admin",
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
    onNavigateToDetail: (AppFeedItemResponse) -> Unit,
) {
    when (screenType) {
        MainScreenType.UPLOADER -> UploadView()
        MainScreenType.FEEDS -> FeedView(onNavigateToUpdater = onNavigateToDetail)
        MainScreenType.DETAIL -> Unit
        MainScreenType.ANALYTICS -> AnalyticsView()
        MainScreenType.CATEGORIES -> CategoriesView()
        MainScreenType.INFO -> InfoView()
    }
}
