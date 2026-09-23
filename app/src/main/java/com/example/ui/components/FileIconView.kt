package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.ui.theme.AppleBlue
import java.io.File

@Composable
fun FileIconView(
    item: FileItem,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    if (item.isDirectory) {
        AppleFolderIcon(size = size, modifier = modifier)
    } else if (item.category == FileCategory.IMAGES) {
        // Thumbnail preview for images
        Box(
            modifier = modifier
                .size(size)
                .shadow(2.dp, RoundedCornerShape(10.dp))
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF2F2F7))
                .border(0.8.dp, Color(0xFFE5E5EA), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = File(item.path),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                error = null
            )
        }
    } else {
        AppleDocumentIcon(item = item, size = size, modifier = modifier)
    }
}

@Composable
fun AppleFolderIcon(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val folderBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF5AC8FA),
            Color(0xFF007AFF)
        )
    )

    Box(
        modifier = modifier
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        // Folder base
        Box(
            modifier = Modifier
                .size(size * 0.92f)
                .clip(RoundedCornerShape(size * 0.22f))
                .background(folderBrush)
                .border(0.8.dp, Color(0x33FFFFFF), RoundedCornerShape(size * 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder",
                tint = Color.White.copy(alpha = 0.95f),
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}

@Composable
fun AppleDocumentIcon(
    item: FileItem,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val ext = item.extension.uppercase()
    val (docColor, docIcon) = when (item.category) {
        FileCategory.DOCUMENTS -> {
            when (ext) {
                "PDF" -> Color(0xFFFF3B30) to Icons.Default.Description
                "CSV", "XLS", "XLSX" -> Color(0xFF34C759) to Icons.Default.TableChart
                "MD" -> Color(0xFF5856D6) to Icons.Default.Description
                "JSON", "XML" -> Color(0xFFFF9500) to Icons.Default.Description
                else -> Color(0xFF007AFF) to Icons.Default.Description
            }
        }
        FileCategory.AUDIO -> Color(0xFFFF2D55) to Icons.Default.Audiotrack
        FileCategory.VIDEOS -> Color(0xFFAF52DE) to Icons.Default.Movie
        FileCategory.ARCHIVES -> Color(0xFF8E8E93) to Icons.Default.Archive
        else -> Color(0xFF8E8E93) to Icons.Default.Description
    }

    Box(
        modifier = modifier
            .size(size)
            .shadow(2.dp, RoundedCornerShape(size * 0.18f))
            .clip(RoundedCornerShape(size * 0.18f))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(size * 0.18f)),
        contentAlignment = Alignment.Center
    ) {
        // Document accent top bar / corner
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = docIcon,
                    contentDescription = item.extension,
                    tint = docColor,
                    modifier = Modifier.size(size * 0.44f)
                )
            }

            // Extension label pill at bottom
            Box(
                modifier = Modifier
                    .padding(bottom = size * 0.08f)
                    .clip(RoundedCornerShape(3.dp))
                    .background(docColor.copy(alpha = 0.12f))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = if (ext.length > 4) ext.take(4) else ext,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = docColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.16f).sp
                    )
                )
            }
        }
    }
}
