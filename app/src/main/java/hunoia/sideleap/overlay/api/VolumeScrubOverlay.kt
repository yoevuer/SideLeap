package hunoia.sideleap.overlay.api

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import hunoia.sideleap.system.api.volumeDown
import hunoia.sideleap.system.api.volumeUp

class VolumeScrubOverlay(private val context: Context) {

    private var overlayView: View? = null
    private var lastY = 0f
    private var accumulator = 0f

    private val stepThresholdPx: Float

    init {
        val wm = context.windowManager()
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getRealMetrics(metrics)
        stepThresholdPx = 18 * metrics.density
    }

    fun show(onDismiss: () -> Unit): Boolean {
        if (overlayView != null) return false

        overlayView = View(context).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)

            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastY = event.rawY
                        accumulator = 0f
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaY = event.rawY - lastY
                        lastY = event.rawY
                        accumulator += deltaY
                        while (accumulator >= stepThresholdPx) {
                            context.volumeDown()
                            accumulator -= stepThresholdPx
                        }
                        while (accumulator <= -stepThresholdPx) {
                            context.volumeUp()
                            accumulator += stepThresholdPx
                        }
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        dismiss()
                        onDismiss()
                        true
                    }
                    else -> false
                }
            }
        }

        val wm = context.windowManager()
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(overlayView, params)
        return true
    }

    fun dismiss() {
        overlayView?.let { view ->
            try {
                context.windowManager().removeViewImmediate(view)
            } catch (_: Exception) {}
            overlayView = null
        }
        lastY = 0f
        accumulator = 0f
    }
}
