package id.neotica.neostore.admin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import id.neotica.neostore.admin.di.initializeKoin
import id.neotica.toast.setComposeWindowProvider

fun main() = application {
    initializeKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "NeostoreAdmin",
    ) {
        setComposeWindowProvider {
            window
        }
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    add(KtorNetworkFetcherFactory())
                }
                .crossfade(true)
                .build()
        }
        App()
    }
}