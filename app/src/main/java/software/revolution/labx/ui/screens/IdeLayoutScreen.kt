package software.revolution.labx.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.TablerIcons
import compose.icons.feathericons.Play
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.FolderPlus
import compose.icons.fontawesomeicons.solid.Save
import compose.icons.tablericons.ChevronRight
import kotlinx.coroutines.launch
import software.revolution.labx.domain.model.EditorTab
import software.revolution.labx.domain.model.FileItem
import software.revolution.labx.domain.model.OutputType
import software.revolution.labx.presentation.viewmodel.EditorViewModel
import software.revolution.labx.presentation.viewmodel.FileExplorerViewModel
import software.revolution.labx.presentation.viewmodel.ProjectViewModel
import software.revolution.labx.ui.components.EditorComponent
import software.revolution.labx.ui.components.FileExplorer
import software.revolution.labx.ui.components.GlassCard
import software.revolution.labx.ui.components.OutputPanel
import software.revolution.labx.ui.components.TabBar
import software.revolution.labx.ui.theme.AppDarkColors
import software.revolution.labx.ui.theme.AppLightColors

@Composable
fun IdeLayoutScreen(
    onNavigateToSettings: () -> Unit = {},
    onExitToWelcome: () -> Unit = {},
    editorViewModel: EditorViewModel = hiltViewModel(),
    fileExplorerViewModel: FileExplorerViewModel = hiltViewModel(),
    projectViewModel: ProjectViewModel = hiltViewModel()
) {
    LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    LocalDensity.current

    val editorPreferences by editorViewModel.editorPreferences.collectAsStateWithLifecycle(null)
    val editorState by editorViewModel.editorState.collectAsStateWithLifecycle()
    val outputMessages by editorViewModel.outputMessages.collectAsStateWithLifecycle()

    val files by fileExplorerViewModel.files.collectAsStateWithLifecycle()
    val fileExplorerError by fileExplorerViewModel.error.collectAsStateWithLifecycle()
    val fileExplorerLoading by fileExplorerViewModel.isLoading.collectAsStateWithLifecycle()
    val currentDirectory by fileExplorerViewModel.currentDirectory.collectAsStateWithLifecycle()

    var sidebarOpen by rememberSaveable { mutableStateOf(true) }
    var currentProject by rememberSaveable { mutableStateOf("MyProject") }
    var bottomPanelOpen by rememberSaveable { mutableStateOf(false) }
    var bottomPanelHeightPercent by rememberSaveable { mutableFloatStateOf(0.3f) }
    var isDragging by rememberSaveable { mutableStateOf(false) }
    var moreMenuExpanded by rememberSaveable { mutableStateOf(false) }

    var totalHeight by remember { mutableIntStateOf(0) }
    var totalWidth by remember { mutableIntStateOf(0) }

    var openTabs by rememberSaveable { mutableStateOf<List<EditorTab>>(emptyList()) }

    LaunchedEffect(fileExplorerError) {
        fileExplorerError?.let { error ->
            editorViewModel.addOutputMessage(error, OutputType.ERROR)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    duration = androidx.compose.material3.SnackbarDuration.Short
                )
            }
            fileExplorerViewModel.clearError()
        }
    }

    fun loadFile(fileItem: FileItem) {
        editorViewModel.openFile(fileItem)

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

    fun handleDrag(offsetY: Float) {
        if (isDragging && totalHeight > 0) {
            val newHeightPercent = bottomPanelHeightPercent - (offsetY / totalHeight)
            bottomPanelHeightPercent = newHeightPercent.coerceIn(0.1f, 0.6f)
        }
    }

    editorPreferences?.let { preferences ->
        val isDarkTheme = preferences.isDarkMode
        val gradientBackground = if (isDarkTheme) {
            AppDarkColors.BackgroundGradient
        } else {
            AppLightColors.BackgroundGradient
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBackground)
                .padding()
                .onGloballyPositioned { coordinates ->
                    totalHeight = coordinates.size.height
                    totalWidth = coordinates.size.width
                }
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val maxWidth = this.maxWidth
            val maxHeight = this.maxHeight

            val isCompactWidth = maxWidth < 600.dp
            val sidebarWidth = if (isCompactWidth) maxWidth * 0.85f else 250.dp
            val bottomPanelHeight = maxHeight * bottomPanelHeightPercent

            val titleFontSize = if (isCompactWidth) 14.sp else 16.sp
            if (isCompactWidth) 12.sp else 14.sp

            val animatedSidebarWidth by animateDpAsState(
                targetValue = if (sidebarOpen) sidebarWidth else 0.dp,
                animationSpec = tween(300),
                label = "sidebarWidth"
            )

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { sidebarOpen = !sidebarOpen },
                        modifier = Modifier.size(if (isCompactWidth) 36.dp else 40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = if (sidebarOpen) "Close Sidebar" else "Open Sidebar",
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                    }

                    Button(
                        onClick = { /* Show projects */ },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) Color(0xFF1a1b26) else Color.White,
                        )
                    ) {
                        val projectColor = remember(currentProject) {
                            val colors = listOf(
                                Color(0xFF4CAF50),
                                Color(0xFF2196F3),
                                Color(0xFFFF9800),
                                Color(0xFFF44336),
                                Color(0xFF9C27B0),
                                Color(0xFF009688)
                            )
                            colors[currentProject.hashCode().rem(colors.size).coerceAtLeast(0)]
                        }

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(projectColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentProject,
                            color = if (isDarkTheme) Color(0xFFD4B2FF) else Color(0xFF7C3AED)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = TablerIcons.ChevronRight,
                            contentDescription = "Select Project",
                            modifier = Modifier.size(16.dp),
                            tint = if (isDarkTheme) Color.Gray else Color(0xFF64748B)
                        )
                    }

                    if (!isCompactWidth && editorState.currentFile != null) {
                        Icon(
                            imageVector = TablerIcons.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isDarkTheme) Color.Gray else Color(0xFF64748B)
                        )
                        Text(
                            text = "src",
                            color = if (isDarkTheme) Color.Gray else Color(0xFF64748B),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            imageVector = TablerIcons.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isDarkTheme) Color.Gray else Color(0xFF64748B)
                        )
                        Text(
                            text = editorState.currentFile?.name ?: "",
                            color = if (isDarkTheme) Color.White else Color.Black,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (!isCompactWidth) {
                        Button(
                            onClick = { /* Run code */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF22C55E).copy(alpha = 0.2f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = FeatherIcons.Play,
                                contentDescription = "Run",
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Run",
                                color = Color(0xFF22C55E)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { /* Run code */ },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .background(
                                    color = Color(0xFF22C55E).copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = FeatherIcons.Play,
                                contentDescription = "Run",
                                tint = Color(0xFF22C55E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onNavigateToSettings() },
                        modifier = Modifier.size(if (isCompactWidth) 36.dp else 40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (isDarkTheme) Color.White else Color.Black
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { moreMenuExpanded = true },
                            modifier = Modifier.size(if (isCompactWidth) 36.dp else 40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Actions",
                                tint = if (isDarkTheme) Color.White else Color.Black
                            )
                        }
                        DropdownMenu(
                            expanded = moreMenuExpanded,
                            onDismissRequest = { moreMenuExpanded = false },
                            modifier = Modifier.background(
                                if (isDarkTheme) Color(0xFF1a1b26) else Color.White
                            )
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = FontAwesomeIcons.Solid.FolderPlus,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("New Project")
                                    }
                                },
                                onClick = { moreMenuExpanded = false }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = FontAwesomeIcons.Solid.Save,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save Project")
                                    }
                                },
                                onClick = {
                                    saveCurrentFile()
                                    moreMenuExpanded = false
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = if (isDarkTheme) Color.White.copy(0.1f) else Color.Black.copy(
                                    0.1f
                                )
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color(0xFFF43F5E)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Exit to Welcome Screen",
                                            color = Color(0xFFF43F5E)
                                        )
                                    }
                                },
                                onClick = {
                                    moreMenuExpanded = false
                                    onExitToWelcome()
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(
                    color = if (isDarkTheme) Color.White.copy(0.1f) else Color.Black.copy(0.1f)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (animatedSidebarWidth > 0.dp) {
                            Surface(
                                modifier = Modifier
                                    .width(animatedSidebarWidth)
                                    .fillMaxHeight(),
                                color = if (isDarkTheme) Color(0xFF1a1b26) else Color.White,
                                tonalElevation = 1.dp
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = "Project Explorer",
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = titleFontSize),
                                        color = if (isDarkTheme) Color(0xFFD4B2FF) else Color(
                                            0xFF7C3AED
                                        ),
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    FileExplorer(
                                        files = files,
                                        onFileSelected = { fileItem -> loadFile(fileItem) },
                                        onNavigateToDirectory = { path ->
                                            fileExplorerViewModel.navigateToDirectory(path)
                                        },
                                        onNavigateUp = { fileExplorerViewModel.navigateUp() },
                                        onCreateFile = { fileName ->
                                            fileExplorerViewModel.createNewFile(fileName)
                                        },
                                        onDeleteFile = { fileItem ->
                                            fileExplorerViewModel.deleteFile(fileItem)
                                        },
                                        onRenameFile = { fileItem, newName ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    "Renamed ${fileItem.name} to $newName",
                                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                                )
                                            }
                                        },
                                        isLoading = fileExplorerLoading,
                                        isDarkTheme = isDarkTheme,
                                        currentPath = currentDirectory,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            TabBar(
                                tabs = openTabs,
                                onTabSelected = { tab -> loadFile(tab.file) },
                                onTabClosed = { tab ->
                                    openTabs = openTabs.filter { it.file.path != tab.file.path }
                                    if (tab.isActive && openTabs.isNotEmpty()) {
                                        val newActiveTab = openTabs.first()
                                        loadFile(newActiveTab.file)
                                    } else if (openTabs.isEmpty()) {
                                        editorViewModel.clearFile()
                                    }
                                },
                                onTabsReordered = { reorderedTabs ->
                                    openTabs = reorderedTabs
                                },
                                onSaveTab = { tab ->
                                    if (tab.file.path == editorState.currentFile?.path) {
                                        saveCurrentFile()
                                    }
                                },
                                isDarkTheme = isDarkTheme
                            )

                            HorizontalDivider(
                                color = if (isDarkTheme) Color.White.copy(0.1f) else Color.Black.copy(
                                    0.1f
                                )
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
                                showToolbar = !isCompactWidth
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(
                                        if (isDarkTheme) Color.White.copy(0.1f) else Color.Black.copy(
                                            0.05f
                                        )
                                    )
                                    .pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = { isDragging = true },
                                            onDragEnd = { isDragging = false },
                                            onDragCancel = { isDragging = false },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                handleDrag(dragAmount.y)
                                            }
                                        )
                                    }
                            )

                            AnimatedVisibility(
                                visible = bottomPanelOpen,
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = fadeOut()
                            ) {
                                OutputPanel(
                                    messages = outputMessages,
                                    onClearOutput = { editorViewModel.clearOutput() },
                                    isDarkTheme = isDarkTheme,
                                    isVisible = bottomPanelOpen,
                                    onVisibilityChange = { bottomPanelOpen = it },
                                    modifier = Modifier.height(bottomPanelHeight)
                                )
                            }
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.White.copy(
                        alpha = 0.7f
                    ),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isCompactWidth) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassCard(
                                    isDarkTheme = isDarkTheme,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Kotlin 1.8.0",
                                            color = if (isDarkTheme) Color(0xFFD4B2FF) else Color(
                                                0xFF7C3AED
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                GlassCard(
                                    isDarkTheme = isDarkTheme,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "JDK 17",
                                            color = if (isDarkTheme) Color(0xFF90CAF9) else Color(
                                                0xFF1E88E5
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "UTF-8",
                                    color = if (isDarkTheme) Color.Gray else Color(0xFF64748B),
                                    style = MaterialTheme.typography.bodySmall
                                )

                                GlassCard(
                                    isDarkTheme = isDarkTheme,
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 8.dp)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Line: 12, Col: 24",
                                            color = if (isDarkTheme) Color.LightGray else Color(
                                                0xFF334155
                                            ),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { bottomPanelOpen = !bottomPanelOpen },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (bottomPanelOpen)
                                            Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                        contentDescription = if (bottomPanelOpen) "Hide Output" else "Show Output",
                                        tint = if (isDarkTheme) Color.White else Color.Black
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Kotlin 1.8.0",
                                color = if (isDarkTheme) Color(0xFFD4B2FF) else Color(0xFF7C3AED),
                                style = MaterialTheme.typography.bodySmall
                            )

                            IconButton(
                                onClick = { bottomPanelOpen = !bottomPanelOpen },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (bottomPanelOpen)
                                        Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                    contentDescription = if (bottomPanelOpen) "Hide Output" else "Show Output",
                                    tint = if (isDarkTheme) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}