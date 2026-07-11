package id.neotica.neostore.admin.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import id.neotica.neostore.admin.domain.model.category.response.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelect(
    categories: List<Category>,
    selectedSlug: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Category",
    openTrigger: Int = 0,
) {
    var expanded by remember { mutableStateOf(false) }
    var drillSlug by remember { mutableStateOf<String?>(null) }
    var lastTrigger by remember { mutableStateOf(openTrigger) }

    if (openTrigger != lastTrigger) {
        lastTrigger = openTrigger
        expanded = true
    }

    LaunchedEffect(expanded) {
        if (!expanded && drillSlug != null) drillSlug = null
    }

    val displayText = findName(selectedSlug, categories) ?: "None (not set)"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        TextField(
            value = displayText,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            val currentRoot = drillSlug?.let { slug -> categories.firstOrNull { it.slug == slug } }
            if (currentRoot != null) {
                DropdownMenuItem(
                    text = { Text("← Back", color = Color(0xFF888888)) },
                    onClick = { drillSlug = null }
                )
                DropdownMenuItem(
                    text = { Text("1. ${currentRoot.name}") },
                    onClick = { onSelect(currentRoot.slug); expanded = false }
                )
                currentRoot.children.forEachIndexed { i, child ->
                    DropdownMenuItem(
                        text = { Text("${i + 2}.   └ ${child.name}") },
                        onClick = { onSelect(child.slug); expanded = false }
                    )
                }
            } else {
                DropdownMenuItem(
                    text = { Text("0. None (not set)") },
                    onClick = { onSelect(null); expanded = false }
                )
                categories.forEachIndexed { index, cat ->
                    val hasChildren = cat.children.isNotEmpty()
                    val indicator = if (hasChildren) " ▶ ${cat.name} (${cat.children.size})" else cat.name
                    DropdownMenuItem(
                        text = { Text("${index + 1}. $indicator") },
                        onClick = {
                            if (hasChildren) {
                                drillSlug = cat.slug
                            } else {
                                onSelect(cat.slug); expanded = false
                            }
                        }
                    )
                }
            }
        }
    }

    DisposableEffect(expanded) {
        if (!expanded) return@DisposableEffect onDispose {}
        val dispatcher = java.awt.KeyEventDispatcher { event ->
            if (event.id == java.awt.event.KeyEvent.KEY_PRESSED) {
                val currentSlug = drillSlug
                if (currentSlug != null) {
                    if (event.keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) {
                        drillSlug = null; true
                    } else {
                        val ch = event.keyChar
                        if (ch.isDigit()) {
                            val idx = ch.digitToInt()
                            if (idx == 0) {
                                drillSlug = null; true
                            } else if (idx == 1) {
                                val root = categories.firstOrNull { it.slug == currentSlug }
                                root?.let { onSelect(it.slug) }
                                expanded = false; true
                            } else {
                                val childIdx = idx - 2
                                val root = categories.firstOrNull { it.slug == currentSlug }
                                if (root != null && childIdx < root.children.size) {
                                    onSelect(root.children[childIdx].slug)
                                    expanded = false
                                }
                                true
                            }
                        } else false
                    }
                } else {
                    val ch = event.keyChar
                    if (ch.isDigit()) {
                        val idx = ch.digitToInt() - 1
                        if (idx == -1) {
                            onSelect(null)
                            expanded = false
                            true
                        } else if (idx < categories.size) {
                            val cat = categories[idx]
                            if (cat.children.isNotEmpty()) {
                                drillSlug = cat.slug; true
                            } else {
                                onSelect(cat.slug); expanded = false; true
                            }
                        } else false
                    } else false
                }
            } else false
        }
        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher)
        onDispose { java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher) }
    }
}

private fun findName(slug: String?, roots: List<Category>): String? {
    if (slug == null) return null
    for (root in roots) {
        if (root.slug == slug) return root.name
        val child = root.children.firstOrNull { it.slug == slug }
        if (child != null) return child.name
    }
    return null
}