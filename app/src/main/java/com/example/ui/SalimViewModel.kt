package com.example.ui

import android.app.Application
import android.media.MediaPlayer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SalimDatabase
import com.example.data.model.AppleTag
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.data.model.NavigationTab
import com.example.data.model.PREDEFINED_TAGS
import com.example.data.model.SortOption
import com.example.data.model.ViewMode
import com.example.data.repository.FileManagerRepository
import com.example.util.StorageSpaceInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class Breadcrumb(
    val name: String,
    val path: String
)

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val fileName: String = ""
)

data class SalimUiState(
    val currentTab: NavigationTab = NavigationTab.BROWSE,
    val currentPath: String = "",
    val currentFolderName: String = "Salim Documents",
    val breadcrumbs: List<Breadcrumb> = emptyList(),
    val items: List<FileItem> = emptyList(),
    val displayedItems: List<FileItem> = emptyList(),
    val recents: List<FileItem> = emptyList(),
    val favorites: List<FileItem> = emptyList(),
    val trashItems: List<FileItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: FileCategory = FileCategory.ALL,
    val selectedTagFilter: AppleTag? = null,
    val sortOption: SortOption = SortOption.DATE_DESC,
    val viewMode: ViewMode = ViewMode.GRID,
    val isSelectionMode: Boolean = false,
    val selectedPaths: Set<String> = emptySet(),
    val storageInfo: StorageSpaceInfo? = null,
    val isLoading: Boolean = false,
    // Active Modals & Sheets
    val activeQuickLookItem: FileItem? = null,
    val quickLookTextContent: String = "",
    val isQuickLookTextDirty: Boolean = false,
    val activeInfoItem: FileItem? = null,
    val activeRenameItem: FileItem? = null,
    val activeTagItem: FileItem? = null,
    val showNewFolderDialog: Boolean = false,
    val showNewFileDialog: Boolean = false,
    val showMoveSheet: Boolean = false,
    val moveTargetItem: FileItem? = null,
    val availableFolders: List<FileItem> = emptyList(),
    val showTrashSheet: Boolean = false,
    val showZipDialog: Boolean = false,
    val audioState: AudioPlayerState = AudioPlayerState(),
    val statusMessage: String? = null
)

class SalimViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FileManagerRepository by lazy {
        val db = SalimDatabase.getInstance(application)
        FileManagerRepository(application, db.fileMetaDao())
    }

    private val _uiState = MutableStateFlow(SalimUiState())
    val uiState: StateFlow<SalimUiState> = _uiState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var audioProgressJob: Job? = null

    init {
        val root = repository.defaultRoot
        _uiState.update {
            it.copy(
                currentPath = root.absolutePath,
                currentFolderName = "Salim",
                breadcrumbs = listOf(Breadcrumb("Salim", root.absolutePath))
            )
        }
        refreshCurrentDirectory()
        observeFavoritesAndRecents()
        refreshStorageInfo()
    }

    private fun observeFavoritesAndRecents() {
        viewModelScope.launch {
            repository.observeFavorites().collectLatest { favs ->
                _uiState.update { it.copy(favorites = favs) }
            }
        }
        viewModelScope.launch {
            repository.observeRecents().collectLatest { recs ->
                _uiState.update { it.copy(recents = recs) }
            }
        }
        viewModelScope.launch {
            repository.observeTrash().collectLatest { trash ->
                _uiState.update { it.copy(trashItems = trash) }
            }
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(currentTab = tab, isSelectionMode = false, selectedPaths = emptySet()) }
        if (tab == NavigationTab.STORAGE) {
            refreshStorageInfo()
        } else if (tab == NavigationTab.BROWSE) {
            refreshCurrentDirectory()
        }
    }

    fun navigateToDirectory(folderPath: String, folderName: String) {
        val root = repository.defaultRoot.absolutePath
        val currentBreadcrumbs = _uiState.value.breadcrumbs.toMutableList()

        // Recompute breadcrumbs hierarchy
        val newBreadcrumbs = mutableListOf<Breadcrumb>()
        var curr = File(folderPath)
        val pathChain = mutableListOf<File>()
        while (curr.exists()) {
            pathChain.add(curr)
            if (curr.absolutePath == root || curr.parentFile == null) break
            curr = curr.parentFile ?: break
        }
        pathChain.reverse()
        for (f in pathChain) {
            val label = if (f.absolutePath == root) "Salim" else f.name
            newBreadcrumbs.add(Breadcrumb(label, f.absolutePath))
        }

        _uiState.update {
            it.copy(
                currentPath = folderPath,
                currentFolderName = if (folderPath == root) "Salim" else folderName,
                breadcrumbs = newBreadcrumbs,
                isSelectionMode = false,
                selectedPaths = emptySet(),
                searchQuery = "",
                selectedCategory = FileCategory.ALL,
                selectedTagFilter = null
            )
        }
        refreshCurrentDirectory()
    }

    fun navigateUp(): Boolean {
        val state = _uiState.value
        val root = repository.defaultRoot.absolutePath
        if (state.currentPath == root) return false

        val currentFile = File(state.currentPath)
        val parent = currentFile.parentFile ?: return false
        navigateToDirectory(parent.absolutePath, parent.name)
        return true
    }

    fun refreshCurrentDirectory() {
        val path = _uiState.value.currentPath
        if (path.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val rawItems = repository.getFilesForDirectory(path)
            _uiState.update { state ->
                val filtered = applyFilterAndSort(rawItems, state.searchQuery, state.selectedCategory, state.selectedTagFilter, state.sortOption)
                state.copy(items = rawItems, displayedItems = filtered, isLoading = false)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.items, query, state.selectedCategory, state.selectedTagFilter, state.sortOption)
            state.copy(searchQuery = query, displayedItems = filtered)
        }
    }

    fun setCategoryFilter(category: FileCategory) {
        _uiState.update { state ->
            val newCategory = if (state.selectedCategory == category) FileCategory.ALL else category
            val filtered = applyFilterAndSort(state.items, state.searchQuery, newCategory, state.selectedTagFilter, state.sortOption)
            state.copy(selectedCategory = newCategory, displayedItems = filtered)
        }
    }

    fun setTagFilter(tag: AppleTag?) {
        _uiState.update { state ->
            val newTag = if (state.selectedTagFilter?.id == tag?.id) null else tag
            val filtered = applyFilterAndSort(state.items, state.searchQuery, state.selectedCategory, newTag, state.sortOption)
            state.copy(selectedTagFilter = newTag, displayedItems = filtered)
        }
    }

    fun setSortOption(sort: SortOption) {
        _uiState.update { state ->
            val filtered = applyFilterAndSort(state.items, state.searchQuery, state.selectedCategory, state.selectedTagFilter, sort)
            state.copy(sortOption = sort, displayedItems = filtered)
        }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun toggleSelectionMode() {
        _uiState.update {
            it.copy(isSelectionMode = !it.isSelectionMode, selectedPaths = emptySet())
        }
    }

    fun toggleItemSelection(path: String) {
        _uiState.update { state ->
            val newSelection = state.selectedPaths.toMutableSet()
            if (newSelection.contains(path)) {
                newSelection.remove(path)
            } else {
                newSelection.add(path)
            }
            state.copy(selectedPaths = newSelection)
        }
    }

    fun selectAll() {
        _uiState.update { state ->
            val allPaths = state.displayedItems.map { it.path }.toSet()
            state.copy(selectedPaths = allPaths)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedPaths = emptySet(), isSelectionMode = false) }
    }

    private fun applyFilterAndSort(
        items: List<FileItem>,
        query: String,
        category: FileCategory,
        tag: AppleTag?,
        sort: SortOption
    ): List<FileItem> {
        return items
            .filter { item ->
                if (query.isNotBlank()) {
                    item.name.contains(query, ignoreCase = true)
                } else true
            }
            .filter { item ->
                if (category != FileCategory.ALL) {
                    !item.isDirectory && item.category == category
                } else true
            }
            .filter { item ->
                if (tag != null) {
                    item.tagColorHex?.equals(tag.colorHex, ignoreCase = true) == true
                } else true
            }
            .sortedWith { a, b ->
                // Keep directories first in normal views
                if (a.isDirectory && !b.isDirectory) return@sortedWith -1
                if (!a.isDirectory && b.isDirectory) return@sortedWith 1

                when (sort) {
                    SortOption.NAME_ASC -> a.name.compareTo(b.name, ignoreCase = true)
                    SortOption.NAME_DESC -> b.name.compareTo(a.name, ignoreCase = true)
                    SortOption.DATE_DESC -> b.lastModified.compareTo(a.lastModified)
                    SortOption.DATE_ASC -> a.lastModified.compareTo(b.lastModified)
                    SortOption.SIZE_DESC -> b.size.compareTo(a.size)
                    SortOption.SIZE_ASC -> a.size.compareTo(b.size)
                    SortOption.KIND -> {
                        val kindA = if (a.isDirectory) "folder" else a.extension
                        val kindB = if (b.isDirectory) "folder" else b.extension
                        kindA.compareTo(kindB, ignoreCase = true)
                    }
                }
            }
    }

    // --- File Actions ---

    fun openItem(item: FileItem) {
        if (item.isDirectory) {
            navigateToDirectory(item.path, item.name)
        } else {
            // Open in Quick Look
            viewModelScope.launch {
                repository.recordOpened(item.path)
                val isText = item.mimeType.startsWith("text") || listOf("txt", "md", "json", "csv", "xml", "html", "kt", "java").contains(item.extension.lowercase())
                val textContent = if (isText) {
                    try { repository.readText(item.path) } catch (e: Exception) { "" }
                } else ""

                if (item.category == FileCategory.AUDIO) {
                    startAudioPlayback(item)
                }

                _uiState.update {
                    it.copy(
                        activeQuickLookItem = item,
                        quickLookTextContent = textContent,
                        isQuickLookTextDirty = false
                    )
                }
            }
        }
    }

    fun dismissQuickLook() {
        stopAudioPlayback()
        _uiState.update {
            it.copy(
                activeQuickLookItem = null,
                quickLookTextContent = "",
                isQuickLookTextDirty = false
            )
        }
    }

    fun updateQuickLookText(newText: String) {
        _uiState.update {
            it.copy(quickLookTextContent = newText, isQuickLookTextDirty = true)
        }
    }

    fun saveQuickLookChanges() {
        val item = _uiState.value.activeQuickLookItem ?: return
        val text = _uiState.value.quickLookTextContent
        viewModelScope.launch {
            val ok = repository.writeText(item.path, text)
            if (ok) {
                showToast("Saved changes to ${item.name}")
                _uiState.update { it.copy(isQuickLookTextDirty = false) }
                refreshCurrentDirectory()
            } else {
                showToast("Failed to save changes")
            }
        }
    }

    // --- Audio Player in Quick Look ---

    private fun startAudioPlayback(item: FileItem) {
        stopAudioPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(item.path)
                prepare()
                start()
                val dur = duration
                _uiState.update {
                    it.copy(
                        audioState = AudioPlayerState(
                            isPlaying = true,
                            currentPositionMs = 0,
                            durationMs = dur,
                            fileName = item.name
                        )
                    )
                }
                setOnCompletionListener {
                    _uiState.update { s -> s.copy(audioState = s.audioState.copy(isPlaying = false, currentPositionMs = 0)) }
                }
            }

            audioProgressJob = viewModelScope.launch {
                while (true) {
                    delay(300)
                    mediaPlayer?.let { mp ->
                        if (mp.isPlaying) {
                            _uiState.update { s ->
                                s.copy(audioState = s.audioState.copy(currentPositionMs = mp.currentPosition))
                            }
                        }
                    } ?: break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Cannot play audio file")
        }
    }

    fun toggleAudioPlayPause() {
        mediaPlayer?.let { mp ->
            if (mp.isPlaying) {
                mp.pause()
                _uiState.update { it.copy(audioState = it.audioState.copy(isPlaying = false)) }
            } else {
                mp.start()
                _uiState.update { it.copy(audioState = it.audioState.copy(isPlaying = true)) }
            }
        }
    }

    fun seekAudio(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _uiState.update { it.copy(audioState = it.audioState.copy(currentPositionMs = positionMs)) }
    }

    private fun stopAudioPlayback() {
        audioProgressJob?.cancel()
        audioProgressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // ignore
        }
        mediaPlayer = null
        _uiState.update { it.copy(audioState = AudioPlayerState()) }
    }

    // --- Create Folder & File ---

    fun showCreateFolderDialog() {
        _uiState.update { it.copy(showNewFolderDialog = true) }
    }

    fun dismissCreateFolderDialog() {
        _uiState.update { it.copy(showNewFolderDialog = false) }
    }

    fun createFolder(name: String) {
        val parent = _uiState.value.currentPath
        viewModelScope.launch {
            val result = repository.createFolder(parent, name)
            if (result.isSuccess) {
                showToast("Folder \"$name\" created")
                dismissCreateFolderDialog()
                refreshCurrentDirectory()
                refreshStorageInfo()
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Error creating folder")
            }
        }
    }

    fun showCreateFileDialog() {
        _uiState.update { it.copy(showNewFileDialog = true) }
    }

    fun dismissCreateFileDialog() {
        _uiState.update { it.copy(showNewFileDialog = false) }
    }

    fun createFile(name: String, initialContent: String = "") {
        val parent = _uiState.value.currentPath
        viewModelScope.launch {
            val result = repository.createFile(parent, name, initialContent)
            if (result.isSuccess) {
                showToast("File \"$name\" created")
                dismissCreateFileDialog()
                refreshCurrentDirectory()
                refreshStorageInfo()
                // Optionally open it in editor
                result.getOrNull()?.let { openItem(it) }
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Error creating file")
            }
        }
    }

    // --- Rename ---

    fun showRenameDialog(item: FileItem) {
        _uiState.update { it.copy(activeRenameItem = item) }
    }

    fun dismissRenameDialog() {
        _uiState.update { it.copy(activeRenameItem = null) }
    }

    fun renameItem(item: FileItem, newName: String) {
        viewModelScope.launch {
            val result = repository.renameItem(item.path, newName)
            if (result.isSuccess) {
                showToast("Renamed to $newName")
                dismissRenameDialog()
                refreshCurrentDirectory()
            } else {
                showToast(result.exceptionOrNull()?.message ?: "Rename failed")
            }
        }
    }

    // --- Move & Duplicate ---

    fun showMoveSheet(item: FileItem? = null) {
        viewModelScope.launch {
            val folders = repository.getAllFoldersTree()
            _uiState.update {
                it.copy(
                    showMoveSheet = true,
                    moveTargetItem = item,
                    availableFolders = folders
                )
            }
        }
    }

    fun dismissMoveSheet() {
        _uiState.update { it.copy(showMoveSheet = false, moveTargetItem = null) }
    }

    fun moveItemsTo(destinationPath: String) {
        val singleItem = _uiState.value.moveTargetItem
        val pathsToMove = if (singleItem != null) {
            listOf(singleItem.path)
        } else {
            _uiState.value.selectedPaths.toList()
        }

        viewModelScope.launch {
            var count = 0
            for (p in pathsToMove) {
                if (repository.moveItem(p, destinationPath).isSuccess) {
                    count++
                }
            }
            showToast("Moved $count item(s)")
            dismissMoveSheet()
            clearSelection()
            refreshCurrentDirectory()
        }
    }

    fun duplicateItem(item: FileItem) {
        viewModelScope.launch {
            val result = repository.duplicateItem(item.path)
            if (result.isSuccess) {
                showToast("Duplicated ${item.name}")
                refreshCurrentDirectory()
            } else {
                showToast("Failed to duplicate item")
            }
        }
    }

    // --- Delete & Trash ---

    fun deleteItem(item: FileItem) {
        viewModelScope.launch {
            val result = repository.moveToTrash(item.path)
            if (result.isSuccess) {
                showToast("Moved \"${item.name}\" to Trash")
                refreshCurrentDirectory()
                refreshStorageInfo()
            } else {
                showToast("Failed to delete item")
            }
        }
    }

    fun deleteSelectedItems() {
        val paths = _uiState.value.selectedPaths.toList()
        viewModelScope.launch {
            var count = 0
            for (p in paths) {
                if (repository.moveToTrash(p).isSuccess) count++
            }
            showToast("Moved $count item(s) to Trash")
            clearSelection()
            refreshCurrentDirectory()
            refreshStorageInfo()
        }
    }

    fun restoreFromTrash(item: FileItem) {
        viewModelScope.launch {
            repository.restoreFromTrash(item.path)
            showToast("Restored \"${item.name}\"")
            refreshCurrentDirectory()
        }
    }

    fun deletePermanently(item: FileItem) {
        viewModelScope.launch {
            repository.deletePermanently(item.path)
            showToast("Permanently deleted \"${item.name}\"")
            refreshStorageInfo()
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            repository.emptyTrash()
            showToast("Trash emptied")
            refreshStorageInfo()
        }
    }

    fun showTrashSheet() {
        _uiState.update { it.copy(showTrashSheet = true) }
    }

    fun dismissTrashSheet() {
        _uiState.update { it.copy(showTrashSheet = false) }
    }

    // --- Favorites & Tags ---

    fun toggleFavorite(item: FileItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item.path, item.isFavorite)
            showToast(if (!item.isFavorite) "Added to Favorites" else "Removed from Favorites")
            refreshCurrentDirectory()
        }
    }

    fun showTagSheet(item: FileItem) {
        _uiState.update { it.copy(activeTagItem = item) }
    }

    fun dismissTagSheet() {
        _uiState.update { it.copy(activeTagItem = null) }
    }

    fun assignTag(item: FileItem, tag: AppleTag?) {
        viewModelScope.launch {
            repository.setTag(item.path, tag?.colorHex, tag?.name)
            dismissTagSheet()
            showToast(if (tag != null) "Tagged with ${tag.name}" else "Tag removed")
            refreshCurrentDirectory()
        }
    }

    // --- Info Inspector ---

    fun showInfoSheet(item: FileItem) {
        _uiState.update { it.copy(activeInfoItem = item) }
    }

    fun dismissInfoSheet() {
        _uiState.update { it.copy(activeInfoItem = null) }
    }

    // --- ZIP Archiving ---

    fun showZipDialog() {
        _uiState.update { it.copy(showZipDialog = true) }
    }

    fun dismissZipDialog() {
        _uiState.update { it.copy(showZipDialog = false) }
    }

    fun createZipArchive(zipName: String) {
        val selected = _uiState.value.selectedPaths.toList()
        val currentDir = _uiState.value.currentPath
        viewModelScope.launch {
            val result = repository.compressZip(selected, zipName, currentDir)
            if (result.isSuccess) {
                showToast("Created ${result.getOrNull()?.name}")
                dismissZipDialog()
                clearSelection()
                refreshCurrentDirectory()
                refreshStorageInfo()
            } else {
                showToast("Failed to create ZIP")
            }
        }
    }

    fun extractZip(item: FileItem) {
        viewModelScope.launch {
            val targetDir = File(item.parentPath, item.nameWithoutExtension).absolutePath
            val result = repository.extractZip(item.path, targetDir)
            if (result.isSuccess) {
                showToast("Extracted to ${item.nameWithoutExtension}")
                refreshCurrentDirectory()
                refreshStorageInfo()
            } else {
                showToast("Failed to extract ZIP")
            }
        }
    }

    private val FileItem.nameWithoutExtension: String
        get() {
            val dot = name.lastIndexOf('.')
            return if (dot > 0) name.substring(0, dot) else name
        }

    // --- Storage Space ---

    fun refreshStorageInfo() {
        viewModelScope.launch {
            val info = repository.getStorageInfo()
            _uiState.update { it.copy(storageInfo = info) }
        }
    }

    // --- Toast / Status Notice ---

    private fun showToast(msg: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = msg) }
            delay(2400)
            _uiState.update { if (it.statusMessage == msg) it.copy(statusMessage = null) else it }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudioPlayback()
    }
}
