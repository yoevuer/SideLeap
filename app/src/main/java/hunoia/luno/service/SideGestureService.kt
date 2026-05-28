package hunoia.luno.service

import android.app.WallpaperManager
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.Composable
import com.aaron.composeaccessibility.ComponentAccessibilityService
import hunoia.luno.settings.model.ActionSettings
import hunoia.luno.settings.model.AdvancedSettings
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.settings.model.InitialSettings
import hunoia.luno.core.AppContext
import hunoia.luno.core.Events
import hunoia.luno.system.event.WallpaperChangedEvent
import hunoia.luno.launcher.LauncherFacade
import hunoia.luno.launcher.query.LauncherEnvironment
import hunoia.luno.service.runtime.PointerRuntime
import hunoia.luno.service.runtime.VolumeScrubRuntime
import hunoia.luno.service.runtime.GestureButtonHideRuntime
import hunoia.luno.ui.event.SubscribeEvent
import java.lang.ref.WeakReference
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.overlay.api.QuickAppLauncherOverlay
import hunoia.luno.overlay.api.QuickAppLauncherOverlayHost
import hunoia.luno.overlay.api.RuntimePanelOverlay
import hunoia.luno.overlay.api.RuntimePanelOverlayHost
import hunoia.luno.overlay.api.PointerOverlayHost
import hunoia.luno.freeze.FrozenPackageEnabler
import hunoia.luno.gesture.GestureButton
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.gesture.application.PointerAction
import hunoia.luno.system.accessibility.Accessibility
import hunoia.luno.system.copySensitiveText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SideGestureService : ComponentAccessibilityService(), SideGestureRuntime, QuickAppLauncherOverlayHost, RuntimePanelOverlayHost, PointerOverlayHost {

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
                hiddenGestureButtons = hideRuntime.getSnapshot(),
                isMouseMode = pointerRuntime.isActive,
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
    private val pointerRuntime = PointerRuntime(
        host = this,
        scope = coroutineScope,
        gestureSettingsProvider = { gestureSettings },
        onStateChanged = { updateGestureButtons() },
    )
    private val volumeScrubRuntime = VolumeScrubRuntime(
        context = this,
        actionSettingsProvider = { actionSettings },
        onStateChanged = { updateGestureButtons() },
    )
    private val hideRuntime = GestureButtonHideRuntime(
        scope = coroutineScope,
        onStateChanged = { updateGestureButtons() },
    )

    private var orientation = if (AppContext.get().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
    private var isNowInLockScreenPage = false
    private var isKeyboardInputActive = false

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
        pointerRuntime.onDestroy()
        volumeScrubRuntime.onDestroy()
        proxy.onRelease()
        screenLockObserver.unregister()
        wallpaperColorsListener?.let { listener ->
            WallpaperManager.getInstance(this).removeOnColorsChangedListener(listener)
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
            onPointerStart = { pointerRuntime.show() },
            onPointerEnd = { pointerRuntime.end() },
            onPointerSettingsUpdate = { settings -> pointerRuntime.onSettingsUpdate(settings) },
            pointerPreviousPosition = { pointerRuntime.getLastPosition() },
            onPointerActionAtPosition = { x, y, keepActive, action ->
                when (action) {
                    PointerAction.Click -> Accessibility.click(this, x, y)
                    PointerAction.LongPress -> Accessibility.longPress(this, x, y)
                }
                if (!keepActive) pointerRuntime.end()
            },
            windowController = windowController,
        )
    }

    private fun registerScreenLockObserver() {
        screenLockObserver.register()
    }

    private fun registerWallpaperChangeObserver() {
        wallpaperChangeObserver.register()
        val listener = WallpaperManager.OnColorsChangedListener { _, _ ->
            Events.post(WallpaperChangedEvent())
        }
        wallpaperColorsListener = listener
        WallpaperManager.getInstance(this).addOnColorsChangedListener(listener, Handler(Looper.getMainLooper()))
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

    fun showPointerOverlay(continuousModeOverride: Boolean? = null): Boolean =
        pointerRuntime.show(continuousModeOverride)

    fun beginPointerMode(): Boolean = pointerRuntime.show()  // dummy: no-op, handled by runtime

    fun endPointerMode() = pointerRuntime.end()

    fun showVolumeScrubOverlay(): Boolean = volumeScrubRuntime.show()

    fun beginVolumeScrubMode(): Boolean = volumeScrubRuntime.show()  // dummy: no-op

    fun endVolumeScrubMode() = volumeScrubRuntime.end()

    fun hideGestureButtonTemporarily(button: GestureButton, delayMs: Long) {
        hideRuntime.hideTemporarily(button, delayMs)
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
