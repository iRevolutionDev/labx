package software.revolution.labx.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.LineAwesomeIcons
import compose.icons.feathericons.File
import compose.icons.feathericons.FileText
import compose.icons.feathericons.Image
import compose.icons.lineawesomeicons.FileCodeSolid
import compose.icons.lineawesomeicons.FilePdfSolid
import software.revolution.labx.model.EditorTab
import software.revolution.labx.ui.theme.BackgroundDark
import software.revolution.labx.ui.theme.BackgroundLight
import software.revolution.labx.ui.theme.PrimaryLight
import software.revolution.labx.ui.theme.SecondaryLight
import software.revolution.labx.ui.theme.SurfaceDark
import software.revolution.labx.ui.theme.SurfaceLight

@Composable
fun TabBar(
    tabs: List<EditorTab>,
    onTabSelected: (EditorTab) -> Unit,
    onTabClosed: (EditorTab) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isDarkTheme) SurfaceDark else SurfaceLight)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(end = 4.dp)
        ) {
            tabs.forEach { tab ->
                TabItem(
                    tab = tab,
                    onTabSelected = onTabSelected,
                    onTabClosed = onTabClosed,
                    isDarkTheme = isDarkTheme
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun TabItem(
    tab: EditorTab,
    onTabSelected: (EditorTab) -> Unit,
    onTabClosed: (EditorTab) -> Unit,
    isDarkTheme: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (tab.isActive) {
            if (isDarkTheme) SurfaceDark.copy(alpha = 0.9f) else SurfaceLight.copy(alpha = 0.9f)
        } else {
            if (isDarkTheme) BackgroundDark else BackgroundLight
        },
        label = "tabBackground"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (tab.isActive) 2.dp else 0.dp,
        label = "borderWidth"
    )

    Surface(
        modifier = Modifier
            .height(36.dp)
            .clip(MaterialTheme.shapes.small),
        color = backgroundColor,
        tonalElevation = if (tab.isActive) 2.dp else 0.dp,
        shadowElevation = if (tab.isActive) 2.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier
                .clickable { onTabSelected(tab) }
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .border(
                    width = borderWidth,
                    color = if (tab.isActive) PrimaryLight else backgroundColor,
                    shape = MaterialTheme.shapes.small
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val fileIcon = when (tab.file.extension.lowercase()) {
                "kt", "java" -> LineAwesomeIcons.FileCodeSolid
                "txt", "md" -> FeatherIcons.FileText
                "png", "jpg", "jpeg", "gif" -> FeatherIcons.Image
                "pdf" -> LineAwesomeIcons.FilePdfSolid
                else -> FeatherIcons.File
            }

            Icon(
                imageVector = fileIcon,
                contentDescription = "Tipo de arquivo",
                tint = when (tab.file.extension.lowercase()) {
                    "kt", "java" -> PrimaryLight
                    "txt", "md" -> SecondaryLight
                    "png", "jpg", "jpeg", "gif" -> Color(0xFF43A047)
                    "pdf" -> Color(0xFFF44336)
                    else -> if (isDarkTheme) Color.White else Color.Black
                },
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = tab.file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isDarkTheme) Color.White else Color.Black
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { onTabClosed(tab) },
                modifier = Modifier.size(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close tab",
                    tint = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}