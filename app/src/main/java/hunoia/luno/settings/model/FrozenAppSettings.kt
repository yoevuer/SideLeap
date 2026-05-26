package hunoia.luno.settings.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class FrozenAppSettings(
    val oneKeyPackageNames: Set<String> = emptySet(),
)
