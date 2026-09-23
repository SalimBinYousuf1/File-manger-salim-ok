package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppleTag
import com.example.data.model.FileItem
import com.example.data.model.PREDEFINED_TAGS
import com.example.data.model.ViewMode
import com.example.ui.components.AppleTagSelector
import com.example.ui.components.FileGridCard
import com.example.ui.components.FileListItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun FavoritesScreen(
    favorites: List<FileItem>,
    allItems: List<FileItem>,
    viewMode: ViewMode,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit,
    onItemMenuClick: (FileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTag by remember { mutableStateOf<AppleTag?>(null) }

    val displayedItems = if (selectedTag != null) {
        allItems.filter { it.tagColorHex?.equals(selectedTag?.colorHex, ignoreCase = true) == true }
    } else {
        favorites
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Large Title
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = if (selectedTag != null) "${selectedTag?.name} Tag" else "Favorites & Tags",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppleTextPrimary,
                    fontSize = 32.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quick access to starred and color-tagged items",
                style = MaterialTheme.typography.bodySmall.copy(color = AppleTextSecondary)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Apple Color Tag Row
            AppleTagSelector(
                selectedColorHex = selectedTag?.colorHex,
                onTagSelected = { selectedTag = it }
            )
        }

        if (displayedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (selectedTag != null) Icons.Default.Bookmark else Icons.Default.Star,
                        contentDescription = null,
                        tint = AppleTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedTag != null) "No files tagged with ${selectedTag?.name}" else "No Favorites Yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedTag != null) "Assign the ${selectedTag?.name} tag to files to group them here." else "Tap the star icon on any file or folder to keep it here for fast access.",
                        style = MaterialTheme.typography.bodySmall.copy(color = AppleTextSecondary),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            if (viewMode == ViewMode.GRID) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 104.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize().testTag("favorites_grid")
                ) {
                    items(displayedItems, key = { it.path }) { item ->
                        FileGridCard(
                            item = item,
                            isSelected = false,
                            isSelectionMode = false,
                            onClick = { onItemClick(item) },
                            onLongClick = { onItemLongClick(item) },
                            onMenuClick = { onItemMenuClick(item) }
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().testTag("favorites_list")) {
                    items(displayedItems, key = { it.path }) { item ->
                        FileListItem(
                            item = item,
                            isSelected = false,
                            isSelectionMode = false,
                            onClick = { onItemClick(item) },
                            onLongClick = { onItemLongClick(item) },
                            onMenuClick = { onItemMenuClick(item) }
                        )
                    }
                }
            }
        }
    }
}
