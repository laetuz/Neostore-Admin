package id.neotica.neostore.admin.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AppPagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = DarkBackgroundV2,
) {
    val maxVisiblePages = 5
    var startPage = maxOf(1, currentPage - maxVisiblePages / 2)
    var endPage = minOf(totalPages, startPage + maxVisiblePages - 1)

    if (endPage - startPage + 1 < maxVisiblePages) {
        startPage = maxOf(1, endPage - maxVisiblePages + 1)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor)
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (currentPage > 1) {
                PageButton("<") { onPageChange(currentPage - 1) }
            }

            if (startPage > 1) {
                PageButton("1") { onPageChange(1) }
                if (startPage > 2) {
                    Text("...", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray)
                }
            }

            for (pageIndex in startPage..endPage) {
                PageButton(
                    text = pageIndex.toString(),
                    isSelected = pageIndex == currentPage,
                    onClick = { onPageChange(pageIndex) },
                )
            }

            if (endPage < totalPages) {
                if (endPage < totalPages - 1) {
                    Text("...", modifier = Modifier.padding(horizontal = 8.dp), color = Color.Gray)
                }
                PageButton(totalPages.toString()) { onPageChange(totalPages) }
            }

            if (currentPage < totalPages) {
                PageButton(">") { onPageChange(currentPage + 1) }
            }
        }
    }
}

@Composable
private fun PageButton(text: String, isSelected: Boolean = false, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(enabled = !isSelected) { onClick() }

            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                shape = RoundedCornerShape(8.dp),
            )
            .clip(
                shape = RoundedCornerShape(8.dp)
            )
            .background(color = DarkBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.primary else DarkPrimary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
