package hunoia.sideleap.ktx

import hunoia.sideleap.ui.widget.ActionPanelState

/**
 * @author DS-Z
 * @since 2025/6/30
 */

fun ActionPanelState.TriggerType.isMiniWindow(longPressLaunchPopup: Boolean): Boolean {
    return when (this) {
        ActionPanelState.TriggerType.Press -> !longPressLaunchPopup
        ActionPanelState.TriggerType.LongPress -> longPressLaunchPopup
    }
}