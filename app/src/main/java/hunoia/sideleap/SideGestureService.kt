package hunoia.sideleap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Rect
import android.media.AudioManager
import android.os.PowerManager
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_MEDIA_NEXT
import android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS
import android.view.KeyEvent.KEYCODE_VOLUME_DOWN
import android.view.KeyEvent.KEYCODE_VOLUME_UP
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.aaron.composeaccessibility.ComponentAccessibilityService
import hunoia.sideleap.gesture.GestureButton
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.GestureSettings
import hunoia.sideleap.settings.model.InitialSettings
import hunoia.sideleap.core.event.WallpaperChangedEvent
import hunoia.sideleap.system.audio.dispatchMediaKeyEvent
import hunoia.sideleap.system.packages.queryIntentActivitiesCompat
import hunoia.sideleap.system.window.removeWindow
import hunoia.sideleap.system.window.removeWindows
import hunoia.sideleap.system.window.setBasic
import hunoia.sideleap.system.window.updateGestureButton
import hunoia.sideleap.system.window.updateLayout
import hunoia.sideleap.system.window.updateMainView
import hunoia.sideleap.service.SideGestureServiceProxy
import hunoia.sideleap.service.SideGestureButtonRefreshCoordinator
import hunoia.sideleap.service.SideGestureOverlayLifecycle
import hunoia.sideleap.service.SideGestureRuntime
import hunoia.sideleap.service.SideGestureRuntimeState
import hunoia.sideleap.ui.event.SubscribeEvent
import hunoia.sideleap.ui.widget.GestureView
import hunoia.sideleap.gesture.input.MotionEventDispatcher
import java.lang.ref.WeakReference
import hunoia.sideleap.system.audio.volumeDown
import hunoia.sideleap.system.audio.volumeUp
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.ui.theme.SideGestureTheme
import hunoia.sideleap.ui.widget.SideGestureContainer
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.core.event.Events
import hunoia.sideleap.core.diagnostics.LauncherDiagnostics
import hunoia.sideleap.overlay.QuickAppLauncherOverlay
import hunoia.sideleap.freeze.ShizukuBridgeService
import com.blankj.utilcode.util.ScreenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Handler
import android.os.Looper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/14
 */
class SideGestureService : ComponentAccessibilityService(), SideGestureRuntime {

    companion object {
        private var currentRef: WeakReference<SideGestureService>? = null
        var current: SideGestureService?
            get() = currentRef?.get()
            private set(value) { currentRef = if (value != null) WeakReference(value) else null }
    }

