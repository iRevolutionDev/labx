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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowUp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import software.revolution.labx.model.FileItem
import software.revolution.labx.ui.theme.PrimaryLight
import software.revolution.labx.ui.theme.SurfaceDark
import software.revolution.labx.ui.theme.SurfaceLight
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorer(
    rootPath: String,
    onFileSelected: (FileItem) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    var currentPath by remember { mutableStateOf(rootPath) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var expandedItems by remember { mutableStateOf(setOf<String>()) }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    val rootFiles by remember(currentPath, searchQuery) {
        derivedStateOf {
            val rootDir = File(currentPath)
            if (rootDir.exists() && rootDir.isDirectory) {
                rootDir.listFiles()
                    ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    ?.map { file ->
                        FileItem(
                            file = file,
                            children = if (file.isDirectory) {
                                file.listFiles()
                                    ?.sortedWith(
                                        compareBy(
                                            { !it.isDirectory },
                                            { it.name.lowercase() })
                                    )
                                    ?.map { childFile -> FileItem(childFile) }
                                    ?.filter { childItem ->
                                        searchQuery.isEmpty() || childItem.name.contains(
                                            searchQuery,
                                            ignoreCase = true
                                        )
                                    }
                            } else null
                        )
                    }
                    ?.filter { fileItem ->
                        searchQuery.isEmpty() || fileItem.name.contains(
                            searchQuery,
                            ignoreCase = true
                        )
                    } ?: emptyList()
            } else {
                emptyList()
            }
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
                text = "Explorer",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )

            Row {
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
                        contentDescription = if (isSearchActive) "Close Search" else "Search Files"
                    )
                }

                IconButton(onClick = {

                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
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
                placeholder = { Text("Search files...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .focusRequester(focusRequester),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon"
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
                        currentPath = root + File.separator
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
                            onClick = { currentPath = pathForClick },
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

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            val parentFile = File(currentPath).parentFile
            if (parentFile != null && searchQuery.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentPath = parentFile.absolutePath }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FeatherIcons.ArrowUp,
                            contentDescription = "Go up",
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
            }

            items(rootFiles) { fileItem ->
                val isExpanded = expandedItems.contains(fileItem.path)
                FileItemComponent(
                    fileItem = fileItem,
                    isExpanded = isExpanded,
                    onFileClick = { clickedFile ->
                        if (clickedFile.isDirectory) {
                            currentPath = clickedFile.path
                        } else {
                            onFileSelected(clickedFile)
                        }
                    },
                    onExpandToggle = {
                        expandedItems = if (isExpanded) {
                            expandedItems - fileItem.path
                        } else {
                            expandedItems + fileItem.path
                        }
                    },
                    isDarkTheme = isDarkTheme
                )
                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(
                        alpha = 0.05f
                    )
                )
            }

            if (rootFiles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching files found" else "No files found in this directory",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}