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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.dsl.languages
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.subscribeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.eclipse.tm4e.core.registry.IThemeSource
import software.revolution.labx.R
import software.revolution.labx.domain.model.EditorState
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
    val coroutineScope = rememberCoroutineScope()

    var editor by remember { mutableStateOf<CodeEditor?>(null) }

    var stableContent by remember(editorState.currentFile?.path) {
        mutableStateOf(editorState.content)
    }

    var cursorPosition by rememberSaveable { mutableStateOf("Line: 1, Col: 1") }
    var wordCount by rememberSaveable { mutableIntStateOf(0) }

    var prevLineNumbers by remember { mutableStateOf(showLineNumbers) }
    var prevWordWrap by remember { mutableStateOf(wordWrap) }
    var prevFontSize by remember { mutableFloatStateOf(fontSize) }
    var prevIsDarkTheme by remember { mutableStateOf(isDarkTheme) }
    var prevEditorTheme by remember { mutableStateOf(editorState.theme) }

    val currentFile = remember(editorState.currentFile?.path) { editorState.currentFile }

    val fileExtension by remember(currentFile) {
        derivedStateOf { currentFile?.extension?.uppercase() ?: "TXT" }
    }

    LaunchedEffect(stableContent) {
        if (stableContent.isNotEmpty()) {
            wordCount = stableContent.split(Regex("\\s+")).count { it.isNotEmpty() }
        }
    }

    LaunchedEffect(currentFile?.extension) {
        editor?.let { currentEditor ->
            val languageScopeName = when (currentFile?.extension?.lowercase()) {
                "java" -> "source.java"
                "kotlin" -> "source.kotlin"
                else -> "text.plain"
            }

            val language = TextMateLanguage.create(
                languageScopeName, true
            )

            editor?.setEditorLanguage(language)
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    editor?.let { currentEditor ->
                        val currentContent = currentEditor.text.toString()
                        if (currentContent != stableContent) {
                            stableContent = currentContent
                            onEditorStateChange(
                                editorState.copy(
                                    content = currentContent,
                                    isModified = true,
                                    cursorPosition = currentEditor.cursor.leftLine * 100 + currentEditor.cursor.leftColumn,
                                    selectionStart = currentEditor.cursor.left,
                                    selectionEnd = currentEditor.cursor.right
                                )
                            )
                        }
                    }
                }

                Lifecycle.Event.ON_RESUME -> {
                    editor?.let { currentEditor ->
                        if (currentEditor.text.toString() != stableContent) {
                            currentEditor.setText(stableContent)
                        }
                    }
                }

                else -> {}
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
                        val content = text.toString()
                        stableContent = content
                        onEditorStateChange(
                            editorState.copy(
                                content = content,
                                isModified = false
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
                        stableContent,
                        fontSize,
                        editorState.language,
                        editorState.theme
                    ).also { newEditor ->
                        editor = newEditor

                        newEditor.subscribeEvent<ContentChangeEvent> { event, _ ->
                            val content = event.changedText
                            wordCount = content.split(Regex("\\s+")).count { it.isNotEmpty() }

                            coroutineScope.launch(Dispatchers.Main) {
                                onEditorStateChange(
                                    editorState.copy(
                                        content = content.toString(),
                                        isModified = true,
                                        cursorPosition = newEditor.cursor.leftLine * 100 + newEditor.cursor.leftColumn,
                                        selectionStart = newEditor.cursor.left,
                                        selectionEnd = newEditor.cursor.right
                                    )
                                )
                            }
                        }

                        newEditor.subscribeEvent<SelectionChangeEvent> { _, _ ->
                            val cursor = newEditor.cursor
                            cursorPosition =
                                "Line: ${cursor.leftLine + 1}, Col: ${cursor.leftColumn + 1}"

                            coroutineScope.launch(Dispatchers.Main) {
                                onEditorStateChange(
                                    editorState.copy(
                                        cursorPosition = cursor.leftLine * 100 + cursor.leftColumn,
                                        selectionStart = cursor.left,
                                        selectionEnd = cursor.right
                                    )
                                )
                            }
                        }
                    }
                },
                update = { view ->
                    if (prevLineNumbers != showLineNumbers) {
                        view.isLineNumberEnabled = showLineNumbers
                        prevLineNumbers = showLineNumbers
                    }

                    if (prevWordWrap != wordWrap) {
                        view.isWordwrap = wordWrap
                        prevWordWrap = wordWrap
                    }

                    if (prevFontSize != fontSize) {
                        view.setTextSize(fontSize)
                        prevFontSize = fontSize
                    }

                    if (prevIsDarkTheme != isDarkTheme || prevEditorTheme != editorState.theme) {
                        applyTheme(view, editorState.theme)
                        prevIsDarkTheme = isDarkTheme
                        prevEditorTheme = editorState.theme
                    }

                    if (view.tag != editorState.currentFile?.path && editorState.currentFile != null) {
                        view.tag = editorState.currentFile.path
                        val line = 0
                        val col = 0

                        view.setText(stableContent)
                        try {
                            view.setSelection(line, col)
                        } catch (_: Exception) {
                        }
                    }
                },

                onRelease = {
                    editor?.release()
                },
                modifier = Modifier.fillMaxSize()
            )

            if (editorState.currentFile == null) {
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
    fontSize: Float = 14f,
    fileLanguage: String? = null,
    theme: String? = null
): CodeEditor {
    val editor = CodeEditor(context)

    editor.layoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    loadThemes(context)

    applyTheme(editor, theme)
    editor.setText(initialText)

    val languageScopeName = when (fileLanguage?.lowercase()) {
        "java" -> "source.java"
        "kotlin" -> "source.kotlin"
        else -> "text.plain"
    }
    val language = TextMateLanguage.create(
        languageScopeName, true
    )

    editor.setEditorLanguage(language)

    editor.isWordwrap = true
    editor.nonPrintablePaintingFlags = 0
    editor.isLineNumberEnabled = true
    editor.setTextSize(fontSize)
    editor.setLineSpacing(2f, 1.2f)

    return editor
}

