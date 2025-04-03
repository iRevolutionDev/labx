package software.revolution.labx.domain.model

data class ProjectConfigState(
    val name: String,
    val type: ProjectType,
    val templateType: TemplateType,
    val kotlinSdkVersion: String,
    val javaSdkVersion: String,
    val androidSdkVersion: String,
    val minSdkVersion: String,
    val packageName: String = "com.example.myproject"
)

fun ProjectConfigState.toProjectConfig(): ProjectConfig {
    return ProjectConfig(
        name = name,
        type = type,
        kotlinSdkVersion = kotlinSdkVersion,
        javaSdkVersion = javaSdkVersion,
        androidSdkVersion = androidSdkVersion,
        minSdkVersion = minSdkVersion,
        packageName = packageName
    )
}