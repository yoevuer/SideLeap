package hunoia.luno

import android.app.WallpaperManager
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import com.aaron.composeaccessibility.ComponentAccessibilityService
import hunoia.luno.settings.model.ActionSettings
import hunoia.luno.settings.model.AdvancedSettings
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.settings.model.InitialSettings
import hunoia.luno.settings.model.QuickAppLauncherSettings
import hunoia.luno.core.AppContext
import hunoia.luno.core.event.Events
import hunoia.luno.system.event.WallpaperChangedEvent
import hunoia.luno.launcher.LauncherFacade
import hunoia.luno.launcher.query.LauncherEnvironment
import hunoia.luno.service.SideGestureServiceProxy
import hunoia.luno.service.GestureOverlayView
import hunoia.luno.service.takeScreenshot
import hunoia.luno.service.SideGestureButtonRefreshCoordinator
import hunoia.luno.service.SideGestureOverlayLifecycle
import hunoia.luno.service.SideGestureRuntime
import hunoia.luno.service.SideGestureRuntimeState
import hunoia.luno.service.ScreenLockObserver
import hunoia.luno.service.SideGestureSettingsObserver
import hunoia.luno.service.SideGestureWindowController
import hunoia.luno.service.WallpaperChangeObserver
import hunoia.luno.ui.event.SubscribeEvent
import java.lang.ref.WeakReference
import hunoia.luno.service.hiddenKey
import hunoia.luno.gesture.application.VirtualMousePointerAction
import hunoia.luno.gesture.application.clampVirtualMousePosition
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.overlay.api.QuickAppLauncherOverlay
import hunoia.luno.overlay.api.QuickAppLauncherOverlayHost
import hunoia.luno.overlay.api.RuntimePanelOverlay
import hunoia.luno.overlay.api.RuntimePanelOverlayHost
import hunoia.luno.overlay.api.VirtualMouseOverlay
import hunoia.luno.overlay.api.VirtualMouseOverlayHost
import hunoia.luno.overlay.api.VolumeScrubOverlay
import hunoia.luno.freeze.FrozenPackageEnabler
import hunoia.luno.gesture.GestureButton
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.system.copySensitiveText
import hunoia.luno.system.accessibility.Accessibility
import hunoia.luno.ui.component.password.PasswordPanelContent
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
        log = { message -> android.util.Log.d("LunoLauncher", message) }
    )
    private val wallpaperChangeObserver = WallpaperChangeObserver(this)
    private var wallpaperColorsListener: WallpaperManager.OnColorsChangedListener? = null
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
    private var orientation = if (AppContext.get().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1

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
        wallpaperColorsListener?.let { listener ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WallpaperManager.getInstance(this).removeOnColorsChangedListener(listener)
            }
        }
        wallpaperChangeObserver.unregister()
    }

    override fun onSetOverlay() {
        current = this
        registerRuntimeObservers()
        windowController.replaceMainOverlay { renderMainOverlay() }
        settingsObserver.start()
        coroutineScope.launch(Dispatchers.IO) {
            LauncherFacade.queryApps(this@SideGestureService)
        }
    }

    private fun registerRuntimeObservers() {
        registerScreenLockObserver()
        registerWallpaperChangeObserver()
    }

    @Composable
    private fun renderMainOverlay() {
        val screenshotService = this
        GestureOverlayView(
            screenshotService = screenshotService,
            onSubGestureModeChanged = { inSubGesture ->
                if (inSubGesture) windowController.attachSubGestureOverlay()
                else windowController.detachSubGestureOverlay()
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    screenshotService.takeScreenshot()
                } else null
            },
            windowController = windowController,
        )
    }

    private fun registerScreenLockObserver() {
        screenLockObserver.register()
    }

    private fun registerWallpaperChangeObserver() {
        wallpaperChangeObserver.register()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val listener = WallpaperManager.OnColorsChangedListener { _, _ ->
                Events.post(WallpaperChangedEvent())
            }
            wallpaperColorsListener = listener
            WallpaperManager.getInstance(this).addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
        }
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
        val scrubSettings = actionSettings?.volumeScrub ?: ActionSettings.VolumeScrub()
        val overlay = VolumeScrubOverlay(this, scrubSettings.horizontalEnabled, scrubSettings.stepThresholdDp).also { volumeScrubOverlay = it }
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
