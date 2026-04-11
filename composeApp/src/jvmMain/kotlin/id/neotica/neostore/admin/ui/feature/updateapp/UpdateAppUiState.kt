package id.neotica.neostore.admin.ui.feature.updateapp

import kotlinx.serialization.SerialName

data class UpdateAppUiState(
    val isLoading: Boolean = false,
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    @SerialName("icon_url")
    val iconUrl: String = "",
    val statusMessage: String = ""
)
