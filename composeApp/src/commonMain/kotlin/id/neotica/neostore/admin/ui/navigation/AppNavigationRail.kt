package id.neotica.neostore.admin.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
fun AppNavigationRail(
    currentScreen: MainScreenType,
    onNavigate: (MainScreenType) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItem> = navItems,
) {
    NavigationRail(
        containerColor = DarkPrimaryCard,
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        items.forEach { item ->
            if (item.children.isNotEmpty()) {
                RailGroupItem(item = item, currentScreen = currentScreen, onNavigate = onNavigate)
            } else {
                RailLeafItem(item = item, currentScreen = currentScreen, onNavigate = onNavigate)
            }
        }
    }
}

@Composable
private fun ColumnScope.RailLeafItem(
    item: NavItem,
    currentScreen: MainScreenType,
    onNavigate: (MainScreenType) -> Unit,
) {
    val type = item.type ?: return
    NavigationRailItem(
        selected = currentScreen == type ||
            (currentScreen == MainScreenType.DETAIL && type == MainScreenType.FEEDS),
        onClick = { onNavigate(type) },
        icon = {
            Text(text = item.indicator, style = MaterialTheme.typography.titleLarge)
        },
        label = {
            Text(text = item.label, style = MaterialTheme.typography.labelSmall)
        },
        colors = NavigationRailItemDefaults.colors(
            selectedIconColor = DarkPrimary,
            unselectedIconColor = DarkPrimary.copy(alpha = 0.5f),
            selectedTextColor = DarkPrimary,
            unselectedTextColor = DarkPrimary.copy(alpha = 0.5f),
            indicatorColor = DarkPrimary.copy(alpha = 0.15f),
        ),
    )
}

@Composable
private fun ColumnScope.RailGroupItem(
    item: NavItem,
    currentScreen: MainScreenType,
    onNavigate: (MainScreenType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = item.children.any { it.type == currentScreen }

    Box {
        NavigationRailItem(
            selected = selected,
            onClick = { expanded = true },
            icon = {
                Text(text = item.indicator, style = MaterialTheme.typography.titleLarge)
            },
            label = {
                Text(text = item.label, style = MaterialTheme.typography.labelSmall)
            },
            colors = NavigationRailItemDefaults.colors(
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
