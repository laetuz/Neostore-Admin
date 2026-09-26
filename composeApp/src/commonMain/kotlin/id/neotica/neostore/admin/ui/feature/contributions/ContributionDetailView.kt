package id.neotica.neostore.admin.ui.feature.contributions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import coil3.compose.SubcomposeAsyncImage
import id.neotica.neostore.admin.domain.model.contributor.ContributionStatus
import id.neotica.neostore.admin.domain.model.contributor.ContributorRole
import id.neotica.neostore.admin.platform.rememberPlatformImagesPicker
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.DarkPrimaryTransparent40
import id.neotica.neostore.admin.ui.components.NegativePrimary
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import id.neotica.neostore.admin.ui.components.TransparentText40
import id.neotica.neostore.admin.utils.Constants.BASE_URL_BUCKET_PUBLIC
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ContributionDetailView(
    id: String,
    role: ContributorRole,
    onBack: () -> Unit,
    viewModel: ContributionDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(id) { viewModel.load(id) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val pickScreenshots = rememberPlatformImagesPicker(onImagesPicked = viewModel::addScreenshots)

        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Text(
                text = "\u2190 Back",
                color = Color.White,
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val item = uiState.contribution
                Text(
                    text = item?.title?.ifBlank { item.packageName } ?: "Contribution",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkPrimary,
                )

                if (uiState.isLoading && item == null) {
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

                if (item != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StatusBadge(item.status)
                        ButtonBasic("Download APK", viewModel::download)
                    }

                    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            DetailRow("Package", item.packageName)
                            DetailRow("Version", "${item.versionName} (code ${item.versionCode})")
                            DetailRow("Developer", item.developer)
                            DetailRow("Category", item.category)
                            if (item.categories.isNotEmpty()) DetailRow("Categories", item.categories.joinToString(", "))
                            if (item.description.isNotBlank()) DetailRow("Description", item.description)
                            if (item.changelog.isNotBlank()) DetailRow("Changelog", item.changelog)
                            DetailRow("Submitted by", item.submittedBy)
                            if (item.declineReason.isNotBlank()) DetailRow("Decline reason", item.declineReason)
                        }
                    }

                    Text(
                        text = "Screenshots (${item.screenshots.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkPrimary,
                        fontWeight = FontWeight.Bold,
                    )

                    if (item.status == ContributionStatus.PENDING) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (uiState.isUploadingScreenshots) DarkPrimaryTransparent40 else DarkPrimaryTransparent40)
                                .clickable(enabled = !uiState.isUploadingScreenshots) { pickScreenshots() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (uiState.isUploadingScreenshots) "Uploading..." else "Add Screenshots",
                                color = DarkPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }

                    if (item.screenshots.isEmpty()) {
                        Text("No screenshots.", color = TransparentText40, style = MaterialTheme.typography.bodySmall)
                    } else {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            item.screenshots.forEachIndexed { index, url ->
                                StagedScreenshot(
                                    url = url,
                                    canRemove = item.status == ContributionStatus.PENDING,
                                    onRemove = { viewModel.removeScreenshot(index + 1) },
                                )
                            }
                        }
                    }

                    if (item.status == ContributionStatus.PENDING && role == ContributorRole.OWNER) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ButtonBasic("Approve", viewModel::approve)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NegativePrimary.copy(alpha = 0.2f))
                                    .clickable { viewModel.requestDecline() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Decline", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    if (item.status == ContributionStatus.DECLINED) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NegativePrimary.copy(alpha = 0.2f))
                                .clickable { viewModel.purge() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Purge Declined", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (uiState.showDeclineDialog) {
                    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("Decline this contribution?", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            TextField(
                                value = uiState.declineReason,
                                onValueChange = viewModel::setDeclineReason,
                                label = { Text("Reason (optional)") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ButtonBasic("Cancel", viewModel::cancelDecline)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NegativePrimary.copy(alpha = 0.2f))
                                        .clickable { viewModel.confirmDecline() }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text("Decline", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$label:",
            color = PurpleGrey40,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(110.dp),
        )
        Text(text = value, color = Color.White, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StagedScreenshot(url: String, canRemove: Boolean, onRemove: () -> Unit) {
    val imageUrl = if (url.startsWith("http")) url else "$BASE_URL_BUCKET_PUBLIC$url"
    Column(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkPrimaryCard)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "Screenshot",
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
        }
        if (canRemove) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NegativePrimary.copy(alpha = 0.2f))
                    .clickable { onRemove() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("\u00d7 Remove", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}