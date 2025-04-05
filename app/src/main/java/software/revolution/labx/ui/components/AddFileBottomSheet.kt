package software.revolution.labx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.FilePlus
import compose.icons.tablericons.FolderPlus
import software.revolution.labx.ui.theme.PrimaryLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFileBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    isDarkTheme: Boolean,
    newFileType: String,
    onNewFileTypeChange: (String) -> Unit,
    newFileLanguage: String,
    onNewFileLanguageChange: (String) -> Unit,
    newFileName: String,
    onNewFileNameChange: (String) -> Unit,
    onCreateFile: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDarkTheme) Color(0xFF1a1b26) else Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        tonalElevation = 4.dp,
        scrimColor = Color.Black.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .windowInsetsPadding(WindowInsets(bottom = 16.dp))
        ) {
            Text(
                text = "Create New",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isDarkTheme) Color.White else Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Create a new file or folder in your project",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkTheme) Color.Gray else Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300, 50)) +
                        slideInVertically(
                            animationSpec = tween(300, 50, FastOutSlowInEasing)
                        ) { it / 2 }
            ) {
                Column {
                    Text(
                        text = "Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fileButtonColor by animateColorAsState(
                    targetValue = if (newFileType == "file") {
                        if (isDarkTheme) Color(0xFF7C3AED) else PrimaryLight
                    } else {
                        if (isDarkTheme) Color(0xFF2D2D3F) else Color(0xFFE6EDF5)
                    },
                    animationSpec = tween(250),
                    label = "fileButtonColor"
                )

                val folderButtonColor by animateColorAsState(
                    targetValue = if (newFileType == "folder") {
                        if (isDarkTheme) Color(0xFF7C3AED) else PrimaryLight
                    } else {
                        if (isDarkTheme) Color(0xFF2D2D3F) else Color(0xFFE6EDF5)
                    },
                    animationSpec = tween(250),
                    label = "folderButtonColor"
                )

                Button(
                    onClick = { onNewFileTypeChange("file") },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = fileButtonColor
                    )
                ) {
                    Icon(
                        imageVector = TablerIcons.FilePlus,
                        contentDescription = null,
                        tint = if (newFileType == "file") {
                            Color.White
                        } else {
                            if (isDarkTheme) Color.White else Color.Black.copy(
                                alpha = 0.7f
                            )
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "File",
                        color = if (newFileType == "file") {
                            Color.White
                        } else {
                            if (isDarkTheme) Color.White else Color.Black.copy(alpha = 0.7f)
                        }
                    )
                }

                Button(
                    onClick = { onNewFileTypeChange("folder") },
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = folderButtonColor
                    )
                ) {
                    Icon(
                        imageVector = TablerIcons.FolderPlus,
                        contentDescription = null,
                        tint = if (newFileType == "folder") {
                            Color.White
                        } else {
                            if (isDarkTheme) Color.White else Color.Black.copy(
                                alpha = 0.7f
                            )
                        },
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Folder",
                        color = if (newFileType == "folder") {
                            Color.White
                        } else {
                            if (isDarkTheme) Color.White else Color.Black.copy(alpha = 0.7f)
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = newFileType == "file",
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = 300,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = LinearOutSlowInEasing
                    )
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200)
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Language",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LanguageSelector(
                        isDarkTheme = isDarkTheme,
                        newFileLanguage = newFileLanguage,
                        onNewFileLanguageChange = onNewFileLanguageChange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300, 150)) +
                        slideInVertically(
                            animationSpec = tween(300, 150, FastOutSlowInEasing)
                        ) { it / 2 }
            ) {
                Column {
                    Text(
                        text = "Name",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = onNewFileNameChange,
                        placeholder = {
                            Text(
                                text = if (newFileType == "file") {
                                    when (newFileLanguage) {
                                        "kotlin" -> "MyClass.kt"
                                        "java" -> "MyClass.java"
                                        "xml" -> "layout.xml"
                                        "gradle" -> "build.gradle.kts"
                                        else -> "file.txt"
                                    }
                                } else "MyFolder"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDarkTheme) Color(0xFF7C3AED) else PrimaryLight,
                            unfocusedBorderColor = if (isDarkTheme) Color.Gray.copy(alpha = 0.5f) else Color.Gray.copy(
                                alpha = 0.3f
                            ),
                            focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                            unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                            focusedContainerColor = if (isDarkTheme) Color(0xFF2D2D3F) else Color(
                                0xFFF1F5F9
                            ),
                            unfocusedContainerColor = if (isDarkTheme) Color(0xFF2D2D3F) else Color(
                                0xFFF1F5F9
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ActionButtons(
                isDarkTheme = isDarkTheme,
                newFileName = newFileName,
                onDismiss = onDismiss,
                onCreateFile = onCreateFile
            )
        }
    }
}

@Composable
private fun LanguageSelector(
    isDarkTheme: Boolean,
    newFileLanguage: String,
    onNewFileLanguageChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isDarkTheme) Color(0xFF2D2D3F) else Color(0xFFE6EDF5)
            )
            .padding(8.dp)
    ) {
        val languages = listOf("kotlin", "java", "xml", "gradle")

        languages.forEachIndexed { index, language ->
            val isSelected = newFileLanguage == language
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    if (isDarkTheme) Color(0xFF3a3c4e) else Color.White.copy(alpha = 0.6f)
                } else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "languageBackground$index"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(backgroundColor)
                    .clickable { onNewFileLanguageChange(language) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onNewFileLanguageChange(language) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = if (isDarkTheme) Color(0xFF7C3AED) else PrimaryLight,
                        unselectedColor = if (isDarkTheme) Color.Gray else Color.Gray.copy(
                            alpha = 0.5f
                        )
                    )
                )

                Text(
                    text = language.capitalize(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) Color.White else Color.Black
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isDarkTheme: Boolean,
    newFileName: String,
    onDismiss: () -> Unit,
    onCreateFile: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = onDismiss,
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isDarkTheme) Color.Gray else Color.Gray
            )
        ) {
            Text("Cancel")
        }

        Spacer(modifier = Modifier.width(8.dp))

        val createButtonEnabled = newFileName.trim().isNotEmpty()
        val createButtonColor by animateColorAsState(
            targetValue = if (createButtonEnabled) {
                if (isDarkTheme) Color(0xFF7C3AED) else PrimaryLight
            } else {
                if (isDarkTheme) Color(0xFF3a3c4e) else Color.Gray.copy(alpha = 0.3f)
            },
            animationSpec = tween(150),
            label = "createButtonColor"
        )

        Button(
            onClick = {
                if (newFileName.trim().isNotEmpty()) {
                    onCreateFile()
                }
            },
            enabled = createButtonEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = createButtonColor,
                disabledContainerColor = createButtonColor
            ),
            modifier = Modifier.padding(4.dp)
        ) {
            Text("Create")
        }
    }
}

@Composable
private fun String.capitalize(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase() else it.toString()
    }
}