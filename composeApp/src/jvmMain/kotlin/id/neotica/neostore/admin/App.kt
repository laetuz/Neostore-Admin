package id.neotica.neostore.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import id.neotica.neostore.admin.domain.local.TokenStorage
import id.neotica.neostore.admin.ui.MainView
import id.neotica.neostore.admin.ui.feature.auth.AuthView
import id.neotica.neostore.admin.ui.feature.clipboard.ClipboardView
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardCopiedIndex
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardItems
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardPageDownCount
import id.neotica.neostore.admin.ui.feature.clipboard.copyToClipboard
import id.neotica.neostore.admin.ui.feature.detailapp.DetailAppView
import id.neotica.neostore.admin.ui.navigation.Screen
import id.neotica.neostore.admin.ui.navigation.toScreen
import org.koin.compose.koinInject
import java.awt.KeyEventDispatcher
import java.awt.KeyboardFocusManager
import java.awt.Robot
import java.awt.event.KeyEvent
import kotlin.concurrent.thread

@Composable
fun App(tokenStorage: TokenStorage = koinInject()) {
    val startScreen: Screen = if (tokenStorage.getToken() != null) Screen.Feed else Screen.Auth
    val backStack = remember { NavBackStack<Screen>(startScreen) }

    val tabTargets = mapOf(
        KeyEvent.VK_1 to Screen.Upload,
        KeyEvent.VK_2 to Screen.Feed,
        KeyEvent.VK_3 to Screen.Categories,
        KeyEvent.VK_4 to Screen.Analytics,
        KeyEvent.VK_5 to Screen.Info,
    )

    DisposableEffect(Unit) {
        val dispatcher = KeyEventDispatcher { event ->
            if (event.id == KeyEvent.KEY_PRESSED) {
                when {
                    event.keyCode in tabTargets
                        && (event.isMetaDown || event.isControlDown)
                        -> {
                        backStack.clear()
                        backStack.add(tabTargets[event.keyCode]!!)
                        true
                    }

                    event.keyCode == KeyEvent.VK_0
                        && (event.isMetaDown || event.isControlDown)
                        -> {
                        if (backStack.lastOrNull() is Screen.Clipboard) {
                            backStack.removeLastOrNull()
                        } else {
                            backStack.add(Screen.Clipboard)
                        }
                        true
                    }

                    event.keyCode == KeyEvent.VK_ESCAPE
                        -> {
                        when (backStack.lastOrNull()) {
                            is Screen.Clipboard, is Screen.Detail -> {
                                backStack.removeLastOrNull()
                                true
                            }
                            else -> false
                        }
                    }

                    event.keyCode in KeyEvent.VK_1..KeyEvent.VK_9
                        && backStack.lastOrNull() is Screen.Clipboard
                        && !event.isMetaDown && !event.isControlDown
                        -> {
                        val index = event.keyCode - KeyEvent.VK_1
                        if (index < clipboardItems.size) {
                            copyToClipboard(clipboardItems[index])
                            clipboardCopiedIndex.value = index
                        }
                        true
                    }

                    event.keyCode == KeyEvent.VK_D
                        && backStack.lastOrNull() is Screen.Clipboard
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