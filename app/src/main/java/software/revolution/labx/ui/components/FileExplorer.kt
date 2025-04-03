package software.revolution.labx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.TablerIcons
import compose.icons.feathericons.ArrowUp
import compose.icons.tablericons.FilePlus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import software.revolution.labx.R
import software.revolution.labx.domain.model.FileItem
import software.revolution.labx.ui.theme.PrimaryLight
import software.revolution.labx.ui.theme.SurfaceDark
import software.revolution.labx.ui.theme.SurfaceLight
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorer(
    files: List<FileItem>,
    currentPath: String,
    onFileSelected: (FileItem) -> Unit,
    onNavigateToDirectory: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onCreateFile: (String) -> Unit,
    onDeleteFile: (FileItem) -> Unit,
    isLoading: Boolean = false,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var isNewFileDialogVisible by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isEmpty()) {
            files
        } else {
            files.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(if (isDarkTheme) SurfaceDark else SurfaceLight)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.explorer_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )

            Row {
                IconButton(onClick = { isNewFileDialogVisible = true }) {
                    Icon(
                        imageVector = TablerIcons.FilePlus,
                        contentDescription = stringResource(R.string.new_file),
                        tint = PrimaryLight
                    )
                }

                IconButton(onClick = {
                    isSearchActive = !isSearchActive
                    if (isSearchActive) {
                        coroutineScope.launch {
                            delay(100)
                            focusRequester.requestFocus()
                        }
                    } else {
                        searchQuery = ""
                    }
                }) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (isSearchActive) stringResource(R.string.close_search) else stringResource(
                            R.string.search_files
                        )
                    )
                }

                IconButton(onClick = {
                    onNavigateToDirectory(currentPath)
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.refresh)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(stringResource(R.string.search_files_placeholder)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .focusRequester(focusRequester),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_icon)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = PrimaryLight,
                    unfocusedTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color.Black.copy(
                        alpha = 0.5f
                    )
                )
            )
        }

        val pathParts = currentPath.split(File.separator)
        if (pathParts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                var currentPathBuilder = ""

                val root = pathParts.firstOrNull() ?: ""
                Button(
                    onClick = {
                        onNavigateToDirectory(root + File.separator)
                    },
                    modifier = Modifier.padding(end = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryLight.copy(alpha = 0.7f)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (root.isEmpty()) "/" else root,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                currentPathBuilder = root + File.separator

                for (i in 1 until pathParts.size) {
                    val part = pathParts[i]
                    if (part.isNotEmpty()) {
                        currentPathBuilder += part + File.separator
                        val pathForClick = currentPathBuilder

                        Text(
                            text = " > ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )

                        Button(
                            onClick = { onNavigateToDirectory(pathForClick) },
                            modifier = Modifier.padding(end = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryLight.copy(alpha = 0.7f)
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = part, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryLight
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Botão de navegação para o diretório pai
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateUp() }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FeatherIcons.ArrowUp,
                                contentDescription = stringResource(R.string.go_up),
                                tint = if (isDarkTheme) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        HorizontalDivider(
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(
                                alpha = 0.05f
                            )
                        )
                    }

                    items(filteredFiles) { fileItem ->
                        FileItemComponent(
                            fileItem = fileItem,
                            onClick = {
                                if (fileItem.isDirectory) {
                                    onNavigateToDirectory(fileItem.path)
                                } else {
                                    onFileSelected(fileItem)
                                }
                            },
                            onDelete = {
                                onDeleteFile(fileItem)
                            },
                            isDarkTheme = isDarkTheme
                        )
                        HorizontalDivider(
                            color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(
                                alpha = 0.05f
                            )
                        )
                    }

                    if (filteredFiles.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty())
                                        stringResource(R.string.no_matching_files)
                                    else
                                        stringResource(R.string.no_files_found),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isNewFileDialogVisible) {
        AlertDialog(
            onDismissRequest = {
                isNewFileDialogVisible = false
                newFileName = ""
            },
            title = { Text(stringResource(R.string.create_new_file)) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text(stringResource(R.string.file_name)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Create,
                            contentDescription = null
                        )
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotEmpty()) {
                            onCreateFile(newFileName)
                            isNewFileDialogVisible = false
                            newFileName = ""
                        }
                    },
                    enabled = newFileName.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.create))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    isNewFileDialogVisible = false
                    newFileName = ""
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}