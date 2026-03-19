package id.neotica.neostore.admin.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.feature.UploadView

@Composable
fun MainView() {
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
                        backgroundColor = DarkBackground
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