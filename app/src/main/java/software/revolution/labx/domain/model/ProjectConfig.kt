package software.revolution.labx.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ProjectConfig(
    /**
     * The name of the project.
     */
    val name: String,
    /**
     * The type of project to create.
     */
    val type: ProjectType,
    /**
     * The mode of project creation.
     */
    val kotlinSdkVersion: String,
    /**
     * The version of Java SDK to use.
     */
    val javaSdkVersion: String,
    /**
     * The version of Android SDK to use.
     */
    val androidSdkVersion: String,
    /**
     * The minimum SDK version for the project.
     */
    val minSdkVersion: String,
    /**
     * The type of template to use for the project.
     */
    val packageName: String = "com.example.myproject"
)

enum class CreationMode {
    SIMPLE, ADVANCED
}

enum class ProjectType {
    KOTLIN, JAVA
}

enum class TemplateType {
    BASIC, NAVIGATION, EMPTY
}
