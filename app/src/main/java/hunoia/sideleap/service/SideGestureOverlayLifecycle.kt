package hunoia.sideleap.service

import hunoia.sideleap.SideGestureService

internal class SideGestureOverlayLifecycle(
    private val host: SideGestureService,
) {

    fun onScreenLocked() {
        host.quickAppLauncherOverlay.closeImmediately()
        host.runtimePanelOverlay.close()
    }

    fun onDestroy() {
        host.quickAppLauncherOverlay.closeImmediately()
        host.runtimePanelOverlay.close()
    }
}
