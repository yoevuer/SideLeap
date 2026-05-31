package hunoia.luno.pointer

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import hunoia.luno.bridge.DensityProvider
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.ui.theme.SideGestureTheme
import hunoia.luno.bridge.window.applyOverlayViewTreeOwners
import hunoia.luno.bridge.window.windowManager

interface PointerOverlayHost : androidx.lifecycle.LifecycleOwner,
    androidx.lifecycle.ViewModelStoreOwner,
    androidx.savedstate.SavedStateRegistryOwner {
    val context: Context
}

class PointerOverlay(private val host: PointerOverlayHost) {
    private var overlayView: View? = null
    private var lastTouch = Offset.Unspecified
    private var clickPulseKey = 0
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var longPressRunnable: Runnable? = null

    fun show(
        settings: GestureSettings.Pointer,
        previousPosition: Offset = Offset.Unspecified,
        onPointerAction: (Int, Int, Boolean, PointerAction) -> Unit,
        onDismiss: () -> Unit,
    ) {
        closeImmediately()
        val wm = host.context.windowManager()
        val cursorPosition = mutableStateOf(pointerInitialPosition(settings, previousPosition))
        val clickPulse = mutableStateOf(clickPulseKey)
        var leftCancelEdge = false
        var longPressTriggered = false
        var longPressAnchor = Offset.Unspecified
        fun resetTimeout() {
            timeoutRunnable?.let(timeoutHandler::removeCallbacks)
            timeoutRunnable = null
            if (!settings.continuousMode || settings.continuousModeTimeoutMs <= 0L) return
            timeoutRunnable = Runnable {
                closeImmediately()
                onDismiss()
            }.also { timeoutHandler.postDelayed(it, settings.continuousModeTimeoutMs) }
        }
        fun resetLongPress(anchor: Offset = lastTouch) {
            longPressRunnable?.let(timeoutHandler::removeCallbacks)
            longPressRunnable = null
            if (!settings.longPressEnabled || settings.longPressDelayMs <= 0L || longPressTriggered) return
            longPressAnchor = anchor
            longPressRunnable = Runnable {
                if (!isPointerWithinLongPressTolerance(longPressAnchor, lastTouch, settings)) return@Runnable
                longPressTriggered = true
                val target = cursorPosition.value
                clickPulseKey += 1
                clickPulse.value = clickPulseKey
                onPointerAction(
                    target.x.toInt(),
                    target.y.toInt(),
                    settings.continuousMode,
                    PointerAction.LongPress,
                )
            }.also { timeoutHandler.postDelayed(it, settings.longPressDelayMs) }
        }
        val composeView = ComposeView(host.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            applyOverlayViewTreeOwners(host)
            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        resetTimeout()
                        lastTouch = Offset(event.rawX, event.rawY)
                        resetLongPress()
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        resetTimeout()
                        val current = Offset(event.rawX, event.rawY)
                        val previous = lastTouch
                        if (previous != Offset.Unspecified) {
                            val dragAmount = current - previous
                            if (!isPointerWithinLongPressTolerance(longPressAnchor, current, settings) && !longPressTriggered) {
                                resetLongPress(current)
                            }
                            val inCancelEdge = settings.continuousMode &&
                                isPointerCancelGesture(current, settings)
                            if (!inCancelEdge) {
                                leftCancelEdge = true
                            } else if (leftCancelEdge) {
                                closeImmediately()
                                onDismiss()
                                return@setOnTouchListener true
                            }
                            cursorPosition.value = movePointerCursor(cursorPosition.value, dragAmount, settings)
                        }
                        lastTouch = current
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        resetTimeout()
                        longPressRunnable?.let(timeoutHandler::removeCallbacks)
                        longPressRunnable = null
                        view.performClick()
                        val target = cursorPosition.value
                        closeImmediately()
                        if (!longPressTriggered) {
                            clickPulseKey += 1
                            clickPulse.value = clickPulseKey
                            onPointerAction(
                                target.x.toInt(),
                                target.y.toInt(),
                                settings.continuousMode,
                                PointerAction.Click,
                            )
                        } else {
                            onDismiss()
                        }
                        true
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        closeImmediately()
                        onDismiss()
                        true
                    }
                    else -> true
                }
            }
            setContent {
                SideGestureTheme {
                    PointerCursor(
                        position = cursorPosition.value,
                        modifier = Modifier,
                        settings = settings,
                        clickPulseKey = clickPulse.value,
                    )
                }
            }
        }
        wm.addView(composeView, createLayoutParams())
        overlayView = composeView
        resetTimeout()
    }

    fun closeImmediately() {
        timeoutRunnable?.let(timeoutHandler::removeCallbacks)
        timeoutRunnable = null
        longPressRunnable?.let(timeoutHandler::removeCallbacks)
        longPressRunnable = null
        val view = overlayView ?: return
        overlayView = null
        lastTouch = Offset.Unspecified
        runCatching { host.context.windowManager().removeViewImmediate(view) }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            width = DensityProvider.screenWidthPx
            height = DensityProvider.screenHeightPx
            gravity = Gravity.START or Gravity.TOP
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
        }
    }
}
