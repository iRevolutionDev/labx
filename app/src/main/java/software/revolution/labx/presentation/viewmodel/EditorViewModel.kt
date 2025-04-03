package software.revolution.labx.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import software.revolution.labx.data.repository.EditorPreferencesRepository
import software.revolution.labx.data.repository.FileRepository
import software.revolution.labx.domain.model.EditorPreferences
import software.revolution.labx.domain.model.EditorState
import software.revolution.labx.domain.model.FileItem
import software.revolution.labx.domain.model.OutputMessage
import software.revolution.labx.domain.model.OutputType
import software.revolution.labx.domain.model.Result
import javax.inject.Inject

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val editorPreferencesRepository: EditorPreferencesRepository
) : ViewModel() {

    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private val _editorPreferences = MutableStateFlow<EditorPreferences?>(null)
    val editorPreferences: StateFlow<EditorPreferences?> = _editorPreferences.asStateFlow()

    private val _outputMessages = MutableStateFlow<List<OutputMessage>>(emptyList())
    val outputMessages: StateFlow<List<OutputMessage>> = _outputMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEditorPreferences()
    }

    private fun loadEditorPreferences() {
        viewModelScope.launch {
            editorPreferencesRepository.editorPreferences.collectLatest { preferences ->
                _editorPreferences.value = preferences
            }
        }
    }

    fun openFile(fileItem: FileItem) {
        viewModelScope.launch {
            fileRepository.getFileContent(fileItem).collectLatest { result ->
                when (result) {
                    is Result.Loading -> {
                        _isLoading.value = true
                    }

                    is Result.Success -> {
                        _isLoading.value = false
                        _editorState.update {
                            it.copy(
                                currentFile = fileItem,
                                content = result.data,
                                isModified = false,
                                language = determineLanguage(fileItem.extension)
                            )
                        }
                        addOutputMessage("Arquivo aberto: ${fileItem.name}", OutputType.INFO)
                    }

                    is Result.Error -> {
                        _isLoading.value = false
                        addOutputMessage(
                            "Erro ao abrir arquivo: ${result.message}",
                            OutputType.ERROR
                        )
                    }
                }
            }
        }
    }

    fun saveCurrentFile() {
        val currentState = _editorState.value
        val currentFile = currentState.currentFile

        if (currentFile == null) {
            addOutputMessage("Nenhum arquivo aberto para salvar", OutputType.WARNING)
            return
        }

        viewModelScope.launch {
            fileRepository.saveFileContent(currentFile, currentState.content)
                .collectLatest { result ->
                    when (result) {
                        is Result.Loading -> {
                            _isLoading.value = true
                        }

                        is Result.Success -> {
                            _isLoading.value = false
                            _editorState.update { it.copy(isModified = false) }
                            addOutputMessage(
                                "Arquivo salvo: ${currentFile.name}",
                                OutputType.SUCCESS
                            )
                        }

                        is Result.Error -> {
                            _isLoading.value = false
                            addOutputMessage(
                                "Erro ao salvar arquivo: ${result.message}",
                                OutputType.ERROR
                            )
                        }
                    }
                }
        }
    }

    fun updateContent(content: String) {
        _editorState.update {
            it.copy(
                content = content,
                isModified = it.currentFile != null && content != it.content
            )
        }
    }

    fun updateCursorPosition(position: Int) {
        _editorState.update { it.copy(cursorPosition = position) }
    }

    fun updateSelection(start: Int, end: Int) {
        _editorState.update {
            it.copy(
                selectionStart = start,
                selectionEnd = end
            )
        }
    }

    fun clearFile() {
        _editorState.value = EditorState()
        addOutputMessage("Clear editor", OutputType.INFO)
    }

    fun addOutputMessage(message: String, type: OutputType = OutputType.INFO) {
        val newMessage = OutputMessage(message = message, type = type)
        _outputMessages.update { currentList ->
            (currentList + newMessage).takeLast(100)
        }
    }

    fun clearOutput() {
        _outputMessages.value = emptyList()
    }

    private fun determineLanguage(extension: String): String {
        return when (extension.lowercase()) {
            "kt", "kts" -> "kotlin"
            "java" -> "java"
            "js" -> "javascript"
            "ts" -> "typescript"
            "html" -> "html"
            "css" -> "css"
            "xml" -> "xml"
            "json" -> "json"
            "md" -> "markdown"
            "py" -> "python"
            "c", "cpp", "h" -> "c"
            "cs" -> "csharp"
            "go" -> "go"
            "rb" -> "ruby"
            "php" -> "php"
            "rs" -> "rust"
            "swift" -> "swift"
            "dart" -> "dart"
            "sh", "bash" -> "shell"
            "sql" -> "sql"
            "yaml", "yml" -> "yaml"
            else -> "text"
        }
    }

    fun updateEditorPreference(preferences: EditorPreferences) {
        viewModelScope.launch {
            with(preferences) {
                editorPreferencesRepository.updateIsDarkMode(isDarkMode)
                editorPreferencesRepository.updateFontSize(fontSize)
                editorPreferencesRepository.updateShowLineNumbers(showLineNumbers)
                editorPreferencesRepository.updateTabSize(tabSize)
                editorPreferencesRepository.updateWordWrap(wordWrap)
                editorPreferencesRepository.updateAutoSave(autoSave)
                editorPreferencesRepository.updateAccentColor(accentColor.value.toLong())
            }
        }
    }
}