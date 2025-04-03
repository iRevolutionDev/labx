package software.revolution.labx.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Project(
    val name: String,
    val path: String,
    val type: ProjectType,
    val lastModified: Long,
    val lastOpenedDate: Long = System.currentTimeMillis()
) {
    val lastOpenedFormatted: String
        get() {
            val now = System.currentTimeMillis()
            val diff = now - lastOpenedDate

            return when {
                diff < 60 * 60 * 1000 -> "Just now"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
                diff < 48 * 60 * 60 * 1000 -> "Yesterday"
                else -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    sdf.format(Date(lastOpenedDate))
                }
            }
        }
}