package id.neotica.neostore.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.TopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.feature.upload.UploadView

@Composable
fun MainView(
    onLogout: () -> Unit = {}
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    MaterialTheme {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Neostore Admin",
                                color = DarkPrimary
                            )
                        },
                        backgroundColor = DarkBackground,
                        actions = {
                            Box(
                                Modifier
                                    .border(1.dp, DarkPrimary)
                                    .clickable { dropdownExpanded = !dropdownExpanded }
                            ) {
                                Text(
                                    text = "More ...",
                                    color = DarkPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Logout") },
                                        onClick = {
                                            dropdownExpanded = false
                                            onLogout()
                                        }
                                    )
                                }
                            }

                        }
                    )
                    Divider(
                        thickness = 2.dp,
                        color = DarkPrimary
                    )
                }
            }
        ) {
            LazyColumn(
                contentPadding = it,
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground)
            ) {
                item {
                    UploadView()
                }
            }
        }
    }
}