package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppleTag
import com.example.data.model.FileItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleLightBorder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GetInfoModal(
    item: FileItem,
    onDismiss: () -> Unit,
    onTagSelected: (AppleTag?) -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val md5 = remember(item.path) {
        if (!item.isDirectory) FileUtils.calculateMD5(item.file) else "N/A (Directory)"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Icon + Name
            Spacer(modifier = Modifier.height(8.dp))
            FileIconView(item = item, size = 68.dp)
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppleTextPrimary
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${item.formattedSize}  •  ${if (item.isDirectory) "Folder" else item.mimeType}",
                style = MaterialTheme.typography.bodySmall.copy(color = AppleTextSecondary)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Apple Tags Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleLightBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TAGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppleTextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppleTagSelector(
                        selectedColorHex = item.tagColorHex,
                        onTagSelected = onTagSelected
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // General Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleLightBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GENERAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppleTextSecondary,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow(label = "Kind", value = if (item.isDirectory) "Folder" else "${item.extension.uppercase()} Document")
                    HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    InfoRow(label = "Size", value = "${item.formattedSize} (${item.size} bytes)")
                    HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    InfoRow(label = "Where", value = item.parentPath)
                    HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    InfoRow(label = "Modified", value = FileUtils.formatAppleDate(item.lastModified))
                    HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    InfoRow(label = "MD5 Checksum", value = md5.take(16) + "...")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleLightBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) Color(0xFFFFCC00) else AppleBlue
                        )
                    }

                    if (!item.isDirectory) {
                        IconButton(onClick = onShare) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = AppleBlue)
                        }
                    }

                    IconButton(onClick = onDuplicate) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = AppleBlue)
                    }

                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFFF3B30))
                    }
                }
            }
        }
    }
}
