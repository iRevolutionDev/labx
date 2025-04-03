package software.revolution.labx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.File
import compose.icons.feathericons.Folder
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.FileCode
import compose.icons.fontawesomeicons.solid.FileImage
import compose.icons.fontawesomeicons.solid.FilePdf
import compose.icons.fontawesomeicons.solid.FileVideo
import software.revolution.labx.R
import software.revolution.labx.domain.model.FileItem

@Composable
fun FileItemComponent(
    fileItem: FileItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fileIcon = when {
            fileItem.isDirectory -> FeatherIcons.Folder
            fileItem.extension in listOf(
                "jpg",
                "jpeg",
                "png",
                "gif",
                "bmp",
                "webp"
            ) -> FontAwesomeIcons.Solid.FileImage

            fileItem.extension in listOf(
                "mp4",
                "avi",
                "mov",
                "mkv",
                "webm"
            ) -> FontAwesomeIcons.Solid.FileVideo

            fileItem.extension == "pdf" -> FontAwesomeIcons.Solid.FilePdf
            fileItem.extension in listOf(
                "java",
                "kt",
                "js",
                "py",
                "c",
                "cpp",
                "h",
                "html",
                "css",
                "xml",
                "json"
            ) -> FontAwesomeIcons.Solid.FileCode

            else -> FeatherIcons.File
        }

        val iconTint = when {
            fileItem.isDirectory -> Color(0xFF42A5F5)
            fileItem.extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp") -> Color(
                0xFF4CAF50
            )

            fileItem.extension in listOf("mp4", "avi", "mov", "mkv", "webm") -> Color(0xFFF44336)
            fileItem.extension == "pdf" -> Color(0xFFFF5722)
            fileItem.extension in listOf(
                "java",
                "kt",
                "js",
                "py",
                "c",
                "cpp",
                "h",
                "html",
                "css",
                "xml",
                "json"
            ) -> Color(0xFF9C27B0)

            else -> if (isDarkTheme) Color.White else Color.Black
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fileIcon,
                contentDescription = if (fileItem.isDirectory)
                    stringResource(R.string.folder_icon)
                else
                    stringResource(R.string.file_icon),
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = fileItem.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_file),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}