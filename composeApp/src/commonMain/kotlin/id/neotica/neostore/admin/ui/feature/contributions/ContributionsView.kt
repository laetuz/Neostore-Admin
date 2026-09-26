package id.neotica.neostore.admin.ui.feature.contributions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.contributor.ContributionStatus
import id.neotica.neostore.admin.domain.model.contributor.ContributorRole
import id.neotica.neostore.admin.domain.model.contributor.PendingContribution
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.DarkPrimaryTransparent40
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import id.neotica.neostore.admin.ui.components.TransparentText40
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ContributionsView(
    role: ContributorRole,
    myUserId: String,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ContributionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(role, myUserId) {
        viewModel.setMyUserId(myUserId, onlyMine = role == ContributorRole.CONTRIBUTOR)
        viewModel.load()
    }

    ContributionsViewContent(
        uiState = uiState,
        onFilterChange = viewModel::setFilter,
        onNavigateToDetail = onNavigateToDetail,
    )
}

@Composable
private fun ContributionsViewContent(
    uiState: ContributionsUiState,
    onFilterChange: (ContributionStatus?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    val visible = if (uiState.onlyMine) {
        uiState.contributions.filter { it.submittedBy == uiState.myUserId }
    } else {
        uiState.contributions
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (uiState.onlyMine) "My Submissions" else "Contribution Queue",
                style = MaterialTheme.typography.headlineSmall,
                color = DarkPrimary,
            )

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip("All", uiState.statusFilter == null) { onFilterChange(null) }
                FilterChip("Pending", uiState.statusFilter == ContributionStatus.PENDING) {
                    onFilterChange(ContributionStatus.PENDING)
                }
                FilterChip("Approved", uiState.statusFilter == ContributionStatus.APPROVED) {
                    onFilterChange(ContributionStatus.APPROVED)
                }
                FilterChip("Declined", uiState.statusFilter == ContributionStatus.DECLINED) {
                    onFilterChange(ContributionStatus.DECLINED)
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            if (uiState.statusMessage.isNotEmpty()) {
                Text(
                    text = uiState.statusMessage,
                    color = if (uiState.statusMessage.contains("Failed", true)) MaterialTheme.colorScheme.error
                    else Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (!uiState.isLoading && visible.isEmpty()) {
                Text("No contributions.", color = TransparentText40, style = MaterialTheme.typography.bodyMedium)
            }

            visible.forEach { item ->
                ContributionCard(item = item, onClick = { onNavigateToDetail(item.id) })
            }
        }
    }
}

@Composable
private fun ContributionCard(item: PendingContribution, onClick: () -> Unit) {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.title.ifBlank { item.packageName },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
                StatusBadge(item.status)
            }
            Text(item.packageName, color = TransparentText40, style = MaterialTheme.typography.bodySmall)
            Text(
                text = "v${item.versionName} (code ${item.versionCode})",
                color = PurpleGrey40,
                style = MaterialTheme.typography.bodySmall,
            )
            if (item.submittedBy.isNotBlank()) {
                Text("By ${item.submittedBy}", color = TransparentText40, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun StatusBadge(status: ContributionStatus) {
    val (label, color) = when (status) {
        ContributionStatus.PENDING -> "PENDING" to Color(0xFFFFB300)
        ContributionStatus.APPROVED -> "APPROVED" to Color(0xFF4CAF50)
        ContributionStatus.DECLINED -> "DECLINED" to Color(0xFFE53935)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) DarkPrimaryTransparent40 else DarkPrimaryCard)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (selected) DarkPrimary else TransparentText40,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}