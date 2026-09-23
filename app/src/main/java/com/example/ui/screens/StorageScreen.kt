package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleLightBorder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.util.StorageSpaceInfo

@Composable
fun StorageScreen(
    storageInfo: StorageSpaceInfo?,
    trashCount: Int,
    onOpenTrash: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = storageInfo?.totalBytes ?: (64L * 1024 * 1024 * 1024)
    val used = storageInfo?.usedBytes ?: (22L * 1024 * 1024 * 1024)
    val free = storageInfo?.freeBytes ?: (total - used)

    val docBytes = storageInfo?.documentsBytes ?: (4L * 1024 * 1024 * 1024)
    val imgBytes = storageInfo?.imagesBytes ?: (8L * 1024 * 1024 * 1024)
    val audioBytes = storageInfo?.audioBytes ?: (2L * 1024 * 1024 * 1024)
    val videoBytes = storageInfo?.videosBytes ?: (5L * 1024 * 1024 * 1024)
    val archiveBytes = storageInfo?.archivesBytes ?: (1L * 1024 * 1024 * 1024)
    val systemBytes = (used - (docBytes + imgBytes + audioBytes + videoBytes + archiveBytes)).coerceAtLeast(1024 * 1024 * 1024L)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Storage",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = "Device & Salim Workspace",
                    style = MaterialTheme.typography.bodySmall.copy(color = AppleTextSecondary)
                )
            }

            IconButton(onClick = onRefresh) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = AppleBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Apple Storage Meter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleLightBorder)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Large Numbers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Used Storage",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AppleTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "${formatGB(used)} GB",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppleTextPrimary,
                                fontSize = 28.sp
                            )
                        )
                    }

                    Text(
                        text = "of ${formatGB(total)} GB (${formatGB(free)} GB free)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AppleTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Multi-Segment Colored Storage Bar (Apple Style)
                val totalF = total.toFloat().coerceAtLeast(1f)
                val docWeight = (docBytes.toFloat() / totalF).coerceAtLeast(0.02f)
                val imgWeight = (imgBytes.toFloat() / totalF).coerceAtLeast(0.04f)
                val videoWeight = (videoBytes.toFloat() / totalF).coerceAtLeast(0.03f)
                val audioWeight = (audioBytes.toFloat() / totalF).coerceAtLeast(0.02f)
                val archWeight = (archiveBytes.toFloat() / totalF).coerceAtLeast(0.01f)
                val sysWeight = (systemBytes.toFloat() / totalF).coerceAtLeast(0.04f)
                val freeWeight = (free.toFloat() / totalF).coerceAtLeast(0.05f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFE5E5EA))
                ) {
                    Box(modifier = Modifier.weight(imgWeight).fillMaxSize().background(Color(0xFFFF9500))) // Photos / Yellow-Orange
                    Box(modifier = Modifier.weight(videoWeight).fillMaxSize().background(Color(0xFFAF52DE))) // Videos / Purple
                    Box(modifier = Modifier.weight(docWeight).fillMaxSize().background(Color(0xFF007AFF))) // Docs / Blue
                    Box(modifier = Modifier.weight(audioWeight).fillMaxSize().background(Color(0xFFFF2D55))) // Audio / Pink
                    Box(modifier = Modifier.weight(archWeight).fillMaxSize().background(Color(0xFF34C759))) // Archives / Green
                    Box(modifier = Modifier.weight(sysWeight).fillMaxSize().background(Color(0xFF8E8E93))) // System / Gray
                    Box(modifier = Modifier.weight(freeWeight).fillMaxSize().background(Color(0xFFE5E5EA))) // Free / Light
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breakdown list
                StorageCategoryRow(color = Color(0xFFFF9500), title = "Photos & Images", sizeStr = formatBytes(imgBytes))
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                StorageCategoryRow(color = Color(0xFFAF52DE), title = "Videos & Media", sizeStr = formatBytes(videoBytes))
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                StorageCategoryRow(color = Color(0xFF007AFF), title = "Documents & Code", sizeStr = formatBytes(docBytes))
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                StorageCategoryRow(color = Color(0xFFFF2D55), title = "Audio & Music", sizeStr = formatBytes(audioBytes))
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                StorageCategoryRow(color = Color(0xFF34C759), title = "Archives (ZIP)", sizeStr = formatBytes(archiveBytes))
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                StorageCategoryRow(color = Color(0xFF8E8E93), title = "System & Workspace", sizeStr = formatBytes(systemBytes))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Trash Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenTrash)
                .testTag("storage_trash_card"),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleLightBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF3B30).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Recently Deleted",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AppleTextPrimary,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = if (trashCount == 1) "1 item in Trash" else "$trashCount items in Trash",
                            style = MaterialTheme.typography.labelSmall.copy(color = AppleTextSecondary)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFC7C7CC),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun StorageCategoryRow(
    color: Color,
    title: String,
    sizeStr: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AppleTextPrimary,
                    fontSize = 14.sp
                )
            )
        }
        Text(
            text = sizeStr,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = AppleTextSecondary,
                fontSize = 14.sp
            )
        )
    }
}

private fun formatGB(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return String.format("%.1f", gb)
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
    val value = bytes / Math.pow(1024.0, digitGroups.toDouble())
    return String.format("%.1f %s", value, units[digitGroups])
}
