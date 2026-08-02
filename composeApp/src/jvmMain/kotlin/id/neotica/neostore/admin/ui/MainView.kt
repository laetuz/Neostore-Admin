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
import androidx.compose.runtime.DisposableEffect
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
import id.neotica.neostore.admin.ui.feature.detailapp.DetailAppView
import id.neotica.neostore.admin.ui.feature.feed.FeedView
import id.neotica.neostore.admin.ui.feature.info.InfoView
import id.neotica.neostore.admin.ui.feature.clipboard.ClipboardView
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardCopiedIndex
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardItems
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardPageDownCount
import id.neotica.neostore.admin.ui.feature.clipboard.copyToClipboard
import id.neotica.neostore.admin.ui.feature.upload.UploadView
import id.neotica.neostore.admin.ui.navigation.AppNavigationRail
import id.neotica.neostore.admin.ui.navigation.MainScreenType
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.Robot
import java.awt.event.KeyEvent
import kotlin.concurrent.thread

@Composable
fun MainView(
    onLogout: () -> Unit = {}
) {
    var moreDropdownExpanded by remember { mutableStateOf(false) }
    var screenType by remember { mutableStateOf(MainScreenType.FEEDS) }
    var selectedAppToUpdate by remember { mutableStateOf<AppFeedItemResponse?>(null) }
    var showClipboardScreen by remember { mutableStateOf(false) }

    val tabKeyCodes = mapOf(
        KeyEvent.VK_1 to MainScreenType.UPLOADER,
        KeyEvent.VK_2 to MainScreenType.FEEDS,
        KeyEvent.VK_3 to MainScreenType.CATEGORIES,
        KeyEvent.VK_4 to MainScreenType.ANALYTICS,
        KeyEvent.VK_5 to MainScreenType.INFO,
    )

    DisposableEffect(Unit) {
        val dispatcher = KeyEventDispatcher { event ->
            if (event.id == KeyEvent.KEY_PRESSED) {
                when (event.keyCode) {
                    in tabKeyCodes
                        if (event.isMetaDown || event.isControlDown)
                        -> {
                        screenType = tabKeyCodes[event.keyCode]!!
                        true
                    }

                    KeyEvent.VK_ESCAPE
                        if showClipboardScreen
                        -> {
                        showClipboardScreen = false
                        true
                    }

                    KeyEvent.VK_ESCAPE
                        if screenType == MainScreenType.DETAIL
                        -> {
                        screenType = MainScreenType.FEEDS
                        true
                    }

                    KeyEvent.VK_0
                        if (event.isMetaDown || event.isControlDown)
                        -> {
                        showClipboardScreen = !showClipboardScreen
                        true
                    }

                    in KeyEvent.VK_1..KeyEvent.VK_9
                        if showClipboardScreen
                            && !event.isMetaDown && !event.isControlDown
                        -> {
                        val index = event.keyCode - KeyEvent.VK_1
                        if (index < clipboardItems.size) {
                            copyToClipboard(clipboardItems[index])
                            clipboardCopiedIndex.value = index
                        }
                        true
                    }

                    KeyEvent.VK_D
                        if showClipboardScreen
                            && (event.isMetaDown || event.isControlDown)
                        -> {
                        thread {
                            val robot = Robot()
                            Thread.sleep(50)
                            robot.keyPress(KeyEvent.VK_META)
                            robot.keyPress(KeyEvent.VK_TAB)
                            robot.keyRelease(KeyEvent.VK_TAB)
                            robot.keyRelease(KeyEvent.VK_META)
                            Thread.sleep(100)
                            repeat(clipboardPageDownCount.value) {
                                robot.keyPress(KeyEvent.VK_PAGE_DOWN)
                                robot.keyRelease(KeyEvent.VK_PAGE_DOWN)
                                Thread.sleep(50)
                            }
                        }
                        true
                    }

                    else -> false
                }
            } else false
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        onDispose { KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher) }
    }

    MaterialTheme {
        if (showClipboardScreen) {
            ClipboardView(onBack = { showClipboardScreen = false })
        } else {
            Scaffold(
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "Neostore Admin",
                                    color = DarkPrimary
                                )
                            },
                            backgroundColor = DarkBackground,
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
                        Divider(thickness = 2.dp, color = DarkPrimary)
                    }
                }
            ) { paddingValues ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DarkBackground)
                        .padding(paddingValues)
                ) {
                    AppNavigationRail(
                        currentScreen = screenType,
                        onNavigate = { screenType = it },
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 1.dp)
                    ) {
                        when (screenType) {
                            MainScreenType.UPLOADER -> UploadView()
                            MainScreenType.FEEDS -> FeedView {
                                selectedAppToUpdate = it
                                screenType = MainScreenType.DETAIL
                            }
                            MainScreenType.DETAIL -> DetailAppView(
                                packageName = selectedAppToUpdate?.packageName.toString(),
                                onClick = { screenType = MainScreenType.FEEDS }
                            )
                            MainScreenType.ANALYTICS -> AnalyticsView()
                            MainScreenType.CATEGORIES -> CategoriesView()
                            MainScreenType.INFO -> InfoView()
                        }
                    }
                }
            }
        }
    }
}
