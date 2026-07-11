package id.neotica.neostore.admin.ui.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.category.response.Category
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.CategorySelect
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.DarkPrimaryTransparent40
import id.neotica.neostore.admin.ui.components.NegativePrimary
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import id.neotica.neostore.admin.ui.components.TransparentText40
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CategoriesView(
    viewModel: CategoriesViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    CategoriesViewContent(
        state = uiState,
        onNameChange = viewModel::setName,
        onSlugChange = viewModel::setSlug,
        onParentSlugChange = viewModel::setParentSlug,
        onCreate = viewModel::createCategory,
        onStartEdit = viewModel::startEdit,
        onCancelEdit = viewModel::cancelEdit,
        onEditingNameChange = viewModel::setEditingName,
        onEditingParentSlugChange = viewModel::setEditingParentSlug,
        onSaveEdit = viewModel::saveEdit,
        onToggleExpand = viewModel::toggleExpand,
        onDelete = viewModel::deleteCategory,
    )
}

@Composable
private fun CategoriesViewContent(
    state: CategoriesUiState,
    onNameChange: (String) -> Unit,
    onSlugChange: (String) -> Unit,
    onParentSlugChange: (String?) -> Unit,
    onCreate: () -> Unit,
    onStartEdit: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onEditingNameChange: (String) -> Unit,
    onEditingParentSlugChange: (String?) -> Unit,
    onSaveEdit: () -> Unit,
    onToggleExpand: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.headlineSmall,
            color = DarkPrimary,
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Loading...", color = TransparentText40)
            }
            return@Column
        }

        if (state.statusMessage.isNotEmpty()) {
            val isError = state.statusMessage.contains("Failed", ignoreCase = true)
            Text(
                text = state.statusMessage,
                color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Create Category", color = Color.White, style = MaterialTheme.typography.titleSmall)

                TextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Name") },
                    placeholder = { Text("Action", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                TextField(
                    value = state.slug,
                    onValueChange = onSlugChange,
                    label = { Text("Slug") },
                    placeholder = { Text("action", color = PurpleGrey40) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                CategorySelect(
                    categories = state.categories,
                    selectedSlug = state.parentSlug,
                    onSelect = onParentSlugChange,
                    modifier = Modifier.fillMaxWidth(),
                )

                ButtonBasic("Create", onCreate)
            }
        }

        if (state.categories.isEmpty()) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No categories yet.", color = TransparentText40)
            }
            return@Column
        }

        state.categories.forEach { root ->
            CategoryCard(
                category = root,
                children = root.children,
                isExpanded = state.expandedSlug == root.slug,
                isEditing = state.editingSlug == root.slug,
                editingName = if (state.editingSlug == root.slug) state.editingName else "",
                editingParentSlug = if (state.editingSlug == root.slug) state.editingParentSlug else root.parentSlug,
                roots = state.categories,
                onToggleExpand = onToggleExpand,
                onStartEditSlug = onStartEdit,
                onCancelEdit = onCancelEdit,
                onEditingNameChange = onEditingNameChange,
                onEditingParentSlugChange = onEditingParentSlugChange,
                onSaveEdit = onSaveEdit,
                onDeleteSlug = onDelete,
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    children: List<Category>,
    isExpanded: Boolean,
    isEditing: Boolean,
    editingName: String,
    editingParentSlug: String?,
    roots: List<Category>,
    onToggleExpand: (String) -> Unit,
    onStartEditSlug: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onEditingNameChange: (String) -> Unit,
    onEditingParentSlugChange: (String?) -> Unit,
    onSaveEdit: () -> Unit,
    onDeleteSlug: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkPrimaryCard)
            .padding(12.dp)
    ) {
        if (isEditing) {
            TextField(
                value = editingName,
                onValueChange = onEditingNameChange,
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategorySelect(
                categories = roots,
                selectedSlug = editingParentSlug,
                onSelect = onEditingParentSlugChange,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ButtonBasic("Save", onSaveEdit)
                ButtonBasic("Cancel", onCancelEdit)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand(category.slug) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${if (category.parentSlug != null) "  └ " else ""}${category.name}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = category.slug + if (children.isNotEmpty()) " (${children.size})" else "",
                        color = TransparentText40,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkPrimaryTransparent40)
                            .clickable { onStartEditSlug(category.slug) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Edit", color = DarkPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NegativePrimary.copy(alpha = 0.2f))
                            .clickable { onDeleteSlug(category.slug) }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Delete", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            if (isExpanded && children.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(thickness = 1.dp, color = DarkPrimaryTransparent40)
                Spacer(modifier = Modifier.height(8.dp))
                children.forEach { child ->
                    CategoryCard(
                        category = child,
                        children = emptyList(),
                        isExpanded = false,
                        isEditing = false,
                        editingName = "",
                        editingParentSlug = null,
                        roots = roots,
                        onToggleExpand = onToggleExpand,
                        onStartEditSlug = onStartEditSlug,
                        onCancelEdit = onCancelEdit,
                        onEditingNameChange = onEditingNameChange,
                        onEditingParentSlugChange = onEditingParentSlugChange,
                        onSaveEdit = onSaveEdit,
                        onDeleteSlug = onDeleteSlug,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun CategoriesViewPreview() {
    MaterialTheme {
    CategoriesViewContent(
        state = CategoriesUiState(
            isLoading = false,
            categories = listOf(
                Category("application", "Application"),
                Category("game", "Game"),
                Category("action", "Action", "game"),
                Category("racing", "Racing", "game"),
                Category("utilities", "Utilities"),
            ),
            statusMessage = ""
        ),
        onNameChange = {},
        onSlugChange = {},
        onParentSlugChange = {},
        onCreate = {},
        onStartEdit = {},
        onCancelEdit = {},
        onEditingNameChange = {},
        onEditingParentSlugChange = {},
        onSaveEdit = {},
        onToggleExpand = {},
        onDelete = {},
    )
    }
}
