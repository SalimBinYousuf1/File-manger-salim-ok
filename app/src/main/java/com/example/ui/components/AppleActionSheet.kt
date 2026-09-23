package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleLightBorder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleActionSheet(
    item: FileItem,
    onDismiss: () -> Unit,
    onQuickLook: () -> Unit,
    onGetInfo: () -> Unit,
    onRename: () -> Unit,
    onDuplicate: () -> Unit,
    onMove: () -> Unit,
    onTags: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onCompress: () -> Unit,
    onExtract: () -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFF2F2F7),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFC7C7CC))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header card with item preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FileIconView(item = item, size = 44.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = AppleTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.formattedSize}  •  ${if (item.isDirectory) "Folder" else item.extension.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AppleTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Group 1: Viewing & Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
            ) {
                if (!item.isDirectory) {
                    ActionSheetRow(
                        title = "Quick Look & Edit",
                        icon = Icons.Default.Visibility,
                        onClick = { onDismiss(); onQuickLook() },
                        testTag = "action_quick_look"
                    )
                    HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                }

                ActionSheetRow(
                    title = "Get Info",
                    icon = Icons.Default.Info,
                    onClick = { onDismiss(); onGetInfo() },
                    testTag = "action_get_info"
                )
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))

                ActionSheetRow(
                    title = if (item.isFavorite) "Remove from Favorites" else "Add to Favorites",
                    icon = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarOutline,
                    iconTint = if (item.isFavorite) Color(0xFFFFCC00) else AppleBlue,
                    onClick = { onDismiss(); onToggleFavorite() },
                    testTag = "action_favorite"
                )
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))

                ActionSheetRow(
                    title = "Tags",
                    icon = Icons.Default.Label,
                    onClick = { onDismiss(); onTags() },
                    testTag = "action_tags"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Group 2: File operations
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
            ) {
                ActionSheetRow(
                    title = "Rename",
                    icon = Icons.Default.Edit,
                    onClick = { onDismiss(); onRename() },
                    testTag = "action_rename"
                )
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))

                ActionSheetRow(
                    title = "Duplicate",
                    icon = Icons.Default.ContentCopy,
                    onClick = { onDismiss(); onDuplicate() },
                    testTag = "action_duplicate"
                )
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))

                ActionSheetRow(
                    title = "Move to...",
                    icon = Icons.Default.DriveFileMove,
                    onClick = { onDismiss(); onMove() },
                    testTag = "action_move"
                )

                if (!item.isDirectory) {
                    HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    ActionSheetRow(
                        title = "Share",
                        icon = Icons.Default.Share,
                        onClick = { onDismiss(); onShare() },
                        testTag = "action_share"
                    )
                }

                if (item.extension.equals("zip", ignoreCase = true)) {
                    HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    ActionSheetRow(
                        title = "Extract All (Unzip)",
                        icon = Icons.Default.Unarchive,
                        onClick = { onDismiss(); onExtract() },
                        testTag = "action_extract"
                    )
                } else {
                    HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(start = 52.dp))
                    ActionSheetRow(
                        title = "Compress to ZIP",
                        icon = Icons.Default.Archive,
                        onClick = { onDismiss(); onCompress() },
                        testTag = "action_compress"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Group 3: Destructive Trash
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
            ) {
                ActionSheetRow(
                    title = "Move to Trash",
                    icon = Icons.Default.Delete,
                    isDestructive = true,
                    onClick = { onDismiss(); onDelete() },
                    testTag = "action_delete"
                )
            }
        }
    }
}

@Composable
private fun ActionSheetRow(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    iconTint: Color = if (isDestructive) Color(0xFFFF3B30) else AppleBlue,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.5.sp,
                color = if (isDestructive) Color(0xFFFF3B30) else AppleTextPrimary
            )
        )
    }
}
