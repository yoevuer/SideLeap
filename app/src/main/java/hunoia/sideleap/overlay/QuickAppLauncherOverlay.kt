package hunoia.sideleap.overlay

import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.HapticFeedbackConstants
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
import hunoia.sideleap.SideGestureService
import hunoia.sideleap.ui.widget.quickapplaunch.QuickAppLauncherAdjustPanel
import hunoia.sideleap.ui.widget.quickapplaunch.QuickAppLauncherContent
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.launch.Launcher
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.core.diagnostics.LauncherDiagnostics
import com.blankj.utilcode.util.ScreenUtils
import androidx.core.content.ContextCompat
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import hunoia.sideleap.ui.theme.SideGestureTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.max
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

class QuickAppLauncherOverlay(private val service: SideGestureService) {
    private var overlayView: View? = null
    private var overlayParams: WindowManager.LayoutParams? = null
    private var adjustView: View? = null
    private var isShowing = false
    private var isHiding = false
    private var triggerCloseAnimated: (() -> Unit)? = null
    private var lastCloseMs: Long = 0L

    fun toggle() {
        LauncherDiagnostics.d(service,"toggle: overlayView=${overlayView != null}")
        if (overlayView != null) {
            close()
        } else {
            show()
        }
    }

    fun close() {
        if (isHiding || overlayView == null) {
            LauncherDiagnostics.d(service,"close: skipped (isHiding=$isHiding overlayView=${overlayView != null})")
            return
        }
        val reason = "explicit close"
        isHiding = true
        lastCloseMs = System.currentTimeMillis()
        LauncherDiagnostics.d(service,"close: reason=$reason hasAnimation=${triggerCloseAnimated != null}")
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
        LauncherDiagnostics.d(service, "closeImmediately: removing overlay")
        removeOverlayView()
    }

