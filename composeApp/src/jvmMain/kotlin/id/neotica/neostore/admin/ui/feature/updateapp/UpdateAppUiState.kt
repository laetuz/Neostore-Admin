package id.neotica.neostore.admin.ui.feature.updateapp

data class UpdateAppUiState(
    val isLoading: Boolean = false,
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val iconUrl: String = "",
    val githubRepo: String = "",
    val statusMessage: String = ""
)
