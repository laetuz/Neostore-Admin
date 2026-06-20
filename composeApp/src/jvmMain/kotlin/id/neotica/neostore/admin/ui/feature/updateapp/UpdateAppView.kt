package id.neotica.neostore.admin.ui.feature.updateapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UpdateAppView(
    viewModel: UpdateAppViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    UpdateAppViewContent(
        uiState = uiState,
        onPackageNameChange = viewModel::setPackageName,
        onTitleChange = viewModel::setTitle,
        onCategoryChange = viewModel::setCategory,
        onDescriptionChange = viewModel::setDescription,
        onIconUrlChange = viewModel::setIconUrl,
        onCheckApp = viewModel::getAppDetail,
        onClear = viewModel::clear,
        onUpdate = viewModel::updateApp,
    )
}

@Composable
private fun UpdateAppViewContent(
    uiState: UpdateAppUiState,
    onPackageNameChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIconUrlChange: (String) -> Unit,
    onCheckApp: () -> Unit,
    onClear: () -> Unit,
    onUpdate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Update App",
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

                TextField(
                    value = uiState.category,
                    onValueChange = onCategoryChange,
                    label = { Text("Category") },
                    placeholder = { Text("APPLICATION", color = PurpleGrey40) },
                    singleLine = true,
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
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            ButtonBasic("Check App", onCheckApp)
            Spacer(Modifier.weight(1f))
            ButtonBasic("Clear All", onClear)
            ButtonBasic("Update", onUpdate)
        }
    }
}

@Preview
@Composable
private fun UpdateAppViewPreview() {
    UpdateAppViewContent(
        uiState = UpdateAppUiState(
            packageName = "id.neotica.neomart",
            title = "Neomart",
            category = "APPLICATION",
            description = "A marketplace app for legacy Android devices.",
            iconUrl = "https://storage.example.com/buckets/neostore/id.neotica.neomart/icon.jpg",
            statusMessage = "",
        ),
        onPackageNameChange = {},
        onTitleChange = {},
        onCategoryChange = {},
        onDescriptionChange = {},
        onIconUrlChange = {},
        onCheckApp = {},
        onClear = {},
        onUpdate = {},
    )
}
