package hunoia.sideleap.overlay.api

import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import hunoia.sideleap.R
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.launch.Launcher
import hunoia.sideleap.launcher.query.AppSearch.key
import hunoia.sideleap.settings.api.SettingsProvider
import com.blankj.utilcode.util.ScreenUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistryOwner
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.math.max
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

interface QuickAppLauncherOverlayHost : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    val context: Context
    val coroutineScope: CoroutineScope
    val advancedSettings: AdvancedSettings?

    fun requestEnableFrozenPackage(packageName: String, onResult: (Boolean) -> Unit)

    @Composable
    fun RenderQuickAppLauncherContent(
        initialSettings: QuickAppLauncherSettings,
        quickLauncherAppLongPressLaunchPopup: Boolean,
        onCloseAnimated: () -> Unit,
        onToggleAdjust: () -> Unit,
        onLaunch: (AppInfo, Boolean) -> Boolean,
        onRegisterCloseAnimated: ((() -> Unit) -> Unit)? = null,
    )

    @Composable
    fun RenderQuickAppLauncherAdjustPanel(onSettingsChanged: (QuickAppLauncherSettings) -> Unit)
}

class QuickAppLauncherOverlay(private val host: QuickAppLauncherOverlayHost) {
    private var overlayView: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var adjustView: View? = null
    private var isShowing = false
    private var isHiding = false
    private var triggerCloseAnimated: (() -> Unit)? = null
    private var lastCloseMs: Long = 0L
    var onAppLaunchRequested: ((AppInfo) -> Unit)? = null

    fun toggle() {
        Log.d("SideLeapLauncher","toggle: overlayView=${overlayView != null}")
        if (overlayView != null) {
            close()
        } else {
            show()
        }
    }

    fun close() {
        if (isHiding || overlayView == null) {
            Log.d("SideLeapLauncher","close: skipped (isHiding=$isHiding overlayView=${overlayView != null})")
            return
        }
        val reason = "explicit close"
        isHiding = true
        lastCloseMs = System.currentTimeMillis()
        Log.d("SideLeapLauncher","close: reason=$reason hasAnimation=${triggerCloseAnimated != null}")
        if (triggerCloseAnimated != null) {
            triggerCloseAnimated?.invoke()
        } else {
            removeOverlayView()
        }
    }

    fun closeImmediately() {
        if (overlayView == null) {
            return
        }
        isShowing = false
        isHiding = true
        lastCloseMs = System.currentTimeMillis()
        Log.d("SideLeapLauncher", "closeImmediately: removing overlay")
        removeOverlayView()
    }

    private fun removeOverlayView() {
        Log.d("SideLeapLauncher","removeOverlayView: removing overlay")
        overlayView?.let {
            it.animate().cancel()
            val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
            runCatching { wm.removeView(it) }
        }
        overlayView = null
        overlayParams = null
        isHiding = false
        triggerCloseAnimated = null
        closeAdjustPanel()
    }

    fun show() {
        val now = System.currentTimeMillis()
        val interval = if (lastCloseMs > 0) now - lastCloseMs else -1L
        Log.d("SideLeapLauncher","show: isShowing=$isShowing isHiding=$isHiding overlayView=${overlayView != null} intervalSinceLastClose=${interval}ms")
        if (isShowing || isHiding || overlayView != null) return
        isShowing = true

        cleanupExistingOverlay()

        host.coroutineScope.launch {
            val initialSettings = loadSettingsAsync()
            withContext(Dispatchers.Main.immediate) {
                showOverlayView(initialSettings)
            }
        }
    }

    private suspend fun loadSettingsAsync() = withContext(Dispatchers.IO) {
        SettingsProvider.getQuickAppLauncherSettings()
    }

    private fun cleanupExistingOverlay() {
        if (overlayView != null && !isHiding) {
            val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
            runCatching { overlayView?.let { wm.removeView(it) } }
            overlayView = null
            overlayParams = null
        }
    }

    private fun showOverlayView(initialSettings: QuickAppLauncherSettings) {
        val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
        val lp = createLayoutParams(initialSettings)
        val composeView = ComposeView(host.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setViewTreeLifecycleOwner(host)
            setViewTreeViewModelStoreOwner(host)
            setViewTreeSavedStateRegistryOwner(host)

            setOnTouchListener(createDismissOnOutsideTouch(onOutsideTouch = {
                close()
            }, logTag = "touch"))

            setContent {
                val advancedSettings = host.advancedSettings ?: AdvancedSettings()
                val quickLauncherPopup = advancedSettings.quickLauncherAppLongPressLaunchPopup
                host.RenderQuickAppLauncherContent(
                    initialSettings = initialSettings,
                    quickLauncherAppLongPressLaunchPopup = quickLauncherPopup,
                    onCloseAnimated = {
                        isShowing = false
                        isHiding = true
                        lastCloseMs = System.currentTimeMillis()
                        Log.d("SideLeapLauncher","closeAnimated: triggered")
                        removeOverlayView()
                    },
                    onToggleAdjust = { toggleAdjustPanel() },
                    onLaunch = { appInfo, miniWindow ->
                        val now = System.currentTimeMillis()
                        val interval = if (lastCloseMs > 0) now - lastCloseMs else -1L
                        Log.d("SideLeapLauncher","appClick: ${appInfo.label} pkg=${appInfo.packageName} miniWindow=$miniWindow intervalSinceClose=${interval}ms")
                        val success = Launcher.launchAppInfo(
                            host.context,
                            appInfo,
                            miniWindow,
                            advancedSettings.miniWindowHorizontalBias,
                            advancedSettings.miniWindowVerticalBias,
                            advancedSettings.miniWindowVerticalEdgeMarginFraction,
                        )
                        Log.d("SideLeapLauncher","appClick: ${appInfo.label} launchResult=$success")
                        if (success) onAppLaunchRequested?.invoke(appInfo)
                        success
                    },
                    onRegisterCloseAnimated = { callback -> triggerCloseAnimated = callback }
                )
            }
        }
        composeView.alpha = 0f
        wm.addView(composeView, lp)
        overlayView = composeView
        overlayParams = lp

        composeView.animate()
            .alpha(1f)
            .setDuration(200L)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction { isShowing = false }
            .start()
    }


