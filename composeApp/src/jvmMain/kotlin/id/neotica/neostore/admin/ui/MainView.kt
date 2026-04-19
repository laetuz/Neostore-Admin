package id.neotica.neostore.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.feature.detailapp.DetailAppView
import id.neotica.neostore.admin.ui.feature.feed.FeedView
import id.neotica.neostore.admin.ui.feature.registerapp.RegisterAppView
import id.neotica.neostore.admin.ui.feature.updateapp.UpdateAppView
import id.neotica.neostore.admin.ui.feature.upload.UploadView

@Composable
fun MainView(
    onLogout: () -> Unit = {}
) {
    var screenTypeDropdownExpanded by remember { mutableStateOf(false) }
    var moreDropdownExpanded by remember { mutableStateOf(false) }
    var screenType by remember { mutableStateOf(MainScreenType.FEEDS) }

    MaterialTheme {
        Scaffold(
            topBar = {
                Column {
                    Row {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Neostore Admin",
                                    color = DarkPrimary
                                )
                            },
                            backgroundColor = DarkBackground,
                            actions = {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .clickable { screenTypeDropdownExpanded = !screenTypeDropdownExpanded }
                                            .border(1.dp, MaterialTheme.colorScheme.primary)
                                            .padding(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = screenType.name,
                                                color = DarkPrimary
                                            )
                                            Text(
                                                text = if (screenTypeDropdownExpanded) "⬆️" else "⬇️",
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = screenTypeDropdownExpanded,
                                            onDismissRequest = { screenTypeDropdownExpanded = false }
                                        ) {
                                            MainScreenType.entries.forEach { target ->
                                                DropdownMenuItem(
                                                    text = { Text(target.name) },
                                                    onClick = {
                                                        screenTypeDropdownExpanded = false
                                                        screenType = target
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        Modifier
                                            .border(1.dp, DarkPrimary)
                                            .clickable { moreDropdownExpanded = !moreDropdownExpanded }
                                    ) {
                                        Text(
                                            text = "More ...",
                                            color = DarkPrimary,
                                            modifier = Modifier.padding(8.dp)
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
                            }
                        )
                    }

                    Divider(
                        thickness = 2.dp,
                        color = DarkPrimary
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
                    .padding(it)
            ) {
                var selectedAppToUpdate by remember { mutableStateOf<AppFeedItemResponse?>(null) }

                when (screenType) {
                    MainScreenType.UPLOADER -> UploadView()
                    MainScreenType.REGISTRAR -> RegisterAppView()
                    MainScreenType.UPDATER -> UpdateAppView()
                    MainScreenType.FEEDS -> FeedView {
                        selectedAppToUpdate = it
                        screenType = MainScreenType.DETAIL
                    }

                    MainScreenType.DETAIL -> DetailAppView(packageName = selectedAppToUpdate?.packageName.toString()) {
                        screenType = MainScreenType.FEEDS
                    }
                }
            }
        }
    }
}

enum class MainScreenType {
    UPLOADER,
    REGISTRAR,
    UPDATER,
    FEEDS,
    DETAIL
}