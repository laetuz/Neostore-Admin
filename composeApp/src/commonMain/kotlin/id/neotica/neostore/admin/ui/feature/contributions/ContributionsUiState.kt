package id.neotica.neostore.admin.ui.feature.contributions

import id.neotica.neostore.admin.domain.model.contributor.ContributionStatus
import id.neotica.neostore.admin.domain.model.contributor.PendingContribution

data class ContributionsUiState(
    val isLoading: Boolean = false,
    val contributions: List<PendingContribution> = emptyList(),
    val statusFilter: ContributionStatus? = null,
    val myUserId: String = "",
    val onlyMine: Boolean = false,
    val statusMessage: String = ""
)