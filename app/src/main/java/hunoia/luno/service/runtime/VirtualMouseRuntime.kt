package hunoia.luno.service.runtime

import android.os.Build
import androidx.compose.ui.geometry.Offset
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.overlay.api.VirtualMouseOverlay
import hunoia.luno.overlay.api.VirtualMouseOverlayHost
import hunoia.luno.gesture.application.VirtualMousePointerAction
import hunoia.luno.gesture.application.clampVirtualMousePosition
import hunoia.luno.system.accessibility.Accessibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VirtualMouseRuntime(
    private val host: VirtualMouseOverlayHost,
    private val scope: CoroutineScope,
    private val gestureSettingsProvider: () -> GestureSettings?,
    private val onStateChanged: () -> Unit,
) {
    private var overlay: VirtualMouseOverlay? = null
    private var sessionSettings: GestureSettings.VirtualMouse? = null
    private var lastPosition = Offset.Unspecified
    var isActive: Boolean = false
        private set

    fun show(continuousModeOverride: Boolean? = null): Boolean {
        if (!begin()) return false
        val settings = (gestureSettingsProvider()?.virtualMouse ?: GestureSettings.VirtualMouse()).let {
            if (continuousModeOverride == null) it else it.copy(continuousMode = continuousModeOverride)
        }
        sessionSettings = settings
        val o = overlay ?: VirtualMouseOverlay(host).also { overlay = it }
        o.show(
            settings = settings,
            onPointerAction = { x, y, keepActive, action ->
                performPointerAction(x, y, keepActive, action)
            },
            previousPosition = lastPosition,
            onDismiss = { end() },
        )
        return true
    }

    private fun begin(): Boolean {
        if (isActive) return false
        isActive = true
        onStateChanged()
        return true
    }

    fun end() {
        if (!isActive && overlay == null) return
        isActive = false
        overlay?.closeImmediately()
        sessionSettings = null
        onStateChanged()
    }

    private fun performPointerAction(
        x: Int, y: Int, keepActive: Boolean, action: VirtualMousePointerAction,
    ) {
        lastPosition = clampVirtualMousePosition(Offset(x.toFloat(), y.toFloat()))
        overlay?.closeImmediately()
        scope.launch {
            delay(80)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val service = (host as? android.accessibilityservice.AccessibilityService) ?: return@launch
                when (action) {
                    VirtualMousePointerAction.Click -> Accessibility.click(service, x, y)
                    VirtualMousePointerAction.LongPress -> Accessibility.longPress(service, x, y)
                }
            }
            if (keepActive && isActive) {
                val o = overlay ?: VirtualMouseOverlay(host).also { overlay = it }
                o.show(
                    settings = sessionSettings ?: gestureSettingsProvider()?.virtualMouse ?: GestureSettings.VirtualMouse(),
                    previousPosition = lastPosition,
                    onPointerAction = { nextX, nextY, nextKeepActive, nextAction ->
                        performPointerAction(nextX, nextY, nextKeepActive, nextAction)
                    },
                    onDismiss = { end() },
                )
            } else {
                end()
            }
        }
    }

    fun getCurrentSettings(): GestureSettings.VirtualMouse? = sessionSettings
    fun getLastPosition(): Offset = lastPosition
    fun onSettingsUpdate(settings: GestureSettings.VirtualMouse) { sessionSettings = settings }
    fun onDestroy() { overlay?.closeImmediately() }
}
