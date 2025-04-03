package software.revolution.labx.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.TablerIcons
import compose.icons.tablericons.Code
import compose.icons.tablericons.File
import compose.icons.tablericons.Folder
import kotlinx.coroutines.delay
import software.revolution.labx.presentation.viewmodel.ProjectViewModel
import software.revolution.labx.ui.components.GlassCard
import software.revolution.labx.ui.theme.AppDarkColors
import software.revolution.labx.ui.theme.AppLightColors
import kotlin.math.absoluteValue

@Composable
fun WelcomeScreen(
    isDarkTheme: Boolean,
    onCreateProject: () -> Unit,
    onOpenProject: (String) -> Unit,
    onCreateFile: () -> Unit,
    projectViewModel: ProjectViewModel = hiltViewModel()
) {
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var animationPlayed by remember { mutableStateOf(false) }

    val projects by projectViewModel.recentProjects.collectAsStateWithLifecycle()
    val isLoading by projectViewModel.isLoading.collectAsStateWithLifecycle()
    val error by projectViewModel.error.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        animationPlayed = true
        projectViewModel.loadProjects()
    }

    error?.let {
        LaunchedEffect(it) {
            projectViewModel.clearError()
        }
    }

    val headerOffset by animateDpAsState(
        targetValue = if (animationPlayed) 0.dp else (-20).dp,
        animationSpec = tween(durationMillis = 500),
        label = "headerOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = if (isDarkTheme) AppDarkColors.BackgroundGradient else AppLightColors.BackgroundGradient)
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .offset(y = headerOffset),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8B5Cf6),
                                        Color(0xFF4F46E5)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = TablerIcons.Code,
                            contentDescription = "LabX Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "LabX",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isDarkTheme) Color.White else MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { settingsOpen = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (isDarkTheme) Color.LightGray else MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.7f
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 500),
                            initialOffsetY = { it / 5 }
                        )
            ) {
                GlassCard(
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Start Coding",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isDarkTheme) AppDarkColors.TextPrimary else AppLightColors.TextPrimary
                        )

                        Text(
                            text = "Create a new project or file",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) AppDarkColors.TextMuted else AppLightColors.TextMuted,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onCreateProject,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkTheme)
                                        AppDarkColors.Primary else AppLightColors.Primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "New Project",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = "New Project",
                                            color = Color.White,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Create a Java or Kotlin project",
                                            color = Color.White.copy(alpha = 0.7f),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = onCreateFile,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkTheme) Color(0xFF2D2D3F) else AppLightColors.SecondaryBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                color = if (isDarkTheme)
                                                    Color.White.copy(alpha = 0.2f)
                                                else AppLightColors.Primary.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = TablerIcons.File,
                                            contentDescription = "New File",
                                            tint = if (isDarkTheme)
                                                AppDarkColors.TextPrimary
                                            else AppLightColors.TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = "New File",
                                            color = if (isDarkTheme)
                                                AppDarkColors.TextPrimary
                                            else AppLightColors.TextPrimary,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Create a standalone file",
                                            color = if (isDarkTheme)
                                                AppDarkColors.TextMuted
                                            else AppLightColors.TextMuted,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = animationPlayed,
                enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 100)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 500, delayMillis = 100),
                            initialOffsetY = { it / 5 }
                        )
            ) {
                GlassCard(
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Recent Projects",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isDarkTheme) AppDarkColors.TextPrimary else AppLightColors.TextPrimary
                        )

                        Text(
                            text = "Pick up where you left off",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) AppDarkColors.TextMuted else AppLightColors.TextMuted,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (projects.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No projects found. Create a new project to get started!",
                                    color = if (isDarkTheme) AppDarkColors.TextMuted else AppLightColors.TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            projects.forEachIndexed { index, project ->
                                ProjectItem(
                                    project = project,
                                    isDarkTheme = isDarkTheme,
                                    delayMultiplier = index,
                                    onClick = { onOpenProject(project.path) }
                                )

                                if (index < projects.size - 1) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectItem(
    project: software.revolution.labx.presentation.viewmodel.Project,
    isDarkTheme: Boolean,
    delayMultiplier: Int = 0,
    onClick: () -> Unit
) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100L * delayMultiplier)
        animationPlayed = true
    }

    val offsetX by animateDpAsState(
        targetValue = if (animationPlayed) 0.dp else (-20).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offsetX"
    )

    val projectColor = remember(project.name) {
        val colors = listOf(
            Color(0xFF4CAF50),
            Color(0xFF2196F3),
            Color(0xFFFF9800),
            Color(0xFFF44336),
            Color(0xFF9C27B0),
            Color(0xFF009688)
        )

        val hash = project.name.hashCode().absoluteValue
        colors[hash % colors.size]
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = offsetX),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(projectColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = TablerIcons.Folder,
                    contentDescription = "Project Folder",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDarkTheme) AppDarkColors.TextPrimary else AppLightColors.TextPrimary
                )

                Text(
                    text = project.lastOpenedFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDarkTheme) AppDarkColors.TextMuted else AppLightColors.TextMuted
                )
            }
        }
    }
}