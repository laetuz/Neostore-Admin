package id.neotica.neostore.admin.ui.feature.collections

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.collection.response.AppCollection
import id.neotica.neostore.admin.domain.model.collection.response.CollectionOrganizerItem
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.DarkPrimaryTransparent40
import id.neotica.neostore.admin.ui.components.NegativePrimary
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import id.neotica.neostore.admin.ui.components.TransparentText40
import id.neotica.neostore.admin.platform.installPlatformKeyDispatcher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CollectionsView(
    viewModel: CollectionsViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        val handle = installPlatformKeyDispatcher { event ->
            if (event.isEscape) {
                if (uiState.selectedCollection != null) {
                    viewModel.deselectCollection()
                } else if (uiState.viewingOrganizer) {
                    viewModel.toggleOrganizerMode()
                } else {
                    onBack()
                }
                true
            } else false
        }
        onDispose { handle() }
    }

    if (uiState.selectedCollection != null) {
        CollectionDetailView(
            state = uiState,
            onEditTitleChange = viewModel::setEditTitle,
            onEditDescriptionChange = viewModel::setEditDescription,
            onAddPackageNameChange = viewModel::setAddPackageName,
            onAddSortOrderChange = viewModel::setAddSortOrder,
            onUpdate = viewModel::updateCollection,
            onAddApp = viewModel::addAppToCollection,
            onRemoveApp = viewModel::removeAppFromCollection,
            onRequestDelete = viewModel::requestDelete,
            onCancelDelete = viewModel::cancelDelete,
            onDelete = viewModel::deleteCollection,
            onBack = { viewModel.deselectCollection() },
        )
    } else if (uiState.viewingOrganizer) {
        OrganizerListView(
            state = uiState,
            onAddSlugChange = viewModel::setAddOrganizerSlug,
            onAddSortOrderChange = viewModel::setAddOrganizerSortOrder,
            onAdd = viewModel::addToOrganizer,
            onRemove = viewModel::removeFromOrganizer,
            onBack = { viewModel.toggleOrganizerMode() },
        )
    } else {
        CollectionListView(
            state = uiState,
            onCreateTitleChange = viewModel::setCreateTitle,
            onCreateDescriptionChange = viewModel::setCreateDescription,
            onCreateSlugChange = viewModel::setCreateSlug,
            onCreate = viewModel::createCollection,
            onSelect = viewModel::selectCollection,
            onToggleOrganizer = viewModel::toggleOrganizerMode,
            onBack = onBack,
        )
    }
}

