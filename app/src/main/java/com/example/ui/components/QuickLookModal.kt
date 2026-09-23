package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.FileCategory
import com.example.data.model.FileItem
import com.example.ui.AudioPlayerState
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleLightBorder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary
import com.example.util.FileUtils
import java.io.File
import java.util.Locale

@Composable
fun QuickLookModal(
    item: FileItem,
    textContent: String,
    isTextDirty: Boolean,
    audioState: AudioPlayerState,
    onTextChange: (String) -> Unit,
    onSaveText: () -> Unit,
    onToggleAudioPlay: () -> Unit,
    onSeekAudio: (Int) -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Apple Quick Look Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("quick_look_done_button")
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = AppleBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 17.sp
                            )
                        )
                    }

                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = AppleTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        textAlign = TextAlign.Center
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isTextDirty) {
                            Button(
                                onClick = onSaveText,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppleBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("quick_look_save_button")
                            ) {
                                Text(
                                    text = "Save",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            IconButton(onClick = onShare) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share",
                                    tint = AppleBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp)

                // Main Viewer Canvas by Category
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when {
                        item.category == FileCategory.IMAGES -> {
                            ImageViewerContent(item = item)
                        }
                        item.category == FileCategory.AUDIO -> {
                            AudioPlayerContent(
                                item = item,
                                audioState = audioState,
                                onTogglePlay = onToggleAudioPlay,
                                onSeek = onSeekAudio
                            )
                        }
                        isTextFile(item) -> {
                            TextEditorContent(
                                content = textContent,
                                onContentChange = onTextChange
                            )
                        }
                        else -> {
                            DocumentPreviewContent(item = item, onShare = onShare)
                        }
                    }
                }
            }
        }
    }
}

private fun isTextFile(item: FileItem): Boolean {
    val ext = item.extension.lowercase()
    return item.mimeType.startsWith("text") ||
            listOf("txt", "md", "json", "csv", "xml", "html", "kt", "java", "gradle", "properties", "sql", "sh").contains(ext)
}

@Composable
private fun TextEditorContent(
    content: String,
    onContentChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    val linesCount = content.lines().size
    val wordsCount = if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
    val charsCount = content.length

    Column(modifier = Modifier.fillMaxSize()) {
        // Stats bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9F9FB))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$linesCount lines  •  $wordsCount words  •  $charsCount characters",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AppleTextSecondary,
                    fontSize = 11.5.sp
                )
            )
            Text(
                text = "Tap text to edit",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AppleBlue,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp)

        // Text area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("text_editor_field"),
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = AppleTextPrimary
                ),
                cursorBrush = SolidColor(AppleBlue)
            )
        }
    }
}

@Composable
private fun ImageViewerContent(item: FileItem) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = File(item.path),
            contentDescription = item.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        )
    }
}

@Composable
private fun AudioPlayerContent(
    item: FileItem,
    audioState: AudioPlayerState,
    onTogglePlay: () -> Unit,
    onSeek: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Apple Music Style Album Artwork Card
        Box(
            modifier = Modifier
                .size(220.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF2F2F7))
                .border(1.dp, AppleLightBorder, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            FileIconView(item = item, size = 110.dp)
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = item.name,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AppleTextPrimary
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${item.extension.uppercase()} Audio  •  ${item.formattedSize}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AppleTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Slider
        val currentMs = audioState.currentPositionMs.toFloat()
        val totalMs = audioState.durationMs.coerceAtLeast(1).toFloat()

        Slider(
            value = currentMs,
            onValueChange = { onSeek(it.toInt()) },
            valueRange = 0f..totalMs,
            colors = SliderDefaults.colors(
                thumbColor = AppleBlue,
                activeTrackColor = AppleBlue,
                inactiveTrackColor = Color(0xFFE5E5EA)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(audioState.currentPositionMs),
                style = MaterialTheme.typography.labelSmall.copy(color = AppleTextSecondary)
            )
            Text(
                text = formatDuration(audioState.durationMs),
                style = MaterialTheme.typography.labelSmall.copy(color = AppleTextSecondary)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Play / Pause round control button
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(AppleBlue),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onTogglePlay,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (audioState.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun DocumentPreviewContent(
    item: FileItem,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FileIconView(item = item, size = 96.dp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = item.name,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AppleTextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${item.extension.uppercase()} Document  •  ${item.formattedSize}",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AppleTextSecondary,
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Inspector Preview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(0.8.dp, AppleLightBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(label = "Format", value = item.extension.uppercase())
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Size", value = item.formattedSize)
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Modified", value = FileUtils.formatAppleDate(item.lastModified))
                HorizontalDivider(color = AppleLightBorder, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))
                InfoRow(label = "Location", value = item.path)
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onShare,
            colors = ButtonDefaults.buttonColors(
                containerColor = AppleBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Share File", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AppleTextSecondary,
                fontSize = 14.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = AppleTextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDuration(ms: Int): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.getDefault(), "%d:%02d", min, sec)
}
