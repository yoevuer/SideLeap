package hunoia.luno.ui.component

import hunoia.luno.action.TriggerType

fun TriggerType.isMiniWindow(longPressLaunchPopup: Boolean): Boolean {
    return when (this) {
        TriggerType.Press -> !longPressLaunchPopup
        TriggerType.LongPress -> longPressLaunchPopup
    }
}