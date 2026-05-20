package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class FrozenAppSettings(
    val oneKeyPackageNames: Set<String> = emptySet(),
    val showSystemAppsInManagePage: Boolean = false,
)
