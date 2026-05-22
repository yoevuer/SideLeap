package hunoia.sideleap.overlay.api

import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.util.Log
import android.view.MotionEvent
import hunoia.sideleap.BuildConfig
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
import androidx.compose.ui.layout.onSizeChanged
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
import hunoia.sideleap.system.window.applyOverlayViewTreeOwners
import hunoia.sideleap.system.window.windowManager
import hunoia.sideleap.launcher.query.AppSearch.key
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.core.DensityProvider
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.ui.theme.SideGestureTheme
import hunoia.sideleap.ui.component.quickapplaunch.QuickAppLauncherAdjustPanel
import hunoia.sideleap.ui.component.quickapplaunch.QuickAppLauncherContent
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
}

class QuickAppLauncherOverlay(private val host: QuickAppLauncherOverlayHost) {
    private var overlayView: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var adjustView: View? = null
    private var isShowing = false
    private var isHiding = false
    private var triggerCloseAnimated: (() -> Unit)? = null
    private var lastCloseMs: Long = 0L
    private var lastAdjustCloseMs: Long = 0L
    var onAppLaunchRequested: ((AppInfo) -> Unit)? = null

    fun toggle() {
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher","toggle: overlayView=${overlayView != null}")
        if (overlayView != null) {
            close()
        } else {
            show()
        }
    }

