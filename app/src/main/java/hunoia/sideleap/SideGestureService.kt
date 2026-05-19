package hunoia.sideleap

import android.content.res.Configuration
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aaron.composeaccessibility.ComponentAccessibilityService
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.InitialSettings
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import hunoia.sideleap.core.event.WallpaperChangedEvent
import hunoia.sideleap.launcher.query.LauncherEnvironment
import hunoia.sideleap.service.SideGestureServiceProxy
import hunoia.sideleap.service.takeScreenshot
import hunoia.sideleap.service.SideGestureButtonRefreshCoordinator
import hunoia.sideleap.service.SideGestureOverlayLifecycle
import hunoia.sideleap.service.SideGestureRuntime
import hunoia.sideleap.service.SideGestureRuntimeState
import hunoia.sideleap.service.ScreenLockObserver
import hunoia.sideleap.service.SideGestureSettingsObserver
import hunoia.sideleap.service.SideGestureWindowController
import hunoia.sideleap.service.WallpaperChangeObserver
import hunoia.sideleap.ui.event.SubscribeEvent
import java.lang.ref.WeakReference
import hunoia.sideleap.ui.theme.SideGestureTheme
import hunoia.sideleap.ui.theme.AnimOverlayFade
import hunoia.sideleap.ui.theme.AnimPanelShift
import hunoia.sideleap.ui.theme.AnimPostHideDelay
import hunoia.sideleap.gesture.application.VirtualMousePointerAction
import hunoia.sideleap.gesture.application.clampVirtualMousePosition
import hunoia.sideleap.ui.component.SideGestureContainer
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.overlay.api.QuickAppLauncherOverlay
import hunoia.sideleap.overlay.api.QuickAppLauncherOverlayHost
import hunoia.sideleap.overlay.api.RuntimePanelOverlay
import hunoia.sideleap.overlay.api.RuntimePanelOverlayHost
import hunoia.sideleap.overlay.api.VirtualMouseOverlay
import hunoia.sideleap.overlay.api.VirtualMouseOverlayHost
import hunoia.sideleap.overlay.api.VolumeScrubOverlay
import hunoia.sideleap.freeze.FrozenPackageEnabler
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.service.hiddenKey
import hunoia.sideleap.ui.component.quickapplaunch.QuickAppLauncherAdjustPanel
import hunoia.sideleap.ui.component.quickapplaunch.QuickAppLauncherContent
import hunoia.sideleap.system.copySensitiveText
import hunoia.sideleap.system.accessibility.Accessibility
import hunoia.sideleap.ui.component.password.PasswordGeneratorPanel
import hunoia.sideleap.ui.component.password.PasswordPanelContent
import com.blankj.utilcode.util.ScreenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/14
 */
class SideGestureService : ComponentAccessibilityService(), SideGestureRuntime, QuickAppLauncherOverlayHost, RuntimePanelOverlayHost, VirtualMouseOverlayHost {

    companion object {
        private var currentRef: WeakReference<SideGestureService>? = null
        var current: SideGestureService?
            get() = currentRef?.get()
            private set(value) { currentRef = if (value != null) WeakReference(value) else null }
    }

    private val proxy = SideGestureServiceProxy(this)
    override val context: android.content.Context
        get() = this

