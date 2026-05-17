package hunoia.sideleap

import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
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
import hunoia.sideleap.service.ImeInsetObserver
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
import hunoia.sideleap.ui.widget.SideGestureContainer
import hunoia.sideleap.settings.api.SettingsProvider
import hunoia.sideleap.overlay.api.QuickAppLauncherOverlay
import hunoia.sideleap.overlay.api.QuickAppLauncherOverlayHost
import hunoia.sideleap.overlay.api.RuntimePanelOverlay
import hunoia.sideleap.overlay.api.RuntimePanelOverlayHost
import hunoia.sideleap.freeze.FrozenPackageEnabler
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.ui.widget.quickapplaunch.QuickAppLauncherAdjustPanel
import hunoia.sideleap.ui.widget.quickapplaunch.QuickAppLauncherContent
import hunoia.sideleap.system.api.copySensitiveText
import hunoia.sideleap.ui.widget.password.PasswordGeneratorPanel
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
class SideGestureService : ComponentAccessibilityService(), SideGestureRuntime, QuickAppLauncherOverlayHost, RuntimePanelOverlayHost {

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
                imePadding = imeInsetObserver.flow.value,
            )
        },
    )

    private val imeInsetObserver = ImeInsetObserver(this) { windowController.mainView }
    private val settingsObserver = SideGestureSettingsObserver(
        scope = coroutineScope,
        imeInsetObserver = imeInsetObserver,
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
        proxy.onRelease()
        screenLockObserver.unregister()
        wallpaperChangeObserver.unregister()
        imeInsetObserver.unregister()
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
                    val imePadding by imeInsetObserver
                        .flow
                        .collectAsStateWithLifecycle()
                    val actionSettings by SettingsProvider
                        .actionSettings
                        .collectAsStateWithLifecycle(initialValue = ActionSettings())
                    SideGestureContainer(
                        modifier = Modifier.matchParentSize(),
                        buttons = sideButtons + bottomButtons,
                        imePadding = imePadding,
                        animationStyle = when (advancedSettings.animationStyles.isAnimationEnabled) {
                            true -> advancedSettings.animationStyles.value
                            else -> null
                        },
                        onAction = { action ->
                            proxy.onAction(action)
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
        runtimePanelOverlay.show {
            SideGestureTheme {
                val coroutineScope = rememberCoroutineScope()
                var panelVisible by remember { mutableStateOf(false) }
                var closing by remember { mutableStateOf(false) }
                val panelAlpha by animateFloatAsState(
                    targetValue = if (panelVisible) 1f else 0f,
                    animationSpec = tween(AnimOverlayFade.toInt()),
                    label = "passwordPanelAlpha"
                )
                val panelShiftY by animateFloatAsState(
                    targetValue = if (panelVisible) 0f else 18f,
                    animationSpec = tween(AnimPanelShift.toInt()),
                    label = "passwordPanelShiftY"
                )
                val closeAnimated = {
                    if (!closing) {
                        closing = true
                        panelVisible = false
                        coroutineScope.launch {
                            delay(AnimPostHideDelay)
                            onCloseAnimated()
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    onRegisterCloseAnimated?.invoke(closeAnimated)
                    panelVisible = true
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .graphicsLayer {
                            alpha = panelAlpha
                            translationY = panelShiftY
                        }
                        .onSizeChanged { size ->
                            updatePanelSize(size.width, size.height)
                        },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    PasswordGeneratorPanel(
                        onClose = closeAnimated,
                        onCopyPassword = { password ->
                            copySensitiveText(
                                context = applicationContext,
                                label = "Generated Password",
                                text = password,
                            )
                        }
                    )
                }
            }
        }
    }

    @Composable
    override fun RenderQuickAppLauncherContent(
        initialSettings: QuickAppLauncherSettings,
        quickLauncherAppLongPressLaunchPopup: Boolean,
        onCloseAnimated: () -> Unit,
        onToggleAdjust: () -> Unit,
        onLaunch: (AppInfo, Boolean) -> Boolean,
        onRegisterCloseAnimated: ((() -> Unit) -> Unit)?,
    ) {
        SideGestureTheme {
            QuickAppLauncherContent(
                initialSettings = initialSettings,
                quickLauncherAppLongPressLaunchPopup = quickLauncherAppLongPressLaunchPopup,
                requestEnableFrozenPackage = ::requestEnableFrozenPackage,
                onCloseAnimated = onCloseAnimated,
                onToggleAdjust = onToggleAdjust,
                onLaunch = onLaunch,
                onRegisterCloseAnimated = onRegisterCloseAnimated,
            )
        }
    }

    @Composable
    override fun RenderQuickAppLauncherAdjustPanel(onSettingsChanged: (QuickAppLauncherSettings) -> Unit) {
        SideGestureTheme {
            QuickAppLauncherAdjustPanel(onSettingsChanged = onSettingsChanged)
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
