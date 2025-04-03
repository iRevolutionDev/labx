package software.revolution.labx.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

data class Template(
    val id: TemplateType,
    val name: String,
    val description: String,
    val icon: ImageVector
)