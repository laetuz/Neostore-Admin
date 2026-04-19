package id.neotica.neostore.admin.ui.feature.detailapp

import kotlinx.serialization.SerialName

data class DetailAppUiState(
    val isLoading: Boolean = false,
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    @SerialName("icon_url")
    val iconUrl: String = "",
    val statusMessage: String = ""
)
