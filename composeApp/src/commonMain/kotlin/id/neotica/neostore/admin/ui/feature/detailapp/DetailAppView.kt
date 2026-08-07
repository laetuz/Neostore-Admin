package id.neotica.neostore.admin.ui.feature.detailapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.AppVersionResponse
import id.neotica.neostore.admin.domain.model.category.response.Category
import id.neotica.neostore.admin.platform.PlatformFile
import id.neotica.neostore.admin.platform.installPlatformKeyDispatcher
import id.neotica.neostore.admin.platform.rememberPlatformImagePicker
import id.neotica.neostore.admin.platform.rememberPlatformImagesPicker
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
import coil3.compose.SubcomposeAsyncImage
import id.neotica.neostore.admin.platform.rememberPlatformFileDropTarget
import id.neotica.neostore.admin.ui.components.NeoCard
import id.neotica.neostore.admin.utils.Constants.BASE_URL_BUCKET_PUBLIC

@Composable
fun DetailAppView(
    viewModel: DetailAppViewModel = koinViewModel(),
    packageName: String,
    onClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val openCategoryTrigger by viewModel.openCategoryTrigger.collectAsState()

    var isDraggingScreenshots by remember { mutableStateOf(false) }
    val dropScreenshotsTarget = rememberPlatformFileDropTarget(
        onDragStarted = { isDraggingScreenshots = true },
        onDragEnded = { isDraggingScreenshots = false },
        onFilesDropped = { files -> viewModel.addScreenshots(files) },
    )

    LaunchedEffect(packageName) {
        viewModel.clear()
        viewModel.setPackageName(packageName)
        viewModel.getAppDetail()
    }

    DisposableEffect(Unit) {
        val handle = installPlatformKeyDispatcher { event ->
            when {
                event.char?.equals('c', ignoreCase = true) == true
                        && event.isShiftDown
                        && (event.isMetaDown || event.isCtrlDown) -> {
                    viewModel.requestOpenCategory(); true
                }
                event.isEnter && (event.isMetaDown || event.isCtrlDown) -> {
                    viewModel.updateApp(); true
                }
                else -> false
            }
        }
        onDispose { handle() }
    }

    DetailAppViewContent(
        uiState = uiState,
        openCategoryTrigger = openCategoryTrigger,
        isDraggingScreenshots = isDraggingScreenshots,
        dropScreenshotsTarget = dropScreenshotsTarget,
        onPackageNameChange = viewModel::setPackageName,
        onTitleChange = viewModel::setTitle,
        onCategoryChange = viewModel::setCategorySlug,
        onDescriptionChange = viewModel::setDescription,
        onIconUrlChange = viewModel::setIconUrl,
        onDeveloperChange = viewModel::setDeveloper,
        onGithubRepoChange = viewModel::setGithubRepo,
        onCheckApp = viewModel::getAppDetail,
        onClear = viewModel::clear,
        onUpdate = viewModel::updateApp,
        onResetGithubTag = viewModel::resetGithubTag,
        onDeleteVersion = viewModel::deleteVersion,
        onBack = { onClick(); viewModel.clear() },
        onUploadIcon = viewModel::uploadIcon,
        onAddScreenshots = viewModel::addScreenshots,
        onRequestDeleteScreenshot = viewModel::requestDeleteScreenshot,
        onCancelDeleteScreenshot = viewModel::cancelDeleteScreenshot,
        onConfirmDeleteScreenshot = viewModel::confirmDeleteScreenshot,
        onMoveScreenshot = viewModel::moveScreenshot,
        onRequestUnregister = viewModel::requestUnregister,
        onCancelUnregister = viewModel::cancelUnregister,
        onUnregisterApp = { viewModel.unregisterApp { onClick(); viewModel.clear() } },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DetailAppViewContent(
    uiState: DetailAppUiState,
    openCategoryTrigger: Int = 0,
    isDraggingScreenshots: Boolean = false,
    dropScreenshotsTarget: DragAndDropTarget? = null,
    onPackageNameChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIconUrlChange: (String) -> Unit,
    onDeveloperChange: (String) -> Unit,
    onGithubRepoChange: (String) -> Unit,
    onCheckApp: () -> Unit,
    onClear: () -> Unit,
    onUpdate: () -> Unit,
    onResetGithubTag: () -> Unit,
    onDeleteVersion: (String) -> Unit,
    onBack: () -> Unit,
    onUploadIcon: (PlatformFile) -> Unit = {},
    onAddScreenshots: (List<PlatformFile>) -> Unit = {},
    onRequestDeleteScreenshot: (String) -> Unit = {},
    onCancelDeleteScreenshot: () -> Unit = {},
    onConfirmDeleteScreenshot: () -> Unit = {},
    onMoveScreenshot: (String, Int) -> Unit = { _, _ -> },
    onRequestUnregister: () -> Unit = {},
    onCancelUnregister: () -> Unit = {},
    onUnregisterApp: () -> Unit = {},
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val isCompact = maxWidth < 600.dp
        val pickIcon = rememberPlatformImagePicker(onImagePicked = onUploadIcon)
        val pickScreenshots = rememberPlatformImagesPicker(onImagesPicked = onAddScreenshots)

        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
        ) {
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
                    text = "App Detail",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkPrimary,
                )

                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }

                if (uiState.statusMessage.isNotEmpty()) {
                    val isError = uiState.statusMessage.contains("Failed", ignoreCase = true)

                    Text(
                        text = uiState.statusMessage,
                        color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        )
                }


                NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {

                        Text(
                            text = "App Identity",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )


                        AppIdentityFields(
                            uiState = uiState,
                            onPackageNameChange = onPackageNameChange,
                            onTitleChange = onTitleChange,
                            isCompact = isCompact,
                        )

                        CategorySelect(
                            categories = uiState.categories,
                            selectedSlug = uiState.categorySlug,
                            onSelect = onCategoryChange,
                            openTrigger = openCategoryTrigger,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        TextField(

                            value = uiState.description,

                            onValueChange = onDescriptionChange,
                            label = { Text("Description") },
                            placeholder = { Text("Describe what this app does", color = PurpleGrey40) },
                            minLines = 3,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth(),
                        )


                        TextField(
                            value = uiState.iconUrl,
                            onValueChange = onIconUrlChange,
                            label = { Text("Icon URL") },
                            placeholder = { Text("https://storage.example.com/.../icon.jpg", color = PurpleGrey40) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (uiState.isUploadingIcon) DarkPrimaryTransparent40 else DarkPrimaryTransparent40)
                                    .clickable(enabled = !uiState.isUploadingIcon) {
                                        pickIcon()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (uiState.isUploadingIcon) "Uploading..." else "Browse & Upload Icon",
                                    color = DarkPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                )
                            }
                        }

                        TextField(
                            value = uiState.developer,
                            onValueChange = onDeveloperChange,
                            label = { Text("Developer") },
                            placeholder = { Text("laetuz", color = PurpleGrey40) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        TextField(
                            value = uiState.githubRepo,
                            onValueChange = onGithubRepoChange,
                            label = { Text("GitHub Repo") },
                            placeholder = { Text("laetuz/RepoName", color = PurpleGrey40) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        if (uiState.lastGithubTag.isNotBlank()) {
                            Text(
                                text = "Last GitHub Tag: ${uiState.lastGithubTag}",
                                color = TransparentText40,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(Modifier.weight(1f))
                    ButtonBasic("Clear All", onClear)
                    ButtonBasic("Update", onUpdate)
                }

                ButtonBasic("Reset GitHub Tag", onResetGithubTag)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(NegativePrimary.copy(alpha = 0.2f))
                        .clickable { onRequestUnregister() }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Unregister App", color = NegativePrimary, fontWeight = FontWeight.Bold)
                }

                if (uiState.showUnregisterConfirm) {
                    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Are you sure? This will permanently delete the app and all its files (including S3 objects).",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ButtonBasic("Cancel", onCancelUnregister)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NegativePrimary.copy(alpha = 0.2f))
                                        .clickable { onUnregisterApp() }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text("Yes, Delete", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                if (uiState.versions.isNotEmpty()) {
                    Text(
                        text = "Versions (${uiState.versions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkPrimary,
                        fontWeight = FontWeight.Bold,
                    )

                    uiState.versions.forEach { version ->
                        VersionCard(version = version, onDelete = { onDeleteVersion(version.id) })
                    }
                }

                if (dropScreenshotsTarget != null) {
                    NeoCard(
                        isDragging = isDraggingScreenshots,
                        dropTarget = dropScreenshotsTarget
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ScreenshotsSection(
                                screenshots = uiState.screenshots,
                                isUploading = uiState.isUploadingScreenshots,
                                onAdd = { pickScreenshots() },
                                onMove = onMoveScreenshot,
                                onRequestDelete = onRequestDeleteScreenshot,
                            )
                        }
                    }
                } else {
                    // Fallback just in case the target fails to initialize
                    ScreenshotsSection(
                        screenshots = uiState.screenshots,
                        isUploading = uiState.isUploadingScreenshots,
                        onAdd = { pickScreenshots() },
                        onMove = onMoveScreenshot,
                        onRequestDelete = onRequestDeleteScreenshot,
                    )
                }

                if (uiState.screenshotToDelete != null) {
                    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                text = "Delete this screenshot?",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                ButtonBasic("Cancel", onCancelDeleteScreenshot)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NegativePrimary.copy(alpha = 0.2f))
                                        .clickable { onConfirmDeleteScreenshot() }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text("Delete", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
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
private fun AppIdentityFields(
    uiState: DetailAppUiState,
    onPackageNameChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    isCompact: Boolean,
) {
    if (isCompact) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextField(
                value = uiState.packageName,
                onValueChange = onPackageNameChange,
                label = { Text("Package Name") },
                placeholder = { Text("id.neotica.neomart", color = PurpleGrey40) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                placeholder = { Text("App display name", color = PurpleGrey40) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            TextField(
                value = uiState.packageName,
                onValueChange = onPackageNameChange,
                label = { Text("Package Name") },
                placeholder = { Text("id.neotica.neomart", color = PurpleGrey40) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            TextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                placeholder = { Text("App display name", color = PurpleGrey40) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun VersionCard(version: AppVersionResponse, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkPrimaryCard)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${version.versionName} (code ${version.versionCode})",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SDK: ${version.minSdk} - ${version.maxSdk ?: version.minSdk}",
                    color = TransparentText40,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NegativePrimary.copy(alpha = 0.2f))
                    .clickable { onDelete() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Delete",
                    color = NegativePrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        if (version.changelog.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = version.changelog,
                color = TransparentText40.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun ScreenshotsSection(
    screenshots: List<String>,
    isUploading: Boolean,
    onAdd: () -> Unit,
    onMove: (String, Int) -> Unit,
    onRequestDelete: (String) -> Unit,
) {
    Text(
        text = "Screenshots (${screenshots.size})",
        style = MaterialTheme.typography.titleMedium,
        color = DarkPrimary,
        fontWeight = FontWeight.Bold,
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isUploading) DarkPrimaryTransparent40 else DarkPrimaryTransparent40)
            .clickable(enabled = !isUploading) { onAdd() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isUploading) "Uploading..." else "Add Screenshots",
            color = DarkPrimary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
        )
    }

    if (screenshots.isEmpty()) {
        Text(
            text = "No screenshots yet.",
            color = TransparentText40,
            style = MaterialTheme.typography.bodySmall,
        )
        return
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        screenshots.forEachIndexed { index, url ->
            ScreenshotThumbnail(
                url = url,
                canMoveLeft = index > 0,
                canMoveRight = index < screenshots.lastIndex,
                onMoveLeft = { onMove(url, -1) },
                onMoveRight = { onMove(url, 1) },
                onDelete = { onRequestDelete(url) },
            )
        }
    }
}

@Composable
private fun ScreenshotThumbnail(
    url: String,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkPrimaryCard)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val imageUrl = if (url.startsWith("http")) url else "$BASE_URL_BUCKET_PUBLIC$url"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ArrowButton(enabled = canMoveLeft, label = "\u2190", onClick = onMoveLeft)
                ArrowButton(enabled = canMoveRight, label = "\u2192", onClick = onMoveRight)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(NegativePrimary.copy(alpha = 0.2f))
                    .clickable { onDelete() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("\u00d7", color = NegativePrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ArrowButton(enabled: Boolean, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (enabled) DarkPrimaryTransparent40 else DarkPrimaryTransparent40.copy(alpha = 0.3f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = if (enabled) DarkPrimary else TransparentText40.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview
@Composable
private fun DetailAppViewPreview() {
    DetailAppViewContent(
        uiState = DetailAppUiState(
            packageName = "id.neotica.neomart",
            title = "Neomart",
            categorySlug = "application",
            categories = listOf(
                Category("application", "Application"),
                Category("game", "Game"),
            ),
            description = "A marketplace app for legacy Android devices.",
            iconUrl = "https://storage.example.com/buckets/neostore/id.neotica.neomart/icon.jpg",
            versions = listOf(
                AppVersionResponse("v1", "1", "1.0", 1, "/file.apk", "Initial release.", 3, 21, 1000L),
                AppVersionResponse("v2", "1", "1.1", 2, "/file.apk", "Bug fixes.", 7, 21, 2000L),
            ),
            screenshots = listOf(
                "/buckets/neostore/id.neotica.neomart/screenshots/1.jpg",
                "/buckets/neostore/id.neotica.neomart/screenshots/2.jpg",
            ),
            statusMessage = "",
        ),
        onPackageNameChange = {},
        onTitleChange = {},
        onCategoryChange = {},
        onDescriptionChange = {},
        onIconUrlChange = {},
        onDeveloperChange = {},
        onGithubRepoChange = {},
        onCheckApp = {},
        onClear = {},
        onUpdate = {},
        onResetGithubTag = {},
        onDeleteVersion = {},
        onBack = {},
        onUploadIcon = {},
        onAddScreenshots = {},
        onRequestDeleteScreenshot = {},
        onCancelDeleteScreenshot = {},
        onConfirmDeleteScreenshot = {},
        onMoveScreenshot = { _, _ -> },
        onRequestUnregister = {},
        onCancelUnregister = {},
        onUnregisterApp = {},
    )
}
