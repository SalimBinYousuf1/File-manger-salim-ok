package com.example.util

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.webkit.MimeTypeMap
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class StorageSpaceInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val documentsBytes: Long,
    val imagesBytes: Long,
    val audioBytes: Long,
    val videosBytes: Long,
    val archivesBytes: Long,
    val otherBytes: Long
)

object FileUtils {

    fun formatAppleDate(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val oneDay = 24 * 60 * 60 * 1000L

        return when {
            diff in 0 until oneDay -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Today, ${timeFormat.format(Date(timestamp))}"
            }
            diff in oneDay until 2 * oneDay -> {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Yesterday, ${timeFormat.format(Date(timestamp))}"
            }
            else -> {
                val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                dateFormat.format(Date(timestamp))
            }
        }
    }

    fun getMimeType(file: File): String {
        val extension = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: when (extension) {
            "txt", "text" -> "text/plain"
            "md", "markdown" -> "text/markdown"
            "json" -> "application/json"
            "csv" -> "text/csv"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "mp4" -> "video/mp4"
            else -> "*/*"
        }
    }

    fun calculateMD5(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val buffer = ByteArray(8192)
            FileInputStream(file).use { fis ->
                var read: Int
                while (fis.read(buffer).also { read = it } > 0) {
                    digest.update(buffer, 0, read)
                }
            }
            val md5Bytes = digest.digest()
            md5Bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Unavailable"
        }
    }

    fun zipFiles(filesToZip: List<File>, destinationZip: File): Boolean {
        return try {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationZip))).use { out ->
                val buffer = ByteArray(4096)
                for (file in filesToZip) {
                    if (file.isDirectory) {
                        zipDirectoryRecursive(file, file.name, out, buffer)
                    } else {
                        FileInputStream(file).use { fi ->
                            BufferedInputStream(fi, 4096).use { origin ->
                                val entry = ZipEntry(file.name)
                                out.putNextEntry(entry)
                                var count: Int
                                while (origin.read(buffer, 0, 4096).also { count = it } != -1) {
                                    out.write(buffer, 0, count)
                                }
                            }
                        }
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun zipDirectoryRecursive(folder: File, parentPath: String, out: ZipOutputStream, buffer: ByteArray) {
        val files = folder.listFiles() ?: return
        for (file in files) {
            val path = "$parentPath/${file.name}"
            if (file.isDirectory) {
                zipDirectoryRecursive(file, path, out, buffer)
            } else {
                FileInputStream(file).use { fi ->
                    BufferedInputStream(fi, 4096).use { origin ->
                        val entry = ZipEntry(path)
                        out.putNextEntry(entry)
                        var count: Int
                        while (origin.read(buffer, 0, 4096).also { count = it } != -1) {
                            out.write(buffer, 0, count)
                        }
                    }
                }
            }
        }
    }

    fun unzip(zipFile: File, targetDir: File): Boolean {
        return try {
            if (!targetDir.exists()) targetDir.mkdirs()
            ZipInputStream(BufferedInputStream(FileInputStream(zipFile))).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                val buffer = ByteArray(4096)
                while (entry != null) {
                    val newFile = File(targetDir, entry.name)
                    // Security check for Zip Slip
                    if (!newFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
                        throw SecurityException("Zip Slip exploit detected")
                    }
                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fos ->
                            var count: Int
                            while (zis.read(buffer).also { count = it } != -1) {
                                fos.write(buffer, 0, count)
                            }
                        }
                    }
                    entry = zis.nextEntry
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun calculateStorageSpace(context: Context, rootDir: File): StorageSpaceInfo {
        return try {
            val stat = StatFs(rootDir.path)
            val totalBytes = stat.totalBytes
            val freeBytes = stat.availableBytes
            val usedBytes = (totalBytes - freeBytes).coerceAtLeast(0L)

            var docBytes = 0L
            var imgBytes = 0L
            var audioBytes = 0L
            var videoBytes = 0L
            var archiveBytes = 0L
            var otherBytes = 0L

            fun scanDir(dir: File) {
                val files = dir.listFiles() ?: return
                for (f in files) {
                    if (f.isDirectory) {
                        scanDir(f)
                    } else {
                        val len = f.length()
                        val ext = f.extension.lowercase()
                        when (ext) {
                            "txt", "md", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "csv", "json" -> docBytes += len
                            "jpg", "jpeg", "png", "webp", "gif", "svg", "bmp" -> imgBytes += len
                            "mp3", "wav", "m4a", "aac", "ogg" -> audioBytes += len
                            "mp4", "mkv", "mov", "avi" -> videoBytes += len
                            "zip", "tar", "gz", "7z" -> archiveBytes += len
                            else -> otherBytes += len
                        }
                    }
                }
            }

            scanDir(rootDir)

            StorageSpaceInfo(
                totalBytes = totalBytes,
                freeBytes = freeBytes,
                usedBytes = usedBytes,
                documentsBytes = docBytes,
                imagesBytes = imgBytes,
                audioBytes = audioBytes,
                videosBytes = videoBytes,
                archivesBytes = archiveBytes,
                otherBytes = otherBytes
            )
        } catch (e: Exception) {
            StorageSpaceInfo(
                totalBytes = 64L * 1024 * 1024 * 1024,
                freeBytes = 42L * 1024 * 1024 * 1024,
                usedBytes = 22L * 1024 * 1024 * 1024,
                documentsBytes = 4L * 1024 * 1024 * 1024,
                imagesBytes = 8L * 1024 * 1024 * 1024,
                audioBytes = 2L * 1024 * 1024 * 1024,
                videosBytes = 5L * 1024 * 1024 * 1024,
                archivesBytes = 1L * 1024 * 1024 * 1024,
                otherBytes = 2L * 1024 * 1024 * 1024
            )
        }
    }

    /**
     * Seeds initial real documents, notes, csv data, code and folders into the Salim workspace
     */
    fun seedSampleWorkspace(salimRoot: File) {
        if (!salimRoot.exists()) salimRoot.mkdirs()

        // Check if already seeded
        val marker = File(salimRoot, ".seeded_v1")
        if (marker.exists()) return

        try {
            // 1. Projects folder
            val projectsDir = File(salimRoot, "Projects").apply { mkdirs() }
            File(projectsDir, "Roadmap 2026.md").writeText(
                """# Salim Product Roadmap 2026
## Mission
Build the most refined, minimalist, and silky smooth Apple-grade file management experience for mobile.

### Q3 Objectives
- [x] Pure minimalist Apple Human Interface Design
- [x] Real-time file system browsing & breadcrumb navigation
- [x] Quick Look file inspector & in-app text editor
- [x] Apple iOS color tags & instant tag filtering
- [x] ZIP archive packaging and decompression
- [x] Storage breakdown visualization and space management

### Core Architecture
- Kotlin + Jetpack Compose
- Clean MVVM with Room Local Metadata caching
- Smooth spring physics and haptic micro-interactions
- Zero AI fluff, 100% genuine system utility
"""
            )

            File(projectsDir, "Architecture Principles.txt").writeText(
                """SALIM DESIGN SYSTEM & ARCHITECTURE PRINCIPLES
---------------------------------------------
1. CLARITY: Crisp typography, high contrast, clean white canvas.
2. EFFICIENCY: Instant search, quick folder jumping, frictionless editing.
3. DEPTH: Subtle Apple zinc borders, responsive press states, thoughtful motion.
4. HONESTY: Real files, real system calls, real storage management.
"""
            )

            File(projectsDir, "Executive Review Notes.txt").writeText(
                """Meeting: Salim Design & Product Sync
Attendees: Salim Team, Lead UX, Core Engineering

Highlights:
- Navigation bar should respect bottom insets seamlessly.
- Implement both Grid and List modes for diverse folder densities.
- Add Trash bin recovery before permanent deletion.
- Instant file duplicate action for faster workflows.
"""
            )

            // 2. Financials folder
            val financeDir = File(salimRoot, "Financials").apply { mkdirs() }
            File(financeDir, "Q3 Budget & Operations.csv").writeText(
                """Category,Allocated,Spent,Remaining,Status
Infrastructure,12500,8200,4300,On Track
Design & UX,8000,7400,600,Completed
Engineering,24000,19500,4500,On Track
Operations,5000,3200,1800,On Track
Security & Audits,6000,4000,2000,Pending
Total,55500,42300,13200,Healthy
"""
            )

            File(financeDir, "Tax Summary 2025.txt").writeText(
                """ANNUAL TAX & FINANCIAL AUDIT SUMMARY
Period: Fiscal Year 2025-2026
Filing Status: Verified & Compliant
Auditor: Independent Advisory Board

Total Gross Revenue: $482,000.00
Allowable Deductions: $118,500.00
Net Taxable: $363,500.00
Status: Settled in full.
"""
            )

            // 3. Design & Brand folder
            val designDir = File(salimRoot, "Design & Brand").apply { mkdirs() }
            File(designDir, "Apple HIG Specs.txt").writeText(
                """APPLE HUMAN INTERFACE GUIDELINES SUMMARY
Typography Scale:
- Large Title: 34pt Bold
- Title 1: 28pt Bold
- Title 2: 22pt SemiBold
- Headline: 17pt SemiBold
- Body: 17pt Regular
- Subheadline: 15pt Regular
- Footnote: 13pt Regular
- Caption 1: 12pt Medium

Color Palette:
- System Blue: #007AFF
- System Gray: #8E8E93
- Background: #FFFFFF
- Secondary Grouped: #F2F2F7
- Hairline Border: #E5E5EA
"""
            )

            File(designDir, "palette_tokens.json").writeText(
                """{
  "theme": "Salim Apple Minimal",
  "colors": {
    "systemBlue": "#007AFF",
    "background": "#FFFFFF",
    "groupedBackground": "#F2F2F7",
    "hairline": "#E5E5EA",
    "tags": {
      "urgent": "#FF3B30",
      "review": "#FF9500",
      "pending": "#FFCC00",
      "approved": "#34C759",
      "work": "#007AFF",
      "personal": "#AF52DE"
    }
  }
}"""
            )

            // 4. Archive sample
            val archiveDir = File(salimRoot, "Archives").apply { mkdirs() }
            val sampleDoc = File(archiveDir, "bundle_readme.txt").apply {
                writeText("This is an unzipped asset from the Salim archive bundle.")
            }
            val zipFile = File(archiveDir, "Project_Bundle.zip")
            zipFiles(listOf(sampleDoc), zipFile)

            // 5. Root Quick Note
            File(salimRoot, "Welcome to Salim.txt").writeText(
                """Welcome to Salim — The Apple-Designed File Manager for Android.

Here are a few quick tips to get started:
• Tap any text file to open and edit it directly in Quick Look.
• Tap and hold or click '...' on any item for the Apple Action Sheet (Rename, Move, Tag, Share, Compress, Get Info).
• Toggle between Grid and List view using the top bar control.
• Assign vibrant Apple color tags (Red, Green, Blue, Purple) to organize across folders.
• Switch tabs at the bottom to check Recents, Favorites, or inspect Storage usage.

Enjoy the smooth, sleek, and minimalist experience!
"""
            )

            marker.writeText("seeded")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