    val quickAppLauncherOverlay by lazy {
        QuickAppLauncherOverlay(this).apply {
            onAppLaunchRequested = { app ->
                coroutineScope.launch(Dispatchers.IO) {
                    recordQuickAppLaunchIfSuccess(true, "${app.packageName}/${app.className}") {
                        SettingsProvider.recordQuickAppLaunch(it)
                    }
                }
            }
        }
    }
    val runtimePanelOverlay by lazy {
        RuntimePanelOverlay(this)
    }
    internal val overlayLifecycle = SideGestureOverlayLifecycle(this)
    override val coroutineScope = MainScope()
    private val windowController = SideGestureWindowController(this)
    private var virtualMouseOverlay: VirtualMouseOverlay? = null
    private var virtualMouseSessionSettings: GestureSettings.VirtualMouse? = null
    private var volumeScrubOverlay: VolumeScrubOverlay? = null
    private val buttonRefreshCoordinator = SideGestureButtonRefreshCoordinator(
        host = this,
        scopeProvider = { coroutineScope },
        initialSettingsProvider = { SettingsProvider.getInitialSettings() },
        advancedSettingsProvider = { advancedSettings },
        buttonViewsProvider = { windowController.buttonViews },
        runtimeStateProvider = {
            SideGestureRuntimeState(
                currentPackageName = getCurrentPackageName(),
                isNowInLockScreenPage = isNowInLockScreenPage,
                isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE,
                isInLauncher = nowInLauncher(),
                isKeyboardInputActive = isKeyboardInputActive,
                hiddenGestureButtons = hiddenGestureButtons.toMap(),
                isMouseMode = isMouseMode,
                nowMs = SystemClock.uptimeMillis(),
            )
        },
    )

    private val settingsObserver = SideGestureSettingsObserver(
        scope = coroutineScope,
        onInitialSettings = { initialSettings = it },
        onAdvancedSettings = { advancedSettings = it },
        onGestureSettings = { gestureSettings = it },
        onActionSettings = { actionSettings = it },
        onGestureButtons = { buttons ->
            windowController.replaceGestureButtons(buttons)
            updateGestureButtons()
        },
        onRefreshGestureButtons = { updateGestureButtons() }
    )
    private val frozenPackageEnabler = FrozenPackageEnabler(
        context = this,
        scopeProvider = { coroutineScope },
        log = { message -> android.util.Log.d("SideLeapLauncher", message) }
    )
    private val wallpaperChangeObserver = WallpaperChangeObserver(this)
    private val screenLockObserver = ScreenLockObserver(
        context = this,
        onScreenOff = {
            isNowInLockScreenPage = true
            overlayLifecycle.onScreenLocked()
            updateGestureButtons()
        },
        onUserPresent = {
            isNowInLockScreenPage = false
            updateGestureButtons()
        }
    )
    private var orientation = if (ScreenUtils.isLandscape()) 2 else 1

    private var isNowInLockScreenPage = false
    private var isMouseMode = false
    private var isVolumeScrubMode = false
    private var isKeyboardInputActive = false
    private var virtualMouseLastPosition = Offset.Unspecified
    private val hiddenGestureButtons = mutableMapOf<String, Long>()

