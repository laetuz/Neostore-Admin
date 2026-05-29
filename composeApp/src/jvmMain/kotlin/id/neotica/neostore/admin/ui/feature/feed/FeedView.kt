package id.neotica.neostore.admin.ui.feature.feed

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import id.neotica.neostore.admin.domain.model.response.AppFeedItemResponse
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.NeoCard
import id.neotica.neostore.admin.utils.Constants.BASE_URL_BUCKET_PUBLIC
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedView(
    viewModel: FeedViewModel = koinViewModel(),
    onNavigateToUpdater: (AppFeedItemResponse) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val categories = listOf("ALL", "APPLICATION", "GAME", "UTILITIES")
    var dropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- HEADER & CONTROLS ---
        Text(
            text = "NeoStore Ecosystem Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            color = DarkPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search Bar
            TextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search apps...") },
                singleLine = true,
                trailingIcon = {
                    Text(
                        text = "🔎",
                        modifier = Modifier.clickable { viewModel.fetchFeeds(page = 1) }
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // Category Dropdown
            Box (
                modifier = Modifier
                    .clickable { dropdownExpanded = true }
                    .border(1.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                    .padding(16.dp)
            ) {
                Text(text = "Category: ${uiState.category}", color = DarkPrimary)
                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                dropdownExpanded = false
                                viewModel.setCategory(category)
                            }
                        )
                    }
                }
            }

            ButtonBasic("Search") { viewModel.fetchFeeds(page = 1) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- STATUS & ERRORS ---
        if (uiState.errorMessage.isNotEmpty()) {
            Text(text = "Error: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Box(modifier = Modifier.weight(1f).align(Alignment.CenterHorizontally).fillMaxWidth()) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.apps) { app ->
                    AppCard(app) {
                        onNavigateToUpdater(app)
                    }
                }
            }
        }

        // --- NUMBERED PAGINATION ROW ---
        // --- SMART PAGINATION ROW ---
        if (uiState.totalPages > 1) {
            // Determine visible page window (e.g., max 5 pages visible at once)
            val maxVisiblePages = 5
            var startPage = maxOf(1, uiState.currentPage - maxVisiblePages / 2)
            var endPage = minOf(uiState.totalPages, startPage + maxVisiblePages - 1)

            // Adjust start if we are near the end
            if (endPage - startPage + 1 < maxVisiblePages) {
                startPage = maxOf(1, endPage - maxVisiblePages + 1)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                if (uiState.currentPage > 1) {
                    PageButton("<") { viewModel.fetchFeeds(uiState.currentPage - 1) }
                }

                // First Page + Ellipsis
                if (startPage > 1) {
                    PageButton("1") { viewModel.fetchFeeds(1) }
                    if (startPage > 2) {
                        Text("...", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray)
                    }
                }

                // Page Numbers
                for (pageIndex in startPage..endPage) {
                    PageButton(
                        text = pageIndex.toString(),
                        isSelected = pageIndex == uiState.currentPage
                    ) {
                        viewModel.fetchFeeds(pageIndex)
                    }
                }

                // Last Page + Ellipsis
                if (endPage < uiState.totalPages) {
                    if (endPage < uiState.totalPages - 1) {
                        Text("...", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray)
                    }
                    PageButton(uiState.totalPages.toString()) { viewModel.fetchFeeds(uiState.totalPages) }
                }

                // Next Button
                if (uiState.currentPage < uiState.totalPages) {
                    PageButton(">") { viewModel.fetchFeeds(uiState.currentPage + 1) }
                }
            }
        }
    }
}

@Composable
fun PageButton(text: String, isSelected: Boolean = false, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(enabled = !isSelected) { onClick() }
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else DarkPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun AppCard(app: AppFeedItemResponse, onClick: () -> Unit) {
    NeoCard(isDragging = false, dropTarget = null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(1.dp, Color.LightGray, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                if (!app.iconUrl.isNullOrEmpty()) {
                    val imageUrl = "$BASE_URL_BUCKET_PUBLIC${app.iconUrl}"
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .border(1.dp, Color.LightGray, MaterialTheme.shapes.small),
                        contentAlignment = Alignment.Center
                    ) {
                        if (app.iconUrl.isNotEmpty()) {
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
                                }
                            )
                        } else {
                            Text("No Icon", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    println(imageUrl)
//                    Text("IMG", color = Color.Gray)
                } else {
                    Text("No Icon", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                }
            }

            Column {
                Text(text = app.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(text = app.packageName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = app.description.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(app.category, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}