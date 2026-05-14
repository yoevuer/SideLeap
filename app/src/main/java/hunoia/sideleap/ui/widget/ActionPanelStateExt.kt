package hunoia.sideleap.ui.widget

fun ActionPanelState.TriggerType.isMiniWindow(longPressLaunchPopup: Boolean): Boolean {
    return when (this) {
        ActionPanelState.TriggerType.Press -> !longPressLaunchPopup
        ActionPanelState.TriggerType.LongPress -> longPressLaunchPopup
    }
}