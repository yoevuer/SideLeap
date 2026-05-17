package hunoia.sideleap.overlay.api

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.blankj.utilcode.util.ScreenUtils

interface RuntimePanelOverlayHost : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    val context: Context
}

class RuntimePanelScope internal constructor(
    val close: () -> Unit,
    val updatePanelSize: (width: Int, height: Int) -> Unit,
)

class RuntimePanelOverlay(private val host: RuntimePanelOverlayHost) {
    private var overlayView: View? = null
    private var imeHeight: Int = 0

    fun show(content: @Composable RuntimePanelScope.() -> Unit) {
        close()
        val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
        val lp = createLayoutParams()

        val composeView = ComposeView(host.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setViewTreeLifecycleOwner(host)
            setViewTreeViewModelStoreOwner(host)
            setViewTreeSavedStateRegistryOwner(host)

            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    close()
                    performClick()
                    true
                } else false
            }

            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                val nextImeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val keyboardVisible = nextImeHeight > 0 ||
                    (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                     insets.isVisible(WindowInsetsCompat.Type.ime()))
                val newImeHeight = if (keyboardVisible) nextImeHeight else 0
                if (imeHeight != newImeHeight) {
                    imeHeight = newImeHeight
                    updatePanelLayout(wm, view, lp, lp.width, lp.height)
                }
                insets
            }

            setContent {
                RuntimePanelScope(
                    close = ::close,
                    updatePanelSize = { width, height ->
                        updatePanelLayout(wm, this, lp, width, height)
                    },
                ).content()
            }
        }

        wm.addView(composeView, lp)
        overlayView = composeView
        ViewCompat.requestApplyInsets(composeView)
        composeView.post { ViewCompat.requestApplyInsets(composeView) }
    }

    fun close() {
        val view = overlayView ?: return
        overlayView = null
        imeHeight = 0
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
        val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
        runCatching { wm.removeView(view) }
    }

    private fun updatePanelLayout(
        wm: WindowManager,
        view: View,
        lp: WindowManager.LayoutParams,
        width: Int,
        height: Int,
    ) {
        if (width <= 0 || height <= 0) return
        val screenWidth = ScreenUtils.getScreenWidth()
        val screenHeight = ScreenUtils.getScreenHeight()
        val nextWidth = width.coerceIn(1, screenWidth)
        val nextHeight = height.coerceIn(1, screenHeight)
        val nextX = ((screenWidth - nextWidth) / 2).coerceAtLeast(0)
        val nextY = (screenHeight - nextHeight - BottomMarginPx - imeHeight).coerceAtLeast(0)
        if (lp.width == nextWidth && lp.height == nextHeight && lp.x == nextX && lp.y == nextY) return

        lp.width = nextWidth
        lp.height = nextHeight
        lp.x = nextX
        lp.y = nextY
        runCatching { wm.updateViewLayout(view, lp) }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        val screenWidth = ScreenUtils.getScreenWidth()
        val panelWidth = (screenWidth * 0.9f).toInt()

        return WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.RGBA_8888
            width = panelWidth
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            gravity = Gravity.START or Gravity.TOP
            x = ((screenWidth - panelWidth) / 2).coerceAtLeast(0)
            y = 0
        }
    }

    private companion object {
        const val BottomMarginPx = 180
    }
}
