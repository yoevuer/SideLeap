package hunoia.sideleap.ui.widget

import hunoia.sideleap.action.TriggerType

fun TriggerType.isMiniWindow(longPressLaunchPopup: Boolean): Boolean {
    return when (this) {
        TriggerType.Press -> !longPressLaunchPopup
        TriggerType.LongPress -> longPressLaunchPopup
    }
}