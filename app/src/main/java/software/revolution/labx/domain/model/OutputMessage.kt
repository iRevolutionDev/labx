package software.revolution.labx.domain.model

data class OutputMessage(
    val id: String = System.currentTimeMillis().toString(),
    val message: String,
    val type: OutputType = OutputType.INFO,
    val timestamp: Long = System.currentTimeMillis()
)

enum class OutputType {
    INFO, WARNING, ERROR, SUCCESS
}