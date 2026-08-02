package id.neotica.neostore.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import id.neotica.neostore.admin.domain.local.TokenStorage
import id.neotica.neostore.admin.platform.copyToClipboard
import id.neotica.neostore.admin.platform.installPlatformKeyDispatcher
import id.neotica.neostore.admin.platform.performPlatformPageDownScroll
import id.neotica.neostore.admin.ui.MainView
import id.neotica.neostore.admin.ui.feature.auth.AuthView
import id.neotica.neostore.admin.ui.feature.clipboard.ClipboardView
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardCopiedIndex
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardItems
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardPageDownCount
import id.neotica.neostore.admin.ui.feature.detailapp.DetailAppView
import id.neotica.neostore.admin.ui.navigation.Screen
import id.neotica.neostore.admin.ui.navigation.toScreen
import org.koin.compose.koinInject

@Composable
fun App(tokenStorage: TokenStorage = koinInject()) {
    val startScreen: Screen = if (tokenStorage.getToken() != null) Screen.Feed else Screen.Auth
    val backStack = remember { NavBackStack<Screen>(startScreen) }

    val tabTargets = mapOf(
        1 to Screen.Upload,
        2 to Screen.Feed,
        3 to Screen.Categories,
        4 to Screen.Analytics,
        5 to Screen.Info,
    )

    DisposableEffect(Unit) {
        val handle = installPlatformKeyDispatcher { event ->
            val digit = event.char?.digitToIntOrNull() ?: -1
            when {
                digit in 1..5 && (event.isMetaDown || event.isCtrlDown) -> {
                    backStack.clear()
                    backStack.add(tabTargets[digit]!!)
                    true
                }

                digit == 0 && (event.isMetaDown || event.isCtrlDown) -> {
                    if (backStack.lastOrNull() is Screen.Clipboard) {
                        backStack.removeLastOrNull()
                    } else {
                        backStack.add(Screen.Clipboard)
                    }
                    true
                }

                event.isEscape -> {
                    when (backStack.lastOrNull()) {
                        is Screen.Clipboard, is Screen.Detail -> {
                            backStack.removeLastOrNull()
                            true
                        }
                        else -> false
                    }
                }

                digit in 1..9
                    && backStack.lastOrNull() is Screen.Clipboard
                    && !event.isMetaDown && !event.isCtrlDown
                    -> {
                    val index = digit - 1
                    if (index < clipboardItems.size) {
                        copyToClipboard(clipboardItems[index])
                        clipboardCopiedIndex.value = index
                    }
                    true
                }

                event.isD
                    && backStack.lastOrNull() is Screen.Clipboard
                    && (event.isMetaDown || event.isCtrlDown)
                    -> {
                    performPlatformPageDownScroll(clipboardPageDownCount.value)
                    true
                }

                else -> false
            }
        }
        onDispose { handle() }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = { screen ->
            NavEntry(key = screen) { key ->
                when (key) {
                    Screen.Auth -> AuthView(
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(Screen.Feed)
                        }
                    )
                    Screen.Clipboard -> ClipboardView(
                        onBack = { backStack.removeLastOrNull() }
                    )
                    is Screen.Detail -> DetailAppView(
                        packageName = key.packageName,
                        onClick = { backStack.removeLastOrNull() },
                    )
                    else -> MainView(
                        screen = key,
                        onNavigateTab = { type ->
                            backStack.clear()
                            backStack.add(type.toScreen())
                        },
                        onNavigateToDetail = { app ->
                            backStack.add(Screen.Detail(app.packageName))
                        },
                        onLogout = {
                            tokenStorage.clearToken()
                            backStack.clear()
                            backStack.add(Screen.Auth)
                        },
                    )
                }
            }
        }
    )
}