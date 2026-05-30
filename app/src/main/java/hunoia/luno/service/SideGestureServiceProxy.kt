package hunoia.luno.service

import hunoia.luno.config.model.Action
import hunoia.luno.config.model.GestureButton


class SideGestureServiceProxy(private val host: SideGestureService) {

    private val actionCoordinator = SideGestureServiceProxyActionCoordinator(host) { host.coroutineScope }

    fun onRelease() {
        actionCoordinator.onRelease()
    }

    fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        actionCoordinator.onAccessibilityEvent(event)
    }

    fun onAction(action: Action, sourceButton: GestureButton?) {
        actionCoordinator.onAction(action, sourceButton)
    }
}