@Composable
private fun CollectionListView(
    state: CollectionsUiState,
    onCreateTitleChange: (String) -> Unit,
    onCreateDescriptionChange: (String) -> Unit,
    onCreateSlugChange: (String) -> Unit,
    onCreate: () -> Unit,
    onSelect: (String) -> Unit,
    onToggleOrganizer: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkPrimary)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Back", color = DarkBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            Text("Collections", style = MaterialTheme.typography.headlineSmall, color = DarkPrimary)
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkPrimaryTransparent40)
                    .clickable(onClick = onToggleOrganizer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Organizer", color = DarkPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }

        if (state.isLoading && state.collections.isEmpty()) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (state.statusMessage.isNotEmpty()) {
            Text(
                text = state.statusMessage,
                color = if (state.statusMessage.contains("Failed", ignoreCase = true))
                    MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        state.collections.forEach { col ->
            CollectionCard(collection = col, onClick = { onSelect(col.slug) })
        }

        NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Create Collection", color = Color.White, style = MaterialTheme.typography.titleSmall)
                TextField(
                    value = state.createTitle,
                    onValueChange = onCreateTitleChange,
                    label = { Text("Title") },
                    placeholder = { Text("Featured Apps", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = state.createDescription,
                    onValueChange = onCreateDescriptionChange,
                    label = { Text("Description") },
                    placeholder = { Text("A curated collection of top apps", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = state.createSlug,
                    onValueChange = onCreateSlugChange,
                    label = { Text("Custom Slug (optional)") },
                    placeholder = { Text("Auto-generated if blank", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ButtonBasic("Create", onCreate)
            }
        }
    }
}

@Composable
private fun CollectionCard(collection: AppCollection, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkPrimaryCard)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(collection.title, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
        if (collection.description.isNotBlank()) {
            Text(collection.description, color = TransparentText40, style = MaterialTheme.typography.bodySmall,
                maxLines = 2, modifier = Modifier.padding(top = 2.dp))
        }
        Text(collection.slug, color = DarkPrimaryTransparent40, style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun CollectionDetailView(
    state: CollectionsUiState,
    onEditTitleChange: (String) -> Unit,
    onEditDescriptionChange: (String) -> Unit,
    onAddPackageNameChange: (String) -> Unit,
    onAddSortOrderChange: (String) -> Unit,
    onUpdate: () -> Unit,
    onAddApp: () -> Unit,
    onRemoveApp: (String) -> Unit,
    onRequestDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    val collection = state.selectedCollection ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkPrimary)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Back", color = DarkBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            Text(collection.title, style = MaterialTheme.typography.headlineSmall, color = DarkPrimary)
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (state.statusMessage.isNotEmpty()) {
            Text(
                text = state.statusMessage,
                color = if (state.statusMessage.contains("Failed", ignoreCase = true))
                    MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit Collection", color = Color.White, style = MaterialTheme.typography.titleSmall)
                TextField(
                    value = state.editTitle,
                    onValueChange = onEditTitleChange,
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = state.editDescription,
                    onValueChange = onEditDescriptionChange,
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = collection.slug,
                    onValueChange = {},
                    label = { Text("Slug") },
                    readOnly = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ButtonBasic("Save", onUpdate)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NegativePrimary.copy(alpha = 0.2f))
                            .clickable(onClick = onRequestDelete)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text("Delete Collection", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (state.showDeleteConfirm) {
                    Text("Are you sure? Apps in this collection must be removed first.", color = Color(0xFFFFCC00),
                        style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkPrimaryTransparent40)
                                .clickable(onClick = onCancelDelete)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("Cancel", color = DarkPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NegativePrimary.copy(alpha = 0.2f))
                                .clickable(onClick = onDelete)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) { Text("Yes, Delete", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }

        NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add App", color = Color.White, style = MaterialTheme.typography.titleSmall)
                TextField(
                    value = state.addPackageName,
                    onValueChange = onAddPackageNameChange,
                    label = { Text("Package Name") },
                    placeholder = { Text("id.neotica.neomart", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = state.addSortOrder,
                    onValueChange = onAddSortOrderChange,
                    label = { Text("Sort Order") },
                    placeholder = { Text("0", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ButtonBasic("Add to Collection", onAddApp)
            }
        }

        if (state.viewFeed.isNotEmpty()) {
            Text(
                text = "Apps (${state.viewTotal})",
                style = MaterialTheme.typography.titleMedium,
                color = DarkPrimary,
                fontWeight = FontWeight.Bold,
            )

            state.viewFeed.forEachIndexed { index, item ->
                CollectionAppCard(index = index, item = item, onRemove = { onRemoveApp(item.packageName) })
            }
        } else if (!state.isLoading) {
            Box(Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                Text("No apps in this collection.", color = TransparentText40)
            }
        }
    }
}

@Composable
private fun OrganizerListView(
    state: CollectionsUiState,
    onAddSlugChange: (String) -> Unit,
    onAddSortOrderChange: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkPrimary)
                    .clickable(onClick = onBack)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Back", color = DarkBackground, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
            Text("Organizer", style = MaterialTheme.typography.headlineSmall, color = DarkPrimary)
        }

        if (state.isLoading && state.organizerItems.isEmpty()) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (state.statusMessage.isNotEmpty()) {
            Text(
                text = state.statusMessage,
                color = if (state.statusMessage.contains("Failed", ignoreCase = true))
                    MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        state.organizerItems.forEach { item ->
            OrganizerCard(item = item, onRemove = { onRemove(item.slug) })
        }

        if (state.organizerItems.isEmpty() && !state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No items in organizer.", color = TransparentText40)
            }
        }

        NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Add to Organizer", color = Color.White, style = MaterialTheme.typography.titleSmall)
                TextField(
                    value = state.addOrganizerSlug,
                    onValueChange = onAddSlugChange,
                    label = { Text("Collection Slug") },
                    placeholder = { Text("featured-apps", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                TextField(
                    value = state.addOrganizerSortOrder,
                    onValueChange = onAddSortOrderChange,
                    label = { Text("Sort Order") },
                    placeholder = { Text("0", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ButtonBasic("Add to Organizer", onAdd)
            }
        }
    }
}

@Composable
private fun OrganizerCard(item: CollectionOrganizerItem, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkPrimaryCard)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Text(item.slug, color = TransparentText40, style = MaterialTheme.typography.bodySmall)
                if (item.description.isNotBlank()) {
                    Text(item.description, color = TransparentText40.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Index: ${item.index}", color = DarkPrimary, style = MaterialTheme.typography.labelSmall)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(NegativePrimary.copy(alpha = 0.2f))
                        .clickable(onClick = onRemove)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Remove", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun CollectionAppCard(index: Int, item: AppFeedItemResponse, onRemove: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkPrimaryCard)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("$index", color = DarkPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Column {
                    Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    Text(item.packageName, color = TransparentText40, style = MaterialTheme.typography.bodySmall)
                    if (!item.description.isNullOrBlank()) {
                        Text(item.description, color = TransparentText40.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NegativePrimary.copy(alpha = 0.2f))
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Remove", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}