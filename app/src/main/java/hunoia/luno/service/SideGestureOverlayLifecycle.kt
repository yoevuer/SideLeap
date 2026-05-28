package hunoia.luno.service

internal class SideGestureOverlayLifecycle(
    private val host: SideGestureService,
) {

    fun onScreenLocked() {
        host.quickAppLauncherOverlay.closeImmediately()
        host.runtimePanelOverlay.closeImmediately()
    }

    fun onDestroy() {
        host.quickAppLauncherOverlay.closeImmediately()
        host.runtimePanelOverlay.close()
    }
}
