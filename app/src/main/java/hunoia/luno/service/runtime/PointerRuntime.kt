package hunoia.luno.service.runtime

import android.os.Build
import androidx.compose.ui.geometry.Offset
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.overlay.api.PointerOverlay
import hunoia.luno.overlay.api.PointerOverlayHost
import hunoia.luno.gesture.application.PointerAction
import hunoia.luno.gesture.application.clampPointerPosition
import hunoia.luno.system.accessibility.Accessibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PointerRuntime(
    private val host: PointerOverlayHost,
    private val scope: CoroutineScope,
    private val gestureSettingsProvider: () -> GestureSettings?,
    private val onStateChanged: () -> Unit,
) {
    private var overlay: PointerOverlay? = null
    private var sessionSettings: GestureSettings.Pointer? = null
    private var lastPosition = Offset.Unspecified
    var isActive: Boolean = false
        private set

    fun show(continuousModeOverride: Boolean? = null): Boolean {
        if (!begin()) return false
        val settings = (gestureSettingsProvider()?.pointer ?: GestureSettings.Pointer()).let {
            if (continuousModeOverride == null) it else it.copy(continuousMode = continuousModeOverride)
        }
        sessionSettings = settings
        val o = overlay ?: PointerOverlay(host).also { overlay = it }
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
        x: Int, y: Int, keepActive: Boolean, action: PointerAction,
    ) {
        lastPosition = clampPointerPosition(Offset(x.toFloat(), y.toFloat()))
        overlay?.closeImmediately()
        scope.launch {
            delay(80)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val service = (host as? android.accessibilityservice.AccessibilityService) ?: return@launch
                when (action) {
                    PointerAction.Click -> Accessibility.click(service, x, y)
                    PointerAction.LongPress -> Accessibility.longPress(service, x, y)
                }
            }
            if (keepActive && isActive) {
                val o = overlay ?: PointerOverlay(host).also { overlay = it }
                o.show(
                    settings = sessionSettings ?: gestureSettingsProvider()?.pointer ?: GestureSettings.Pointer(),
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

    fun getCurrentSettings(): GestureSettings.Pointer? = sessionSettings
    fun getLastPosition(): Offset = lastPosition
    fun onSettingsUpdate(settings: GestureSettings.Pointer) { sessionSettings = settings }
    fun onDestroy() { overlay?.closeImmediately() }
}
