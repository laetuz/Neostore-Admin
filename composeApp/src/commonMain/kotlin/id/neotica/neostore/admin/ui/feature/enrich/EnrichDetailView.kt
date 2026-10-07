package id.neotica.neostore.admin.ui.feature.enrich

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.enrich.ScraperSource
import id.neotica.neostore.admin.domain.model.enrich.WaybackVersion
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.CategorySelect
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.DarkPrimaryTransparent40
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import id.neotica.neostore.admin.ui.components.TransparentText40
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun EnrichDetailView(
    packageName: String,
    onBack: () -> Unit,
    viewModel: EnrichDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(packageName) { viewModel.load(packageName) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
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
                Text(
                    text = uiState.app?.title?.ifBlank { packageName } ?: packageName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkPrimary,
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }

                if (uiState.statusMessage.isNotEmpty()) {
                    Text(
                        text = uiState.statusMessage,
                        color = if (uiState.statusMessage.contains("failed", true)) MaterialTheme.colorScheme.error
                        else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Source", color = Color.White, style = MaterialTheme.typography.titleSmall)
                        SourceDropdown(
                            selected = uiState.source,
                            onSelect = viewModel::setSource,
                        )

                        if (uiState.source == ScraperSource.WAYBACK) {
                            when {
                                uiState.isLoadingVersions -> Text(
                                    text = "Loading versions...",
                                    color = TransparentText40,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                uiState.waybackVersions.isNotEmpty() -> {
                                    VersionDropdown(
                                        versions = uiState.waybackVersions,
                                        selected = uiState.selectedWaybackTimestamp,
                                        onSelect = viewModel::setWaybackVersion,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        ButtonBasic("\u2190 Older", viewModel::olderVersion)
                                        ButtonBasic("Newer \u2192", viewModel::nextNewerVersion)
                                    }
                                }
                            }
                        }

                        if (uiState.resolvedSnapshot != null) {
                            Text(
                                text = "Version: ${uiState.resolvedSnapshot}",
                                color = DarkPrimary,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }

                        ButtonBasic(
                            if (uiState.isScraping) "Scraping..." else "Scrape",
                            viewModel::scrape,
                        )
                    }
                }

                NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Metadata", color = Color.White, style = MaterialTheme.typography.titleSmall)

                        TextField(
                            value = uiState.developer,
                            onValueChange = viewModel::setDeveloper,
                            label = { Text("Developer") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (uiState.scrapedDeveloper.isNotBlank() && uiState.scrapedDeveloper != uiState.developer) {
                            ScrapedSuggestion(
                                label = "developer",
                                value = uiState.scrapedDeveloper,
                                canUse = true,
                                onUse = viewModel::useScrapedDeveloper,
                            )
                        }

                        TextField(
                            value = uiState.description,
                            onValueChange = viewModel::setDescription,
                            label = { Text("Description") },
                            minLines = 3,
                            maxLines = 8,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (uiState.scrapedDescription.isNotBlank() && uiState.scrapedDescription != uiState.description) {
                            ScrapedSuggestion(
                                label = "description",
                                value = uiState.scrapedDescription,
                                canUse = true,
                                onUse = viewModel::useScrapedDescription,
                            )
                        }

                        CategorySelect(
                            categories = uiState.categories,
                            selectedSlug = uiState.categorySlug,
                            onSelect = viewModel::setCategorySlug,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (uiState.scrapedCategoryName.isNotBlank() && uiState.scrapedCategorySlug != uiState.categorySlug) {
                            ScrapedSuggestion(
                                label = "category",
                                value = if (uiState.scrapedCategorySlug != null) {
                                    uiState.scrapedCategoryName
                                } else {
                                    "${uiState.scrapedCategoryName} (no matching category — pick manually)"
                                },
                                canUse = uiState.scrapedCategorySlug != null,
                                onUse = viewModel::useScrapedCategory,
                            )
                        }
                    }
                }

                if (uiState.screenshots.isNotEmpty()) {
                    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Screenshots (${uiState.screenshots.size})",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                            )

                            Text(
                                text = "JPEG quality: ${uiState.quality}",
                                color = DarkPrimary,
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Slider(
                                value = uiState.quality.toFloat(),
                                onValueChange = { viewModel.setQuality(it.roundToInt()) },
                                valueRange = 1f..100f,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            uiState.screenshots.forEachIndexed { index, shot ->
                                ScreenshotRow(
                                    index = index,
                                    shot = shot,
                                    onToggleInclude = { viewModel.toggleInclude(index) },
                                )
                            }
                        }
                    }
                }

                if (uiState.hasScraped) {
                    ButtonBasic(
                        if (uiState.isApplying) "Applying..." else "Apply",
                        viewModel::apply,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScrapedSuggestion(
    label: String,
    value: String,
    canUse: Boolean,
    onUse: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkPrimaryCard)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Scraped $label", color = TransparentText40, style = MaterialTheme.typography.labelSmall)
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (canUse) {
            ButtonBasic("Use", onUse)
        }
    }
}

@Composable
private fun VersionDropdown(
    versions: List<WaybackVersion>,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = versions.firstOrNull { it.timestamp == selected }?.date ?: "Oldest"

    Box {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(DarkPrimaryCard)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Version: $selectedLabel \u25BE",
                color = Color.White,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            versions.forEach { version ->
                DropdownMenuItem(
                    text = { Text(version.date) },
                    onClick = {
                        onSelect(version.timestamp)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SourceDropdown(
    selected: ScraperSource,
    onSelect: (ScraperSource) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(DarkPrimaryTransparent40)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${selected.label} \u25BE",
                color = DarkPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ScraperSource.entries.forEach { source ->
                DropdownMenuItem(
                    text = { Text(source.label) },
                    onClick = {
                        onSelect(source)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ScreenshotRow(
    index: Int,
    shot: ScrapedScreenshot,
    onToggleInclude: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkPrimaryCard)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#${index + 1}",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium,
            )
            IncludeChip(included = shot.included, onClick = onToggleInclude)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PreviewColumn(
                title = "Original",
                size = formatSize(shot.originalSize),
                bytes = shot.bytes,
                loading = shot.isLoading,
                failed = shot.failed,
            )
            PreviewColumn(
                title = "JPEG",
                size = if (shot.compressedBytes != null) formatSize(shot.compressedSize) else "-",
                bytes = shot.compressedBytes,
                loading = false,
                failed = false,
            )
        }

        if (shot.conversionFailed) {
            Text(
                text = "Conversion to JPEG failed — this screenshot will be skipped.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun PreviewColumn(
    title: String,
    size: String,
    bytes: ByteArray?,
    loading: Boolean,
    failed: Boolean,
) {
    Column(
        modifier = Modifier.width(150.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.size(20.dp))
                failed -> Text("failed", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                bytes != null -> {
                    val bitmap = remember(bytes) {
                        runCatching { bytes.decodeToImageBitmap() }.getOrNull()
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Text("preview n/a", color = PurpleGrey40, style = MaterialTheme.typography.labelSmall)
                    }
                }
                else -> Text("no bytes", color = PurpleGrey40, style = MaterialTheme.typography.labelSmall)
            }
        }
        Text(title, color = Color.White, style = MaterialTheme.typography.labelSmall)
        Text(size, color = TransparentText40, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun IncludeChip(included: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (included) DarkPrimaryTransparent40 else DarkPrimaryCard)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (included) "Included" else "Excluded",
            color = if (included) DarkPrimary else TransparentText40,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "-"
    val kb = bytes / 1024.0
    return if (kb < 1024) {
        "${(kb * 10).roundToInt() / 10.0} KB"
    } else {
        "${(kb / 1024 * 100).roundToInt() / 100.0} MB"
    }
}
