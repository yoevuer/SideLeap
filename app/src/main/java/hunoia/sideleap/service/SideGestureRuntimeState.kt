package hunoia.sideleap.service

import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.InitialSettings

internal data class SideGestureRuntimeState(
    val currentPackageName: String,
    val isNowInLockScreenPage: Boolean,
    val isLandscape: Boolean,
    val isInLauncher: Boolean,
    val imePadding: Int,
    val hiddenGestureButtons: Map<String, Long>,
    val isMouseMode: Boolean,
    val nowMs: Long,
)

internal data class GestureButtonRefreshState(
    val initialSettings: InitialSettings,
    val advancedSettings: AdvancedSettings,
    val runtimeState: SideGestureRuntimeState,
) {
    fun shouldShow(button: GestureButton): Boolean {
        return initialSettings.gestureEnabled &&
            (runtimeState.hiddenGestureButtons[button.hiddenKey()] ?: 0L) <= runtimeState.nowMs &&
            !runtimeState.isMouseMode &&
            !(advancedSettings.hideLandscape && runtimeState.isLandscape) &&
            !(advancedSettings.hideHomeScreen && runtimeState.isInLauncher) &&
            !(advancedSettings.hideScreenLock && runtimeState.isNowInLockScreenPage) &&
            runtimeState.currentPackageName !in advancedSettings.excludeApps &&
            button.enabled
    }
}

internal fun GestureButton.hiddenKey(): String = "${position.name}:$id"
