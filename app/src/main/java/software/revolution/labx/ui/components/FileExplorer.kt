package software.revolution.labx.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import software.revolution.labx.domain.model.FileExplorerEvents
import software.revolution.labx.domain.model.FileItem
import software.revolution.labx.domain.model.Result
import software.revolution.labx.presentation.viewmodel.FileExplorerViewModel
import java.io.File

/**
 * FileExplorer is a composable function that displays a file explorer UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorer(
    files: List<FileItem>,
    onFileSelected: (FileItem) -> Unit,
    onNavigateToDirectory: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onCreateFile: (String) -> Unit,
    onDeleteFile: (FileItem) -> Unit,
    onRenameFile: (FileItem, String) -> Unit = { _, _ -> },
    isLoading: Boolean = false,
    isDarkTheme: Boolean = false,
    currentPath: String = "",
    modifier: Modifier = Modifier,
    fileExplorerViewModel: FileExplorerViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }
    val folderContents = remember { mutableStateMapOf<String, List<FileItem>>() }
    val loadingFolders = remember { mutableStateMapOf<String, Boolean>() }
    val fileRenameStates = remember { mutableStateMapOf<String, Boolean>() }

    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    var isFileActionsOpen by rememberSaveable { mutableStateOf(false) }
    var isAddingFile by rememberSaveable { mutableStateOf(false) }
    var newFileName by rememberSaveable { mutableStateOf("") }
    var newFilePath by rememberSaveable { mutableStateOf("") }
    var newFileType by rememberSaveable { mutableStateOf("file") }
    var newFileLanguage by rememberSaveable { mutableStateOf("kotlin") }

    val sheetState = rememberModalBottomSheetState()

    val fileExplorerEvents = object : FileExplorerEvents {
        override fun startRenaming(file: FileItem) {
            selectedFile = file
            newFileName = file.name
            fileRenameStates[file.path] = true
            isFileActionsOpen = false
            coroutineScope.launch {
                delay(100)
                try {
                    focusRequester.requestFocus()
                } catch (_: Exception) {
                }
            }
        }

        override fun confirmRename(file: FileItem) {
            if (newFileName.trim().isNotEmpty()) {
                onRenameFile(file, newFileName)
            }
            newFileName = ""
            fileRenameStates.remove(file.path)
            focusManager.clearFocus()
        }

        override fun cancelRename(file: FileItem) {
            fileRenameStates.remove(file.path)
            newFileName = ""
            focusManager.clearFocus()
        }

        override fun handleCreateFile() {
            if (newFileName.trim().isEmpty()) return

            val finalFileName = if (newFileType == "file" && !newFileName.contains('.')) {
                val extension = when (newFileLanguage) {
                    "kotlin" -> ".kt"
                    "java" -> ".java"
                    "xml" -> ".xml"
                    "gradle" -> ".gradle.kts"
                    else -> ".txt"
                }
                newFileName + extension
            } else {
                newFileName
            }

            onCreateFile(finalFileName)
            newFileName = ""
            isAddingFile = false
        }

        override fun openFileActions(file: FileItem) {
            selectedFile = file
            newFileName = file.name
            newFilePath = file.path
            isFileActionsOpen = true
        }
        
        override fun openFile(file: FileItem) {
            onFileSelected(file)
        }

        override fun handleDeleteFile() {
            selectedFile?.let { file ->
                onDeleteFile(file)
            }
            isFileActionsOpen = false
        }

        override fun loadFolderContents(folderPath: String) {
            if (folderContents.containsKey(folderPath) || loadingFolders[folderPath] == true) return

            loadingFolders[folderPath] = true
            coroutineScope.launch {
                fileExplorerViewModel.listFilesInDirectoryFlow(folderPath).collectLatest { result ->
                    when (result) {
                        is Result.Success -> {
                            folderContents[folderPath] = result.data.sortedWith(
                                compareBy({ !it.isDirectory }, { it.name.lowercase() })
                            )
                            loadingFolders[folderPath] = false
                        }

                        is Result.Error -> {
                            folderContents[folderPath] = emptyList()
                            loadingFolders[folderPath] = false
                        }

                        is Result.Loading -> {}
                    }
                }
            }
        }

        override fun toggleFolderExpansion(folderPath: String) {
            val isExpanded = expandedFolders[folderPath] == true
            expandedFolders[folderPath] = !isExpanded

            if (!isExpanded && !folderContents.containsKey(folderPath)) {
                loadFolderContents(folderPath)
            }
        }

        override fun updateNewFileName(name: String) {
            newFileName = name
        }

        override fun updateNewFileType(type: String) {
            newFileType = type
        }

        override fun updateNewFileLanguage(language: String) {
            newFileLanguage = language
        }

        override fun dismissFileActions() {
            isFileActionsOpen = false
        }

        override fun dismissAddFile() {
            isAddingFile = false
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        FileExplorerHeader(
            isDarkTheme = isDarkTheme,
            onAddFileClick = { isAddingFile = true }
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = lazyListState
            ) {
                item {
                    if (currentPath.isEmpty()) {
                        EmptyProjectIndicator(isDarkTheme)
                    } else {
                        val pathComponents = currentPath
                            .trim(File.separatorChar)
                            .split(File.separator)
                            .filter { it.isNotEmpty() }

                        if (pathComponents.isEmpty()) {
                            DisplayRootFiles(
                                files = files,
                                isDarkTheme = isDarkTheme,
                                fileRenameStates = fileRenameStates,
                                selectedFile = selectedFile,
                                expandedFolders = expandedFolders,
                                loadingFolders = loadingFolders,
                                folderContents = folderContents,
                                newFileName = newFileName,
                                focusRequester = focusRequester,
                                events = fileExplorerEvents
                            )
                        } else {
                            DisplayCurrentDirFiles(
                                currentPath = currentPath,
                                files = files,
                                isDarkTheme = isDarkTheme,
                                fileRenameStates = fileRenameStates,
                                selectedFile = selectedFile,
                                expandedFolders = expandedFolders,
                                loadingFolders = loadingFolders,
                                folderContents = folderContents,
                                newFileName = newFileName,
                                focusRequester = focusRequester,
                                events = fileExplorerEvents
                            )
                        }
                    }
                }
            }
        }

        if (isAddingFile) {
            AddFileBottomSheet(
                onDismiss = fileExplorerEvents::dismissAddFile,
                sheetState = sheetState,
                isDarkTheme = isDarkTheme,
                newFileType = newFileType,
                onNewFileTypeChange = fileExplorerEvents::updateNewFileType,
                newFileLanguage = newFileLanguage,
                onNewFileLanguageChange = fileExplorerEvents::updateNewFileLanguage,
                newFileName = newFileName,
                onNewFileNameChange = fileExplorerEvents::updateNewFileName,
                onCreateFile = fileExplorerEvents::handleCreateFile
            )
        }

        if (isFileActionsOpen && selectedFile != null) {
            FileActionsBottomSheet(
                file = selectedFile!!,
                onDismiss = fileExplorerEvents::dismissFileActions,
                onRename = fileExplorerEvents::startRenaming,
                onOpen = { file ->
                    isFileActionsOpen = false
                    onFileSelected(file)
                },
                onDelete = fileExplorerEvents::handleDeleteFile,
                sheetState = sheetState,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun FileExplorerHeader(
    isDarkTheme: Boolean,
    onAddFileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "FILES",
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isDarkTheme) Color.Gray else Color.Gray.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        )

        IconButton(
            onClick = onAddFileClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add File",
                tint = if (isDarkTheme) Color.White else Color.Black
            )
        }
    }
}

@Composable
private fun EmptyProjectIndicator(isDarkTheme: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nenhum projeto aberto",
            color = if (isDarkTheme) Color.Gray else Color.Gray.copy(alpha = 0.8f),
        )
    }
}

@Composable
private fun DisplayRootFiles(
    files: List<FileItem>,
    isDarkTheme: Boolean,
    fileRenameStates: Map<String, Boolean>,
    selectedFile: FileItem?,
    expandedFolders: Map<String, Boolean>,
    loadingFolders: Map<String, Boolean>,
    folderContents: Map<String, List<FileItem>>,
    newFileName: String,
    focusRequester: FocusRequester,
    events: FileExplorerEvents
) {
    val rootDirectories = files.filter {
        !it.path.contains(File.separator) ||
                it.path.count { c -> c == File.separatorChar } == 1
    }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

    rootDirectories.forEachIndexed { index, file ->
        AnimatedFileTreeItem(
            file = file,
            level = 0,
            index = index,
            isDarkTheme = isDarkTheme,
            isRenaming = fileRenameStates[file.path] == true,
            isSelected = selectedFile == file,
            isExpanded = expandedFolders[file.path] == true,
            hasLoadingChildren = loadingFolders[file.path] == true,
            newFileName = newFileName,
            onRename = events::updateNewFileName,
            onCancelRename = { events.cancelRename(file) },
            onConfirmRename = { events.confirmRename(file) },
            onClick = {
                if (file.isDirectory) {
                    events.toggleFolderExpansion(file.path)
                } else {
                    events.openFile(file)
                }
            },
            onLongClick = { events.openFileActions(file) },
            focusRequester = focusRequester,
            content = if (file.isDirectory && expandedFolders[file.path] == true) {
                {
                    if (loadingFolders[file.path] == true) {
                        LoadingFolderIndicator(level = 1, isDarkTheme = isDarkTheme)
                    } else {
                        val children = folderContents[file.path] ?: emptyList()
                        AnimatedFolderContent(
                            isEmpty = children.isEmpty(),
                            level = 1,
                            isDarkTheme = isDarkTheme
                        ) {
                            children.forEachIndexed { childIndex, child ->
                                RenderFileTreeItem(
                                    file = child,
                                    level = 1,
                                    index = childIndex,
                                    isDarkTheme = isDarkTheme,
                                    fileRenameStates = fileRenameStates,
                                    selectedFile = selectedFile,
                                    expandedFolders = expandedFolders,
                                    loadingFolders = loadingFolders,
                                    folderContents = folderContents,
                                    newFileName = newFileName,
                                    focusRequester = focusRequester,
                                    events = events
                                )
                            }
                        }
                    }
                }
            } else null
        )
    }
}

@Composable
private fun RenderFileTreeItem(
    file: FileItem,
    level: Int,
    index: Int,
    isDarkTheme: Boolean,
    fileRenameStates: Map<String, Boolean>,
    selectedFile: FileItem?,
    expandedFolders: Map<String, Boolean>,
    loadingFolders: Map<String, Boolean>,
    folderContents: Map<String, List<FileItem>>,
    newFileName: String,
    focusRequester: FocusRequester,
    events: FileExplorerEvents
) {
    AnimatedFileTreeItem(
        file = file,
        level = level,
        index = index,
        isDarkTheme = isDarkTheme,
        isRenaming = fileRenameStates[file.path] == true,
        isSelected = selectedFile == file,
        isExpanded = expandedFolders[file.path] == true,
        hasLoadingChildren = loadingFolders[file.path] == true,
        newFileName = newFileName,
        onRename = events::updateNewFileName,
        onCancelRename = { events.cancelRename(file) },
        onConfirmRename = { events.confirmRename(file) },
        onClick = {
            if (file.isDirectory) {
                events.toggleFolderExpansion(file.path)
            } else {
                events.openFile(file)
            }
        },
        onLongClick = { events.openFileActions(file) },
        focusRequester = focusRequester,
        content = if (file.isDirectory && expandedFolders[file.path] == true) {
            {
                if (loadingFolders[file.path] == true) {
                    LoadingFolderIndicator(level = level + 1, isDarkTheme = isDarkTheme)
                } else {
                    val children = folderContents[file.path] ?: emptyList()
                    AnimatedFolderContent(
                        isEmpty = children.isEmpty(),
                        level = level + 1,
                        isDarkTheme = isDarkTheme
                    ) {
                        children.forEachIndexed { childIndex, child ->
                            RenderFileTreeItem(
                                file = child,
                                level = level + 1,
                                index = childIndex,
                                isDarkTheme = isDarkTheme,
                                fileRenameStates = fileRenameStates,
                                selectedFile = selectedFile,
                                expandedFolders = expandedFolders,
                                loadingFolders = loadingFolders,
                                folderContents = folderContents,
                                newFileName = newFileName,
                                focusRequester = focusRequester,
                                events = events
                            )
                        }
                    }
                }
            }
        } else null
    )
}

@Composable
private fun DisplayCurrentDirFiles(
    currentPath: String,
    files: List<FileItem>,
    isDarkTheme: Boolean,
    fileRenameStates: Map<String, Boolean>,
    selectedFile: FileItem?,
    expandedFolders: Map<String, Boolean>,
    loadingFolders: Map<String, Boolean>,
    folderContents: Map<String, List<FileItem>>,
    newFileName: String,
    focusRequester: FocusRequester,
    events: FileExplorerEvents
) {
    val currentDirFiles = files.filter { file ->
        file.path.startsWith(currentPath) &&
                file.path != currentPath &&
                (file.path.removePrefix(currentPath)
                    .count { c -> c == File.separatorChar } == 0)
    }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

    currentDirFiles.forEachIndexed { index, file ->
        RenderFileTreeItem(
            file = file,
            level = 0,
            index = index,
            isDarkTheme = isDarkTheme,
            fileRenameStates = fileRenameStates,
            selectedFile = selectedFile,
            expandedFolders = expandedFolders,
            loadingFolders = loadingFolders,
            folderContents = folderContents,
            newFileName = newFileName,
            focusRequester = focusRequester,
            events = events
        )
    }
}