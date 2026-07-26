package id.neotica.neostore.admin.ui.feature.collections

import id.neotica.neostore.admin.domain.model.collection.response.AppCollection
import id.neotica.neostore.admin.domain.model.collection.response.CollectionOrganizerItem
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse

data class CollectionsUiState(
    val isLoading: Boolean = false,
    val statusMessage: String = "",
    val collections: List<AppCollection> = emptyList(),
    val selectedCollection: AppCollection? = null,
    val editTitle: String = "",
    val editDescription: String = "",
    val createTitle: String = "",
    val createDescription: String = "",
    val createSlug: String = "",
    val addPackageName: String = "",
    val addSortOrder: String = "0",
    val viewFeed: List<AppFeedItemResponse> = emptyList(),
    val viewPage: Int = 1,
    val viewTotal: Int = 0,
    val viewPages: Int = 0,
    val showDeleteConfirm: Boolean = false,
    val viewingOrganizer: Boolean = false,
    val organizerItems: List<CollectionOrganizerItem> = emptyList(),
    val addOrganizerSlug: String = "",
    val addOrganizerSortOrder: String = "0",
)