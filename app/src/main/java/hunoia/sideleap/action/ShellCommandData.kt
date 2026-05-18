package hunoia.sideleap.action

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class ShellCommandData(
    val command: String = ""
)
