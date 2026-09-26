package id.neotica.neostore.admin.ui.feature.contributors

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import id.neotica.neostore.admin.domain.model.contributor.AdminContributor
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.NegativePrimary
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.TransparentText40
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ContributorsView(
    viewModel: ContributorsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

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
                text = "Contributors",
                style = MaterialTheme.typography.headlineSmall,
                color = DarkPrimary,
            )

            NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Add a contributor", color = Color.White, style = MaterialTheme.typography.titleSmall)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::setSearchQuery,
                            label = { Text("Search username") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        ButtonBasic("Search", viewModel::search)
                    }

                    if (uiState.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }

                    uiState.searchResults.forEach { user ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkPrimaryCard)
                                .clickable { viewModel.selectUser(user) }
                                .padding(10.dp)
                        ) {
                            Text("${user.username}  (${user.id})", color = Color.White, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (uiState.selectedUserId.isNotBlank()) {
                        Text("Selected: ${uiState.selectedUsername}", color = DarkPrimary, style = MaterialTheme.typography.bodySmall)
                        ButtonBasic("Add", viewModel::addSelected)
                    }
                }
            }

            if (uiState.statusMessage.isNotEmpty()) {
                Text(
                    text = uiState.statusMessage,
                    color = if (uiState.statusMessage.contains("Failed", true)) MaterialTheme.colorScheme.error
                    else Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            uiState.contributors.forEach { contributor ->
                ContributorRow(
                    contributor = contributor,
                    onRemove = { viewModel.remove(contributor.userId) },
                )
            }
        }
    }
}

@Composable
private fun ContributorRow(
    contributor: AdminContributor,
    onRemove: () -> Unit,
) {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contributor.username.ifBlank { contributor.userId },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(contributor.userId, color = TransparentText40, style = MaterialTheme.typography.labelSmall)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NegativePrimary.copy(alpha = 0.2f))
                    .clickable { onRemove() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("Remove", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}