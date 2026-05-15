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
)

internal data class GestureButtonRefreshState(
    val initialSettings: InitialSettings,
    val advancedSettings: AdvancedSettings,
    val runtimeState: SideGestureRuntimeState,
) {
    fun shouldShow(button: GestureButton): Boolean {
        return initialSettings.gestureEnabled &&
            !(advancedSettings.hideLandscape && runtimeState.isLandscape) &&
            !(advancedSettings.hideHomeScreen && runtimeState.isInLauncher) &&
            !(advancedSettings.hideScreenLock && runtimeState.isNowInLockScreenPage) &&
            runtimeState.currentPackageName !in advancedSettings.excludeApps &&
            button.enabled
    }
}
