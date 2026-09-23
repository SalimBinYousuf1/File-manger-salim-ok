package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.SortOption
import com.example.data.model.ViewMode
import com.example.ui.Breadcrumb
import com.example.ui.SalimUiState
import com.example.ui.components.AppleFolderIcon
import com.example.ui.components.AppleSearchBar
import com.example.ui.components.FileGridCard
import com.example.ui.components.FileListItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleLightBorder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun BrowseScreen(
    state: SalimUiState,
    onNavigateToDir: (String, String) -> Unit,
    onNavigateUp: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategorySelected: (FileCategory) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onViewModeToggle: () -> Unit,
    onToggleSelectMode: () -> Unit,
    onToggleItemSelection: (String) -> Unit,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit,
    onItemMenuClick: (FileItem) -> Unit,
    onNewFolder: () -> Unit,
    onNewFile: () -> Unit,
    onDeleteSelected: () -> Unit,
    onMoveSelected: () -> Unit,
    onZipSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Header & Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Title & Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Back button or Folder title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (state.breadcrumbs.size > 1) {
                        IconButton(
                            onClick = onNavigateUp,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("browse_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = AppleBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = state.currentFolderName,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppleTextPrimary,
                            fontSize = if (state.breadcrumbs.size > 1) 24.sp else 30.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right Top Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // View Mode Toggle (Grid / List)
                    IconButton(
                        onClick = onViewModeToggle,
                        modifier = Modifier.testTag("view_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (state.viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Toggle View",
                            tint = AppleBlue
                        )
                    }

                    // Sort menu
                    Box {
                        IconButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier.testTag("sort_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = AppleBlue
                            )
                        }

                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (state.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            color = if (state.sortOption == option) AppleBlue else AppleTextPrimary
                                        )
                                    },
                                    onClick = {
                                        onSortSelected(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Select mode toggle
                    TextButton(
                        onClick = onToggleSelectMode,
                        modifier = Modifier.testTag("select_mode_button")
                    ) {
                        Text(
                            text = if (state.isSelectionMode) "Done" else "Select",
                            color = AppleBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Breadcrumbs Trail (Apple Style)
            if (state.breadcrumbs.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.breadcrumbs.forEachIndexed { idx, crumb ->
                        val isLast = idx == state.breadcrumbs.lastIndex
                        Text(
                            text = crumb.name,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isLast) AppleTextPrimary else AppleBlue,
                                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable(!isLast) {
                                onNavigateToDir(crumb.path, crumb.name)
                            }
                        )
                        if (!isLast) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color(0xFFC7C7CC),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Apple Search Input
            AppleSearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchChange
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FileCategory.values().forEach { cat ->
                    val isSelected = state.selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AppleBlue else Color(0xFFF2F2F7))
                            .clickable { onCategorySelected(cat) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("category_filter_${cat.name}")
                    ) {
                        Text(
                            text = cat.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else AppleTextPrimary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }

        // Selection Mode Action Bar
        AnimatedVisibility(visible = state.isSelectionMode) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9F9FB))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.selectedPaths.size} selected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AppleTextPrimary
                        )
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onSelectAll) {
                            Text("Select All", color = AppleBlue, fontSize = 14.sp)
                        }
                        TextButton(onClick = onClearSelection) {
                            Text("Clear", color = AppleBlue, fontSize = 14.sp)
                        }
                    }
                }

                if (state.selectedPaths.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F4F8))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = onMoveSelected, modifier = Modifier.testTag("batch_move_button")) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = "Move", tint = AppleBlue)
                        }
                        IconButton(onClick = onZipSelected, modifier = Modifier.testTag("batch_zip_button")) {
                            Icon(Icons.Default.Archive, contentDescription = "Compress", tint = AppleBlue)
                        }
                        IconButton(onClick = onDeleteSelected, modifier = Modifier.testTag("batch_delete_button")) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30))
                        }
                    }
                }
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp)
            }
        }

        HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp)

        // Main File List / Grid Canvas
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (state.displayedItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AppleFolderIcon(size = 72.dp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "No matching files" else "This Folder is Empty",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AppleTextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (state.searchQuery.isNotEmpty()) "Try searching for something else" else "Tap '+' below to create a new folder or file.",
                            style = MaterialTheme.typography.bodySmall.copy(color = AppleTextSecondary),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onNewFolder,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F7), contentColor = AppleBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Folder", fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = onNewFile,
                                colors = ButtonDefaults.buttonColors(containerColor = AppleBlue, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.NoteAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New File", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                if (state.viewMode == ViewMode.GRID) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 104.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().testTag("browse_grid")
                    ) {
                        items(state.displayedItems, key = { it.path }) { item ->
                            val isSelected = state.selectedPaths.contains(item.path)
                            FileGridCard(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = state.isSelectionMode,
                                onClick = {
                                    if (state.isSelectionMode) {
                                        onToggleItemSelection(item.path)
                                    } else {
                                        onItemClick(item)
                                    }
                                },
                                onLongClick = {
                                    if (!state.isSelectionMode) onItemLongClick(item)
                                },
                                onMenuClick = { onItemMenuClick(item) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag("browse_list")
                    ) {
                        items(state.displayedItems, key = { it.path }) { item ->
                            val isSelected = state.selectedPaths.contains(item.path)
                            FileListItem(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = state.isSelectionMode,
                                onClick = {
                                    if (state.isSelectionMode) {
                                        onToggleItemSelection(item.path)
                                    } else {
                                        onItemClick(item)
                                    }
                                },
                                onLongClick = {
                                    if (!state.isSelectionMode) onItemLongClick(item)
                                },
                                onMenuClick = { onItemMenuClick(item) }
                            )
                        }
                    }
                }
            }

            // Floating Quick Action Buttons at bottom right
            if (!state.isSelectionMode) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingActionButton(
                        onClick = onNewFolder,
                        containerColor = Color.White,
                        contentColor = AppleBlue,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("floating_new_folder_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder",
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = onNewFile,
                        containerColor = AppleBlue,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("floating_new_file_fab")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Document",
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
