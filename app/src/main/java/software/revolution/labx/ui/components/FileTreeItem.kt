package software.revolution.labx.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.FontAwesomeIcons
import compose.icons.feathericons.Code
import compose.icons.feathericons.FileText
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Folder
import compose.icons.fontawesomeicons.solid.FolderOpen
import kotlinx.coroutines.delay
import software.revolution.labx.domain.model.FileItem
import software.revolution.labx.ui.theme.PrimaryLight

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileTreeItem(
    file: FileItem,
    level: Int,
    isDarkTheme: Boolean,
    isRenaming: Boolean,
    isSelected: Boolean,
    isFolder: Boolean,
    isExpanded: Boolean,
    hasLoadingChildren: Boolean,
    onRename: (String) -> Unit,
    onCancelRename: () -> Unit,
    onConfirmRename: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    focusRequester: FocusRequester,
    newFileName: String,
    content: @Composable (() -> Unit)? = null
) {
    val offsetX by animateDpAsState(
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offsetX"
    )

    val backgroundColorAnimated by animateColorAsState(
        targetValue = if (isDarkTheme) {
            if (isSelected) Color(0xFF2D2D3F) else Color.Transparent
        } else {
            if (isSelected) Color(0xFFE6EDF5) else Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColorAnimation"
    )

    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scaleAnimation"
    )

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 0f else -90f,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "arrowRotation"
    )

    if (isRenaming) {
        Surface(
            color = backgroundColorAnimated,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = offsetX)
                .padding(start = (level * 16).dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(4.dp)
            ) {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { onRename(it) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDarkTheme) Color(0xFF7C3AED) else PrimaryLight,
                        unfocusedBorderColor = if (isDarkTheme) Color.Gray else Color.Gray.copy(
                            alpha = 0.5f
                        ),
                        focusedTextColor = if (isDarkTheme) Color.White else Color.Black,
                        unfocusedTextColor = if (isDarkTheme) Color.White else Color.Black
                    )
                )

                IconButton(onClick = { onConfirmRename() }) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirm",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = { onCancelRename() }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = Color(0xFFF43F5E),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
        Column {
            Surface(
                color = backgroundColorAnimated,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = offsetX)
                    .padding(start = (level * 16).dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .combinedClickable(
                        onClick = {
                            onClick()
                            isPressed = true
                        },
                        onLongClick = onLongClick
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    )
                ) {
                    if (isFolder) {
                        Box(
                            modifier = Modifier.size(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = if (isExpanded) "Collapse folder" else "Expand folder",
                                tint = if (isDarkTheme) Color.Gray else Color.Gray.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .graphicsLayer {
                                        rotationZ = arrowRotation
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Spacer(modifier = Modifier.width(20.dp))
                    }

                    val folderOpenProgress by animateFloatAsState(
                        targetValue = if (isFolder && isExpanded) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = LinearOutSlowInEasing
                        ),
                        label = "folderOpenProgress"
                    )

                    val fileIcon = when {
                        isFolder -> {
                            if (folderOpenProgress > 0.5f) FontAwesomeIcons.Solid.FolderOpen
                            else FontAwesomeIcons.Solid.Folder
                        }

                        file.extension == "kt" -> FeatherIcons.Code
                        file.extension == "java" -> FeatherIcons.Code
                        else -> FeatherIcons.FileText
                    }

                    val iconTint = when {
                        isFolder -> Color(0xFFFFB224)
                        file.extension == "kt" || file.extension == "java" -> Color(0xFF2196F3)
                        else -> if (isDarkTheme) Color.LightGray else Color.DarkGray
                    }

                    val iconScale = if (isFolder) {
                        animateFloatAsState(
                            targetValue = if (isExpanded) 1.1f else 1f,
                            animationSpec = tween(durationMillis = 200),
                            label = "iconScale"
                        ).value
                    } else 1f

                    Icon(
                        imageVector = fileIcon,
                        contentDescription = if (isFolder) "Folder" else "File",
                        tint = iconTint,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = file.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isDarkTheme) Color.White else Color.Black
                    )
                }
            }

            if (content != null) {
                content()
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun AnimatedFileTreeItem(
    file: FileItem,
    level: Int,
    index: Int,
    isDarkTheme: Boolean,
    isRenaming: Boolean,
    isSelected: Boolean,
    isExpanded: Boolean,
    hasLoadingChildren: Boolean,
    newFileName: String,
    onRename: (String) -> Unit,
    onCancelRename: () -> Unit,
    onConfirmRename: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    focusRequester: FocusRequester,
    content: @Composable (() -> Unit)? = null
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(30 * index.toLong())
        startAnimation = true
    }

    val slideInDelay = 30 * index
    val slideInDuration = 200

    key(file.path) {
        val offsetY by animateDpAsState(
            targetValue = if (startAnimation) 0.dp else (-15).dp,
            animationSpec = tween(
                durationMillis = slideInDuration,
                delayMillis = slideInDelay,
                easing = LinearOutSlowInEasing
            ),
            label = "offsetY-${file.path}"
        )

        val itemAlpha by animateFloatAsState(
            targetValue = if (startAnimation) 1f else 0f,
            animationSpec = tween(
                durationMillis = slideInDuration,
                delayMillis = slideInDelay
            ),
            label = "alpha-${file.path}"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .graphicsLayer(alpha = itemAlpha)
        ) {
            FileTreeItem(
                file = file,
                level = level,
                isDarkTheme = isDarkTheme,
                isRenaming = isRenaming,
                isSelected = isSelected,
                isFolder = file.isDirectory,
                isExpanded = isExpanded,
                hasLoadingChildren = hasLoadingChildren,
                onRename = onRename,
                onCancelRename = onCancelRename,
                onConfirmRename = onConfirmRename,
                onClick = onClick,
                onLongClick = onLongClick,
                focusRequester = focusRequester,
                newFileName = newFileName,
                content = content
            )
        }
    }
}

@Composable
fun LoadingFolderIndicator(level: Int, isDarkTheme: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ((level + 1) * 16).dp, top = 8.dp, bottom = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(16.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = if (isDarkTheme) Color(0xFF7C3AED) else PrimaryLight
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Carregando...",
                style = MaterialTheme.typography.bodySmall,
                color = if (isDarkTheme) Color.Gray else Color.Gray
            )
        }
    }
}

@Composable
fun EmptyFolderIndicator(level: Int, isDarkTheme: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = ((level + 1) * 16).dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "Pasta vazia",
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkTheme) Color.Gray else Color.Gray
        )
    }
}

@Composable
fun AnimatedFolderContent(
    isEmpty: Boolean,
    level: Int,
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isEmpty) {
            EmptyFolderIndicator(level, isDarkTheme)
        } else {
            content()
        }
    }
}