package software.revolution.labx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import software.revolution.labx.presentation.viewmodel.EditorViewModel
import software.revolution.labx.presentation.viewmodel.PermissionViewModel
import software.revolution.labx.presentation.viewmodel.ProjectViewModel
import software.revolution.labx.ui.screens.MainEditorScreen
import software.revolution.labx.ui.screens.NewProjectScreen
import software.revolution.labx.ui.screens.SettingsScreen
import software.revolution.labx.ui.screens.SplashScreen
import software.revolution.labx.ui.screens.WelcomeScreen
import software.revolution.labx.ui.theme.LabxTheme
import software.revolution.labx.util.StoragePermissionManager

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionManager = StoragePermissionManager(this) {
            permissionViewModel.checkPermissions()
        }
        permissionViewModel.initPermissionManager(permissionManager)

        setContent {
            val permissionsGranted by permissionViewModel.storagePermissionsGranted.collectAsStateWithLifecycle()

            val editorViewModel: EditorViewModel = hiltViewModel()
            val preferences by editorViewModel.editorPreferences.collectAsStateWithLifecycle(null)

            val projectViewModel: ProjectViewModel = hiltViewModel()
            val creationSuccessProject by projectViewModel.creationSuccess.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                if (!permissionsGranted) {
                    permissionViewModel.requestPermissions()
                }
            }

            preferences?.let { prefs ->
                LabxTheme(
                    darkTheme = prefs.isDarkMode,
                    dynamicColor = false
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()

                        creationSuccessProject?.let { project ->
                            LaunchedEffect(project) {
                                projectViewModel.clearCreationSuccess()
                                navController.navigate("main?path=${project.path}") {
                                    popUpTo("welcome") { inclusive = false }
                                }
                            }
                        }

                        AppNavigation(
                            navController = navController,
                            projectViewModel = projectViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionViewModel.checkPermissions()
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    projectViewModel: ProjectViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable(
            route = "splash",
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(700)
                ) + fadeOut(animationSpec = tween(700))
            }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("welcome") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "welcome",
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            }
        ) {
            val editorViewModel: EditorViewModel = hiltViewModel()
            val preferences by editorViewModel.editorPreferences.collectAsStateWithLifecycle()

            WelcomeScreen(
                isDarkTheme = preferences?.isDarkMode == true,
                onCreateProject = {
                    navController.navigate("new_project")
                },
                onOpenProject = { path ->
                    navController.navigate("main?path=$path") {
                        popUpTo("welcome") { inclusive = false }
                    }
                },
                onCreateFile = {
                    navController.navigate("main") {
                        popUpTo("welcome") { inclusive = false }
                    }
                },
                projectViewModel = projectViewModel
            )
        }

        composable(
            route = "new_project",
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                ) + fadeOut(animationSpec = tween(500))
            }
        ) {
            NewProjectScreen(
                navController = navController,
                onCreateProject = { projectConfig ->
                    projectViewModel.createProject(projectConfig)
                }
            )
        }

        composable(
            route = "main?path={path}",
            arguments = listOf(
                navArgument("path") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(700)
                ) + fadeIn(animationSpec = tween(700))
            }
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""

            LaunchedEffect(path) {
                if (path.isNotEmpty()) {
                    projectViewModel.openProject(path)
                }
            }

            MainEditorScreen(
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable(
            route = "settings",
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(500)
                )
            }
        ) {
            SettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
