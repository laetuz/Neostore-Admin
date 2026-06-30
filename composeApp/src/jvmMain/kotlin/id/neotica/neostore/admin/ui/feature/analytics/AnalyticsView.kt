package id.neotica.neostore.admin.ui.feature.analytics

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import id.neotica.neostore.admin.domain.model.analytics.AnalyticsEvent
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
        onRefresh = viewModel::refresh,
        onDateSelected = viewModel::selectDate,
        onBack = viewModel::backToDashboard,
        onLoadNextPage = viewModel::loadNextPage,
        onEventSelected = viewModel::selectEvent,
        onBackFromEvent = viewModel::clearEventDetail
    )
}

@Composable
private fun AnalyticsViewContent(
    state: AnalyticsUiState,
    onRefresh: () -> Unit,
    onDateSelected: (String) -> Unit,
    onBack: () -> Unit,
    onLoadNextPage: () -> Unit,
    onEventSelected: (String) -> Unit,
    onBackFromEvent: () -> Unit
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

        if (state.errorMessage != null && state.counts == null && !state.isShowingDetail) {
            ErrorView(message = state.errorMessage)
            return@Column
        }

        if (state.isShowingEventDetail) {
            EventFullDetailContent(state = state, onBack = onBackFromEvent)
        } else if (state.isShowingDetail) {
            EventDetailContent(
                state = state,
                onBack = onBack,
                onLoadNextPage = onLoadNextPage,
                onEventSelected = onEventSelected
            )
        } else {
            DashboardContent(state = state, onDateSelected = onDateSelected)
        }
    }
}

@Composable
private fun DashboardContent(state: AnalyticsUiState, onDateSelected: (String) -> Unit) {
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
            DailyCountCard(
                day = day,
                maxCount = daily.maxOfOrNull { it.count } ?: 1,
                onClick = { onDateSelected(day.date) }
            )
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

@Composable
private fun EventDetailContent(
    state: AnalyticsUiState,
    onBack: () -> Unit,
    onLoadNextPage: () -> Unit,
    onEventSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkPrimaryCard)
                .clickable { onBack() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\u2190 Back to Dashboard",
                color = DarkPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Events for ${state.selectedDate}",
            color = DarkPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (state.errorEvents != null && state.events.isEmpty()) {
            ErrorView(message = state.errorEvents)
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.events, key = { it.id }) { event ->
                EventCard(event = event, onClick = { onEventSelected(event.id) })
            }

            if (state.isLoadingEvents) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Loading more events...",
                            color = TransparentText40,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            if (state.eventsPage < state.eventsTotalPages && !state.isLoadingEvents) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkPrimaryCard)
                            .clickable { onLoadNextPage() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Load More",
                            color = DarkPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun EventFullDetailContent(state: AnalyticsUiState, onBack: () -> Unit) {
    val event = state.selectedEvent ?: return

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkPrimaryCard)
                .clickable { onBack() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\u2190 Back to Events",
                color = DarkPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Event Detail",
            color = DarkPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (state.isLoadingEventDetail) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading event detail...", color = TransparentText40)
            }
            return@Column
        }

        if (state.eventDetailError != null) {
            ErrorView(message = state.eventDetailError)
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailRow("ID", event.id)
            DetailRow("Event Name", event.eventName)
            DetailRow("Event Type", event.eventType)
            DetailRow("Source Service", event.sourceService)
            DetailRow("User ID", event.userId ?: "-")
            DetailRow("IP Address", event.ipAddress ?: "-")
            DetailRow("User Agent", event.userAgent ?: "-")
            DetailRow("Created At", event.createdAt)

            if (event.properties.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Properties",
                    color = DarkPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                event.properties.forEach { (key, value) ->
                    DetailRow(key, value)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkPrimaryCard)
            .padding(12.dp)
    ) {
        Text(
            text = label,
            color = TransparentText40,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EventCard(event: AnalyticsEvent, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkPrimaryCard)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = event.eventName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = event.eventType,
                color = DarkPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = event.sourceService,
                color = TransparentText40,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = formatTime(event.createdAt),
                color = TransparentText40,
                style = MaterialTheme.typography.bodySmall
            )
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
private fun DailyCountCard(day: DailyCount, maxCount: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkPrimaryCard)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = day.date,
                color = Color.White,
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
            color = Color.White,
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

private fun formatTime(isoTimestamp: String): String {
    return try {
        val instant = java.time.LocalDateTime.parse(
            isoTimestamp.substringBefore("Z").take(19),
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        ).toInstant(java.time.ZoneOffset.UTC)
        java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))
    } catch (_: Exception) {
        val afterT = isoTimestamp.substringAfter("T", "")
        afterT.substringBefore(".").takeIf { it.isNotEmpty() } ?: isoTimestamp
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
            onRefresh = {},
            onDateSelected = {},
            onBack = {},
            onLoadNextPage = {},
            onEventSelected = {},
            onBackFromEvent = {}
        )
    }
}

@Preview
@Composable
private fun AnalyticsViewLoadingPreview() {
    MaterialTheme {
        AnalyticsViewContent(
            state = AnalyticsUiState(isLoading = true, accessGranted = true),
            onRefresh = {},
            onDateSelected = {},
            onBack = {},
            onLoadNextPage = {},
            onEventSelected = {},
            onBackFromEvent = {}
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
            onRefresh = {},
            onDateSelected = {},
            onBack = {},
            onLoadNextPage = {},
            onEventSelected = {},
            onBackFromEvent = {}
        )
    }
}

@Preview
@Composable
private fun AnalyticsViewDetailPreview() {
    MaterialTheme {
        AnalyticsViewContent(
            state = AnalyticsUiState(
                isLoading = false,
                accessGranted = true,
                selectedDate = "2026-06-26",
                events = listOf(
                    AnalyticsEvent("1", eventType = "click", eventName = "download_apk", sourceService = "neostore", createdAt = "2026-06-26T10:00:00Z"),
                    AnalyticsEvent("2", eventType = "view", eventName = "app_detail", sourceService = "neostore", createdAt = "2026-06-26T09:30:00Z")
                ),
                eventsPage = 1,
                eventsTotalPages = 2
            ),
            onRefresh = {},
            onDateSelected = {},
            onBack = {},
            onLoadNextPage = {},
            onEventSelected = {},
            onBackFromEvent = {}
        )
    }
}

@Preview
@Composable
private fun AnalyticsViewEventDetailPreview() {
    MaterialTheme {
        AnalyticsViewContent(
            state = AnalyticsUiState(
                isLoading = false,
                accessGranted = true,
                selectedEvent = AnalyticsEvent(
                    id = "abc-123",
                    eventType = "click",
                    eventName = "download_apk",
                    sourceService = "neostore",
                    userId = "user-uuid",
                    ipAddress = "192.168.1.1",
                    userAgent = "Mozilla/5.0",
                    properties = mapOf("version" to "1.8.0", "platform" to "android"),
                    createdAt = "2026-06-26T10:00:00Z"
                )
            ),
            onRefresh = {},
            onDateSelected = {},
            onBack = {},
            onLoadNextPage = {},
            onEventSelected = {},
            onBackFromEvent = {}
        )
    }
}
