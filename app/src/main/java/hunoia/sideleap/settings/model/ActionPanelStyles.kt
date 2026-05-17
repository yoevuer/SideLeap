package hunoia.sideleap.settings.model

import androidx.annotation.Keep
import com.blankj.utilcode.util.ConvertUtils
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
    }

    @Transient
    val value: ActionPanelStyle = run {
        val json = json
        if (json.isEmpty()) {
            return@run ArcStyle()
        }
        when (type) {
            TYPE_ARC -> JsonHelper.decodeFromString<ArcStyle>(json)
            TYPE_LIST -> JsonHelper.decodeFromString<ListStyle>(json)
            TYPE_GRID -> JsonHelper.decodeFromString<GridStyle>(json)
            else -> error("Unknown ActionPanelStyle type: $type")
        }
    }

    fun displayName(): String {
        return when (type) {
            TYPE_ARC -> "弧形"
            TYPE_LIST -> "自适应列表"
            TYPE_GRID -> "自适应网格"
            else -> "未知"
        }
    }
}

object ActionPanelStylesDefaults {

    const val TYPE_ARC = 1
    const val TYPE_LIST = 2
    const val TYPE_GRID = 3

    const val Type = TYPE_ARC
    val ArcStyleItemSize = ConvertUtils.dp2px(48f)
    val ListStyleItemSize = ConvertUtils.dp2px(48f)
    val GridStyleItemSize = ConvertUtils.dp2px(48f)
}

sealed interface ActionPanelStyle

@Serializable
@Keep
data class ArcStyle(
    val itemSize: Int = ActionPanelStylesDefaults.ArcStyleItemSize
) : ActionPanelStyle

@Serializable
@Keep
data class ListStyle(
    val itemSize: Int = ActionPanelStylesDefaults.ListStyleItemSize
) : ActionPanelStyle

@Serializable
@Keep
data class GridStyle(
    val itemSize: Int = ActionPanelStylesDefaults.GridStyleItemSize
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