    fun close() {
        if (isHiding || overlayView == null) {
            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher","close: skipped (isHiding=$isHiding overlayView=${overlayView != null})")
            return
        }
        val reason = "explicit close"
        isHiding = true
        lastCloseMs = System.currentTimeMillis()
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher","close: reason=$reason hasAnimation=${triggerCloseAnimated != null}")
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
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "closeImmediately: removing overlay")
        removeOverlayView()
    }

    private fun removeOverlayView() {
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher","removeOverlayView: removing overlay")
        overlayView?.let {
            it.animate().cancel()
            val wm = host.context.windowManager()
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
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher","show: isShowing=$isShowing isHiding=$isHiding overlayView=${overlayView != null} intervalSinceLastClose=${interval}ms")
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
            val wm = host.context.windowManager()
            runCatching { overlayView?.let { wm.removeView(it) } }
            overlayView = null
            overlayParams = null
        }
    }

    private fun showOverlayView(initialSettings: QuickAppLauncherSettings) {
        val wm = host.context.windowManager()
        val lp = createLayoutParams(initialSettings)
        val composeView = ComposeView(host.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            applyOverlayViewTreeOwners(host)

            setOnTouchListener(createDismissOnOutsideTouch(onOutsideTouch = {
                close()
            }, logTag = "touch"))

            setContent {
                val advancedSettings = host.advancedSettings ?: AdvancedSettings()
                val quickLauncherPopup = advancedSettings.quickLauncherAppLongPressLaunchPopup
                SideGestureTheme {
                    QuickAppLauncherContent(
                        initialSettings = initialSettings,
                        quickLauncherAppLongPressLaunchPopup = quickLauncherPopup,
                        requestEnableFrozenPackage = host::requestEnableFrozenPackage,
                        onCloseAnimated = {
                            isShowing = false
                            isHiding = true
                            lastCloseMs = System.currentTimeMillis()
                            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher","closeAnimated: triggered")
                            removeOverlayView()
                        },
                        onToggleAdjust = { toggleAdjustPanel() },
                        onLaunch = { appInfo, miniWindow ->
                            val now = System.currentTimeMillis()
                            val interval = if (lastCloseMs > 0) now - lastCloseMs else -1L
                            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher","appClick: ${appInfo.label} pkg=${appInfo.packageName} miniWindow=$miniWindow intervalSinceClose=${interval}ms")
                            val success = Launcher.launchAppInfo(
                                host.context,
                                appInfo,
                                miniWindow,
                                advancedSettings.miniWindowHorizontalBias,
                                advancedSettings.miniWindowVerticalBias,
                                advancedSettings.miniWindowVerticalEdgeMarginFraction,
                                advancedSettings.miniWindowVerticalOffsetFraction,
                            )
                            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher","appClick: ${appInfo.label} launchResult=$success")
                            if (success) onAppLaunchRequested?.invoke(appInfo)
                            success
                        },
                        onRegisterCloseAnimated = { callback -> triggerCloseAnimated = callback }
                    )
                }
            }
        }
        wm.addView(composeView, lp)
        overlayView = composeView
        overlayParams = lp
        isShowing = false
    }


    private fun toggleAdjustPanel() {
        if (adjustView != null) {
            closeAdjustPanel()
        } else if (System.currentTimeMillis() - lastAdjustCloseMs > 100L) {
            showAdjustPanel()
        }
    }

    private fun closeAdjustPanel() {
        adjustView?.let {
            val wm = host.context.windowManager()
            runCatching { wm.removeView(it) }
        }
        adjustView = null
        lastAdjustCloseMs = System.currentTimeMillis()
    }

    private fun showAdjustPanel() {
        closeAdjustPanel()
        val wm = host.context.windowManager()
        val density = host.context.resources.displayMetrics.density
        val screenWidth = DensityProvider.screenWidthPx
        val screenHeight = DensityProvider.screenHeightPx
        val initialWidth = (screenWidth * 0.92f).roundToInt()
        val initialHeight = 480.dpToPx(density)
        val lp = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.RGBA_8888
            width = initialWidth
            height = initialHeight
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            @Suppress("DEPRECATION")
            gravity = Gravity.START or Gravity.TOP
            x = ((screenWidth - initialWidth) / 2).coerceAtLeast(0)
            y = ((screenHeight - initialHeight) / 2).coerceAtLeast(0)
        }
        val composeView = ComposeView(host.context).apply {
            setBackgroundColor(Color.TRANSPARENT)
            applyOverlayViewTreeOwners(host)
            setOnTouchListener(createDismissOnOutsideTouch(onOutsideTouch = {
                closeAdjustPanel()
            }, logTag = "adjustTouch"))
            setContent {
                SideGestureTheme {
                    Box(Modifier.onSizeChanged { size ->
                        updateAdjustPanelLayout(
                            wm = wm, view = this@apply,
                            lp = lp, contentWidth = size.width, contentHeight = size.height
                        )
                    }) {
                        QuickAppLauncherAdjustPanel(
                            onSettingsChanged = { settings -> updateLayout(settings) },
                        )
                    }
                }
            }
        }
        wm.addView(composeView, lp)
        adjustView = composeView
    }

    private fun updateAdjustPanelLayout(
        wm: WindowManager, view: View, lp: WindowManager.LayoutParams,
        contentWidth: Int, contentHeight: Int
    ) {
        if (contentWidth <= 0 || contentHeight <= 0) return
        val screenWidth = DensityProvider.screenWidthPx
        val screenHeight = DensityProvider.screenHeightPx
        val nextWidth = contentWidth.coerceIn(1, screenWidth)
        val nextHeight = contentHeight.coerceIn(1, screenHeight)
        if (lp.width == nextWidth && lp.height == nextHeight) return
        val nextX = ((screenWidth - nextWidth) / 2).coerceAtLeast(0)
        val nextY = ((screenHeight - nextHeight) / 2).coerceAtLeast(0)
        lp.width = nextWidth
        lp.height = nextHeight
        lp.x = nextX
        lp.y = nextY
        runCatching { wm.updateViewLayout(view, lp) }
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
        val screenWidth = DensityProvider.screenWidthPx
        val screenHeight = DensityProvider.screenHeightPx
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
        val t = ((content.coerceIn(0.35f, 0.75f) - 0.35f) / 0.4f).coerceIn(0f, 1f)
        val candidateRow = (48 + 8 * t).roundToInt()
        val keyHeight = (34 + 6 * t).roundToInt()
        return ((candidateRow * rows) + (keyHeight * 3) + 52).dpToPx(density)
    }

    private fun updateLayout(settings: QuickAppLauncherSettings) {
        val view = overlayView ?: return
        val lp = overlayParams ?: return
        lp.applyPanelLayout(settings)
        runCatching {
            val wm = host.context.windowManager()
            wm.updateViewLayout(view, lp)
        }
    }

}

private fun createDismissOnOutsideTouch(
    onOutsideTouch: () -> Unit,
    logTag: String,
): View.OnTouchListener = View.OnTouchListener { v, event ->
    if (event.action == MotionEvent.ACTION_OUTSIDE) {
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "$logTag: ACTION_OUTSIDE at (${event.rawX.toInt()}, ${event.rawY.toInt()}) → close")
        onOutsideTouch()
        v.performClick()
        true
    } else {
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "$logTag: action=${event.action} at (${event.rawX.toInt()}, ${event.rawY.toInt()})")
        false
    }
}

private fun Int.dpToPx(density: Float) = (this * density).roundToInt()
