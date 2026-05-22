package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import hunoia.sideleap.core.DensityProvider
import hunoia.sideleap.core.serialization.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Keep
data class ActionPanelStyles(
    val type: Int = ActionPanelStylesDefaults.Type,
    val json: String = ""
) {
    companion object {
        const val TYPE_ARC = ActionPanelStylesDefaults.TYPE_ARC
        const val TYPE_LIST = ActionPanelStylesDefaults.TYPE_LIST
        const val TYPE_GRID = ActionPanelStylesDefaults.TYPE_GRID
        const val TYPE_PIE = ActionPanelStylesDefaults.TYPE_PIE

        fun arc(): ActionPanelStyles = ActionPanelStyles(TYPE_ARC)

        fun grid(): ActionPanelStyles = ActionPanelStyles(
            type = TYPE_GRID,
            json = JsonHelper.encodeToString(GridStyle())
        )

        fun pie(): ActionPanelStyles = ActionPanelStyles(
            type = TYPE_PIE,
            json = JsonHelper.encodeToString(PieStyle())
        )
    }

    @Transient
    val value: ActionPanelStyle = run {
        val json = json
        if (json.isEmpty()) {
            return@run when (type) {
                TYPE_GRID -> GridStyle()
                else -> ArcStyle()
            }
        }
        when (type) {
            TYPE_ARC -> JsonHelper.decodeFromString<ArcStyle>(json)
            TYPE_LIST -> ArcStyle()
            TYPE_GRID -> JsonHelper.decodeFromString<GridStyle>(json)
            TYPE_PIE -> JsonHelper.decodeFromString<PieStyle>(json)
            else -> error("Unknown ActionPanelStyle type: $type")
        }
    }
}

object ActionPanelStylesDefaults {

    const val TYPE_ARC = 1
    const val TYPE_LIST = 2
    const val TYPE_GRID = 3
    const val TYPE_PIE = 4

    const val Type = TYPE_ARC
    val ArcStyleItemSize = DensityProvider.dp2px(48f)
    val GridStyleItemSize = DensityProvider.dp2px(48f)
    val PieStyleItemSize = DensityProvider.dp2px(48f)
}

sealed interface ActionPanelStyle

@Serializable
@Keep
data class ArcStyle(
    val itemSize: Int = ActionPanelStylesDefaults.ArcStyleItemSize
) : ActionPanelStyle

@Serializable
@Keep
data class GridStyle(
    val itemSize: Int = ActionPanelStylesDefaults.GridStyleItemSize
) : ActionPanelStyle

@Serializable
@Keep
data class PieStyle(
    val itemSize: Int = ActionPanelStylesDefaults.PieStyleItemSize
) : ActionPanelStyle

@Serializable
@Keep
data class LongSlideActionPanelStyles(
    val center: ActionPanelStyles = ActionPanelStyles(),
    val up: ActionPanelStyles = ActionPanelStyles(),
    val down: ActionPanelStyles = ActionPanelStyles(),
    val up2: ActionPanelStyles = ActionPanelStyles(),
    val down2: ActionPanelStyles = ActionPanelStyles()
)
