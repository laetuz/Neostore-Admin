package id.neotica.neostore.admin.ui.feature.enrich

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import id.neotica.neostore.admin.domain.model.response.AppDetailResponse
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.DarkPrimaryTransparent40
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.TransparentText40
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EnrichView(
    onNavigateToDetail: (String) -> Unit,
    viewModel: EnrichViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    EnrichViewContent(
        uiState = uiState,
        onToggleFilter = viewModel::toggleOnlyIncomplete,
        onNavigateToDetail = onNavigateToDetail,
    )
}

@Composable
private fun EnrichViewContent(
    uiState: EnrichUiState,
    onToggleFilter: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
) {
    val visible = if (uiState.onlyIncomplete) uiState.apps.filter { it.isIncomplete() } else uiState.apps

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
                text = "Metadata Enrich",
                style = MaterialTheme.typography.headlineSmall,
                color = DarkPrimary,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    label = if (uiState.onlyIncomplete) "Missing fields only" else "All apps",
                    selected = uiState.onlyIncomplete,
                    onClick = onToggleFilter,
                )
                Text("${visible.size} app(s)", color = TransparentText40, style = MaterialTheme.typography.labelMedium)
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
                Text("Nothing to enrich. 🎉", color = TransparentText40, style = MaterialTheme.typography.bodyMedium)
            }

            visible.forEach { app ->
                AppRow(app = app, onClick = { onNavigateToDetail(app.packageName) })
            }
        }
    }
}

@Composable
private fun AppRow(app: AppDetailResponse, onClick: () -> Unit) {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = app.title.ifBlank { app.packageName },
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(app.packageName, color = TransparentText40, style = MaterialTheme.typography.bodySmall)
            val missing = app.missingFields()
            if (missing.isNotEmpty()) {
                Text(
                    text = "Missing: ${missing.joinToString(", ")}",
                    color = Color(0xFFFFB300),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
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
