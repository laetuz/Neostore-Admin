package id.neotica.neostore.admin

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.*
import id.neotica.neostore.admin.domain.local.TokenStorage
import id.neotica.neostore.admin.ui.MainView
import id.neotica.neostore.admin.ui.feature.auth.AuthView
import id.neotica.neostore.admin.ui.navigation.Screen
import org.koin.compose.koinInject

@Composable
fun App(tokenStorage: TokenStorage = koinInject()) {
    val startScreen = if (tokenStorage.getToken() != null) Screen.Main else Screen.Auth
    var currentScreen by remember { mutableStateOf(startScreen) }

    Crossfade(currentScreen) {
        when (it) {
            Screen.Main -> MainView {
                tokenStorage.clearToken()
                currentScreen = Screen.Auth
            }
            Screen.RegisterApp -> {}
            else -> AuthView(
                { currentScreen = Screen.Main }
            )
        }
    }
}