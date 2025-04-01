package software.revolution.labx.ui.screens

import android.os.Environment
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
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.TablerIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.regular.Clock
import compose.icons.fontawesomeicons.solid.Moon
import compose.icons.fontawesomeicons.solid.Save
import compose.icons.tablericons.Folder
import compose.icons.tablericons.FolderX
import kotlinx.coroutines.launch
import software.revolution.labx.R
import software.revolution.labx.model.EditorState
import software.revolution.labx.model.EditorTab
import software.revolution.labx.model.FileItem
import software.revolution.labx.model.OutputMessage
import software.revolution.labx.model.OutputType
import software.revolution.labx.ui.components.EditorComponent
import software.revolution.labx.ui.components.FileExplorer
import software.revolution.labx.ui.components.OutputPanel
import software.revolution.labx.ui.components.TabBar
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainEditorScreen(
    preferences: EditorPreferences = EditorPreferences(),
    onNavigateToSettings: () -> Unit = {}
) {
    val isDarkTheme = preferences.isDarkMode
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var editorState by remember {
        mutableStateOf(EditorState())
    }
    var openTabs by remember { mutableStateOf<List<EditorTab>>(emptyList()) }
    var outputMessages by remember { mutableStateOf<List<OutputMessage>>(emptyList()) }
    var isOutputPanelVisible by remember { mutableStateOf(false) }
    var isFileExplorerVisible by remember { mutableStateOf(true) }
    var isFilePickerDialogVisible by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }
    
    var editorStateMap by remember { mutableStateOf<Map<String, EditorState>>(emptyMap()) }

    val rootPath = Environment.getExternalStorageDirectory().absolutePath

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    fun loadFile(fileItem: FileItem) {
        try {
            val cachedState = editorStateMap[fileItem.path]
            if (cachedState != null) {
                editorState = cachedState
                
                openTabs = openTabs.map { tab -> 
                    if (tab.file.path == fileItem.path) {
                        tab.copy(isActive = true, isModified = cachedState.isModified)
                    } else {
                        tab.copy(isActive = false)
                    }
                }
                
                return
            }
            
            val fileSizeInMB = fileItem.size / (1024 * 1024)
            val MAX_SAFE_SIZE_MB = 5 // Limit files to 5MB to avoid memory issues
            
            if (fileSizeInMB > MAX_SAFE_SIZE_MB) {
                outputMessages = outputMessages + OutputMessage(
                    message = context.getString(R.string.file_too_large, fileSizeInMB, MAX_SAFE_SIZE_MB),
                    type = OutputType.WARNING
                )
                
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.file_too_large_snackbar),
                        duration = SnackbarDuration.Long
                    )
                }
                return
            }
            
            val fileContent = StringBuilder()
            fileItem.file.bufferedReader().use { reader ->
                val buffer = CharArray(8192)
                var charsRead: Int
                while (reader.read(buffer).also { charsRead = it } != -1) {
                    fileContent.append(buffer, 0, charsRead)
                    
                    if (fileContent.length > MAX_SAFE_SIZE_MB * 1024 * 1024) {
                        throw OutOfMemoryError(context.getString(R.string.file_too_large))
                    }
                }
            }

            val newEditorState = EditorState(
                currentFile = fileItem,
                content = fileContent.toString(),
                isModified = false,
                language = fileItem.extension
            )
            
            editorState = newEditorState
            
            val newTab = EditorTab(file = fileItem, isActive = true)
            openTabs = openTabs.map { it.copy(isActive = false) }.toMutableList().also {
                if (!it.any { tab -> tab.file.path == fileItem.path }) {
                    it.add(newTab)
                } else {
                    val index = it.indexOfFirst { tab -> tab.file.path == fileItem.path }
                    it[index] = it[index].copy(isActive = true)
                }
            }

            outputMessages = outputMessages + OutputMessage(
                message = context.getString(R.string.file_loaded, fileItem.name),
                type = OutputType.INFO
            )

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.file_loaded, fileItem.name),
                    duration = SnackbarDuration.Short
                )
            }
        } catch (e: IOException) {
            outputMessages = outputMessages + OutputMessage(
                message = context.getString(R.string.error_loading_file, e.message),
                type = OutputType.ERROR
            )

            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.error_loading_file_snackbar),
                    duration = SnackbarDuration.Long
                )
            }
        }
    }

    fun saveFile() {
        editorState.currentFile?.let { fileItem ->
            try {
                fileItem.file.bufferedWriter().use { writer ->
                    writer.write(editorState.content)
                }

                editorState = editorState.copy(isModified = false)
                editorStateMap = editorStateMap + (fileItem.path to editorState)
                
                openTabs = openTabs.map { tab ->
                    if (tab.file.path == fileItem.path) {
                        tab.copy(isModified = false)
                    } else {
                        tab
                    }
                }

                outputMessages = outputMessages + OutputMessage(
                    message = context.getString(R.string.file_saved, fileItem.name),
                    type = OutputType.SUCCESS
                )

                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.file_saved_snackbar),
                        duration = SnackbarDuration.Short
                    )
                }

                if (preferences.autoSave) {
                    outputMessages = outputMessages + OutputMessage(
                        message = context.getString(R.string.autosave_enabled),
                        type = OutputType.INFO
                    )
                }
            } catch (e: IOException) {
                outputMessages = outputMessages + OutputMessage(
                    message = context.getString(R.string.error_saving_file, e.message),
                    type = OutputType.ERROR
                )

                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.error_saving_file_snackbar),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

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
                    icon = { Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.menu_settings)) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                            onNavigateToSettings()
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_theme)) },
                    icon = {
                        Icon(
                            FontAwesomeIcons.Solid.Moon,
                            contentDescription = stringResource(R.string.menu_theme),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_recent)) },
                    icon = {
                        Icon(
                            FontAwesomeIcons.Regular.Clock,
                            contentDescription = stringResource(R.string.menu_recent),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_about)) },
                    icon = { Icon(Icons.Default.Info, contentDescription = stringResource(R.string.menu_about)) },
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
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
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { saveFile() }) {
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
                            Text(text = stringResource(R.string.output_count, outputMessages.size))
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
                            rootPath = rootPath,
                            onFileSelected = { fileItem ->
                                loadFile(fileItem)
                            },
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
                                openTabs =
                                    openTabs.map { it.copy(isActive = it.file.path == tab.file.path) }
                                loadFile(tab.file)
                            },
                            onTabClosed = { tab ->
                                openTabs = openTabs.filter { it.file.path != tab.file.path }

                                if (tab.isActive && openTabs.isNotEmpty()) {
                                    val newActiveTab = openTabs.first()
                                    openTabs = openTabs.map {
                                        if (it == newActiveTab) it.copy(isActive = true) else it
                                    }
                                    loadFile(newActiveTab.file)
                                } else if (openTabs.isEmpty()) {
                                    editorState = EditorState()
                                }
                            },
                            isDarkTheme = isDarkTheme
                        )

                        EditorComponent(
                            editorState = editorState,
                            onEditorStateChange = { newState ->
                                val updatedState = if (newState.currentFile == null && editorState.currentFile != null) {
                                    newState.copy(currentFile = editorState.currentFile)
                                } else {
                                    newState
                                }
                                
                                editorState = updatedState
                                
                                if (updatedState.currentFile != null) {
                                    editorStateMap = editorStateMap + (updatedState.currentFile.path to updatedState)
                                    
                                    openTabs = openTabs.map { tab ->
                                        if (tab.file.path == updatedState.currentFile.path) {
                                            tab.copy(isModified = updatedState.isModified)
                                        } else {
                                            tab
                                        }
                                    }
                                }

                                if (preferences.autoSave && updatedState.isModified && updatedState.currentFile != null) {
                                    try {
                                        updatedState.currentFile.file.writeText(updatedState.content)
                                        editorState = updatedState.copy(isModified = false)
                                        
                                        editorStateMap = editorStateMap + (updatedState.currentFile.path to editorState)
                                        openTabs = openTabs.map { tab ->
                                            if (tab.file.path == updatedState.currentFile.path) {
                                                tab.copy(isModified = false)
                                            } else {
                                                tab
                                            }
                                        }
                                    } catch (e: Exception) {
                                        outputMessages = outputMessages + OutputMessage(
                                            message = context.getString(R.string.error_autosave, e.message),
                                            type = OutputType.WARNING
                                        )
                                    }
                                }
                            },
                            isDarkTheme = isDarkTheme,
                            modifier = Modifier.weight(1f),
                            showLineNumbers = preferences.showLineNumbers,
                            showToolbar = true
                        )

                        OutputPanel(
                            messages = outputMessages,
                            onClearOutput = { outputMessages = emptyList() },
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