package software.revolution.labx.domain.model

import java.io.File

data class FileItem(
    val id: String,
    val name: String,
    val path: String,
    val extension: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val file: File
)