package hunoia.sideleap.entity

import androidx.annotation.Keep
import hunoia.sideleap.constant.ActionPanelStylesDefaults
import hunoia.sideleap.constant.ActionPanelStylesDefaults.ArcStyleItemSize
import hunoia.sideleap.constant.ActionPanelStylesDefaults.Type
import hunoia.sideleap.utils.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/20
 */

@Serializable
@Keep
data class ActionPanelStyles(
    val type: Int = Type,
    val json: String = ""
) {
    companion object {
        const val TYPE_ARC = ActionPanelStylesDefaults.TYPE_ARC
    }

    @Transient
    val value: ActionPanelStyle = run {
        val json = json
        if (json.isEmpty()) {
            return@run ArcStyle()
        }
        when (type) {
            TYPE_ARC -> JsonHelper.decodeFromString<ArcStyle>(json)
            else -> error("Unknown ActionPanelStyle type: $type")
        }
    }
}

sealed interface ActionPanelStyle

@Serializable
@Keep
data class ArcStyle(
    val itemSize: Int = ArcStyleItemSize
) : ActionPanelStyle