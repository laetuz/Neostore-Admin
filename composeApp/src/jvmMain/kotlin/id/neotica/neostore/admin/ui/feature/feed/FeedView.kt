package id.neotica.neostore.admin.ui.feature.feed

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.ui.components.AppPagination
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.NeoCard
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.utils.Constants.BASE_URL_BUCKET_PUBLIC
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedView(
    viewModel: FeedViewModel = koinViewModel(),
    onNavigateToUpdater: (AppFeedItemResponse) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    FeedViewContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::setSearchQuery,
        onCategoryChange = viewModel::setCategory,
        onSearch = { viewModel.fetchFeeds(page = 1) },
        onPageChange = viewModel::fetchFeeds,
        onNavigateToDetail = onNavigateToUpdater,
    )
}

@Composable
private fun FeedViewContent(
    uiState: FeedUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onPageChange: (Int) -> Unit,
    onNavigateToDetail: (AppFeedItemResponse) -> Unit,
) {
    val categories = listOf("ALL", "APPLICATION", "GAME", "UTILITIES")
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
    ) {
        NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Search & Filter",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search apps...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )

                    Box(
                        modifier = Modifier
                            .clickable { dropdownExpanded = true }
                            .border(1.dp, DarkPrimary, MaterialTheme.shapes.small)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = if (uiState.category.isEmpty()) "Category: ALL" else "Category: ${uiState.category}",
                            color = DarkPrimary,
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        dropdownExpanded = false
                                        onCategoryChange(category)
                                    },
                                )
                            }
                        }
                    }

                    ButtonBasic("Search", onSearch)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return
        }

        if (uiState.errorMessage.isNotEmpty()) {
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        if (uiState.apps.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (uiState.errorMessage.isEmpty()) "No apps found." else "",
                    color = Color.Gray,
                )
            }
            return
        }

        Box(modifier = Modifier.weight(1f)) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                contentPadding = PaddingValues(bottom = 64.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(uiState.apps) { app ->
                    AppCard(app) { onNavigateToDetail(app) }
                }
            }

            if (uiState.totalPages > 1) {
                AppPagination(
                    currentPage = uiState.currentPage,
                    totalPages = uiState.totalPages,
                    onPageChange = onPageChange,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun AppCard(app: AppFeedItemResponse, onClick: () -> Unit) {
    NeoCard(isDragging = false, dropTarget = null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, Color.LightGray, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center,
            ) {
                if (!app.iconUrl.isNullOrEmpty()) {
                    val imageUrl = "$BASE_URL_BUCKET_PUBLIC${app.iconUrl}"
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = "App Icon",
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        },
                        error = {
                            Text("Err", color = Color.Red, style = MaterialTheme.typography.labelSmall)
                        },
                    )
                } else {
                    Text("No Icon", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
            }

            Column {
                Text(text = app.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = app.packageName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = app.description.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(app.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Preview
@Composable
private fun FeedViewPreview() {
    FeedViewContent(
        uiState = FeedUiState(
            apps = listOf(
                AppFeedItemResponse("id.neotica.neomart", "Neomart", "A marketplace for legacy Android devices.", "APPLICATION", null),
                AppFeedItemResponse("com.example.game", "Retro Game", "Classic game for old phones.", "GAME", null),
                AppFeedItemResponse("org.tool.utils", "Utility Pack", "Useful tools for daily tasks.", "UTILITIES", "/buckets/neostore/icon.png"),
            ),
            currentPage = 1,
            totalPages = 3,
        ),
        onSearchQueryChange = {},
        onCategoryChange = {},
        onSearch = {},
        onPageChange = {},
        onNavigateToDetail = {},
    )
}
