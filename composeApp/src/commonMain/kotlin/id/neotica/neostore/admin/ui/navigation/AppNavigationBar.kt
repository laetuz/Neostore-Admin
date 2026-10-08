package id.neotica.neostore.admin.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.neotica.neostore.admin.ui.components.DarkPrimary
import id.neotica.neostore.admin.ui.components.DarkPrimaryCard

@Composable
fun AppNavigationBar(
    currentScreen: MainScreenType,
    onNavigate: (MainScreenType) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItem> = navItems,
) {
    NavigationBar(
        containerColor = DarkPrimaryCard,
        tonalElevation = 0.dp,
        modifier = modifier,
    ) {
        items.forEach { item ->
            if (item.children.isNotEmpty()) {
                BarGroupItem(item = item, currentScreen = currentScreen, onNavigate = onNavigate)
            } else {
                BarLeafItem(item = item, currentScreen = currentScreen, onNavigate = onNavigate)
            }
        }
    }
}

@Composable
private fun RowScope.BarLeafItem(
    item: NavItem,
    currentScreen: MainScreenType,
    onNavigate: (MainScreenType) -> Unit,
) {
    val type = item.type ?: return
    NavigationBarItem(
        selected = currentScreen == type ||
            (currentScreen == MainScreenType.DETAIL && type == MainScreenType.FEEDS),
        onClick = { onNavigate(type) },
        icon = {
            Text(text = item.indicator, style = MaterialTheme.typography.titleLarge)
        },
        label = {
            Text(text = item.label, style = MaterialTheme.typography.labelSmall)
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = DarkPrimary,
            unselectedIconColor = DarkPrimary.copy(alpha = 0.5f),
            selectedTextColor = DarkPrimary,
            unselectedTextColor = DarkPrimary.copy(alpha = 0.5f),
            indicatorColor = DarkPrimary.copy(alpha = 0.15f),
        ),
    )
}

@Composable
private fun RowScope.BarGroupItem(
    item: NavItem,
    currentScreen: MainScreenType,
    onNavigate: (MainScreenType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = item.children.any { it.type == currentScreen }

    Box(modifier = Modifier.weight(1f)) {
        this@BarGroupItem.NavigationBarItem(
            modifier = Modifier.fillMaxWidth(),
            selected = selected,
            onClick = { expanded = true },
            icon = {
                Text(text = item.indicator, style = MaterialTheme.typography.titleLarge)
            },
            label = {
                Text(text = item.label, style = MaterialTheme.typography.labelSmall)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = DarkPrimary,
                unselectedIconColor = DarkPrimary.copy(alpha = 0.5f),
                selectedTextColor = DarkPrimary,
                unselectedTextColor = DarkPrimary.copy(alpha = 0.5f),
                indicatorColor = DarkPrimary.copy(alpha = 0.15f),
            ),
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            item.children.forEach { child ->
                DropdownMenuItem(
                    text = { Text("${child.indicator}  ${child.label}") },
                    onClick = {
                        child.type?.let(onNavigate)
                        expanded = false
                    },
                )
            }
        }
    }
}
