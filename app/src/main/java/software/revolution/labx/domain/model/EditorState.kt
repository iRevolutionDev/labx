package software.revolution.labx.domain.model

data class EditorState(
    val currentFile: FileItem? = null,
    val content: String = "",
    val isModified: Boolean = false,
    val language: String = "",
    val cursorPosition: Int = 0,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0
)