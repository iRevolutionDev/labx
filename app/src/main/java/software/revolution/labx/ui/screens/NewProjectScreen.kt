package software.revolution.labx.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.ChevronRight
import compose.icons.feathericons.Code
import compose.icons.feathericons.Layers
import compose.icons.feathericons.Zap
import software.revolution.labx.domain.model.CreationMode
import software.revolution.labx.domain.model.ProjectConfigState
import software.revolution.labx.domain.model.ProjectType
import software.revolution.labx.domain.model.Template
import software.revolution.labx.domain.model.TemplateType
import software.revolution.labx.presentation.viewmodel.EditorViewModel
import software.revolution.labx.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NewProjectScreen(
    editorViewModel: EditorViewModel = hiltViewModel(),
    navController: NavController = rememberNavController(),
    onCreateProject: (ProjectConfigState) -> Unit = {}
) {
    var creationMode by remember { mutableStateOf<CreationMode>(CreationMode.SIMPLE) }
    var previousMode by remember { mutableStateOf<CreationMode>(CreationMode.SIMPLE) }

    var projectType by remember { mutableStateOf<ProjectType>(ProjectType.KOTLIN) }
    var projectName by remember { mutableStateOf("MyProject") }
    var projectPackageName by remember { mutableStateOf("com.example.MyProject") }
    var projectTemplate by remember { mutableStateOf<TemplateType>(TemplateType.BASIC) }

    var kotlinSdk by remember { mutableStateOf("1.8.0") }
    var javaSdk by remember { mutableStateOf("17") }
    var androidSdk by remember { mutableStateOf("33") }
    var minSdk by remember { mutableStateOf("24") }

    val preferences by editorViewModel.editorPreferences.collectAsStateWithLifecycle(null)

    val isDarkMode = preferences?.isDarkMode == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Project") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = FeatherIcons.ArrowLeft,
                            contentDescription = "Go Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CreationModeOption(
                            modifier = Modifier.weight(1f),
                            title = "Simple Mode",
                            description = "Quick setup with templates",
                            icon = FeatherIcons.Zap,
                            selected = creationMode == CreationMode.SIMPLE,
                            onClick = { creationMode = CreationMode.SIMPLE }
                        )

                        CreationModeOption(
                            modifier = Modifier.weight(1f),
                            title = "Advanced Mode",
                            description = "Full configuration options",
                            icon = FeatherIcons.Layers,
                            selected = creationMode == CreationMode.ADVANCED,
                            onClick = { creationMode = CreationMode.ADVANCED }
                        )
                    }
                }

                AnimatedContent(
                    targetState = creationMode,
                    transitionSpec = {
                        val direction = if (targetState == CreationMode.ADVANCED) 1 else -1
                        val enter = slideInHorizontally { width -> direction * width }
                        val exit = slideOutHorizontally { width -> -direction * width }
                        enter togetherWith exit
                    },
                    label = "ModeTransition"
                ) { mode ->
                    when (mode) {
                        CreationMode.SIMPLE -> SimpleMode(
                            isDarkMode,
                            projectName = projectName,
                            projectPackageName = projectPackageName,
                            onProjectPackageNameChange = {
                                projectPackageName = it

                                val lastPart = it.split('.').lastOrNull()
                                if (lastPart == null) return@SimpleMode

                                val newProjectName = projectName.split('.').dropLast(1)
                                    .joinToString(".") + lastPart
                                projectName = newProjectName
                            },
                            onProjectNameChange = {
                                projectName = it

                                val lastPart = it.split('.').lastOrNull()
                                if (lastPart == null) return@SimpleMode

                                val newPackageName = projectPackageName.split('.').dropLast(1)
                                    .joinToString(".") + ".$lastPart"
                                projectPackageName = newPackageName
                            },
                            projectType = projectType,
                            onProjectTypeChange = { projectType = it },
                            selectedTemplate = projectTemplate,
                            onTemplateSelected = { projectTemplate = it },
                            onCreateProjectClick = {
                                onCreateProject(
                                    ProjectConfigState(
                                        name = projectName,
                                        type = projectType,
                                        templateType = projectTemplate,
                                        kotlinSdkVersion = kotlinSdk,
                                        javaSdkVersion = javaSdk,
                                        androidSdkVersion = androidSdk,
                                        minSdkVersion = minSdk,
                                        packageName = projectPackageName
                                    )
                                )
                            }
                        )

                        CreationMode.ADVANCED -> AdvancedMode(
                            isDarkMode,
                            projectName = projectName,
                            projectPackageName = projectPackageName,
                            onProjectPackageNameChange = { projectPackageName = it },
                            onProjectNameChange = { projectName = it },
                            projectType = projectType,
                            onProjectTypeChange = { projectType = it },
                            kotlinSdk = kotlinSdk,
                            onKotlinSdkChange = { kotlinSdk = it },
                            javaSdk = javaSdk,
                            onJavaSdkChange = { javaSdk = it },
                            androidSdk = androidSdk,
                            onAndroidSdkChange = { androidSdk = it },
                            minSdk = minSdk,
                            onMinSdkChange = { minSdk = it },
                            onCreateProjectClick = {
                                onCreateProject(
                                    ProjectConfigState(
                                        name = projectName,
                                        type = projectType,
                                        kotlinSdkVersion = kotlinSdk,
                                        javaSdkVersion = javaSdk,
                                        androidSdkVersion = androidSdk,
                                        minSdkVersion = minSdk,
                                        templateType = projectTemplate,
                                        packageName = projectPackageName
                                    )
                                )
                            }
                        )
                    }
                }

                LaunchedEffect(creationMode) {
                    previousMode = creationMode
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreationModeOption(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val borderWidth = if (selected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
            .background(
                if (selected)
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surface
            )
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .padding(bottom = 12.dp)
        )
        Text(
            text = title,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMode(
    isDarkMode: Boolean,
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    projectType: ProjectType,
    onProjectTypeChange: (ProjectType) -> Unit,
    selectedTemplate: TemplateType,
    onTemplateSelected: (TemplateType) -> Unit,
    onCreateProjectClick: () -> Unit,
    projectPackageName: String,
    onProjectPackageNameChange: (String) -> Unit = {}
) {
    val templates = remember {
        listOf(
            Template(
                id = TemplateType.BASIC,
                name = "Basic App",
                description = "Simple Android application with a single activity",
                icon = FeatherIcons.Zap
            ),
            Template(
                id = TemplateType.NAVIGATION,
                name = "Navigation App",
                description = "App with multiple screens and navigation",
                icon = FeatherIcons.Layers
            ),
            Template(
                id = TemplateType.EMPTY,
                name = "Empty Project",
                description = "Start from scratch with minimal setup",
                icon = FeatherIcons.Code
            )
        )
    }

    GlassCard(
        isDarkTheme = isDarkMode,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Quick Project Setup",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Create a new project in seconds",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = projectName,
                onValueChange = onProjectNameChange,
                label = { Text("Project name") },
                placeholder = { Text("MyKotlinProject") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = projectPackageName,
                onValueChange = onProjectPackageNameChange,
                label = { Text("Package name") },
                placeholder = { Text("com.example.myproject") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Project type",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ProjectTypeTabRow(
                selectedType = projectType,
                onTypeSelected = onProjectTypeChange,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Choose a template",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                templates.forEach { template ->
                    TemplateItem(
                        template = template,
                        selected = selectedTemplate == template.id,
                        onClick = { onTemplateSelected(template.id) }
                    )
                }
            }

            Button(
                onClick = onCreateProjectClick,
                enabled = projectName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Create Project")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectTypeTabRow(
    selectedType: ProjectType,
    onTypeSelected: (ProjectType) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryTabRow(
        selectedTabIndex = if (selectedType == ProjectType.KOTLIN) 0 else 1,
        modifier = modifier,
    ) {
        Tab(
            selected = selectedType == ProjectType.KOTLIN,
            onClick = { onTypeSelected(ProjectType.KOTLIN) },
            text = { Text("Kotlin") }
        )
        Tab(
            selected = selectedType == ProjectType.JAVA,
            onClick = { onTypeSelected(ProjectType.JAVA) },
            text = { Text("Java") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateItem(
    template: Template,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "template-scale"
    )

    val backgroundColor = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.surfaceVariant

    val textColor = if (selected)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurface

    val descriptionColor = if (selected)
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (selected) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.2f
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = template.icon,
                contentDescription = null,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = template.name,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Text(
                text = template.description,
                style = MaterialTheme.typography.bodySmall,
                color = descriptionColor
            )
        }

        AnimatedVisibility(
            visible = selected,
            enter = slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            Icon(
                imageVector = FeatherIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedMode(
    isDarkMode: Boolean,
    projectName: String,
    onProjectNameChange: (String) -> Unit,
    projectType: ProjectType,
    onProjectTypeChange: (ProjectType) -> Unit,
    kotlinSdk: String,
    onKotlinSdkChange: (String) -> Unit,
    javaSdk: String,
    onJavaSdkChange: (String) -> Unit,
    androidSdk: String,
    onAndroidSdkChange: (String) -> Unit,
    minSdk: String,
    onMinSdkChange: (String) -> Unit,
    onCreateProjectClick: () -> Unit,
    projectPackageName: String,
    onProjectPackageNameChange: (String) -> Unit = {}
) {
    GlassCard(
        isDarkMode,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Advanced Project Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Configure your new project in detail",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ProjectTypeTabRow(
                selectedType = projectType,
                onTypeSelected = onProjectTypeChange,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = onProjectNameChange,
                    label = { Text("Project name") },
                    placeholder = { Text("MyKotlinProject") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = projectPackageName,
                    onValueChange = onProjectPackageNameChange,
                    label = { Text("Project name") },
                    placeholder = { Text("MyKotlinProject") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (projectType == ProjectType.KOTLIN) "Kotlin SDK" else "Java SDK",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = if (projectType == ProjectType.KOTLIN) kotlinSdk else javaSdk,
                            onValueChange = {
                                if (projectType == ProjectType.KOTLIN) {
                                    onKotlinSdkChange(it)
                                } else {
                                    onJavaSdkChange(it)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Select SDK") }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Android SDK",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = androidSdk,
                            onValueChange = onAndroidSdkChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Target SDK") }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Minimum SDK",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = minSdk,
                            onValueChange = onMinSdkChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Minimum SDK") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onCreateProjectClick,
                enabled = projectName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Create Project")
            }
        }
    }
}
