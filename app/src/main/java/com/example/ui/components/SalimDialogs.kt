package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FileItem
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleLightBorder
import com.example.ui.theme.AppleTextPrimary
import com.example.ui.theme.AppleTextSecondary

@Composable
fun NewFolderDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            modifier = Modifier
                .width(310.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "New Folder",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        fontSize = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Enter a name for this folder.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AppleTextSecondary,
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFEFF0))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("new_folder_input"),
                        singleLine = true,
                        cursorBrush = SolidColor(AppleBlue),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = AppleTextPrimary,
                            fontSize = 14.sp
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (folderName.isNotBlank()) onConfirm(folderName)
                        })
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Cancel", color = AppleBlue, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { if (folderName.isNotBlank()) onConfirm(folderName) },
                        enabled = folderName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_folder_save_button")
                    ) {
                        Text(text = "Create", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun NewFileDialog(
    onConfirm: (name: String, content: String) -> Unit,
    onDismiss: () -> Unit
) {
    var fileName by remember { mutableStateOf("Untitled.txt") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            modifier = Modifier
                .width(310.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "New Document",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        fontSize = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Supports .txt, .md, .csv, .json",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = AppleTextSecondary,
                        fontSize = 13.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFEFF0))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("new_file_input"),
                        singleLine = true,
                        cursorBrush = SolidColor(AppleBlue),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = AppleTextPrimary,
                            fontSize = 14.sp
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (fileName.isNotBlank()) onConfirm(fileName, "")
                        })
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Cancel", color = AppleBlue, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { if (fileName.isNotBlank()) onConfirm(fileName, "") },
                        enabled = fileName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_file_create_button")
                    ) {
                        Text(text = "Create", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun RenameDialog(
    item: FileItem,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(item.name) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            modifier = Modifier
                .width(310.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rename Item",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        fontSize = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFEFF0))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .testTag("rename_input"),
                        singleLine = true,
                        cursorBrush = SolidColor(AppleBlue),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = AppleTextPrimary,
                            fontSize = 14.sp
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newName.isNotBlank()) onConfirm(newName)
                        })
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Cancel", color = AppleBlue, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { if (newName.isNotBlank()) onConfirm(newName) },
                        enabled = newName.isNotBlank() && newName != item.name,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("rename_confirm_button")
                    ) {
                        Text(text = "Rename", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ZipArchiveDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var archiveName by remember { mutableStateOf("Archive.zip") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            modifier = Modifier
                .width(310.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Compress Items",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppleTextPrimary,
                        fontSize = 17.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Create a standard ZIP archive.",
                    style = MaterialTheme.typography.bodySmall.copy(color = AppleTextSecondary)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFEFF0))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = archiveName,
                        onValueChange = { archiveName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("zip_name_input"),
                        singleLine = true,
                        cursorBrush = SolidColor(AppleBlue),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = AppleTextPrimary,
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Cancel", color = AppleBlue, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = { if (archiveName.isNotBlank()) onConfirm(archiveName) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppleBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Compress", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
