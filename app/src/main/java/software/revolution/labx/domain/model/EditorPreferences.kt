package software.revolution.labx.domain.model

import androidx.compose.ui.graphics.Color

data class EditorPreferences(
    val isDarkMode: Boolean = false,
    val fontSize: Float = 14f,
    val showLineNumbers: Boolean = true,
    val tabSize: Int = 4,
    val wordWrap: Boolean = true,
    val autoSave: Boolean = false,
    val accentColor: Color
)