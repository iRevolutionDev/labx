package software.revolution.labx.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akuleshov7.ktoml.Toml
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.serializer
import software.revolution.labx.domain.model.Project
import software.revolution.labx.domain.model.ProjectConfig
import software.revolution.labx.domain.model.ProjectConfigState
import software.revolution.labx.domain.model.ProjectType
import software.revolution.labx.domain.model.Result
import software.revolution.labx.domain.model.TemplateType
import software.revolution.labx.domain.model.toProjectConfig
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProjectViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _recentProjects = MutableStateFlow<List<Project>>(emptyList())
    val recentProjects: StateFlow<List<Project>> = _recentProjects.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _creationSuccess = MutableStateFlow<Project?>(null)
    val creationSuccess: StateFlow<Project?> = _creationSuccess.asStateFlow()

    init {
        loadProjects()
    }

    fun loadProjects() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val projectsDir = getProjectsDirectory()
                if (!projectsDir.exists() || !projectsDir.isDirectory) {
                    _recentProjects.value = emptyList()
                    return@launch
                }

                val projects = projectsDir.listFiles()
                    ?.filter { it.isDirectory }
                    ?.map { directory ->
                        val configFile = File(directory, "project.toml")

                        val projectType = when {
                            configFile.exists() -> {
                                try {
                                    val projectConfig = Toml.decodeFromString<ProjectConfig>(
                                        serializer(), configFile.readText()
                                    )
                                    val typeStr = projectConfig.type.name

                                    when (typeStr.uppercase()) {
                                        "KOTLIN" -> ProjectType.KOTLIN
                                        "JAVA" -> ProjectType.JAVA
                                        else -> ProjectType.KOTLIN
                                    }
                                } catch (_: Exception) {
                                    ProjectType.KOTLIN
                                }
                            }

                            else -> ProjectType.KOTLIN
                        }

                        val lastModified = configFile.takeIf { it.exists() }?.lastModified()
                            ?: directory.lastModified()

                        Project(
                            name = directory.name,
                            path = directory.absolutePath,
                            type = projectType,
                            lastModified = directory.lastModified(),
                            lastOpenedDate = lastModified
                        )
                    }
                    ?.sortedByDescending { it.lastOpenedDate } ?: emptyList()

                _recentProjects.value = projects
            } catch (e: Exception) {
                _error.value = "Failed to load projects: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createProject(config: ProjectConfigState): Result<Project> {
        _isLoading.value = true

        return try {
            val projectsDir = getProjectsDirectory()
            if (!projectsDir.exists()) {
                projectsDir.mkdirs()
            }

            val projectDir = File(projectsDir, config.name)
            if (projectDir.exists()) {
                _error.value = "A project with this name already exists"
                _isLoading.value = false
                return Result.Error("A project with this name already exists")
            }

            projectDir.mkdirs()

            when (config.templateType) {
                TemplateType.BASIC -> createBasicTemplate(projectDir, config.toProjectConfig())
                TemplateType.NAVIGATION -> createBasicTemplate(projectDir, config.toProjectConfig())
                TemplateType.EMPTY -> createEmptyTemplate(projectDir)
            }

            saveProjectConfig(projectDir, config.toProjectConfig())

            val newProject = Project(
                name = config.name,
                path = projectDir.absolutePath,
                type = config.type,
                lastModified = System.currentTimeMillis(),
                lastOpenedDate = System.currentTimeMillis()
            )

            loadProjects()

            _creationSuccess.value = newProject
            _isLoading.value = false

            Result.Success(newProject)
        } catch (e: Exception) {
            _error.value = "Failed to create project: ${e.localizedMessage}"
            _isLoading.value = false
            Result.Error("Failed to create project: ${e.localizedMessage}")
        }
    }

    fun openProject(path: String) {
        viewModelScope.launch {
            try {
                val projectDir = File(path)
                if (projectDir.exists() && projectDir.isDirectory) {
                    val tomlConfigFile = File(projectDir, "project.toml")

                    if (!tomlConfigFile.exists()) {
                        return@launch
                    }

                    tomlConfigFile.setLastModified(System.currentTimeMillis())

                    loadProjects()
                }
            } catch (e: Exception) {
                _error.value = "Failed to open project: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearCreationSuccess() {
        _creationSuccess.value = null
    }

    private fun saveProjectConfig(projectDir: File, config: ProjectConfig) {
        val projectConfigFile = File(projectDir, "project.toml")

        val toml = Toml.encodeToString(config)
        projectConfigFile.writeText(toml)
        projectConfigFile.setLastModified(System.currentTimeMillis())
    }

    private fun createBasicTemplate(projectDir: File, config: ProjectConfig) {
        val srcDir = File(projectDir, "src/main")
        srcDir.mkdirs()

        val packageDirs = config.packageName.split(".")
        var currentDir = srcDir

        currentDir = if (config.type == ProjectType.KOTLIN) {
            File(currentDir, "kotlin")
        } else {
            File(currentDir, "java")
        }
        currentDir.mkdirs()

        for (dir in packageDirs) {
            currentDir = File(currentDir, dir)
            currentDir.mkdirs()
        }

        if (config.type == ProjectType.KOTLIN) "kt" else "java"

        val assets = context.assets

        val templateAssets = assets.list("templates/basic/${config.type.name.lowercase()}")
            ?: return

        for (template in templateAssets) {
            val templateFile =
                assets.open("templates/basic/${config.type.name.lowercase()}/$template")
            val outputFile = File(currentDir, template)

            outputFile.outputStream().use { output ->
                templateFile.copyTo(output)
            }

            outputFile.writeText(
                outputFile.readText()
                    .replace("{{PACKAGE_NAME}}", config.packageName)
                    .replace("{{PROJECT_NAME}}", config.name)
            )

            outputFile.setLastModified(System.currentTimeMillis())
            templateFile.close()
        }
    }

    private fun createEmptyTemplate(projectDir: File) {
        val srcDir = File(projectDir, "src/main")
        srcDir.mkdirs()

        val gitignore = File(projectDir, ".gitignore")
        gitignore.writeText(
            """
            *.iml
            .gradle
            /local.properties
            /.idea
            .DS_Store
            /build
            /captures
            .externalNativeBuild
            .cxx
            local.properties
        """.trimIndent()
        )
    }

    private fun getProjectsDirectory(): File {
        val appFilesDir = context.getExternalFilesDir(null)
        val projectsDir = File(appFilesDir, "Projects")
        if (!projectsDir.exists()) {
            projectsDir.mkdirs()
        }
        return projectsDir
    }
}