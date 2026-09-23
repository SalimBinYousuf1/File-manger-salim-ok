package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Circle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleLightBorder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.util.FileUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileGridCard(
    item: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isSelected) Color(0xFFF0F6FF) else Color.White
    val borderColor = if (isSelected) AppleBlue else AppleLightBorder

    Box(
        modifier = modifier
            .testTag("file_grid_item_${item.name}")
            .shadow(if (isSelected) 4.dp else 1.5.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(if (isSelected) 1.5.dp else 0.8.dp, borderColor, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Top badges (tag dot, star, selection check, or menu)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left badge: Tag or Star
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.tagColorHex != null) {
                        AppleTagDot(colorHex = item.tagColorHex, size = 9)
                    }
                    if (item.isFavorite) {
                        Spacer(modifier = Modifier.size(4.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite",
                            tint = Color(0xFFFFCC00),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                // Right action: Checkbox or 3-dots
                if (isSelectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
                        contentDescription = "Select",
                        tint = if (isSelected) AppleBlue else Color(0xFFC7C7CC),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .combinedClickable(onClick = onMenuClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = AppleTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Icon / Thumbnail
            FileIconView(
                item = item,
                size = 54.dp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // File Name
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.5.sp,
                    color = AppleTextPrimary
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle: Size or Items
            Text(
                text = item.formattedSize,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AppleTextSecondary,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
