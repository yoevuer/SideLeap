package hunoia.sideleap.overlay.api

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import com.blankj.utilcode.util.ScreenUtils
import hunoia.sideleap.ui.widget.VirtualMouseCursor

interface VirtualMouseOverlayHost : androidx.lifecycle.LifecycleOwner,
    androidx.lifecycle.ViewModelStoreOwner,
    androidx.savedstate.SavedStateRegistryOwner {
    val context: Context
}

class VirtualMouseOverlay(private val host: VirtualMouseOverlayHost) {
    private var overlayView: View? = null
    private var lastTouch = Offset.Unspecified

    fun show(
        onClick: (Int, Int) -> Unit,
        onDismiss: () -> Unit,
    ) {
        closeImmediately()
        val wm = host.context.windowManager()
        val cursorPosition = mutableStateOf(screenCenter())
        val composeView = ComposeView(host.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            applyOverlayViewTreeOwners(host)
            setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        lastTouch = Offset(event.rawX, event.rawY)
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val current = Offset(event.rawX, event.rawY)
                        val previous = lastTouch
                        if (previous != Offset.Unspecified) {
                            cursorPosition.value = clamp(cursorPosition.value + current - previous)
                        }
                        lastTouch = current
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        view.performClick()
                        val target = cursorPosition.value
                        closeImmediately()
                        onClick(target.x.toInt(), target.y.toInt())
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
                VirtualMouseCursor(
                    position = cursorPosition.value,
                    modifier = Modifier,
                )
            }
        }
        wm.addView(composeView, createLayoutParams())
        overlayView = composeView
    }

    fun closeImmediately() {
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
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            width = ScreenUtils.getScreenWidth()
            height = ScreenUtils.getScreenHeight()
            gravity = Gravity.START or Gravity.TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }
}

private fun screenCenter(): Offset {
    return Offset(ScreenUtils.getScreenWidth() / 2f, ScreenUtils.getScreenHeight() / 2f)
}

private fun clamp(position: Offset): Offset {
    return Offset(
        x = position.x.coerceIn(0f, ScreenUtils.getScreenWidth().toFloat()),
        y = position.y.coerceIn(0f, ScreenUtils.getScreenHeight().toFloat()),
    )
}
