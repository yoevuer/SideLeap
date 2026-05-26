package hunoia.sideleap.overlay.api

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat

data class PickerCandidate(
    val bounds: Rect,
    val score: Int,
    val index: Int,
    val viewId: String = "",
)

class ElementPickerOverlay(
    private val context: Context,
    private val candidates: List<PickerCandidate>,
    private val onSelected: (Int) -> Unit,
    private val onDismiss: () -> Unit,
) {
    private var overlayView: View? = null

    fun show() {
        val wm = ContextCompat.getSystemService(context, WindowManager::class.java) ?: return
        val view = object : View(context) {
            private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
            private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textAlign = Paint.Align.CENTER
                textSize = 40f
            }
            private val radius = 48f
            private val centers = candidates.map { c ->
                Pair(
                    (c.bounds.left + c.bounds.right) / 2f,
                    (c.bounds.top + c.bounds.bottom) / 2f
                )
            }

            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                candidates.forEachIndexed { i, c ->
                    val (cx, cy) = centers[i]
                    circlePaint.color = if (c.score >= 5) Color.argb(200, 76, 175, 80) else Color.argb(180, 158, 158, 158)
                    canvas.drawCircle(cx, cy, radius, circlePaint)
                    val numText = "${c.index + 1}"
                    val yOffset = textPaint.fontMetrics.let { (it.descent - it.ascent) / 2 - it.descent }
                    canvas.drawText(numText, cx, cy + yOffset, textPaint)
                }
            }

            override fun onTouchEvent(event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_UP) {
                    val tx = event.x
                    val ty = event.y
                    centers.forEachIndexed { i, (cx, cy) ->
                        val dx = tx - cx
                        val dy = ty - cy
                        if (dx * dx + dy * dy <= radius * radius) {
                            cleanup()
                            onSelected(i)
                            return true
                        }
                    }
                    cleanup()
                    onDismiss()
                    return true
                }
                return super.onTouchEvent(event)
            }
        }

        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.START or Gravity.TOP
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        wm.addView(view, lp)
        overlayView = view
    }

    fun closeImmediately() {
        cleanup()
    }

    private fun cleanup() {
        val v = overlayView ?: return
        overlayView = null
        runCatching {
            ContextCompat.getSystemService(context, WindowManager::class.java)?.removeViewImmediate(v)
        }
    }
}
