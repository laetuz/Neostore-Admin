package id.neotica.neostore.admin.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {
    @Serializable data object Auth : Screen
    @Serializable data object Upload : Screen
    @Serializable data object Feed : Screen
    @Serializable data class Detail(val packageName: String) : Screen
    @Serializable data object Categories : Screen
    @Serializable data object Analytics : Screen
    @Serializable data object Contributions : Screen
    @Serializable data class ContributionDetail(val id: String) : Screen
    @Serializable data object Contributors : Screen
    @Serializable data object Info : Screen
    @Serializable data object Clipboard : Screen
}

fun Screen.toMainScreenType(): MainScreenType? = when (this) {
    is Screen.Upload -> MainScreenType.UPLOADER
    is Screen.Feed -> MainScreenType.FEEDS
    is Screen.Categories -> MainScreenType.CATEGORIES
    is Screen.Analytics -> MainScreenType.ANALYTICS
    is Screen.Contributions -> MainScreenType.CONTRIBUTIONS
    is Screen.ContributionDetail -> MainScreenType.CONTRIBUTIONS
    is Screen.Contributors -> MainScreenType.CONTRIBUTORS
    is Screen.Info -> MainScreenType.INFO
    is Screen.Detail -> MainScreenType.DETAIL
    is Screen.Auth, is Screen.Clipboard -> null
}

fun MainScreenType.toScreen(): Screen = when (this) {
    MainScreenType.UPLOADER -> Screen.Upload
    MainScreenType.FEEDS -> Screen.Feed
    MainScreenType.DETAIL -> Screen.Feed
    MainScreenType.CATEGORIES -> Screen.Categories
    MainScreenType.ANALYTICS -> Screen.Analytics
    MainScreenType.CONTRIBUTIONS -> Screen.Contributions
    MainScreenType.CONTRIBUTORS -> Screen.Contributors
    MainScreenType.INFO -> Screen.Info
}