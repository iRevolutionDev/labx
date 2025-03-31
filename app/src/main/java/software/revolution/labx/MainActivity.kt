package software.revolution.labx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import software.revolution.labx.ui.screens.EditorPreferences
import software.revolution.labx.ui.screens.MainEditorScreen
import software.revolution.labx.ui.screens.SettingsScreen
import software.revolution.labx.ui.screens.SplashScreen
import software.revolution.labx.ui.theme.LabxTheme
import software.revolution.labx.ui.theme.PrimaryLight
import software.revolution.labx.util.StoragePermissionManager

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: StoragePermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = StoragePermissionManager(this) {}

        setContent {
            val permissionsGranted =
                remember { mutableStateOf(permissionManager.hasStoragePermissions()) }
            val isDarkTheme = isSystemInDarkTheme()

            var editorPreferences by remember {
                mutableStateOf(
                    EditorPreferences(
                        isDarkMode = isDarkTheme,
                        fontSize = 14f,
                        showLineNumbers = true,
                        tabSize = 4,
                        wordWrap = true,
                        autoSave = false,
                        accentColor = PrimaryLight
                    )
                )
            }

            LaunchedEffect(Unit) {
                if (!permissionsGranted.value) {
                    permissionManager.checkAndRequestPermissions()
                }
            }

            LabxTheme(
                darkTheme = editorPreferences.isDarkMode,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(
                        navController = navController,
                        editorPreferences = editorPreferences,
                        onPreferencesChanged = { newPreferences ->
                            editorPreferences = newPreferences
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!permissionManager.hasStoragePermissions()) {
            permissionManager.checkAndRequestPermissions()
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    editorPreferences: EditorPreferences,
    onPreferencesChanged: (EditorPreferences) -> Unit
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
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "main",
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(700)
                ) + fadeIn(animationSpec = tween(700))
            }
        ) {
            MainEditorScreen(
                preferences = editorPreferences,
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
                preferences = editorPreferences,
                onPreferencesChanged = onPreferencesChanged,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}
