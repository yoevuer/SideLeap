package hunoia.luno.action

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class ShellCommandData(
    val command: String = "",
    val showToast: Boolean = true,
)
