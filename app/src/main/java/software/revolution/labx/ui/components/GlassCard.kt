package software.revolution.labx.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import software.revolution.labx.ui.theme.AppDarkColors
import software.revolution.labx.ui.theme.AppLightColors

@Composable
fun GlassCard(
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme)
                AppDarkColors.CardBackground else AppLightColors.CardBackground
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFFFFFFF).copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        content()
    }
}