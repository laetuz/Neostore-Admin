package id.neotica.neostore.admin.ui.feature.contributions

import id.neotica.neostore.admin.domain.model.contributor.PendingContribution

data class ContributionDetailUiState(
    val isLoading: Boolean = false,
    val contribution: PendingContribution? = null,
    val isUploadingScreenshots: Boolean = false,
    val showDeclineDialog: Boolean = false,
    val declineReason: String = "",
    val statusMessage: String = ""
)