package software.revolution.labx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.LineAwesomeIcons
import compose.icons.TablerIcons
import compose.icons.feathericons.File
import compose.icons.feathericons.FileText
import compose.icons.feathericons.Image
import compose.icons.lineawesomeicons.FileCodeSolid
import compose.icons.lineawesomeicons.FilePdfSolid
import compose.icons.tablericons.ChevronRight
import compose.icons.tablericons.Folder
import software.revolution.labx.model.FileItem
import software.revolution.labx.ui.theme.AccentColor
import software.revolution.labx.ui.theme.PrimaryDark
import software.revolution.labx.ui.theme.PrimaryLight
import software.revolution.labx.ui.theme.SecondaryLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FileItemComponent(
    fileItem: FileItem,
    isExpanded: Boolean,
    onFileClick: (FileItem) -> Unit,
    onExpandToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val backgroundColor = if (fileItem.isSelected) {
        if (isDarkTheme) PrimaryDark.copy(alpha = 0.2f) else PrimaryLight.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable { onFileClick(fileItem) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (fileItem.isDirectory) {
                IconButton(
                    onClick = { onExpandToggle() },
                    modifier = Modifier.size(24.dp)
                ) {
                    val rotationState = animateFloatAsState(if (isExpanded) 90f else 0f)
                    Icon(
                        imageVector = TablerIcons.ChevronRight,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotationState.value),
                        tint = if (isDarkTheme) Color.White else Color.Black
                    )
                }
                Icon(
                    imageVector = TablerIcons.Folder,
                    contentDescription = "Folder",
                    tint = AccentColor
                )
            } else {
                Spacer(modifier = Modifier.width(24.dp))
                val fileIcon = when (fileItem.extension.lowercase()) {
                    "kt", "java" -> LineAwesomeIcons.FileCodeSolid
                    "txt", "md" -> FeatherIcons.FileText
                    "png", "jpg", "jpeg", "gif" -> FeatherIcons.Image
                    "pdf" -> LineAwesomeIcons.FilePdfSolid
                    else -> FeatherIcons.File
                }
                Icon(
                    imageVector = fileIcon,
                    contentDescription = "File",
                    tint = when (fileItem.extension.lowercase()) {
                        "kt", "java" -> PrimaryLight
                        "txt", "md" -> SecondaryLight
                        "png", "jpg", "jpeg", "gif" -> Color(0xFF43A047)
                        "pdf" -> Color(0xFFF44336)
                        else -> if (isDarkTheme) Color.White else Color.Black
                    }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = fileItem.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (!fileItem.isDirectory) {
                val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val date = Date.from(fileItem.lastModified)
                Text(
                    text = simpleDateFormat.format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded && fileItem.isDirectory && fileItem.children != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                fileItem.children?.forEach { child ->
                    var childExpanded by remember { mutableStateOf(false) }
                    FileItemComponent(
                        fileItem = child,
                        isExpanded = childExpanded,
                        onFileClick = onFileClick,
                        onExpandToggle = { childExpanded = !childExpanded },
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}