    private val proxy = SideGestureServiceProxy(this)
    val quickAppLauncherOverlay by lazy { QuickAppLauncherOverlay(this) }
    internal val overlayLifecycle = SideGestureOverlayLifecycle(this)
    private val buttonRefreshCoordinator = SideGestureButtonRefreshCoordinator(
        host = this,
        scopeProvider = { coroutineScope },
        initialSettingsProvider = { SettingsProvider.getInitialSettings() },
        advancedSettingsProvider = { advancedSettings },
        buttonViewsProvider = { buttonViews },
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

    private val imeInsetObserver = ImeInsetObserver(this) { mainView }
    private var mainView: View? = null
    private var buttonViews: List<View>? = null
    private var orientation = if (ScreenUtils.isLandscape()) 2 else 1

    private var isNowInLockScreenPage = false

    private var volumeButtonSwitchSongJob: Job? = null

    val coroutineScope = MainScope()

    var initialSettings: InitialSettings? = null
        private set
    var advancedSettings: AdvancedSettings? = null
        private set
    var gestureSettings: GestureSettings? = null
        private set
    var actionSettings: ActionSettings? = null
        private set

    private val wallpaperChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Events.post(WallpaperChangedEvent())
        }
    }
    private val screenLockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                isNowInLockScreenPage = true
                overlayLifecycle.onScreenLocked()
            } else if (intent?.action == Intent.ACTION_USER_PRESENT) {
                isNowInLockScreenPage = false
            }
            updateGestureButtons()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (orientation != newConfig.orientation) {
            orientation = newConfig.orientation
            updateLayout()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        proxy.onAccessibilityEvent(event)
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            updateGestureButtons()
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val keyCode = event?.keyCode
        if (advancedSettings?.volumeButtonSwitchSong == true &&
            audioManager.isMusicActive &&
            powerManager.isInteractive.not() &&
            (keyCode == KEYCODE_VOLUME_UP || keyCode == KEYCODE_VOLUME_DOWN)
        ) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    volumeButtonSwitchSongJob = coroutineScope.launch {
                        delay(ViewConfiguration.getLongPressTimeout().toLong())
                        when (keyCode) {
                            KEYCODE_VOLUME_UP -> dispatchMediaKeyEvent(KEYCODE_MEDIA_PREVIOUS)
                            KEYCODE_VOLUME_DOWN -> dispatchMediaKeyEvent(KEYCODE_MEDIA_NEXT)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val isCompleted = volumeButtonSwitchSongJob?.isCompleted == true
                    volumeButtonSwitchSongJob?.cancel()
                    volumeButtonSwitchSongJob = null
                    if (!isCompleted) {
                        when (keyCode) {
                            KEYCODE_VOLUME_UP -> volumeUp()
                            KEYCODE_VOLUME_DOWN -> volumeDown()
                        }
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    volumeButtonSwitchSongJob?.cancel()
                    volumeButtonSwitchSongJob = null
                }
            }
            return true
        }
        return super.onKeyEvent(event)
    }

    override fun onInterrupt() {
    }

    override fun onDestroy() {
        super.onDestroy()
        if (current === this) currentRef = null
        overlayLifecycle.onDestroy()
        coroutineScope.cancel()
        proxy.onRelease()
        unregisterReceiver(screenLockReceiver)
        unregisterReceiver(wallpaperChangedReceiver)
        imeInsetObserver.unregister()
    }

    override fun onSetOverlay() {
        current = this
        registerRuntimeObservers()

        val mainView = mainView
        if (mainView != null) {
            removeWindow(mainView)
        }
        this.mainView = attachComposeOverlay { renderMainOverlay() }
        observeSettings()
    }

    private fun registerRuntimeObservers() {
        registerScreenLockReceiver()
        registerWallpaperChangedReceiver()
        registerImeInsetObserver()
    }

    @Composable
    private fun renderMainOverlay() {
        var key by remember { mutableStateOf(Any()) }
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
                        actionSettings = actionSettings,
                        advancedSettings = advancedSettings,
                        gestureSettings = gestureSettings
                    )
                }
            }
        }
    }

    private fun observeSettings() {
        coroutineScope.launch(Dispatchers.Main.immediate) {
            observeLatestSettings()
            observeGestureButtonChanges()
            observeGestureVisibilityChanges()
            observeTemporaryHideChanges()
        }
    }

    private suspend fun kotlinx.coroutines.CoroutineScope.observeLatestSettings() {
        launch {
            SettingsProvider
                .initialSettings
                .collectLatest {
                    initialSettings = it
                }
        }
        launch {
            SettingsProvider
                .advancedSettings
                .collectLatest {
                    advancedSettings = it
                }
        }
        launch {
            SettingsProvider
                .gestureSettings
                .collectLatest {
                    gestureSettings = it
                }
        }
        launch {
            SettingsProvider
                .actionSettings
                .collectLatest {
                    actionSettings = it
                }
        }
    }

    private suspend fun kotlinx.coroutines.CoroutineScope.observeGestureButtonChanges() {
        launch {
            SettingsProvider
                .sideGestureButtons
                .combine(SettingsProvider.bottomGestureButtons) { l1, l2 ->
                    l1 + l2
                }
                .collectLatest { buttons ->
                    replaceGestureButtons(buttons)
                }
        }
    }

    private suspend fun kotlinx.coroutines.CoroutineScope.observeGestureVisibilityChanges() {
        launch {
            SettingsProvider
                .initialSettings
                .distinctUntilChangedBy {
                    it.gestureEnabled
                }
                .collectLatest {
                    updateGestureButtons()
                }
        }
    }

    private suspend fun kotlinx.coroutines.CoroutineScope.observeTemporaryHideChanges() {
        launch {
            SettingsProvider
                .advancedSettings
                .distinctUntilChangedBy {
                    it.hideTemporary
                }
                .collectLatest {
                    updateGestureButtons()
                }
        }
    }

    private fun replaceGestureButtons(buttons: Collection<GestureButton>) {
        buttonViews?.let { removeWindows(it) }
        buttonViews = attachGestureButtons(buttons)
        updateGestureButtons()
    }

    private fun registerScreenLockReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenLockReceiver, intentFilter)
    }

    private fun registerWallpaperChangedReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_WALLPAPER_CHANGED)
        }
        registerReceiver(wallpaperChangedReceiver, intentFilter)
    }

    private fun registerImeInsetObserver() {
        coroutineScope.launch {
            launch {
                imeInsetObserver.flow.collectLatest {
                    updateGestureButtons()
                }
            }
            launch {
                SettingsProvider
                    .advancedSettings
                    .distinctUntilChangedBy {
                        it.fitSoftKeyboard
                    }
                    .collectLatest {
                        if (it.fitSoftKeyboard) {
                            imeInsetObserver.register()
                        } else {
                            imeInsetObserver.unregister()
                        }
                    }
            }
        }
    }

    private fun attachComposeOverlay(content: @Composable () -> Unit): ComposeView {
        val wm = ContextCompat.getSystemService(this, WindowManager::class.java)!!
        val lp = WindowManager.LayoutParams().apply {
            setBasic(false)
            updateMainView()
        }
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@SideGestureService)
            setViewTreeViewModelStoreOwner(this@SideGestureService)
            setViewTreeSavedStateRegistryOwner(this@SideGestureService)
            setContent {
                content()
            }
        }
        wm.addView(composeView, lp)
        return composeView
    }

    private fun attachGestureButtons(buttons: Collection<GestureButton>): List<View> {
        return buttons.map { button ->
            attachGestureButton(button)
        }
    }

    private fun attachGestureButton(button: GestureButton): View {
        val wm = ContextCompat.getSystemService(this, WindowManager::class.java)!!
        val lp = WindowManager.LayoutParams().apply {
            setBasic(button.enabled)
            updateGestureButton(button)
        }
        val view = GestureView(this, button).apply {
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

    private fun updateLayout() {
        val mainView = mainView
        if (mainView != null) {
            val lp = (mainView.layoutParams as WindowManager.LayoutParams).apply {
                updateMainView()
            }
            updateLayout(mainView, lp)
        }
        updateGestureButtons()
    }

    internal fun updateWindowLayout(view: View, lp: WindowManager.LayoutParams) {
        updateLayout(view, lp)
    }

    private fun updateGestureButtons() {
        buttonRefreshCoordinator.refresh()
    }

    fun getCurrentPackageName(): String {
        return rootInActiveWindow?.packageName?.toString() ?: ""
    }

    override fun nowInLauncher(): Boolean {
        val pkgName = getCurrentPackageName()
        val launcherIntent = Intent().apply {
            setAction(Intent.ACTION_MAIN)
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolves = packageManager
            .queryIntentActivitiesCompat(launcherIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .filter {
                packageManager.getLaunchIntentForPackage(it.activityInfo.packageName ?: "") == null
            }
        return resolves.any { it.activityInfo?.packageName == pkgName }
    }

    private var enablePackageInFlight: String? = null
    private val enablePackageLock = Any()

    override fun requestEnableFrozenPackage(packageName: String, onResult: (Boolean) -> Unit) {
        synchronized(enablePackageLock) {
            if (enablePackageInFlight != null) {
                LauncherDiagnostics.d(this, "enable_package: in-flight ${enablePackageInFlight}, ignoring $packageName")
                onResult(false)
                return
            }
            enablePackageInFlight = packageName
        }

        LauncherDiagnostics.d(this, "enable_package: requesting $packageName")
        val latch = CountDownLatch(1)
        var result = false
        val replyHandler = Handler(Looper.getMainLooper()) { msg ->
            result = msg.data.getBoolean(ShizukuBridgeService.EXTRA_SUCCESS, false)
            val exitCode = msg.data.getInt(ShizukuBridgeService.EXTRA_EXIT_CODE, -1)
            LauncherDiagnostics.d(this, "enable_package: result=$result exitCode=$exitCode")
            latch.countDown()
            true
        }
        val replyMessenger = Messenger(replyHandler)
        val enableLock = enablePackageLock
        val inFlight = enablePackageInFlight

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                try {
                    val messenger = Messenger(binder)
                    val msg = Message.obtain(null, ShizukuBridgeService.MSG_ENABLE_PACKAGE)
                    msg.data.putString(ShizukuBridgeService.EXTRA_PACKAGE_NAME, packageName)
                    msg.replyTo = replyMessenger
                    messenger.send(msg)
                } catch (e: Exception) {
                    LauncherDiagnostics.d(this@SideGestureService, "enable_package: send exception ${e.message}")
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {}

            override fun onBindingDied(name: ComponentName?) {
                LauncherDiagnostics.d(this@SideGestureService, "enable_package: binding died")
                latch.countDown()
            }

            override fun onNullBinding(name: ComponentName?) {
                LauncherDiagnostics.d(this@SideGestureService, "enable_package: null binding")
                latch.countDown()
            }
        }

        val intent = android.content.Intent(this, ShizukuBridgeService::class.java)
        bindService(intent, conn, android.content.Context.BIND_AUTO_CREATE)

        coroutineScope.launch(Dispatchers.IO) {
            val tEnable = System.currentTimeMillis()
            if (!latch.await(10, TimeUnit.SECONDS)) {
                LauncherDiagnostics.d(this@SideGestureService, "enable_package: timeout for $packageName")
                result = false
            }
            if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "enable_package: shizuku_done pkg=$packageName result=$result elapsed=${System.currentTimeMillis() - tEnable}ms")
            try { unbindService(conn) } catch (_: Exception) {}
            synchronized(enableLock) {
                if (enablePackageInFlight == inFlight) enablePackageInFlight = null
            }
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    private class ImeInsetObserver(
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
                    if (localRect.height() == windowRect.height()) {
                        // ime invisible
                        _flow.value = 0
                    } else {
                        // ime visible
                        _flow.value = imePadding
                    }
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

        fun unregister() {
            _flow.value = 0
            insetsView?.let {
                ViewCompat.setOnApplyWindowInsetsListener(it, null)
            }
            insetsView = null
            overlayView?.let {
                removeView(it)
            }
            overlayView = null
        }

        private fun addView(view: View, lp: WindowManager.LayoutParams) {
            val wm = ContextCompat.getSystemService(context, WindowManager::class.java)!!
            wm.addView(view, lp)
        }

        private fun removeView(view: View) {
            val wm = ContextCompat.getSystemService(context, WindowManager::class.java)!!
            try {
                wm.removeViewImmediate(view)
            } catch (ignored: Exception) {
            }
        }
    }
}
