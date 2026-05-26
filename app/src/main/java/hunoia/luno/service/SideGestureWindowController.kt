package hunoia.luno.service

import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import hunoia.luno.SideGestureService
import hunoia.luno.gesture.GestureButton
import hunoia.luno.gesture.Position
import hunoia.luno.gesture.input.MotionEventDispatcher
import hunoia.luno.system.window.removeWindow
import hunoia.luno.system.window.removeWindows
import hunoia.luno.system.window.setBasic
import hunoia.luno.system.window.updateLayout
import hunoia.luno.system.window.updateMainView
import hunoia.luno.service.GestureView
import hunoia.luno.core.DensityProvider

class SideGestureWindowController(private val host: SideGestureService) {
    var mainView: View? = null
        private set

    var buttonViews: List<View>? = null
        private set

    var subGestureOverlayView: View? = null
        private set

    fun replaceMainOverlay(content: @Composable () -> Unit) {
        mainView?.let { host.removeWindow(it) }
        mainView = attachComposeOverlay(content)
    }

    fun replaceGestureButtons(buttons: Collection<GestureButton>) {
        buttonViews?.let { host.removeWindows(it) }
        buttonViews = buttons.map { attachGestureButton(it) }
    }

    fun updateMainLayout() {
        val view = mainView ?: return
        val lp = (view.layoutParams as WindowManager.LayoutParams).apply { updateMainView() }
        updateWindowLayout(view, lp)
    }

    fun attachSubGestureOverlay() {
        if (subGestureOverlayView != null) return
        val wm = ContextCompat.getSystemService(host, WindowManager::class.java)!!
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.RGBA_8888
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            @android.annotation.SuppressLint("RtlHardcoded")
            gravity = Gravity.LEFT or Gravity.TOP
        }
        val view = View(host).apply {
            setOnTouchListener { _, event ->
                MotionEventDispatcher.dispatch(event)
                true
            }
        }
        wm.addView(view, lp)
        subGestureOverlayView = view
    }

    fun detachSubGestureOverlay() {
        val view = subGestureOverlayView ?: return
        host.removeWindow(view)
        subGestureOverlayView = null
    }

    fun updateWindowLayout(view: View, lp: WindowManager.LayoutParams) {
        host.updateLayout(view, lp)
    }

    private fun attachComposeOverlay(content: @Composable () -> Unit): ComposeView {
        val wm = ContextCompat.getSystemService(host, WindowManager::class.java)!!
        val lp = WindowManager.LayoutParams().apply {
            setBasic(false)
            updateMainView()
        }
        val composeView = ComposeView(host).apply {
            setViewTreeLifecycleOwner(host)
            setViewTreeViewModelStoreOwner(host)
            setViewTreeSavedStateRegistryOwner(host)
            setContent { content() }
        }
        wm.addView(composeView, lp)
        return composeView
    }

    private fun attachGestureButton(button: GestureButton): View {
        val wm = ContextCompat.getSystemService(host, WindowManager::class.java)!!
        val lp = WindowManager.LayoutParams().apply {
            setBasic(button.enabled)
            updateGestureButton(button)
        }
        val view = GestureView(host, button).apply {
            tag = button
            setOnTouchListener { v, event ->
                MotionEventDispatcher.dispatch(event)
                if (event.action == MotionEvent.ACTION_UP) v.performClick()
                true
            }
        }
        wm.addView(view, lp)
        return view
    }
}

internal fun WindowManager.LayoutParams.updateGestureButton(button: GestureButton) {
    updateGestureButton(
        button = button,
        rootWidth = DensityProvider.screenWidthPx,
        rootHeight = DensityProvider.screenHeightPx
    )
}

internal fun WindowManager.LayoutParams.updateGestureButton(
    button: GestureButton,
    rootWidth: Int,
    rootHeight: Int,
) {
    when (button.position) {
        Position.Left, Position.Right -> {
            width = button.width
            height = (rootHeight * (button.end - button.start)).toInt()
            y = (rootHeight * button.start).toInt()
        }
        Position.Bottom -> {
            width = (rootWidth * (button.end - button.start)).toInt()
            height = button.width
            x = (rootWidth * button.start).toInt()
            y = rootHeight - button.width
        }
    }
    @android.annotation.SuppressLint("RtlHardcoded")
    gravity = when (button.position) {
        Position.Left -> android.view.Gravity.LEFT or android.view.Gravity.TOP
        Position.Right -> android.view.Gravity.RIGHT or android.view.Gravity.TOP
        Position.Bottom -> android.view.Gravity.LEFT or android.view.Gravity.TOP
    }
}
