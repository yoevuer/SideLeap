package hunoia.sideleap.overlay.api

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
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

interface RuntimePanelOverlayHost : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    val context: Context
}

class RuntimePanelScope internal constructor(
    val close: () -> Unit,
)

class RuntimePanelOverlay(private val host: RuntimePanelOverlayHost) {
    private var overlayView: View? = null

    fun show(content: @Composable RuntimePanelScope.() -> Unit) {
        close()
        val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
        val layoutParams = createLayoutParams()
        val composeView = ComposeView(host.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setViewTreeLifecycleOwner(host)
            setViewTreeViewModelStoreOwner(host)
            setViewTreeSavedStateRegistryOwner(host)
            setContent {
                RuntimePanelScope(close = ::close).content()
            }
            ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
                updateLayoutForSafeArea(wm, view, layoutParams, insets)
                insets
            }
        }
        wm.addView(composeView, layoutParams)
        overlayView = composeView
        ViewCompat.requestApplyInsets(composeView)
    }

    fun close() {
        val view = overlayView ?: return
        overlayView = null
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
        val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
        runCatching { wm.removeView(view) }
    }

    private fun updateLayoutForSafeArea(
        wm: WindowManager,
        view: View,
        lp: WindowManager.LayoutParams,
        insets: WindowInsetsCompat,
    ) {
        val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val systemGestures = insets.getInsets(WindowInsetsCompat.Type.systemGestures())
        val navigationBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val left = systemGestures.left
        val top = statusBars.top
        val right = systemGestures.right
        val bottom = maxOf(systemGestures.bottom, navigationBars.bottom)
        val metrics = host.context.resources.displayMetrics
        val nextWidth = (metrics.widthPixels - left - right).coerceAtLeast(1)
        val nextHeight = (metrics.heightPixels - top - bottom).coerceAtLeast(1)
        if (lp.x == left && lp.y == top && lp.width == nextWidth && lp.height == nextHeight) return

        lp.x = left
        lp.y = top
        lp.width = nextWidth
        lp.height = nextHeight
        runCatching { wm.updateViewLayout(view, lp) }
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            gravity = Gravity.START or Gravity.TOP
        }
    }
}
