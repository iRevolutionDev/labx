package software.revolution.labx.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.FontAwesomeIcons
import compose.icons.TablerIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Save
import compose.icons.tablericons.Folder
import compose.icons.tablericons.FolderX
import kotlinx.coroutines.launch
import software.revolution.labx.R
import software.revolution.labx.domain.model.FileItem
import software.revolution.labx.domain.model.OutputType
import software.revolution.labx.presentation.viewmodel.EditorViewModel
import software.revolution.labx.presentation.viewmodel.FileExplorerViewModel
import software.revolution.labx.ui.components.EditorComponent
import software.revolution.labx.ui.components.EditorTab
import software.revolution.labx.ui.components.FileExplorer
import software.revolution.labx.ui.components.OutputPanel
import software.revolution.labx.ui.components.TabBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEditorScreen(
    onNavigateToSettings: () -> Unit = {},
    editorViewModel: EditorViewModel = hiltViewModel(),
    fileExplorerViewModel: FileExplorerViewModel = hiltViewModel()
) {
    LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val editorPreferences by editorViewModel.editorPreferences.collectAsStateWithLifecycle(null)
    val editorState by editorViewModel.editorState.collectAsStateWithLifecycle()
    val outputMessages by editorViewModel.outputMessages.collectAsStateWithLifecycle()

    val currentDirectory by fileExplorerViewModel.currentDirectory.collectAsStateWithLifecycle()
    val files by fileExplorerViewModel.files.collectAsStateWithLifecycle()
    val fileExplorerError by fileExplorerViewModel.error.collectAsStateWithLifecycle()
    val fileExplorerLoading by fileExplorerViewModel.isLoading.collectAsStateWithLifecycle()

    var openTabs by rememberSaveable { mutableStateOf<List<EditorTab>>(emptyList()) }
    var isOutputPanelVisible by rememberSaveable { mutableStateOf(false) }
    var isFileExplorerVisible by rememberSaveable { mutableStateOf(true) }
    var isFilePickerDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(fileExplorerError) {
        fileExplorerError?.let { error ->
            editorViewModel.addOutputMessage(error, OutputType.ERROR)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = SnackbarDuration.Short
                )
            }
            fileExplorerViewModel.clearError()
        }
    }

    fun loadFile(fileItem: FileItem) {
        val existingTab = openTabs.find { it.file.path == fileItem.path }
        if (existingTab != null) {
            openTabs = openTabs.map { tab ->
                if (tab.file.path == fileItem.path) {
                    tab.copy(isActive = true)
                } else {
                    tab.copy(isActive = false)
                }
            }
        } else {
            val newTab = EditorTab(file = fileItem, isActive = true)
            openTabs = openTabs.map { it.copy(isActive = false) } + newTab
        }

        editorViewModel.openFile(fileItem)
    }

    fun saveCurrentFile() {
        editorViewModel.saveCurrentFile()

        editorState.currentFile?.let { currentFile ->
            openTabs = openTabs.map { tab ->
                if (tab.file.path == currentFile.path) {
                    tab.copy(isModified = false)
                } else {
                    tab
                }
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    editorPreferences?.let { preferences ->
        val isDarkTheme = preferences.isDarkMode

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(300.dp)
                        .fillMaxHeight()
                ) {
                    Text(
                        text = stringResource(R.string.title_app),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )

                    HorizontalDivider()

                    NavigationDrawerItem(
                        label = { Text(stringResource(R.string.menu_settings)) },
                        icon = {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = stringResource(R.string.menu_settings)
                            )
                        },
                        selected = false,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                onNavigateToSettings()
                            }
                        }
                    )
                }
            },
            gesturesEnabled = true
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.title_app),
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { saveCurrentFile() }) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Save,
                                    contentDescription = stringResource(R.string.save_file),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(onClick = { isFilePickerDialogVisible = true }) {
                                Icon(
                                    imageVector = Icons.Default.Create,
                                    contentDescription = stringResource(R.string.new_file)
                                )
                            }

                            IconButton(onClick = {
                                isFileExplorerVisible = !isFileExplorerVisible
                            }) {
                                Icon(
                                    imageVector = if (isFileExplorerVisible)
                                        TablerIcons.Folder
                                    else
                                        TablerIcons.FolderX,
                                    contentDescription = if (isFileExplorerVisible)
                                        stringResource(R.string.hide_explorer)
                                    else
                                        stringResource(R.string.show_explorer)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },
                bottomBar = {
                    Surface(
                        tonalElevation = 2.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (preferences.autoSave)
                                    stringResource(R.string.edit_mode_autosave)
                                else
                                    stringResource(R.string.edit_mode),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Button(
                                onClick = {
                                    isOutputPanelVisible = !isOutputPanelVisible
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (outputMessages.any { it.type == OutputType.ERROR })
                                        MaterialTheme.colorScheme.error else preferences.accentColor
                                )
                            ) {
                                Icon(
                                    imageVector = if (isOutputPanelVisible)
                                        Icons.Default.KeyboardArrowDown
                                    else
                                        Icons.Default.KeyboardArrowUp,
                                    contentDescription = if (isOutputPanelVisible)
                                        stringResource(R.string.hide_output)
                                    else
                                        stringResource(R.string.show_output)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(
                                        R.string.output_count,
                                        outputMessages.size
                                    )
                                )
                            }
                        }
                    }
                },
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        AnimatedVisibility(
                            visible = isFileExplorerVisible,
                            enter = fadeIn(animationSpec = tween(200)),
                            exit = fadeOut(animationSpec = tween(200))
                        ) {
                            FileExplorer(
                                files = files,
                                currentPath = currentDirectory,
                                onFileSelected = { fileItem ->
                                    loadFile(fileItem)
                                },
                                onNavigateToDirectory = { path ->
                                    fileExplorerViewModel.navigateToDirectory(path)
                                },
                                onNavigateUp = {
                                    fileExplorerViewModel.navigateUp()
                                },
                                onCreateFile = { fileName ->
                                    fileExplorerViewModel.createNewFile(fileName)
                                },
                                onDeleteFile = { fileItem ->
                                    fileExplorerViewModel.deleteFile(fileItem)
                                },
                                isLoading = fileExplorerLoading,
                                isDarkTheme = isDarkTheme,
                                modifier = Modifier.width(250.dp)
                            )
                        }

                        if (isFileExplorerVisible) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp),
                                color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(
                                    alpha = 0.1f
                                )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            TabBar(
                                tabs = openTabs,
                                onTabSelected = { tab ->
                                    loadFile(tab.file)
                                },
                                onTabClosed = { tab ->
                                    openTabs = openTabs.filter { it.file.path != tab.file.path }

                                    if (tab.isActive && openTabs.isNotEmpty()) {
                                        val newActiveTab = openTabs.first()
                                        loadFile(newActiveTab.file)
                                    } else if (openTabs.isEmpty()) {
                                        editorViewModel.clearFile()
                                    }
                                },
                                isDarkTheme = isDarkTheme
                            )

                            EditorComponent(
                                editorState = editorState,
                                onEditorStateChange = { newState ->
                                    editorViewModel.updateContent(newState.content)

                                    if (newState.cursorPosition != editorState.cursorPosition) {
                                        editorViewModel.updateCursorPosition(newState.cursorPosition)
                                    }

                                    if (newState.selectionStart != editorState.selectionStart ||
                                        newState.selectionEnd != editorState.selectionEnd
                                    ) {
                                        editorViewModel.updateSelection(
                                            newState.selectionStart,
                                            newState.selectionEnd
                                        )
                                    }

                                    val currentFile = editorState.currentFile
                                    if (currentFile != null && newState.isModified != editorState.isModified) {
                                        openTabs = openTabs.map { tab ->
                                            if (tab.file.path == currentFile.path) {
                                                tab.copy(isModified = newState.isModified)
                                            } else {
                                                tab
                                            }
                                        }
                                    }

                                    if (preferences.autoSave && newState.isModified && newState.content != editorState.content) {
                                        saveCurrentFile()
                                    }
                                },
                                isDarkTheme = isDarkTheme,
                                modifier = Modifier.weight(1f),
                                showLineNumbers = preferences.showLineNumbers,
                                showToolbar = true
                            )

                            OutputPanel(
                                messages = outputMessages,
                                onClearOutput = { editorViewModel.clearOutput() },
                                isDarkTheme = isDarkTheme,
                                isVisible = isOutputPanelVisible,
                                onVisibilityChange = { isOutputPanelVisible = it }
                            )
                        }
                    }

                    if (isFilePickerDialogVisible) {
                        AlertDialog(
                            onDismissRequest = { isFilePickerDialogVisible = false },
                            title = { Text(stringResource(R.string.create_open_file_title)) },
                            text = {
                                Column {
                                    Text(stringResource(R.string.file_picker_dialog_message_1))
                                    Text(stringResource(R.string.file_picker_dialog_message_2))
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { isFilePickerDialogVisible = false }) {
                                    Text(stringResource(R.string.ok))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}