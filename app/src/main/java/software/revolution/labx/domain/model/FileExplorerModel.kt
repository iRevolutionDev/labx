package software.revolution.labx.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class FileAction(
    val text: String,
    val icon: ImageVector,
    val tint: Color,
    val onClick: () -> Unit
)

data class FileExplorerState(
    val expandedFolders: Map<String, Boolean> = emptyMap(),
    val folderContents: Map<String, List<FileItem>> = emptyMap(),
    val loadingFolders: Map<String, Boolean> = emptyMap(),
    val fileRenameStates: Map<String, Boolean> = emptyMap(),
    val selectedFile: FileItem? = null,
    val isFileActionsOpen: Boolean = false,
    val isAddingFile: Boolean = false,
    val newFileName: String = "",
    val newFilePath: String = "",
    val newFileType: String = "file",
    val newFileLanguage: String = "kotlin"
)

interface FileExplorerEvents {
    fun startRenaming(file: FileItem)
    fun confirmRename(file: FileItem)
    fun cancelRename(file: FileItem)
    fun handleCreateFile()
    fun openFileActions(file: FileItem)
    fun openFile(file: FileItem)
    fun handleDeleteFile()
    fun toggleFolderExpansion(folderPath: String)
    fun loadFolderContents(folderPath: String)
    fun updateNewFileName(name: String)
    fun updateNewFileType(type: String)
    fun updateNewFileLanguage(language: String)
    fun dismissFileActions()
    fun dismissAddFile()
}