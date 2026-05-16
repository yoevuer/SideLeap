package hunoia.sideleap.service

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import hunoia.sideleap.SideGestureService
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.gesture.input.MotionEventDispatcher
import hunoia.sideleap.system.window.removeWindow
import hunoia.sideleap.system.window.removeWindows
import hunoia.sideleap.system.window.setBasic
import hunoia.sideleap.system.window.updateLayout
import hunoia.sideleap.system.window.updateMainView
import hunoia.sideleap.service.GestureView
import com.blankj.utilcode.util.ScreenUtils

class SideGestureWindowController(private val host: SideGestureService) {
    var mainView: View? = null
        private set

    var buttonViews: List<View>? = null
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
                false
            }
        }
        wm.addView(view, lp)
        return view
    }
}

internal fun WindowManager.LayoutParams.updateGestureButton(button: GestureButton) {
    updateGestureButton(
        button = button,
        rootWidth = ScreenUtils.getScreenWidth(),
        rootHeight = ScreenUtils.getScreenHeight()
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
