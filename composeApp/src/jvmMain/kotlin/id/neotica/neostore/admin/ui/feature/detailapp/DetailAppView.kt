package id.neotica.neostore.admin.ui.feature.detailapp

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.AppVersionResponse
import id.neotica.neostore.admin.domain.model.category.response.Category
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.CategorySelect
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard
import id.neotica.neostore.admin.ui.components.NegativePrimary
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import id.neotica.neostore.admin.ui.components.TransparentText40
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DetailAppView(
    viewModel: DetailAppViewModel = koinViewModel(),
    packageName: String,
    onClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val openCategoryTrigger by viewModel.openCategoryTrigger.collectAsState()

    LaunchedEffect(packageName) {
        viewModel.clear()
        viewModel.setPackageName(packageName)
        viewModel.getAppDetail()
    }

    DisposableEffect(Unit) {
        val dispatcher = java.awt.KeyEventDispatcher { event ->
            if (event.id == java.awt.event.KeyEvent.KEY_PRESSED) {
                when {
                    event.keyChar == 'c' && event.modifiersEx == 0 -> {
                        viewModel.requestOpenCategory(); true
                    }
                    event.keyCode == java.awt.event.KeyEvent.VK_ENTER &&
                        (event.isMetaDown || event.isControlDown) -> {
                        viewModel.updateApp(); true
                    }
                    else -> false
                }
            } else false
        }
        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        onDispose { java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher) }
    }

    DetailAppViewContent(
        uiState = uiState,
        openCategoryTrigger = openCategoryTrigger,
        onPackageNameChange = viewModel::setPackageName,
        onTitleChange = viewModel::setTitle,
        onCategoryChange = viewModel::setCategorySlug,
        onDescriptionChange = viewModel::setDescription,
        onIconUrlChange = viewModel::setIconUrl,
        onGithubRepoChange = viewModel::setGithubRepo,
        onCheckApp = viewModel::getAppDetail,
        onClear = viewModel::clear,
        onUpdate = viewModel::updateApp,
        onResetGithubTag = viewModel::resetGithubTag,
        onDeleteVersion = viewModel::deleteVersion,
        onBack = { onClick(); viewModel.clear() },
    )
}

@Composable
private fun DetailAppViewContent(
    uiState: DetailAppUiState,
    openCategoryTrigger: Int = 0,
    onPackageNameChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String?) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIconUrlChange: (String) -> Unit,
    onGithubRepoChange: (String) -> Unit,
    onCheckApp: () -> Unit,
    onClear: () -> Unit,
    onUpdate: () -> Unit,
    onResetGithubTag: () -> Unit,
    onDeleteVersion: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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
            statusMessage = "",
        ),
        onPackageNameChange = {},
        onTitleChange = {},
        onCategoryChange = {},
        onDescriptionChange = {},
        onIconUrlChange = {},
        onGithubRepoChange = {},
        onCheckApp = {},
        onClear = {},
        onUpdate = {},
        onResetGithubTag = {},
        onDeleteVersion = {},
        onBack = {},
    )
}
