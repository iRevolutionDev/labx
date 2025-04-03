package software.revolution.labx.ui.components

import software.revolution.labx.domain.model.FileItem

data class EditorTab(
    val file: FileItem,
    val isActive: Boolean = false,
    val isModified: Boolean = false
)