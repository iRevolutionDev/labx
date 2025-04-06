package software.revolution.labx.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.FontAwesomeIcons
import compose.icons.TablerIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Save
import compose.icons.tablericons.Code
import compose.icons.tablericons.Copy
import compose.icons.tablericons.LayoutColumns
import kotlinx.coroutines.launch
import software.revolution.labx.ui.theme.EditorBackgroundDark
import software.revolution.labx.ui.theme.EditorBackgroundLight
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TabBar(
    tabs: List<EditorTab>,
    onTabSelected: (EditorTab) -> Unit,
    onTabClosed: (EditorTab) -> Unit,
    onTabsReordered: (List<EditorTab>) -> Unit = {},
    onSaveTab: (EditorTab) -> Unit = {},
    isDarkTheme: Boolean = false
) {
    if (tabs.isEmpty()) return

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LocalDensity.current

    val activeTabIndex = tabs.indexOfFirst { it.isActive }

    LaunchedEffect(activeTabIndex) {
        if (activeTabIndex != -1) {
            scope.launch {
                listState.animateScrollToItem(activeTabIndex)
            }
        }
    }

    var draggedTabIndex by remember { mutableIntStateOf(-1) }
    var draggedOffset by remember { mutableStateOf(Offset.Zero) }
    var currentReorderedTabs by remember { mutableStateOf(tabs) }

    LaunchedEffect(tabs) {
        currentReorderedTabs = tabs
    }

    fun swapTabs(fromIndex: Int, toIndex: Int) {
        if (fromIndex != toIndex && fromIndex in tabs.indices && toIndex in tabs.indices) {
            val newList = tabs.toMutableList()
            val item = newList.removeAt(fromIndex)
            newList.add(toIndex, item)
            currentReorderedTabs = newList
            onTabsReordered(newList)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        color = if (isDarkTheme) EditorBackgroundDark else EditorBackgroundLight
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(
                items = currentReorderedTabs,
                key = { _, tab -> tab.file.path }
            ) { index, tab ->
                val isDragged = index == draggedTabIndex

                val offset by animateFloatAsState(
                    targetValue = if (isDragged) draggedOffset.x else 0f,
                    label = "tabOffset"
                )

                val elevation by animateDpAsState(
                    targetValue = if (isDragged) 4.dp else 0.dp,
                    label = "tabElevation"
                )

                TabItem(
                    tab = tab,
                    isDarkTheme = isDarkTheme,
                    onTabSelected = { onTabSelected(tab) },
                    onTabClosed = { onTabClosed(tab) },
                    onSaveTab = { onSaveTab(tab) },
                    modifier = Modifier
                        .offset { IntOffset(offset.roundToInt(), 0) }
                        .shadow(elevation)
                        .pointerInput(tab.file.path) {
                            detectDragGestures(
                                onDragStart = { startPosition ->
                                    if (startPosition.x > 10f) {
                                        draggedTabIndex = index
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    if (draggedTabIndex == index) {
                                        change.consume()
                                        draggedOffset += dragAmount

                                        val dragX = draggedOffset.x
                                        val tabWidth = size.width

                                        if (dragX > tabWidth / 2 && index < tabs.lastIndex) {
                                            swapTabs(index, index + 1)
                                            draggedOffset = Offset.Zero
                                            draggedTabIndex = index + 1
                                        } else if (dragX < -tabWidth / 2 && index > 0) {
                                            swapTabs(index, index - 1)
                                            draggedOffset = Offset.Zero
                                            draggedTabIndex = index - 1
                                        }
                                    }
                                },
                                onDragEnd = {
                                    draggedOffset = Offset.Zero
                                    draggedTabIndex = -1
                                },
                                onDragCancel = {
                                    draggedOffset = Offset.Zero
                                    draggedTabIndex = -1
                                }
                            )
                        }
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: EditorTab,
    isDarkTheme: Boolean,
    onTabSelected: () -> Unit,
    onTabClosed: () -> Unit,
    onSaveTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    val backgroundColor = when {
        tab.isActive && isDarkTheme -> Color(0xFF1a1b26)
        tab.isActive && !isDarkTheme -> Color(0xFFF5F9FF)
        else -> Color.Transparent
    }

    val textColor = when {
        tab.isActive && isDarkTheme -> Color.White
        tab.isActive && !isDarkTheme -> Color(0xFF1F2937)
        isDarkTheme -> Color.White.copy(alpha = 0.7f)
        else -> Color(0xFF64748B)
    }

    Box(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = backgroundColor,
            modifier = Modifier
                .padding(end = 4.dp)
                .pointerInput(tab.file.path) {
                    detectTapGestures(
                        onLongPress = { showDropdown = true },
                        onTap = { onTabSelected() }
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = TablerIcons.Code,
                    contentDescription = null,
                    tint = if (tab.file.extension == "kt") Color(0xFF7C3AED) else textColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = tab.file.name,
                    color = textColor,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (tab.isModified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF7C3AED) else Color(0xFF4B5563))
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(18.dp)
                        .background(Color.Transparent)
                        .clickable { onTabClosed() }
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = textColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.background(
                if (isDarkTheme) Color(0xFF1a1b26) else Color.White
            )
        ) {
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Regular.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save")
                    }
                },
                onClick = {
                    onSaveTab()
                    showDropdown = false
                }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = TablerIcons.Copy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Path")
                    }
                },
                onClick = { showDropdown = false }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = TablerIcons.LayoutColumns,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Split Editor")
                    }
                },
                onClick = { showDropdown = false }
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = if (isDarkTheme) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFF43F5E)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Close",
                            color = Color(0xFFF43F5E)
                        )
                    }
                },
                onClick = {
                    onTabClosed()
                    showDropdown = false
                }
            )

            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = Color(0xFFF43F5E)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Close Others",
                            color = Color(0xFFF43F5E)
                        )
                    }
                },
                onClick = { showDropdown = false }
            )
        }
    }
}