private fun loadThemes(context: Context) {
    FileProviderRegistry.getInstance().addFileProvider(
        AssetsFileResolver(
            context.assets
        )
    )

    val themeRegistry = ThemeRegistry.getInstance()

    try {
        val availableThemes = context.assets.list("editor/themes")?.filter {
            it.endsWith(".json")
        }?.mapNotNull {
            it.substringBeforeLast(".json")
        } ?: listOf("darcula", "light")

        for (availableTheme in availableThemes) {
            val themePath = "editor/themes/${availableTheme}.json"
            try {
                themeRegistry.loadTheme(
                    ThemeModel(
                        IThemeSource.fromInputStream(
                            FileProviderRegistry.getInstance().tryGetInputStream(themePath),
                            themePath,
                            null
                        ),
                        availableTheme
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()

        try {
            val darculaPath = "editor/themes/darcula.json"
            themeRegistry.loadTheme(
                ThemeModel(
                    IThemeSource.fromInputStream(
                        FileProviderRegistry.getInstance().tryGetInputStream(darculaPath),
                        darculaPath,
                        null
                    ),
                    "darcula"
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun applyTheme(editor: CodeEditor, theme: String? = "darcula") {
    try {
        val themeRegistry = ThemeRegistry.getInstance()
        val grammarRegistry = GrammarRegistry.getInstance()
        val themeName = theme ?: "darcula"

        themeRegistry.setTheme(themeName)
        grammarRegistry.loadGrammars(
            languages {
                language("java") {
                    grammar = "editor/textmate/java/syntaxes/java.tmLanguage.json"
                    defaultScopeName()
                    languageConfiguration = "editor/textmate/java/language-configuration.json"
                }
            }
        )

        val editorColorScheme = TextMateColorScheme.create(themeRegistry)
        editor.colorScheme = editorColorScheme
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
