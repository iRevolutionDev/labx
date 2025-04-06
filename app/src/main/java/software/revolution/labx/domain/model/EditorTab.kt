package software.revolution.labx.domain.model

data class EditorTab(
    val file: FileItem,
    val isActive: Boolean = false,
    val isModified: Boolean = false
)