package id.neotica.neostore.admin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.domain.model.category.response.Category
import id.neotica.neostore.admin.platform.installPlatformKeyDispatcher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryMultiSelect(
    categories: List<Category>,
    selectedSlugs: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier,
    excludeSlug: String? = null,
    label: String = "Categories",
) {
    var expanded by remember { mutableStateOf(false) }
    var drillSlug by remember { mutableStateOf<String?>(null) }

    val blocked = remember(selectedSlugs, excludeSlug) { (selectedSlugs + listOfNotNull(excludeSlug)).toSet() }

    fun visibleChildren(root: Category): List<Category> =
        root.children.filter { it.slug !in blocked }

    val displayText = when {
        selectedSlugs.isEmpty() -> "None"
        selectedSlugs.size == 1 -> selectedSlugs.joinToString { findName(it, categories) ?: it }
        else -> "${selectedSlugs.size} categories"
    }

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
                    onClick = { onAdd(currentRoot.slug) }
                )
                visibleChildren(currentRoot).forEachIndexed { i, child ->
                    DropdownMenuItem(
                        text = { Text("${i + 2}.   └ ${child.name}") },
                        onClick = { onAdd(child.slug) }
                    )
                }
            } else {
                categories.forEachIndexed { index, cat ->
                    if (cat.slug in blocked) return@forEachIndexed
                    val kids = visibleChildren(cat)
                    val indicator = if (kids.isNotEmpty()) " ▶ ${cat.name} (${kids.size})" else cat.name
                    DropdownMenuItem(
                        text = { Text("${index + 1}. $indicator") },
                        onClick = {
                            if (kids.isNotEmpty()) {
                                drillSlug = cat.slug
                            } else {
                                onAdd(cat.slug)
                            }
                        }
                    )
                }
            }
        }
    }

    if (selectedSlugs.isNotEmpty()) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            selectedSlugs.forEach { slug ->
                val name = findName(slug, categories) ?: slug
                Box(
                    modifier = Modifier
                        .background(DarkPrimaryTransparent40, RoundedCornerShape(6.dp))
                        .clickable { onRemove(slug) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "$name  ×",
                        color = DarkPrimary,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }

    DisposableEffect(expanded) {
        if (!expanded) return@DisposableEffect onDispose {}
        val handle = installPlatformKeyDispatcher { event ->
            val currentSlug = drillSlug
            if (currentSlug != null) {
                if (event.isBackspace) {
                    drillSlug = null; true
                } else {
                    val ch = event.char
                    if (ch != null && ch.isDigit()) {
                        val idx = ch.digitToInt()
                        if (idx == 0) {
                            drillSlug = null; true
                        } else if (idx == 1) {
                            val root = categories.firstOrNull { it.slug == currentSlug }
                            root?.let { onAdd(it.slug) }
                            true
                        } else {
                            val root = categories.firstOrNull { it.slug == currentSlug }
                            val kids = root?.let { visibleChildren(it) }.orEmpty()
                            val childIdx = idx - 2
                            if (childIdx < kids.size) {
                                onAdd(kids[childIdx].slug)
                            }
                            true
                        }
                    } else false
                }
            } else {
                val ch = event.char
                if (ch != null && ch.isDigit()) {
                    val idx = ch.digitToInt() - 1
                    if (idx >= 0 && idx < categories.size) {
                        val cat = categories[idx]
                        if (cat.slug in blocked) {
                            true
                        } else {
                            val kids = visibleChildren(cat)
                            if (kids.isNotEmpty()) drillSlug = cat.slug
                            else onAdd(cat.slug)
                            true
                        }
                    } else false
                } else false
            }
        }
        onDispose { handle() }
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
