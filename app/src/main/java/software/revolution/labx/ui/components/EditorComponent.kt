package software.revolution.labx.ui.components

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Indent
import compose.icons.fontawesomeicons.solid.Redo
import compose.icons.fontawesomeicons.solid.Save
import compose.icons.fontawesomeicons.solid.Undo
import io.github.rosemoe.sora.event.ContentChangeEvent
import io.github.rosemoe.sora.event.SelectionChangeEvent
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.github.rosemoe.sora.widget.subscribeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import software.revolution.labx.R
import software.revolution.labx.model.EditorState
import software.revolution.labx.ui.theme.BackgroundDark
import software.revolution.labx.ui.theme.BackgroundLight
import software.revolution.labx.ui.theme.EditorBackgroundDark
import software.revolution.labx.ui.theme.EditorBackgroundLight
import software.revolution.labx.ui.theme.PrimaryLight
import software.revolution.labx.ui.theme.SurfaceDark
import software.revolution.labx.ui.theme.SurfaceLight

@Composable
fun EditorComponent(
    editorState: EditorState,
    onEditorStateChange: (EditorState) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    showLineNumbers: Boolean = true,
    showToolbar: Boolean = true,
    fontSize: Float = 14f,
    wordWrap: Boolean = true
) {
    LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var editor by remember { mutableStateOf<CodeEditor?>(null) }

    var cursorPosition by remember { mutableStateOf("Linha: 1, Col: 1") }
    var wordCount by remember { mutableIntStateOf(0) }

    val currentFile = remember(editorState.currentFile) { editorState.currentFile }

    val fileExtension by remember(currentFile) {
        derivedStateOf { currentFile?.extension?.uppercase() ?: "TXT" }
    }

    LaunchedEffect(editorState) {
        if (editorState.currentFile == null && currentFile != null) {
            onEditorStateChange(editorState.copy(currentFile = currentFile))
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                editor?.let { currentEditor ->
                    val currentContent = currentEditor.text.toString()
                    if (currentContent != editorState.content) {
                        onEditorStateChange(
                            editorState.copy(
                                content = currentContent,
                                isModified = true,
                                currentFile = editorState.currentFile ?: currentFile
                            )
                        )
                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) EditorBackgroundDark else EditorBackgroundLight)
    ) {
        if (showToolbar) {
            EditorToolbar(
                onSave = {
                    editor?.text?.let { text ->
                        onEditorStateChange(
                            editorState.copy(
                                content = text.toString(),
                                isModified = false,
                                currentFile = editorState.currentFile ?: currentFile
                            )
                        )
                    }
                },
                onUndo = { editor?.undo() },
                onRedo = { editor?.redo() },
                canUndo = editor?.canUndo() == true,
                canRedo = editor?.canRedo() == true,
                isModified = editorState.isModified,
                isDarkTheme = isDarkTheme
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    createEditorView(
                        ctx,
                        editorState.content,
                        isDarkTheme,
                        fontSize
                    ).also { newEditor ->
                        editor = newEditor

                        newEditor.subscribeEvent<ContentChangeEvent> { _, _ ->
                            val content = newEditor.text.toString()
                            wordCount = content.split(Regex("\\s+")).count { it.isNotEmpty() }

                            coroutineScope.launch(Dispatchers.Main) {
                                val fileToUse = editorState.currentFile ?: currentFile
                                withContext(Dispatchers.Main) {
                                    onEditorStateChange(
                                        editorState.copy(
                                            content = content,
                                            isModified = true,
                                            currentFile = fileToUse
                                        )
                                    )
                                }
                            }
                        }

                        newEditor.subscribeEvent<SelectionChangeEvent> { _, _ ->
                            val cursor = newEditor.cursor
                            cursorPosition =
                                "Line: ${cursor.leftLine + 1}, Col: ${cursor.leftColumn + 1}"
                        }
                    }
                },
                update = { view ->
                    if (view.text.toString() != editorState.content) {
                        val line = view.cursor.leftLine
                        val col = view.cursor.leftColumn

                        view.setText(editorState.content)

                        try {
                            if (line < view.lineCount) {
                                view.setSelection(
                                    line,
                                    col.coerceAtMost(view.text.getColumnCount(line))
                                )
                            }
                        } catch (_: Exception) {
                        }
                    }

                    view.isLineNumberEnabled = showLineNumbers
                    view.isWordwrap = wordWrap
                    view.setTextSize(fontSize)
                    applyTheme(view, isDarkTheme)
                },
                modifier = Modifier.fillMaxSize()
            )

            if (editorState.currentFile == null && currentFile == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isDarkTheme) SurfaceDark.copy(alpha = 0.8f) else SurfaceLight.copy(
                                alpha = 0.8f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.select_file_to_edit),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        EditorStatusBar(
            cursorPosition = cursorPosition,
            wordCount = wordCount,
            fileType = fileExtension,
            isDarkTheme = isDarkTheme
        )
    }
}

@Composable
private fun EditorToolbar(
    onSave: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    isModified: Boolean,
    isDarkTheme: Boolean
) {
    Surface(
        color = if (isDarkTheme) SurfaceDark else SurfaceLight,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onSave) {
                val saveIconColor = if (isModified) 1f else 0.5f
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Save,
                    contentDescription = stringResource(R.string.save),
                    tint = PrimaryLight.copy(alpha = saveIconColor),
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
            )

            IconButton(
                onClick = onUndo,
                enabled = canUndo
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Undo,
                    contentDescription = stringResource(R.string.undo),
                    tint = if (canUndo) {
                        if (isDarkTheme) Color.White else Color.Black
                    } else {
                        if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onRedo,
                enabled = canRedo
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Redo,
                    contentDescription = stringResource(R.string.redo),
                    tint = if (canRedo) {
                        if (isDarkTheme) Color.White else Color.Black
                    } else {
                        if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp),
                color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
            )

            IconButton(onClick = { }) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Indent,
                    contentDescription = stringResource(R.string.format_code),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.find_replace)
                )
            }
        }
    }
}

