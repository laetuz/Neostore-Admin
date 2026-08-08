package id.neotica.neostore.admin.ui.feature.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.config.BuildConfig
import id.neotica.neostore.admin.ui.components.TransparentText40

@Composable
fun InfoView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Information",
            style = MaterialTheme.typography.headlineSmall,
            color = DarkPrimary,
        )

        KeyboardShortcutsCard()
        BuildInfoCard()
        FeaturesCard()
        TechStackCard()
        AboutCard()
    }
}

@Composable
private fun KeyboardShortcutsCard() {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Keyboard Shortcuts")

            ShortcutRow("⌘/Ctrl + 1", "Upload tab")
            ShortcutRow("⌘/Ctrl + 2", "Feed tab")
            ShortcutRow("⌘/Ctrl + 3", "Categories tab")
            ShortcutRow("⌘/Ctrl + 4", "Analytics tab")
            ShortcutRow("⌘/Ctrl + 5", "Info tab")
            ShortcutRow("⌘/Ctrl + 0", "Clipboard (temporary)")
            ShortcutRow("Escape", "Back to Feed (from Detail)")
            ShortcutRow("⌘ + Shift + C", "Open category dropdown (in Detail)")
            ShortcutRow("⌘/Ctrl + Enter", "Update app (in Detail)")
            Spacer(modifier = Modifier.height(4.dp))
            SectionTitle("Category dropdown (when open)")
            ShortcutRow("1 – 5", "Select root category by index")
            ShortcutRow("Backspace / 0", "Go back to root list (in drill view)")
            ShortcutRow("1 (in drill)", "Select parent category")
            ShortcutRow("2+ (in drill)", "Select child category")
        }
    }
}

@Composable
private fun BuildInfoCard() {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Build")
            InfoRow("Version", BuildConfig.APP_VERSION)
            InfoRow("Package", "Neostore Admin")
            InfoRow("Kotlin", "2.4.10")
            InfoRow("Compose", "1.11.1")
            InfoRow("JDK", "21+")
        }
    }
}

@Composable
private fun FeaturesCard() {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SectionTitle("Features")
            FeatureItem("APK Upload & Publish: drag-drop, bulk queue, auto-registration fallback")
            FeatureItem("Categories CRUD: expandable tree, inline edit/delete, CategorySelect dropdown")
            FeatureItem("App Detail & Version Management: edit metadata, delete versions, reset GitHub tag")
            FeatureItem("Manual Icon Upload: file chooser, upload to S3, auto-persist via update API")
            FeatureItem("Unregister App: removes app record and all S3 files with confirmation")
            FeatureItem("Analytics Dashboard: daily counts, event drill-down, trending events, event detail")
            FeatureItem("Collections Management: placeholder")
            FeatureItem("Keyboard Shortcuts: global AWT-level")
        }
    }
}

@Composable
private fun TechStackCard() {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("Tech Stack")
            InfoRow("UI", "Compose Multiplatform Desktop (Material3)")
            InfoRow("HTTP", "Ktor 3.1.2 (CIO engine)")
            InfoRow("DI", "Koin 4.0.1")
            InfoRow("Serialization", "kotlinx.serialization")
            InfoRow("APK Parser", "apk-parser 2.6.10")
            InfoRow("Auth", "Bearer token (Java Preferences API)")
        }
    }
}

@Composable
private fun AboutCard() {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SectionTitle("About")
            Text(
                text = "Neostore Admin is a desktop dashboard for managing and uploading APKs to the HoloMarket. Built with Compose Multiplatform.",
                color = TransparentText40,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            InfoRow("Developer", "Neotica")
            InfoRow("Repository", "github.com/laetuz/NeostoreAdmin")
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleSmall,
    )
}

@Composable
private fun ShortcutRow(key: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = key,
            color = DarkPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = description,
            color = TransparentText40,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("•", color = DarkPrimary, style = MaterialTheme.typography.bodySmall)
        Text(text, color = TransparentText40, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TransparentText40,
            style = MaterialTheme.typography.bodySmall,
        )
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}