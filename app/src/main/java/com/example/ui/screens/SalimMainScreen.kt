package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.FileItem
import com.example.data.model.NavigationTab
import com.example.data.model.ViewMode
import com.example.ui.SalimViewModel
import com.example.ui.components.AppleActionSheet
import com.example.ui.components.AppleToastPill
import com.example.ui.components.FolderPickerModal
import com.example.ui.components.GetInfoModal
import com.example.ui.components.NewFileDialog
import com.example.ui.components.NewFolderDialog
import com.example.ui.components.QuickLookModal
import com.example.ui.components.RenameDialog
import com.example.ui.components.SalimBottomBar
import com.example.ui.components.TrashBinModal
import com.example.ui.components.ZipArchiveDialog
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalimMainScreen(
    viewModel: SalimViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Active item for Action Sheet
    var actionSheetItem by remember { mutableStateOf<FileItem?>(null) }

    fun shareFile(item: FileItem) {
        try {
            val file = File(item.path)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = item.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share ${item.name}"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            SalimBottomBar(
                currentTab = state.currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            when (state.currentTab) {
                NavigationTab.BROWSE -> {
                    BrowseScreen(
                        state = state,
                        onNavigateToDir = { path, name -> viewModel.navigateToDirectory(path, name) },
                        onNavigateUp = { viewModel.navigateUp() },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategorySelected = { viewModel.setCategoryFilter(it) },
                        onSortSelected = { viewModel.setSortOption(it) },
                        onViewModeToggle = {
                            viewModel.setViewMode(
                                if (state.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                            )
                        },
                        onToggleSelectMode = { viewModel.toggleSelectionMode() },
                        onToggleItemSelection = { viewModel.toggleItemSelection(it) },
                        onSelectAll = { viewModel.selectAll() },
                        onClearSelection = { viewModel.clearSelection() },
                        onItemClick = { viewModel.openItem(it) },
                        onItemLongClick = { actionSheetItem = it },
                        onItemMenuClick = { actionSheetItem = it },
                        onNewFolder = { viewModel.showCreateFolderDialog() },
                        onNewFile = { viewModel.showCreateFileDialog() },
                        onDeleteSelected = { viewModel.deleteSelectedItems() },
                        onMoveSelected = { viewModel.showMoveSheet() },
                        onZipSelected = { viewModel.showZipDialog() }
                    )
                }

                NavigationTab.RECENTS -> {
                    RecentsScreen(
                        recents = state.recents,
                        viewMode = state.viewMode,
                        onItemClick = { viewModel.openItem(it) },
                        onItemLongClick = { actionSheetItem = it },
                        onItemMenuClick = { actionSheetItem = it }
                    )
                }

                NavigationTab.FAVORITES -> {
                    FavoritesScreen(
                        favorites = state.favorites,
                        allItems = state.items,
                        viewMode = state.viewMode,
                        onItemClick = { viewModel.openItem(it) },
                        onItemLongClick = { actionSheetItem = it },
                        onItemMenuClick = { actionSheetItem = it }
                    )
                }

                NavigationTab.STORAGE -> {
                    StorageScreen(
                        storageInfo = state.storageInfo,
                        trashCount = state.trashItems.size,
                        onOpenTrash = { viewModel.showTrashSheet() },
                        onRefresh = { viewModel.refreshStorageInfo() }
                    )
                }
            }

            // Apple Toast Banner
            AppleToastPill(
                message = state.statusMessage,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // --- Action Sheet ---
    actionSheetItem?.let { item ->
        AppleActionSheet(
            item = item,
            onDismiss = { actionSheetItem = null },
            onQuickLook = { viewModel.openItem(item) },
            onGetInfo = { viewModel.showInfoSheet(item) },
            onRename = { viewModel.showRenameDialog(item) },
            onDuplicate = { viewModel.duplicateItem(item) },
            onMove = { viewModel.showMoveSheet(item) },
            onTags = { viewModel.showTagSheet(item) },
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onShare = { shareFile(item) },
            onCompress = { viewModel.createZipArchive("${item.name}.zip") },
            onExtract = { viewModel.extractZip(item) },
            onDelete = { viewModel.deleteItem(item) }
        )
    }

    // --- Quick Look Modal ---
    state.activeQuickLookItem?.let { item ->
        QuickLookModal(
            item = item,
            textContent = state.quickLookTextContent,
            isTextDirty = state.isQuickLookTextDirty,
            audioState = state.audioState,
            onTextChange = { viewModel.updateQuickLookText(it) },
            onSaveText = { viewModel.saveQuickLookChanges() },
            onToggleAudioPlay = { viewModel.toggleAudioPlayPause() },
            onSeekAudio = { viewModel.seekAudio(it) },
            onShare = { shareFile(item) },
            onDismiss = { viewModel.dismissQuickLook() }
        )
    }

    // --- Get Info Inspector ---
    state.activeInfoItem?.let { item ->
        GetInfoModal(
            item = item,
            onDismiss = { viewModel.dismissInfoSheet() },
            onTagSelected = { viewModel.assignTag(item, it) },
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onShare = { shareFile(item) },
            onDuplicate = { viewModel.duplicateItem(item) },
            onDelete = { viewModel.deleteItem(item); viewModel.dismissInfoSheet() }
        )
    }

    // --- Tag Picker (from action sheet) ---
    state.activeTagItem?.let { item ->
        GetInfoModal(
            item = item,
            onDismiss = { viewModel.dismissTagSheet() },
            onTagSelected = { viewModel.assignTag(item, it) },
            onToggleFavorite = { viewModel.toggleFavorite(item) },
            onShare = { shareFile(item) },
            onDuplicate = { viewModel.duplicateItem(item) },
            onDelete = { viewModel.deleteItem(item); viewModel.dismissTagSheet() }
        )
    }

    // --- Move Folder Picker ---
    if (state.showMoveSheet) {
        FolderPickerModal(
            folders = state.availableFolders,
            onFolderSelected = { viewModel.moveItemsTo(it) },
            onDismiss = { viewModel.dismissMoveSheet() }
        )
    }

    // --- Trash Bin Modal ---
    if (state.showTrashSheet) {
        TrashBinModal(
            trashItems = state.trashItems,
            onRestore = { viewModel.restoreFromTrash(it) },
            onDeletePermanently = { viewModel.deletePermanently(it) },
            onEmptyTrash = { viewModel.emptyTrash() },
            onDismiss = { viewModel.dismissTrashSheet() }
        )
    }

    // --- New Folder Dialog ---
    if (state.showNewFolderDialog) {
        NewFolderDialog(
            onConfirm = { viewModel.createFolder(it) },
            onDismiss = { viewModel.dismissCreateFolderDialog() }
        )
    }

    // --- New File Dialog ---
    if (state.showNewFileDialog) {
        NewFileDialog(
            onConfirm = { name, content -> viewModel.createFile(name, content) },
            onDismiss = { viewModel.dismissCreateFileDialog() }
        )
    }

    // --- Rename Dialog ---
    state.activeRenameItem?.let { item ->
        RenameDialog(
            item = item,
            onConfirm = { viewModel.renameItem(item, it) },
            onDismiss = { viewModel.dismissRenameDialog() }
        )
    }

    // --- Zip Archive Dialog ---
    if (state.showZipDialog) {
        ZipArchiveDialog(
            onConfirm = { viewModel.createZipArchive(it) },
            onDismiss = { viewModel.dismissZipDialog() }
        )
    }
}