@Composable
private fun EditorStatusBar(
    cursorPosition: String,
    wordCount: Int,
    fileType: String,
    isDarkTheme: Boolean
) {
    Surface(
        color = if (isDarkTheme) SurfaceDark else SurfaceLight,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = cursorPosition,
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkTheme) Color.LightGray else Color.DarkGray
            )

            Text(
                text = stringResource(R.string.word_count, wordCount),
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkTheme) Color.LightGray else Color.DarkGray
            )

            Text(
                text = fileType,
                style = MaterialTheme.typography.bodySmall,
                color = PrimaryLight
            )
        }
    }
}

private fun createEditorView(
    context: Context,
    initialText: String,
    isDarkTheme: Boolean,
    fontSize: Float = 14f
): CodeEditor {
    val editor = CodeEditor(context)

    editor.layoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    applyTheme(editor, isDarkTheme)
    editor.setText(initialText)

    editor.isWordwrap = true
    editor.nonPrintablePaintingFlags = 0
    editor.isLineNumberEnabled = true
    editor.setTextSize(fontSize)
    editor.setLineSpacing(2f, 1.2f)

    return editor
}

private fun applyTheme(editor: CodeEditor, isDarkTheme: Boolean) {
    val scheme = if (isDarkTheme) {
        EditorColorScheme().apply {
            setColor(EditorColorScheme.WHOLE_BACKGROUND, EditorBackgroundDark.toArgb())
            setColor(EditorColorScheme.TEXT_NORMAL, Color.White.toArgb())
            setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, SurfaceDark.toArgb())
            setColor(EditorColorScheme.LINE_NUMBER, Color.LightGray.toArgb())
            setColor(EditorColorScheme.LINE_DIVIDER, Color.DarkGray.toArgb())
            setColor(EditorColorScheme.SELECTION_INSERT, PrimaryLight.toArgb())
            setColor(EditorColorScheme.SELECTION_HANDLE, PrimaryLight.toArgb())
            setColor(EditorColorScheme.CURRENT_LINE, BackgroundDark.copy(alpha = 0.3f).toArgb())
        }
    } else {
        EditorColorScheme().apply {
            setColor(EditorColorScheme.WHOLE_BACKGROUND, EditorBackgroundLight.toArgb())
            setColor(EditorColorScheme.TEXT_NORMAL, Color.Black.toArgb())
            setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, SurfaceLight.toArgb())
            setColor(EditorColorScheme.LINE_NUMBER, Color.DarkGray.toArgb())
            setColor(EditorColorScheme.LINE_DIVIDER, Color.LightGray.toArgb())
            setColor(EditorColorScheme.SELECTION_INSERT, PrimaryLight.toArgb())
            setColor(EditorColorScheme.SELECTION_HANDLE, PrimaryLight.toArgb())
            setColor(EditorColorScheme.CURRENT_LINE, BackgroundLight.copy(alpha = 0.3f).toArgb())
        }
    }

    editor.colorScheme = scheme
}