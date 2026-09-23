package com.example.data.model

import java.io.File

enum class FileCategory(val label: String) {
    ALL("All"),
    DOCUMENTS("Documents"),
    IMAGES("Images"),
    AUDIO("Audio"),
    VIDEOS("Videos"),
    ARCHIVES("Archives")
}

enum class SortOption(val label: String) {
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    DATE_DESC("Date Modified (Newest)"),
    DATE_ASC("Date Modified (Oldest)"),
    SIZE_DESC("Size (Largest)"),
    SIZE_ASC("Size (Smallest)"),
    KIND("Kind")
}

enum class ViewMode {
    GRID,
    LIST
}

enum class NavigationTab(val label: String) {
    RECENTS("Recents"),
    BROWSE("Browse"),
    FAVORITES("Favorites"),
    STORAGE("Storage")
}

data class AppleTag(
    val id: String,
    val name: String,
    val colorHex: String
)

val PREDEFINED_TAGS = listOf(
    AppleTag("red", "Urgent", "#FF3B30"),
    AppleTag("orange", "Review", "#FF9500"),
    AppleTag("yellow", "Pending", "#FFCC00"),
    AppleTag("green", "Approved", "#34C759"),
    AppleTag("blue", "Work", "#007AFF"),
    AppleTag("purple", "Personal", "#AF52DE"),
    AppleTag("gray", "Archive", "#8E8E93")
)

data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val parentPath: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val extension: String,
    val mimeType: String,
    val itemCount: Int = 0,
    val isFavorite: Boolean = false,
    val tagColorHex: String? = null,
    val tagName: String? = null,
    val isInTrash: Boolean = false,
    val deletedAt: Long? = null
) {
    val file: File
        get() = File(path)

    val formattedSize: String
        get() {
            if (isDirectory) {
                return if (itemCount == 1) "1 item" else "$itemCount items"
            }
            if (size <= 0) return "0 B"
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
            val value = size / Math.pow(1024.0, digitGroups.toDouble())
            return if (digitGroups == 0) "$size B" else String.format("%.1f %s", value, units[digitGroups])
        }

    val category: FileCategory
        get() {
            if (isDirectory) return FileCategory.ALL
            val ext = extension.lowercase()
            return when (ext) {
                "txt", "md", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "csv", "json", "xml", "html" -> FileCategory.DOCUMENTS
                "jpg", "jpeg", "png", "webp", "gif", "svg", "bmp", "heic" -> FileCategory.IMAGES
                "mp3", "wav", "m4a", "aac", "flac", "ogg" -> FileCategory.AUDIO
                "mp4", "mkv", "mov", "webm", "avi" -> FileCategory.VIDEOS
                "zip", "tar", "gz", "rar", "7z" -> FileCategory.ARCHIVES
                else -> FileCategory.ALL
            }
        }
}
