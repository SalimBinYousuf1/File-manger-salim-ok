package com.example.data.repository

import android.content.Context
import android.os.Environment
import com.example.data.db.FileMetaDao
import com.example.data.db.FileMetaEntity
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.util.FileUtils
import com.example.util.StorageSpaceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FileManagerRepository(
    private val context: Context,
    private val dao: FileMetaDao
) {
    val defaultRoot: File by lazy {
        val dir = File(context.filesDir, "SalimDocuments")
        if (!dir.exists()) dir.mkdirs()
        FileUtils.seedSampleWorkspace(dir)
        dir
    }

    val externalRoot: File? by lazy {
        try {
            val ext = Environment.getExternalStorageDirectory()
            if (ext != null && ext.exists()) ext else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getFilesForDirectory(dirPath: String): List<FileItem> = withContext(Dispatchers.IO) {
        val dir = File(dirPath)
        if (!dir.exists() || !dir.isDirectory) return@withContext emptyList()

        val rawFiles = dir.listFiles() ?: return@withContext emptyList()
        val allMetas = dao.getAllMetas().first().associateBy { it.path }

        rawFiles
            .filter { !it.name.startsWith(".") } // Filter out hidden dot-files
            .map { file ->
                val meta = allMetas[file.absolutePath]
                val isDir = file.isDirectory
                val childCount = if (isDir) (file.listFiles()?.size ?: 0) else 0

                FileItem(
                    id = file.absolutePath,
                    name = file.name,
                    path = file.absolutePath,
                    parentPath = file.parent ?: "",
                    isDirectory = isDir,
                    size = if (isDir) 0L else file.length(),
                    lastModified = file.lastModified(),
                    extension = file.extension,
                    mimeType = FileUtils.getMimeType(file),
                    itemCount = childCount,
                    isFavorite = meta?.isFavorite ?: false,
                    tagColorHex = meta?.tagColorHex,
                    tagName = meta?.tagName,
                    isInTrash = meta?.isInTrash ?: false,
                    deletedAt = meta?.deletedAt
                )
            }
            .filter { !it.isInTrash }
    }

    fun observeFavorites(): Flow<List<FileItem>> {
        return dao.getFavorites().map { metaList ->
            withContext(Dispatchers.IO) {
                metaList.mapNotNull { meta ->
                    val file = File(meta.path)
                    if (file.exists()) {
                        val isDir = file.isDirectory
                        val childCount = if (isDir) (file.listFiles()?.size ?: 0) else 0
                        FileItem(
                            id = file.absolutePath,
                            name = file.name,
                            path = file.absolutePath,
                            parentPath = file.parent ?: "",
                            isDirectory = isDir,
                            size = if (isDir) 0L else file.length(),
                            lastModified = file.lastModified(),
                            extension = file.extension,
                            mimeType = FileUtils.getMimeType(file),
                            itemCount = childCount,
                            isFavorite = true,
                            tagColorHex = meta.tagColorHex,
                            tagName = meta.tagName,
                            isInTrash = meta.isInTrash
                        )
                    } else null
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun observeRecents(limit: Int = 40): Flow<List<FileItem>> {
        return dao.getRecentlyOpened(limit).map { metaList ->
            withContext(Dispatchers.IO) {
                val items = metaList.mapNotNull { meta ->
                    val file = File(meta.path)
                    if (file.exists()) {
                        val isDir = file.isDirectory
                        val childCount = if (isDir) (file.listFiles()?.size ?: 0) else 0
                        FileItem(
                            id = file.absolutePath,
                            name = file.name,
                            path = file.absolutePath,
                            parentPath = file.parent ?: "",
                            isDirectory = isDir,
                            size = if (isDir) 0L else file.length(),
                            lastModified = meta.lastOpenedAt.takeIf { it > 0 } ?: file.lastModified(),
                            extension = file.extension,
                            mimeType = FileUtils.getMimeType(file),
                            itemCount = childCount,
                            isFavorite = meta.isFavorite,
                            tagColorHex = meta.tagColorHex,
                            tagName = meta.tagName,
                            isInTrash = meta.isInTrash
                        )
                    } else null
                }

                // If recent opened is small, complement with latest modified files in Salim workspace
                if (items.size < 6) {
                    val allFiles = mutableListOf<File>()
                    fun scan(d: File) {
                        d.listFiles()?.forEach { f ->
                            if (!f.name.startsWith(".")) {
                                if (f.isDirectory) scan(f) else allFiles.add(f)
                            }
                        }
                    }
                    scan(defaultRoot)
                    val recentFiles = allFiles
                        .sortedByDescending { it.lastModified() }
                        .take(20)
                        .map { f ->
                            FileItem(
                                id = f.absolutePath,
                                name = f.name,
                                path = f.absolutePath,
                                parentPath = f.parent ?: "",
                                isDirectory = false,
                                size = f.length(),
                                lastModified = f.lastModified(),
                                extension = f.extension,
                                mimeType = FileUtils.getMimeType(f),
                                itemCount = 0
                            )
                        }
                    (items + recentFiles).distinctBy { it.path }
                } else {
                    items
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    fun observeTrash(): Flow<List<FileItem>> {
        return dao.getTrashItems().map { metaList ->
            withContext(Dispatchers.IO) {
                metaList.mapNotNull { meta ->
                    val file = File(meta.path)
                    if (file.exists()) {
                        FileItem(
                            id = file.absolutePath,
                            name = file.name,
                            path = file.absolutePath,
                            parentPath = file.parent ?: "",
                            isDirectory = file.isDirectory,
                            size = if (file.isDirectory) 0L else file.length(),
                            lastModified = meta.deletedAt ?: file.lastModified(),
                            extension = file.extension,
                            mimeType = FileUtils.getMimeType(file),
                            itemCount = if (file.isDirectory) (file.listFiles()?.size ?: 0) else 0,
                            isFavorite = meta.isFavorite,
                            tagColorHex = meta.tagColorHex,
                            tagName = meta.tagName,
                            isInTrash = true,
                            deletedAt = meta.deletedAt
                        )
                    } else null
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun createFolder(parentPath: String, folderName: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val safeName = folderName.trim()
            if (safeName.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Name cannot be empty"))
            val target = File(parentPath, safeName)
            if (target.exists()) return@withContext Result.failure(IllegalStateException("Folder already exists"))
            if (!target.mkdirs()) return@withContext Result.failure(IllegalStateException("Failed to create folder"))

            val item = FileItem(
                id = target.absolutePath,
                name = target.name,
                path = target.absolutePath,
                parentPath = parentPath,
                isDirectory = true,
                size = 0L,
                lastModified = target.lastModified(),
                extension = "",
                mimeType = "resource/folder",
                itemCount = 0
            )
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFile(parentPath: String, fileName: String, content: String = ""): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val safeName = fileName.trim()
            if (safeName.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Name cannot be empty"))
            val target = File(parentPath, safeName)
            if (target.exists()) return@withContext Result.failure(IllegalStateException("File already exists"))
            target.writeText(content)

            val item = FileItem(
                id = target.absolutePath,
                name = target.name,
                path = target.absolutePath,
                parentPath = parentPath,
                isDirectory = false,
                size = target.length(),
                lastModified = target.lastModified(),
                extension = target.extension,
                mimeType = FileUtils.getMimeType(target),
                itemCount = 0
            )
            recordOpened(target.absolutePath)
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun renameItem(oldPath: String, newName: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val source = File(oldPath)
            if (!source.exists()) return@withContext Result.failure(NoSuchElementException("File not found"))
            val cleanName = newName.trim()
            if (cleanName.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Invalid name"))
            val dest = File(source.parentFile, cleanName)
            if (dest.exists()) return@withContext Result.failure(IllegalStateException("Name already in use"))
            val success = source.renameTo(dest)
            if (!success) return@withContext Result.failure(IllegalStateException("Rename failed"))

            // Update database metadata if present
            val meta = dao.getMetaByPath(oldPath)
            if (meta != null) {
                dao.delete(oldPath)
                dao.insertOrUpdate(meta.copy(path = dest.absolutePath))
            }

            val item = FileItem(
                id = dest.absolutePath,
                name = dest.name,
                path = dest.absolutePath,
                parentPath = dest.parent ?: "",
                isDirectory = dest.isDirectory,
                size = if (dest.isDirectory) 0L else dest.length(),
                lastModified = dest.lastModified(),
                extension = dest.extension,
                mimeType = FileUtils.getMimeType(dest),
                itemCount = if (dest.isDirectory) (dest.listFiles()?.size ?: 0) else 0,
                isFavorite = meta?.isFavorite ?: false,
                tagColorHex = meta?.tagColorHex,
                tagName = meta?.tagName
            )
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun moveItem(sourcePath: String, destDirPath: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val source = File(sourcePath)
            val destDir = File(destDirPath)
            if (!source.exists() || !destDir.exists() || !destDir.isDirectory) {
                return@withContext Result.failure(IllegalArgumentException("Invalid source or destination"))
            }
            val destFile = File(destDir, source.name)
            val success = source.renameTo(destFile)
            if (!success) {
                // Try copy + delete fallback
                if (source.isDirectory) {
                    source.copyRecursively(destFile, overwrite = true)
                    source.deleteRecursively()
                } else {
                    source.copyTo(destFile, overwrite = true)
                    source.delete()
                }
            }

            val meta = dao.getMetaByPath(sourcePath)
            if (meta != null) {
                dao.delete(sourcePath)
                dao.insertOrUpdate(meta.copy(path = destFile.absolutePath))
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun duplicateItem(sourcePath: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val source = File(sourcePath)
            if (!source.exists()) return@withContext Result.failure(NoSuchElementException("File not found"))

            val baseName = source.nameWithoutExtension
            val ext = if (source.extension.isNotEmpty()) ".${source.extension}" else ""
            var copyIndex = 1
            var destFile: File
            do {
                val candidateName = "$baseName copy${if (copyIndex > 1) " $copyIndex" else ""}$ext"
                destFile = File(source.parentFile, candidateName)
                copyIndex++
            } while (destFile.exists())

            if (source.isDirectory) {
                source.copyRecursively(destFile)
            } else {
                source.copyTo(destFile)
            }

            val item = FileItem(
                id = destFile.absolutePath,
                name = destFile.name,
                path = destFile.absolutePath,
                parentPath = destFile.parent ?: "",
                isDirectory = destFile.isDirectory,
                size = if (destFile.isDirectory) 0L else destFile.length(),
                lastModified = destFile.lastModified(),
                extension = destFile.extension,
                mimeType = FileUtils.getMimeType(destFile),
                itemCount = if (destFile.isDirectory) (destFile.listFiles()?.size ?: 0) else 0
            )
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun moveToTrash(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getMetaByPath(path) ?: FileMetaEntity(path = path)
            dao.insertOrUpdate(existing.copy(isInTrash = true, deletedAt = System.currentTimeMillis()))
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreFromTrash(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getMetaByPath(path)
            if (existing != null) {
                dao.insertOrUpdate(existing.copy(isInTrash = false, deletedAt = null))
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePermanently(path: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val file = File(path)
            if (file.exists()) {
                if (file.isDirectory) file.deleteRecursively() else file.delete()
            }
            dao.delete(path)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun emptyTrash(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val trashItems = dao.getTrashItems().first()
            for (item in trashItems) {
                val file = File(item.path)
                if (file.exists()) {
                    if (file.isDirectory) file.deleteRecursively() else file.delete()
                }
            }
            dao.emptyTrash()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(path: String, current: Boolean) = withContext(Dispatchers.IO) {
        val existing = dao.getMetaByPath(path) ?: FileMetaEntity(path = path)
        dao.insertOrUpdate(existing.copy(isFavorite = !current))
    }

    suspend fun setTag(path: String, colorHex: String?, tagName: String?) = withContext(Dispatchers.IO) {
        val existing = dao.getMetaByPath(path) ?: FileMetaEntity(path = path)
        dao.insertOrUpdate(existing.copy(tagColorHex = colorHex, tagName = tagName))
    }

    suspend fun recordOpened(path: String) = withContext(Dispatchers.IO) {
        val existing = dao.getMetaByPath(path) ?: FileMetaEntity(path = path)
        dao.insertOrUpdate(existing.copy(lastOpenedAt = System.currentTimeMillis()))
    }

    suspend fun readText(path: String): String = withContext(Dispatchers.IO) {
        File(path).readText()
    }

    suspend fun writeText(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(path).writeText(content)
            recordOpened(path)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun compressZip(sourcePaths: List<String>, zipName: String, targetDir: String): Result<FileItem> = withContext(Dispatchers.IO) {
        try {
            val safeName = if (zipName.endsWith(".zip", ignoreCase = true)) zipName else "$zipName.zip"
            val destZip = File(targetDir, safeName)
            val filesToZip = sourcePaths.map { File(it) }.filter { it.exists() }
            val ok = FileUtils.zipFiles(filesToZip, destZip)
            if (ok && destZip.exists()) {
                val item = FileItem(
                    id = destZip.absolutePath,
                    name = destZip.name,
                    path = destZip.absolutePath,
                    parentPath = targetDir,
                    isDirectory = false,
                    size = destZip.length(),
                    lastModified = destZip.lastModified(),
                    extension = "zip",
                    mimeType = "application/zip",
                    itemCount = 0
                )
                Result.success(item)
            } else {
                Result.failure(IllegalStateException("Failed to create ZIP"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun extractZip(zipPath: String, targetDir: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val zipFile = File(zipPath)
            val outDir = File(targetDir)
            val ok = FileUtils.unzip(zipFile, outDir)
            if (ok) Result.success(true) else Result.failure(IllegalStateException("Failed to extract ZIP"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStorageInfo(): StorageSpaceInfo = withContext(Dispatchers.IO) {
        FileUtils.calculateStorageSpace(context, defaultRoot)
    }

    suspend fun getAllFoldersTree(rootDir: File = defaultRoot): List<FileItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<FileItem>()
        fun scan(d: File) {
            if (!d.exists() || !d.isDirectory || d.name.startsWith(".")) return
            val children = d.listFiles() ?: return
            result.add(
                FileItem(
                    id = d.absolutePath,
                    name = d.name,
                    path = d.absolutePath,
                    parentPath = d.parent ?: "",
                    isDirectory = true,
                    size = 0L,
                    lastModified = d.lastModified(),
                    extension = "",
                    mimeType = "resource/folder",
                    itemCount = children.size
                )
            )
            children.filter { it.isDirectory && !it.name.startsWith(".") }.forEach { scan(it) }
        }
        scan(rootDir)
        result
    }
}
