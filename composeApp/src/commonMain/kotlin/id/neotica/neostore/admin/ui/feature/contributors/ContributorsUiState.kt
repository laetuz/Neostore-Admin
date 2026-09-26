package id.neotica.neostore.admin.ui.feature.contributors

import id.neotica.neostore.admin.domain.model.contributor.AdminContributor
import id.neotica.neostore.admin.domain.model.contributor.UserSummary

data class ContributorsUiState(
    val isLoading: Boolean = false,
    val contributors: List<AdminContributor> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<UserSummary> = emptyList(),
    val isSearching: Boolean = false,
    val selectedUserId: String = "",
    val selectedUsername: String = "",
    val statusMessage: String = ""
)