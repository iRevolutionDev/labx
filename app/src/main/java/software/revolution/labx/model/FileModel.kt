package software.revolution.labx.model

import java.io.File
import java.time.Instant

/**
 * Model representing a file or directory in the file manager
 */
data class FileItem(
    val file: File,
    val name: String = file.name,
    val isDirectory: Boolean = file.isDirectory,
    val path: String = file.absolutePath,
    val lastModified: Instant = Instant.ofEpochMilli(file.lastModified()),
    val size: Long = if (file.isDirectory) 0 else file.length(),
    val extension: String = if (file.isDirectory) "" else file.extension,
    val isSelected: Boolean = false,
    val children: List<FileItem>? = null
)

/**
 * Model representing the current editor state
 */
data class EditorState(
    val currentFile: FileItem? = null,
    val content: String = "",
    val isModified: Boolean = false,
    val cursorPosition: Int = 0,
    val scrollPosition: Float = 0f,
    val language: String = "text"
)

/**
 * Model representing a tab in the editor
 */
data class EditorTab(
    val file: FileItem,
    val isActive: Boolean = false,
    val isModified: Boolean = false
)

/**
 * Model representing an output message from the console
 */
data class OutputMessage(
    val message: String,
    val type: OutputType = OutputType.INFO,
    val timestamp: Instant = Instant.now()
)

enum class OutputType {
    INFO, WARNING, ERROR, SUCCESS
}