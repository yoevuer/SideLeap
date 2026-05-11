package hunoia.sideleap.entity

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class QuickAppLauncherSettings(
    // field 1 was favoriteApps, do not reuse
    val hiddenApps: Set<String> = emptySet(),
    val recentLaunchTime: Map<String, Long> = emptyMap(),
    val launchCount: Map<String, Long> = emptyMap(),
    val showSystemApps: Boolean = true,
    val panelHeightFraction: Float = 0.52f,
    val contentHeightFraction: Float = 0.52f,
    val candidateRows: Int = 1,
    val panelWidthFraction: Float = 1.0f,
    val panelHorizontalBias: Float = 0.5f,
)
