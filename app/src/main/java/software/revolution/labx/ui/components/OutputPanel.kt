package software.revolution.labx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronUp
import software.revolution.labx.R
import software.revolution.labx.domain.model.OutputMessage
import software.revolution.labx.domain.model.OutputType
import software.revolution.labx.ui.theme.PrimaryLight
import software.revolution.labx.ui.theme.SurfaceDark
import software.revolution.labx.ui.theme.SurfaceLight
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutputPanel(
    messages: List<OutputMessage>,
    onClearOutput: () -> Unit,
    isDarkTheme: Boolean,
    isVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
        exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(if (isDarkTheme) SurfaceDark else SurfaceLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.output),
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    IconButton(onClick = onClearOutput) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_output)
                        )
                    }

                    IconButton(onClick = { onVisibilityChange(false) }) {
                        Icon(
                            imageVector = TablerIcons.ChevronDown,
                            contentDescription = stringResource(R.string.hide_output)
                        )
                    }
                }
            }

            HorizontalDivider(
                color = if (isDarkTheme) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 200.dp),
                state = lazyListState
            ) {
                items(messages) { message ->
                    OutputMessageItem(message = message, isDarkTheme = isDarkTheme)
                    HorizontalDivider(
                        color = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color.Black.copy(
                            alpha = 0.05f
                        ),
                        thickness = 0.5.dp
                    )
                }

                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_output_messages),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                            )
                        }
                    }
                }
            }
        }
    }

    if (!isVisible) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { onVisibilityChange(true) },
                containerColor = if (isDarkTheme) SurfaceDark else SurfaceLight,
                contentColor = PrimaryLight,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = TablerIcons.ChevronUp,
                    contentDescription = stringResource(R.string.show_output)
                )
            }
        }
    }
}

@Composable
fun OutputMessageItem(
    message: OutputMessage,
    isDarkTheme: Boolean
) {
    val messageColor = when (message.type) {
        OutputType.INFO -> if (isDarkTheme) Color.White else Color.Black
        OutputType.WARNING -> Color(0xFFFFA000)
        OutputType.ERROR -> Color(0xFFE53935)
        OutputType.SUCCESS -> Color(0xFF43A047)
    }

    val icon = when (message.type) {
        OutputType.INFO -> Icons.Default.Info
        OutputType.WARNING -> Icons.Default.Warning
        OutputType.ERROR -> Icons.Default.Clear
        OutputType.SUCCESS -> Icons.Default.Check
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = message.type.name,
            tint = messageColor,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = message.message,
            style = MaterialTheme.typography.bodyMedium,
            color = messageColor,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formattedTime = dateFormat.format(message.timestamp)

        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodySmall,
            color = if (isDarkTheme) Color.LightGray else Color.DarkGray
        )
    }
}