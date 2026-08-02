package id.neotica.neostore.admin.ui.feature.clipboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.DarkBackground
import id.neotica.neostore.admin.ui.components.DarkBackgroundV2
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryTransparent40
import id.neotica.neostore.admin.ui.components.NeoCardSolid
import id.neotica.neostore.admin.ui.components.TransparentText40
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import kotlinx.coroutines.delay

internal val clipboardItems = mutableStateListOf(
    "Ryo Martin Sopian"
)

internal val clipboardCopiedIndex = mutableStateOf(-1)

internal val clipboardPageDownCount = mutableStateOf(2)

@Composable
fun ClipboardView(onBack: () -> Unit) {
    val copiedIndex by clipboardCopiedIndex

    var isEditing by remember { mutableStateOf(false) }
    var draftItems by remember { mutableStateOf(clipboardItems.toList()) }

    LaunchedEffect(isEditing) {
        if (isEditing) draftItems = clipboardItems.toList()
    }

    LaunchedEffect(copiedIndex) {
        if (copiedIndex >= 0) {
            delay(1500)
            clipboardCopiedIndex.value = -1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Clipboard",
                style = MaterialTheme.typography.headlineSmall,
                color = DarkPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )

            Text(
                text = if (isEditing) "Done" else "Edit",
                color = DarkPrimary,
                modifier = Modifier
                    .border(1.dp, DarkPrimary)
                    .clickable {
                        if (isEditing) {
                            clipboardItems.clear()
                            clipboardItems.addAll(draftItems)
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        if (isEditing) {
            Text(
                text = "Edit the values below, then press Done.",
                color = TransparentText40,
                style = MaterialTheme.typography.bodySmall,
            )

            draftItems.forEachIndexed { index, value ->
                EditableItemRow(
                    index = index,
                    value = value,
                    onValueChange = { newValue ->
                        draftItems = draftItems.toMutableList().also { it[index] = newValue }
                    },
                )
            }
        } else {
            Text(
                text = "Press Esc / Cmd+0 to close. Tap an item or press 1\u20139 to copy. Cmd+D: switch + scroll.",
                color = TransparentText40,
                style = MaterialTheme.typography.bodySmall,
            )

            clipboardItems.forEachIndexed { index, text ->
                ClipboardItem(
                    index = index,
                    text = text,
                    isCopied = copiedIndex == index,
                    onClick = {
                        copyToClipboard(text)
                        clipboardCopiedIndex.value = index
                    },
                )
            }
        }
    }
}

@Composable
private fun EditableItemRow(
    index: Int,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "${index + 1}.",
            color = DarkPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(24.dp),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DarkPrimary,
                unfocusedBorderColor = DarkPrimaryTransparent40,
                focusedContainerColor = DarkBackgroundV2,
                unfocusedContainerColor = DarkBackgroundV2,
                cursorColor = DarkPrimary,
            ),
        )
    }
}

@Composable
private fun ClipboardItem(
    index: Int,
    text: String,
    isCopied: Boolean,
    onClick: () -> Unit,
) {
    NeoCardSolid(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${index + 1}.",
                color = DarkPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
            )

            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = if (isCopied) "Copied!" else "Copy",
                color = if (isCopied) Color(0xFF4ADE80) else DarkPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

internal fun copyToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(text), null)
}