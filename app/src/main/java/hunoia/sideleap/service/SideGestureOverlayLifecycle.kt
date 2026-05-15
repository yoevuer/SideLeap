package hunoia.sideleap.service

import hunoia.sideleap.SideGestureService

internal class SideGestureOverlayLifecycle(
    private val host: SideGestureService,
) {

    fun onScreenLocked() {
        closeQuickAppLauncherOverlay()
    }

    fun onDestroy() {
        closeQuickAppLauncherOverlay()
    }

    fun closeQuickAppLauncherOverlay() {
        host.quickAppLauncherOverlay.close()
    }
}