    var initialSettings: InitialSettings? = null
        private set
    override var advancedSettings: AdvancedSettings? = null
        private set
    var gestureSettings: GestureSettings? = null
        private set
    var actionSettings: ActionSettings? = null
        private set

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (orientation != newConfig.orientation) {
            orientation = newConfig.orientation
            updateMainLayout()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        proxy.onAccessibilityEvent(event)
        event?.let { updateKeyboardInputState(it) }
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            updateGestureButtons()
        }
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (current === this) currentRef = null
        overlayLifecycle.onDestroy()
        frozenPackageEnabler.release()
        coroutineScope.cancel()
        virtualMouseOverlay?.closeImmediately()
        volumeScrubOverlay?.dismiss()
        proxy.onRelease()
        screenLockObserver.unregister()
        wallpaperChangeObserver.unregister()
    }

    override fun onSetOverlay() {
        current = this
        registerRuntimeObservers()
        windowController.replaceMainOverlay { renderMainOverlay() }
        settingsObserver.start()
    }

    private fun registerRuntimeObservers() {
        registerScreenLockObserver()
        registerWallpaperChangeObserver()
    }

    @Composable
    private fun renderMainOverlay() {
        var key by remember { mutableStateOf(Any()) }
        val screenshotService = this
        SubscribeEvent(eventClass = WallpaperChangedEvent::class) {
            key = Any()
        }
        key(key) {
            SideGestureTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    val sideButtons by SettingsProvider
                        .sideGestureButtons
                        .collectAsStateWithLifecycle(initialValue = emptyList())
                    val bottomButtons by SettingsProvider
                        .bottomGestureButtons
                        .collectAsStateWithLifecycle(initialValue = emptyList())
                    val advancedSettings by SettingsProvider
                        .advancedSettings
                        .collectAsStateWithLifecycle(initialValue = AdvancedSettings())
                    val gestureSettings by SettingsProvider
                        .gestureSettings
                        .collectAsStateWithLifecycle(initialValue = GestureSettings())
                    val actionSettings by SettingsProvider
                        .actionSettings
                        .collectAsStateWithLifecycle(initialValue = ActionSettings())
                    SideGestureContainer(
                        modifier = Modifier.matchParentSize(),
                        buttons = sideButtons + bottomButtons,
                        animationStyle = when (advancedSettings.animationStyles.isAnimationEnabled) {
                            true -> advancedSettings.animationStyles.value
                            else -> null
                        },
                        onAction = { action, sourceButton ->
                            proxy.onAction(action, sourceButton)
                        },
                        onVirtualMouseStart = { beginVirtualMouseMode() },
                        onVirtualMouseEnd = { endVirtualMouseMode() },
                        onVirtualMouseSettingsUpdate = { settings ->
                            virtualMouseSessionSettings = settings
                        },
                        virtualMousePreviousPosition = { virtualMouseLastPosition },
                        onPointerActionAtPosition = { x, y, keepActive, action ->
                            performVirtualMouseActionAtPosition(x, y, keepActive, action)
                        },
                        onTakeScreenshot = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                screenshotService.takeScreenshot()
                            } else null
                        },
                        actionSettings = actionSettings,
                        advancedSettings = advancedSettings,
                        gestureSettings = gestureSettings
                    )
                }
            }
        }
    }

    private fun registerScreenLockObserver() {
        screenLockObserver.register()
    }

    private fun registerWallpaperChangeObserver() {
        wallpaperChangeObserver.register()
    }

    private fun updateMainLayout() {
        windowController.updateMainLayout()
        updateGestureButtons()
    }

    internal fun updateWindowLayout(view: View, lp: WindowManager.LayoutParams) {
        windowController.updateWindowLayout(view, lp)
    }

    private fun updateGestureButtons() {
        buttonRefreshCoordinator.refresh()
    }

    private fun updateKeyboardInputState(event: AccessibilityEvent) {
        if (advancedSettings?.fitSoftKeyboard != true) {
            setKeyboardInputActive(false)
            return
        }
        val active = when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_FOCUSED,
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED -> isEditableInput(event.source) || hasEditableInputFocus()
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> hasEditableInputFocus()
            else -> return
        }
        setKeyboardInputActive(active)
    }

    private fun setKeyboardInputActive(active: Boolean) {
        if (isKeyboardInputActive == active) return
        isKeyboardInputActive = active
        updateGestureButtons()
    }

    private fun hasEditableInputFocus(): Boolean {
        return isEditableInput(rootInActiveWindow?.findFocus(AccessibilityNodeInfo.FOCUS_INPUT))
    }

    private fun isEditableInput(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val className = node.className?.toString().orEmpty()
        return node.isEditable ||
            className.endsWith("EditText") ||
            node.actionList.any { it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT.id }
    }

    fun getCurrentPackageName(): String {
        return rootInActiveWindow?.packageName?.toString() ?: ""
    }

    override fun nowInLauncher(): Boolean {
        return LauncherEnvironment.isLauncherPackage(this, getCurrentPackageName())
    }

    override fun requestEnableFrozenPackage(packageName: String, onResult: (Boolean) -> Unit) {
        frozenPackageEnabler.request(packageName, onResult)
    }

    fun openPasswordGeneratorPanel() {
        runtimePanelOverlay.show { PasswordPanelContent(applicationContext = applicationContext) }
    }

    fun showVirtualMouseOverlay(continuousModeOverride: Boolean? = null): Boolean {
        if (!beginVirtualMouseMode()) return false
        val overlay = virtualMouseOverlay ?: VirtualMouseOverlay(this).also { virtualMouseOverlay = it }
        val settings = (gestureSettings?.virtualMouse ?: GestureSettings.VirtualMouse()).let {
            if (continuousModeOverride == null) it else it.copy(continuousMode = continuousModeOverride)
        }
        virtualMouseSessionSettings = settings
        overlay.show(
            settings = settings,
            onPointerAction = { x, y, keepActive, action ->
                performVirtualMouseActionAtPosition(x, y, keepActive, action)
            },
            previousPosition = virtualMouseLastPosition,
            onDismiss = { endVirtualMouseMode() },
        )
        return true
    }

    fun beginVirtualMouseMode(): Boolean {
        if (isMouseMode) return false
        isMouseMode = true
        updateGestureButtons()
        return true
    }

    fun endVirtualMouseMode() {
        if (!isMouseMode && virtualMouseOverlay == null) return
        isMouseMode = false
        virtualMouseOverlay?.closeImmediately()
        virtualMouseSessionSettings = null
        updateGestureButtons()
    }

    fun showVolumeScrubOverlay(): Boolean {
        if (!beginVolumeScrubMode()) return false
        val overlay = VolumeScrubOverlay(this).also { volumeScrubOverlay = it }
        overlay.show(onDismiss = { endVolumeScrubMode() })
        return true
    }

    fun beginVolumeScrubMode(): Boolean {
        if (isVolumeScrubMode) return false
        isVolumeScrubMode = true
        updateGestureButtons()
        return true
    }

    fun endVolumeScrubMode() {
        if (!isVolumeScrubMode && volumeScrubOverlay == null) return
        isVolumeScrubMode = false
        volumeScrubOverlay?.dismiss()
        updateGestureButtons()
    }

    private fun performVirtualMouseActionAtPosition(
        x: Int,
        y: Int,
        keepActive: Boolean,
        action: VirtualMousePointerAction,
    ) {
        virtualMouseLastPosition = clampVirtualMousePosition(Offset(x.toFloat(), y.toFloat()))
        virtualMouseOverlay?.closeImmediately()
        coroutineScope.launch {
            delay(80)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                when (action) {
                    VirtualMousePointerAction.Click -> Accessibility.click(this@SideGestureService, x, y)
                    VirtualMousePointerAction.LongPress -> Accessibility.longPress(this@SideGestureService, x, y)
                }
            }
            if (keepActive && isMouseMode) {
                val overlay = virtualMouseOverlay ?: VirtualMouseOverlay(this@SideGestureService).also { virtualMouseOverlay = it }
                overlay.show(
                    settings = virtualMouseSessionSettings ?: gestureSettings?.virtualMouse ?: GestureSettings.VirtualMouse(),
                    previousPosition = virtualMouseLastPosition,
                    onPointerAction = { nextX, nextY, nextKeepActive, nextAction ->
                        performVirtualMouseActionAtPosition(nextX, nextY, nextKeepActive, nextAction)
                    },
                    onDismiss = { endVirtualMouseMode() },
                )
            } else {
                endVirtualMouseMode()
            }
        }
    }

    fun hideGestureButtonTemporarily(button: GestureButton, delayMs: Long) {
        val key = button.hiddenKey()
        hiddenGestureButtons[key] = SystemClock.uptimeMillis() + delayMs
        updateGestureButtons()
        coroutineScope.launch {
            delay(delayMs)
            if ((hiddenGestureButtons[key] ?: 0L) <= SystemClock.uptimeMillis()) {
                hiddenGestureButtons.remove(key)
                updateGestureButtons()
            }
        }
    }

}

internal suspend fun recordQuickAppLaunchIfSuccess(
    success: Boolean,
    appKey: String,
    record: suspend (String) -> Unit,
) {
    if (success) {
        record(appKey)
    }
}
