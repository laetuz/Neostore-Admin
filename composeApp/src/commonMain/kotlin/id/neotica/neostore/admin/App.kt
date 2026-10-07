package id.neotica.neostore.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import id.neotica.neostore.admin.domain.local.TokenStorage
import id.neotica.neostore.admin.domain.model.contributor.ContributorRole
import id.neotica.neostore.admin.platform.copyToClipboard
import id.neotica.neostore.admin.platform.installPlatformKeyDispatcher
import id.neotica.neostore.admin.platform.performPlatformPageDownScroll
import id.neotica.neostore.admin.ui.MainView
import id.neotica.neostore.admin.ui.feature.auth.AuthView
import id.neotica.neostore.admin.ui.feature.clipboard.ClipboardView
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardCopiedIndex
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardItems
import id.neotica.neostore.admin.ui.feature.clipboard.clipboardPageDownCount
import id.neotica.neostore.admin.ui.feature.contributions.ContributionDetailView
import id.neotica.neostore.admin.ui.feature.detailapp.DetailAppView
import id.neotica.neostore.admin.ui.feature.enrich.EnrichDetailView
import id.neotica.neostore.admin.ui.feature.session.NoAccessView
import id.neotica.neostore.admin.ui.feature.session.SessionLoadingView
import id.neotica.neostore.admin.ui.feature.session.SessionStore
import id.neotica.neostore.admin.ui.navigation.Screen
import id.neotica.neostore.admin.ui.navigation.flattenScreens
import id.neotica.neostore.admin.ui.navigation.navItemsFor
import id.neotica.neostore.admin.ui.navigation.toScreen
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import org.koin.compose.koinInject

@Composable
fun App(
    tokenStorage: TokenStorage = koinInject(),
    session: SessionStore = koinInject(),
    httpClient: HttpClient = koinInject(),
) {
    var hasToken by remember { mutableStateOf(tokenStorage.getToken() != null) }
    val me by session.me.collectAsState()
    val isResolving by session.isResolving.collectAsState()

    val logout: () -> Unit = {
        tokenStorage.clearToken()
        session.clear()
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        hasToken = false
    }

    LaunchedEffect(hasToken) {
        if (hasToken && me == null) session.refresh()
    }

    if (!hasToken) {
        AuthView(
            onLoginSuccess = {
                hasToken = true
                session.clear()
                session.refresh()
            }
        )
        return
    }

    val currentMe = me
    if (currentMe == null || isResolving) {
        SessionLoadingView()
        return
    }

    if (currentMe.role == ContributorRole.NONE) {
        NoAccessView(onLogout = logout)
        return
    }

    val role = currentMe.role
    val myUserId = currentMe.userId
    val tabScreens = remember(role) { navItemsFor(role).flattenScreens().map { it.toScreen() } }
    val startScreen = remember(role) {
        if (role == ContributorRole.OWNER) Screen.Feed else Screen.Upload
    }
    val backStack = remember(role) { NavBackStack<Screen>(startScreen) }

    DisposableEffect(role) {
        val handle = installPlatformKeyDispatcher { event ->
            val digit = event.char?.digitToIntOrNull() ?: -1
            when {
                digit in 1..tabScreens.size && (event.isMetaDown || event.isCtrlDown) -> {
                    backStack.clear()
                    backStack.add(tabScreens[digit - 1])
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
                        is Screen.Clipboard, is Screen.Detail, is Screen.ContributionDetail, is Screen.EnrichDetail -> {
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
                            session.clear()
                            session.refresh()
                        }
                    )
                    Screen.Clipboard -> ClipboardView(
                        onBack = { backStack.removeLastOrNull() }
                    )
                    is Screen.Detail -> DetailAppView(
                        packageName = key.packageName,
                        onClick = { backStack.removeLastOrNull() },
                        onEnrich = { backStack.add(Screen.EnrichDetail(key.packageName)) },
                    )
                    is Screen.ContributionDetail -> ContributionDetailView(
                        id = key.id,
                        role = role,
                        onBack = { backStack.removeLastOrNull() },
                    )
                    is Screen.EnrichDetail -> EnrichDetailView(
                        packageName = key.packageName,
                        onBack = { backStack.removeLastOrNull() },
                    )
                    else -> MainView(
                        screen = key,
                        role = role,
                        myUserId = myUserId,
                        onNavigateTab = { type ->
                            backStack.clear()
                            backStack.add(type.toScreen())
                        },
                        onNavigateToDetail = { app ->
                            backStack.add(Screen.Detail(app.packageName))
                        },
                        onNavigateToContribution = { id ->
                            backStack.add(Screen.ContributionDetail(id))
                        },
                        onNavigateToEnrich = { pkg ->
                            backStack.add(Screen.EnrichDetail(pkg))
                        },
                        onLogout = logout,
                    )
                }
            }
        }
    )
}
