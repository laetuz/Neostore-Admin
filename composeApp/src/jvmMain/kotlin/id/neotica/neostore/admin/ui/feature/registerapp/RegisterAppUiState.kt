package id.neotica.neostore.admin.ui.feature.registerapp

data class RegisterAppUiState(
    val isLoading: Boolean = false,
    val filePath: String = "",
    val packageName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val iconUrl: String = "",
    val statusMessage: String = "",
    val iconByteArray: ByteArray? = null,
)
