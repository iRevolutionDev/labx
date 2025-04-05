package software.revolution.labx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Code
import compose.icons.feathericons.Edit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import software.revolution.labx.domain.model.FileAction
import software.revolution.labx.domain.model.FileItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionsBottomSheet(
    file: FileItem,
    onDismiss: () -> Unit,
    onRename: (FileItem) -> Unit,
    onOpen: (FileItem) -> Unit,
    onDelete: () -> Unit,
    sheetState: SheetState,
    isDarkTheme: Boolean
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDarkTheme) Color(0xFF1a1b26) else Color.White,
        tonalElevation = 4.dp,
        scrimColor = Color.Black.copy(alpha = 0.4f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .windowInsetsPadding(WindowInsets(bottom = 16.dp))
        ) {
            Text(
                text = "File Actions",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isDarkTheme) Color.White else Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDarkTheme) Color.Gray else Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val actions = listOf(
                FileAction("Rename", FeatherIcons.Edit, Color(0xFF3B82F6)) {
                    onRename(file)
                },
                FileAction("Open", FeatherIcons.Code, Color(0xFF22C55E)) {
                    onOpen(file)
                },
                FileAction("Delete", Icons.Default.Delete, Color(0xFFF43F5E)) {
                    onDelete()
                }
            )

            ActionButtons(
                actions = actions,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ActionButtons(
    actions: List<FileAction>,
    isDarkTheme: Boolean
) {
    rememberCoroutineScope()

    actions.forEachIndexed { index, action ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = 200,
                    delayMillis = 50 * index,
                    easing = LinearOutSlowInEasing
                )
            ) + slideInVertically(
                animationSpec = tween(
                    durationMillis = 200,
                    delayMillis = 50 * index,
                    easing = FastOutSlowInEasing
                )
            ) { it / 4 }
        ) {
            ActionButton(
                text = action.text,
                icon = action.icon,
                tint = action.tint,
                onClick = action.onClick,
                isDarkTheme = isDarkTheme,
                index = index
            )

            if (index < actions.size - 1) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    index: Int
) {
    var isButtonPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (isButtonPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale$index"
    )

    val coroutineScope = rememberCoroutineScope()

    Button(
        onClick = {
            isButtonPressed = true
            coroutineScope.launch {
                delay(100)
                isButtonPressed = false
                onClick()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp)
            .graphicsLayer {
                scaleX = buttonScale
                scaleY = buttonScale
            },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isDarkTheme) Color(0xFF2D2D3F) else Color(0xFFE6EDF5)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            color = if (text == "Delete") tint else if (isDarkTheme) Color.White else Color.Black,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}