    private fun removeOverlayView() {
        LauncherDiagnostics.d(service,"removeOverlayView: removing overlay")
        overlayView?.let {
            it.animate().cancel()
            val wm = ContextCompat.getSystemService(service, WindowManager::class.java)!!
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
        LauncherDiagnostics.d(service,"show: isShowing=$isShowing isHiding=$isHiding overlayView=${overlayView != null} intervalSinceLastClose=${interval}ms")
        if (isShowing || isHiding || overlayView != null) return
        isShowing = true

        // Close any existing overlay before showing new one
        // Only close immediately if no animation is in progress
        if (overlayView != null && !isHiding) {
            overlayView?.let {
                val wm = ContextCompat.getSystemService(service, WindowManager::class.java)!!
                runCatching { wm.removeView(it) }
            }
            overlayView = null
            overlayParams = null
        }

        service.coroutineScope.launch {
            val initialSettings = kotlinx.coroutines.withContext(Dispatchers.IO) {
                SettingsProvider.getQuickAppLauncherSettings()
            }
            kotlinx.coroutines.withContext(Dispatchers.Main.immediate) {
            val wm = ContextCompat.getSystemService(service, WindowManager::class.java)!!
            val lp = createLayoutParams(initialSettings)
            val composeView = ComposeView(service).apply {
                setBackgroundColor(Color.TRANSPARENT)
                setViewTreeLifecycleOwner(service)
                setViewTreeViewModelStoreOwner(service)
                setViewTreeSavedStateRegistryOwner(service)
                
                setOnTouchListener { v, event ->
                    val now = System.currentTimeMillis()
                    if (event.action == MotionEvent.ACTION_OUTSIDE) {
                        lastCloseMs = now
                        LauncherDiagnostics.d(service,"touch: ACTION_OUTSIDE at (${event.rawX.toInt()}, ${event.rawY.toInt()}) → close")
                        close()
                        v.performClick()
                        true
                    } else {
                        LauncherDiagnostics.d(service,"touch: action=${event.action} at (${event.rawX.toInt()}, ${event.rawY.toInt()})")
                        false
                    }
                }
                
                setContent {
                    SideGestureTheme {
                        val quickLauncherPopup = service.advancedSettings?.quickLauncherAppLongPressLaunchPopup ?: false
                        QuickAppLauncherContent(
                            service = service,
                            initialSettings = initialSettings,
                            quickLauncherAppLongPressLaunchPopup = quickLauncherPopup,
                            onClose = { close() },
                            onCloseAnimated = { 
                                isShowing = false
                                isHiding = true
                                val now = System.currentTimeMillis()
                                lastCloseMs = now
                                LauncherDiagnostics.d(service,"closeAnimated: triggered")
                                removeOverlayView()
                            },
                            onSettingsChanged = { settings -> updateLayout(settings) },
                            onToggleAdjust = { toggleAdjustPanel() },
                            onLaunch = { appInfo, miniWindow ->
                                val now = System.currentTimeMillis()
                                val interval = if (lastCloseMs > 0) now - lastCloseMs else -1L
                                LauncherDiagnostics.d(service,"appClick: ${appInfo.label} pkg=${appInfo.packageName} miniWindow=$miniWindow intervalSinceClose=${interval}ms")
                                val success = Launcher.launchAppInfo(service, appInfo, miniWindow)
                                LauncherDiagnostics.d(service,"appClick: ${appInfo.label} launchResult=$success")
                                if (success) {
                                    updateQuickAppLauncherStats(appInfo)
                                }
                                success
                            },
                            onRegisterCloseAnimated = { callback -> triggerCloseAnimated = callback }
                        )
                    }
                }
            }
            // Set initial alpha to 0 for fade-in effect
            composeView.alpha = 0f
            wm.addView(composeView, lp)
            overlayView = composeView
            overlayParams = lp
            
            // Start fade-in animation with proper cleanup
            composeView.animate()
                .alpha(1f)
                .setDuration(200L)
                .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction {
                    isShowing = false
                }
                .start()
            }
        }
    }


    private fun toggleAdjustPanel() {
        if (adjustView != null) closeAdjustPanel() else showAdjustPanel()
    }

    private fun closeAdjustPanel() {
        adjustView?.let {
            val wm = ContextCompat.getSystemService(service, WindowManager::class.java)!!
            runCatching { wm.removeView(it) }
        }
        adjustView = null
    }

    private fun showAdjustPanel() {
        closeAdjustPanel()
        val wm = ContextCompat.getSystemService(service, WindowManager::class.java)!!
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
        val view = ComposeView(service).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setViewTreeLifecycleOwner(service)
            setViewTreeViewModelStoreOwner(service)
            setViewTreeSavedStateRegistryOwner(service)
            setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_OUTSIDE) {
                    LauncherDiagnostics.d(service,"adjustTouch: ACTION_OUTSIDE at (${event.rawX.toInt()}, ${event.rawY.toInt()}) → close")
                    closeAdjustPanel()
                    v.performClick()
                    true
                } else {
                    LauncherDiagnostics.d(service,"adjustTouch: action=${event.action} at (${event.rawX.toInt()}, ${event.rawY.toInt()})")
                    false
                }
            }
            setContent {
                SideGestureTheme {
                    QuickAppLauncherAdjustPanel(
                        onSettingsChanged = { settings -> updateLayout(settings) }
                    )
                }
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
        val density = service.resources.displayMetrics.density
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
            val wm = ContextCompat.getSystemService(service, WindowManager::class.java)!!
            wm.updateViewLayout(view, lp)
        }
    }

    private fun updateQuickAppLauncherStats(app: AppInfo) {
        CoroutineScope(Dispatchers.IO).launch {
            SettingsProvider.updateQuickAppLauncherSettings { old ->
                val key = app.packageName
                old.copy(
                    recentLaunchTime = old.recentLaunchTime + (key to System.currentTimeMillis()),
                    launchCount = old.launchCount + (key to ((old.launchCount[key] ?: 0L) + 1L))
                )
            }
        }
    }

}

private fun Int.dpToPx(density: Float) = (this * density).roundToInt()

