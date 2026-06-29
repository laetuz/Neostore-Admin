package id.neotica.neostore.admin.ui.feature.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.analytics.CountsResponse
import id.neotica.neostore.admin.domain.model.analytics.DailyCount
import id.neotica.neostore.admin.domain.model.analytics.TrendingItem
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.DarkPrimaryTransparent40
import id.neotica.neostore.admin.ui.components.NegativePrimary
import id.neotica.neostore.admin.ui.components.TransparentText40
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalyticsView(
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    AnalyticsViewContent(
        state = uiState,
        onRefresh = viewModel::refresh
    )
}

@Composable
private fun AnalyticsViewContent(
    state: AnalyticsUiState,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        if (state.accessGranted == false) {
            AccessDeniedView(message = state.errorMessage ?: "Access denied.")
            return@Column
        }

        if (state.isLoading && state.counts == null) {
            LoadingView()
            return@Column
        }

        if (state.errorMessage != null && state.counts == null) {
            ErrorView(message = state.errorMessage)
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Analytics Dashboard",
                    color = DarkPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                TotalEventsCard(total = state.counts?.total ?: 0)
            }

            item {
                Text(
                    text = "Daily Breakdown",
                    color = TransparentText40,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            val daily = state.counts?.daily ?: emptyList()
            items(daily, key = { it.date }) { day ->
                DailyCountCard(day = day, maxCount = daily.maxOfOrNull { it.count } ?: 1)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Trending Events",
                    color = TransparentText40,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(state.trending, key = { it.event_name }) { item ->
                TrendingCard(item = item)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AccessDeniedView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = NegativePrimary)
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Loading analytics...", color = TransparentText40)
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = NegativePrimary)
    }
}

@Composable
private fun TotalEventsCard(total: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkPrimaryTransparent40)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Events",
            color = TransparentText40,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = total.toString(),
            color = DarkPrimary,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DailyCountCard(day: DailyCount, maxCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkPrimaryCard)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.date,
                color = androidx.compose.ui.graphics.Color.White,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${day.count} events",
                color = DarkPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkBackground)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (day.count.toFloat() / maxCount).coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(DarkPrimary)
            )
        }
    }
}

@Composable
private fun TrendingCard(item: TrendingItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkPrimaryCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item.event_name,
            color = androidx.compose.ui.graphics.Color.White,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${item.count}",
            color = DarkPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview
@Composable
private fun AnalyticsViewSuccessPreview() {
    MaterialTheme {
        AnalyticsViewContent(
            state = AnalyticsUiState(
                isLoading = false,
                accessGranted = true,
                counts = CountsResponse(
                    total = 142,
                    daily = listOf(
                        DailyCount("2026-06-25", 40),
                        DailyCount("2026-06-26", 55),
                        DailyCount("2026-06-27", 47)
                    )
                ),
                trending = listOf(
                    TrendingItem("app_downloaded", 89),
                    TrendingItem("product_purchased", 42),
                    TrendingItem("playlist_created", 31)
                )
            ),
            onRefresh = {}
        )
    }
}

@Preview
@Composable
private fun AnalyticsViewLoadingPreview() {
    MaterialTheme {
        AnalyticsViewContent(
            state = AnalyticsUiState(isLoading = true, accessGranted = true),
            onRefresh = {}
        )
    }
}

@Preview
@Composable
private fun AnalyticsViewAccessDeniedPreview() {
    MaterialTheme {
        AnalyticsViewContent(
            state = AnalyticsUiState(
                isLoading = false,
                accessGranted = false,
                errorMessage = "Access denied. User 'somebody' is not authorized."
            ),
            onRefresh = {}
        )
    }
}