    private fun toggleAdjustPanel() {
        if (adjustView != null) closeAdjustPanel() else showAdjustPanel()
    }

    private fun closeAdjustPanel() {
        adjustView?.let {
            val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
            runCatching { wm.removeView(it) }
        }
        adjustView = null
    }

    private fun showAdjustPanel() {
        closeAdjustPanel()
        val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.RGBA_8888
            width = (ScreenUtils.getScreenWidth() * 0.92f).roundToInt()
            height = WindowManager.LayoutParams.WRAP_CONTENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 32
        }
        val view = ComposeView(host.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setViewTreeLifecycleOwner(host)
            setViewTreeViewModelStoreOwner(host)
            setViewTreeSavedStateRegistryOwner(host)
            setOnTouchListener(createDismissOnOutsideTouch(onOutsideTouch = {
                closeAdjustPanel()
            }, logTag = "adjustTouch"))
            setContent {
                    host.RenderQuickAppLauncherAdjustPanel(
                        onSettingsChanged = { settings -> updateLayout(settings) }
                    )
            }
        }
        wm.addView(view, lp)
        adjustView = view
    }

    private fun createLayoutParams(settings: QuickAppLauncherSettings) = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        format = PixelFormat.RGBA_8888
        height = WindowManager.LayoutParams.WRAP_CONTENT
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        @Suppress("DEPRECATION")
        gravity = Gravity.START or Gravity.TOP
        applyPanelLayout(settings)
    }

    private fun WindowManager.LayoutParams.applyPanelLayout(settings: QuickAppLauncherSettings) {
        val screenWidth = ScreenUtils.getScreenWidth()
        val screenHeight = ScreenUtils.getScreenHeight()
        val panelWidth = (screenWidth * settings.panelWidthFraction.coerceIn(0.65f, 1f)).roundToInt().coerceIn(1, screenWidth)
        val estimatedHeight = estimatePanelHeightPx(settings).coerceAtMost(screenHeight)
        width = panelWidth
        x = ((screenWidth - panelWidth) * settings.panelHorizontalBias.coerceIn(0f, 1f)).roundToInt().coerceIn(0, screenWidth - panelWidth)
        // Ensure y position and height properly cover the visible panel area
        val maxY = max(0, screenHeight - estimatedHeight)
        y = (screenHeight * (1f - settings.panelHeightFraction.coerceIn(0.05f, 0.95f))).roundToInt()
            .coerceIn(0, maxY)
        // Set height to exact estimated panel height to avoid oversized window
        height = estimatedHeight
    }

    private fun estimatePanelHeightPx(settings: QuickAppLauncherSettings): Int {
        val density = host.context.resources.displayMetrics.density
        val content = settings.contentHeightFraction.coerceIn(0.35f, 0.9f)
        val rows = settings.candidateRows.coerceIn(1, 3)
        val candidateRow = when {
            content < 0.5f -> 48
            content < 0.75f -> 52
            else -> 56
        }
        val keyHeight = when {
            content < 0.5f -> 34
            content < 0.75f -> 36
            else -> 40
        }
        return ((candidateRow * rows) + (keyHeight * 3) + 52).dpToPx(density)
    }

    private fun updateLayout(settings: QuickAppLauncherSettings) {
        val view = overlayView ?: return
        val lp = overlayParams ?: return
        lp.applyPanelLayout(settings)
        runCatching {
            val wm = ContextCompat.getSystemService(host.context, WindowManager::class.java)!!
            wm.updateViewLayout(view, lp)
        }
    }

}

private fun createDismissOnOutsideTouch(
    onOutsideTouch: () -> Unit,
    logTag: String,
): View.OnTouchListener = View.OnTouchListener { v, event ->
    if (event.action == MotionEvent.ACTION_OUTSIDE) {
        Log.d("SideLeapLauncher", "$logTag: ACTION_OUTSIDE at (${event.rawX.toInt()}, ${event.rawY.toInt()}) → close")
        onOutsideTouch()
        v.performClick()
        true
    } else {
        Log.d("SideLeapLauncher", "$logTag: action=${event.action} at (${event.rawX.toInt()}, ${event.rawY.toInt()})")
        false
    }
}

private fun Int.dpToPx(density: Float) = (this * density).roundToInt()
