package id.neotica.neostore.admin.ui.feature.updateapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.ButtonBasic
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.PurpleGrey40
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UpdateAppView(
    viewModel: UpdateAppViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        Modifier.padding(16.dp)
    ) {
        Text(
            text = "Welcome to Neostore App Updater.",
            modifier = Modifier
                .padding(bottom = 8.dp),
            color = DarkPrimary
        )

        if (uiState.statusMessage.isNotEmpty()) {
            Text(
                text = uiState.statusMessage,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = uiState. packageName,
                onValueChange = { viewModel.setPackageName(it) },
                label = { Text("Package Name") },
                placeholder = { Text("com.something.app", color = PurpleGrey40) },
                singleLine = true
            )
            TextField(
                value = uiState.title,
                onValueChange = { viewModel.setTitle(it) },
                label = { Text("Title") },
                placeholder = { Text("com.something.app", color = PurpleGrey40) },
                singleLine = true
            )
        }
        TextField(
            value = uiState.category,
            onValueChange = { viewModel.setCategory(it) },
            label = { Text("Category") },
            placeholder = { Text("APPLICATION", color = PurpleGrey40) },
            singleLine = true
        )
        TextField(
            value = uiState.description,
            onValueChange = { viewModel.setDescription(it) },
            label = { Text("Description") },
            placeholder = { Text("This app was something.", color = PurpleGrey40) },
        )
        TextField(
            value = uiState.iconUrl,
            onValueChange = { viewModel.setIconUrl(it) },
            label = { Text("Icon URL") },
            placeholder = { Text("This app was something.", color = PurpleGrey40) },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ButtonBasic("Clear All") { viewModel.clear() }


            ButtonBasic("Check App") { viewModel.getAppDetail() }
            ButtonBasic("Update") { viewModel.updateApp() }
        }
    }
}