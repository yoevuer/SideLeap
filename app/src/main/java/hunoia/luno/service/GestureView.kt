package hunoia.luno.service

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import hunoia.luno.gesture.GestureButton

/**
 * @author aaronzzxup@gmail.com
 * @since 2025/11/15
 */
class GestureView @JvmOverloads constructor(
    context: Context,
    private val button: GestureButton? = null,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val exclusionRects: MutableList<Rect> = mutableListOf()

    init {
        updateExclusionRect()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            updateExclusionRect()
            updateSystemGestureExclusion()
        }
    }

    private fun updateExclusionRect() {
        if (!canExcludeSystemGesture()) return
        exclusionRects.clear()
        exclusionRects.add(Rect(0, 0, width, height))
    }

    private fun updateSystemGestureExclusion() {
        exclusionRects.firstOrNull()?.set(0, 0, width, height)
        systemGestureExclusionRects = exclusionRects
    }

    private fun canExcludeSystemGesture(): Boolean {
        val button = button ?: return false
        return button.excludeSystemGestureRects
    }
}