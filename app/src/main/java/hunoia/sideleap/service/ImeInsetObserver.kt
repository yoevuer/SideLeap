package hunoia.sideleap.service

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.blankj.utilcode.util.ScreenUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ImeInsetObserver(
    private val context: Context,
    private val anchorProvider: () -> View?
) {
    private val _flow = MutableStateFlow(0)
    val flow: StateFlow<Int> = _flow.asStateFlow()

    private var overlayView: View? = null
    private var insetsView: View? = null

    fun register() {
        unregister()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            registerWithWindowInsets()
        } else {
            registerWithOverlayFallback()
        }
    }

    fun unregister() {
        _flow.value = 0
        insetsView?.let { ViewCompat.setOnApplyWindowInsetsListener(it, null) }
        insetsView = null
        overlayView?.let { removeView(it) }
        overlayView = null
    }

    private fun registerWithWindowInsets() {
        val anchor = anchorProvider() ?: return
        insetsView = anchor
        ViewCompat.setOnApplyWindowInsetsListener(anchor) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            _flow.value = if (imeVisible) {
                insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            } else {
                0
            }
            insets
        }
        ViewCompat.requestApplyInsets(anchor)
    }

    private fun registerWithOverlayFallback() {
        overlayView = View(context).apply {
            val localRect = Rect()
            val windowRect = Rect()
            viewTreeObserver.addOnGlobalLayoutListener {
                getLocalVisibleRect(localRect)
                getWindowVisibleDisplayFrame(windowRect)
                val navBarHeight = ScreenUtils.getScreenHeight() - windowRect.bottom
                val imePadding = windowRect.height() - localRect.height() + navBarHeight
                _flow.value = if (localRect.height() == windowRect.height()) 0 else imePadding
            }
            val lp = WindowManager.LayoutParams().also { lp ->
                lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                lp.height = WindowManager.LayoutParams.MATCH_PARENT
                lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
                lp.format = PixelFormat.RGBA_8888
                lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            }
            addView(this, lp)
        }
    }

    private fun addView(view: View, lp: WindowManager.LayoutParams) {
        val wm = ContextCompat.getSystemService(context, WindowManager::class.java)!!
        wm.addView(view, lp)
    }

    private fun removeView(view: View) {
        val wm = ContextCompat.getSystemService(context, WindowManager::class.java)!!
        try {
            wm.removeViewImmediate(view)
        } catch (_: Exception) {
        }
    }
}
