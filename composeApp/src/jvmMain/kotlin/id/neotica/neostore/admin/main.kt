package id.neotica.neostore.admin

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
        App()
    }
}