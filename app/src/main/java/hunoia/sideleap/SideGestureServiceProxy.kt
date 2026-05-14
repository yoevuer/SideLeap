package hunoia.sideleap

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_POWER_DIALOG
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.PowerManager
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import hunoia.sideleap.settings.ActionSettingsDefaults.GotoBottomStrength
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.action.ActionRegistry
import hunoia.sideleap.constant.GlobalActions

import hunoia.sideleap.action.Action
import hunoia.sideleap.entity.MoveScreenData
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.action.appInfo
import hunoia.sideleap.system.audio.dispatchMediaKeyEvent
import hunoia.sideleap.ktx.gotoAppDetailSettings

import hunoia.sideleap.freeze.FreezeLaunch
import hunoia.sideleap.system.packages.queryIntentActivitiesCompat
import hunoia.sideleap.action.shortcutInfo
import hunoia.sideleap.system.audio.toggleMute
import hunoia.sideleap.system.audio.volumeDown
import hunoia.sideleap.system.audio.volumeUp
import hunoia.sideleap.ui.widget.ActionPanelState.TriggerType

import hunoia.sideleap.core.serialization.JsonHelper
import hunoia.sideleap.core.diagnostics.LauncherDiagnostics
import hunoia.sideleap.utils.ShizukuBridgeService
import hunoia.sideleap.utils.ShizukuUtils
import hunoia.sideleap.system.feedback.showToast
import hunoia.sideleap.system.feedback.showToastLong
import hunoia.sideleap.system.feedback.showVersionTooLowToast as showVersionTooLowToastUtil
import com.blankj.utilcode.util.FlashlightUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ScreenUtils
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/21
 */
class SideGestureServiceProxy(private val host: SideGestureService) {

    private companion object {
        const val KEEP_SCREEN_ON_WAKE_LOCK_TIMEOUT_MS = 2 * 60 * 1000L
        private val regexThreeVowels = Regex("[aeiou]{3}")
        private val regexThreeConsonants = Regex("[bcdfghjklmnpqrstvwxz]{3}")
    }

    private var prevPackageName: String? = null
    private var currPackageName: String? = null
    private var currActivityName: String? = null

    private val launchablePackageCache = mutableMapOf<String, Boolean>()
    private val activityExistsCache = mutableMapOf<String, Boolean>()

    private var wakeLock: PowerManager.WakeLock? = null

    private var lastRandomName: String? = null

    private val blockedNames = setOf("test", "null", "admin", "root", "system", "user")

    private val openingSyllables = arrayOf(
        "ve", "se", "sa", "si", "ke", "ka", "ki", "me", "ma", "mi", "mo",
        "ne", "na", "ni", "no", "le", "la", "li", "lo", "re", "ra", "ri",
        "ro", "te", "ta", "ti", "to", "be", "ba", "bi", "bo", "de", "da",
        "di", "do", "fe", "fa", "fi", "fo", "ze", "za", "zi", "ly", "my",
        "ny", "ry", "ae", "ei", "ia", "io", "ey", "ya", "so", "lu",
        "ve", "se", "ke", "me", "ne", "le", "re", "te"
    )

    private val middleSyllables = arrayOf(
        "so", "lo", "ro", "mo", "no", "si", "li", "ri", "mi", "ni",
        "sa", "la", "ra", "ma", "na", "ve", "le", "re", "me", "ne",
        "di", "da", "do", "ze", "za", "fa", "fe", "ta", "te", "to",
        "ka", "ki", "ke", "bi", "bo", "be", "ly", "my", "ny", "ry",
        "ae", "ei", "ia", "io"
    )

    private val endingSyllables = arrayOf(
        "ra", "ria", "la", "na", "ya", "el", "iel", "li", "ri", "ni",
        "ly", "ny", "da", "ra", "la", "na", "lia", "nia", "ria",
        "lya", "nya", "ra", "la", "ya", "da", "ra", "la", "na"
    )

    private fun generateRandomName(): String? {
        repeat(20) {
            val useThreeSyllables = Math.random() < 0.4
            val opening = pick(openingSyllables)
            val ending = pick(endingSyllables)
            if (useThreeSyllables) {
                val middle = pick(middleSyllables)
                if (opening == middle || middle == ending) return@repeat
                val name = opening + middle + ending
                if (name.length in 4..8 && passesQualityFilter(name) && name != lastRandomName) {
                    val formatted = name.replaceFirstChar { it.uppercase() }
                    if (formatted.lowercase() !in blockedNames && formatted != lastRandomName) {
                        lastRandomName = formatted
                        return formatted
                    }
                }
            } else {
                if (opening == ending) return@repeat
                val name = opening + ending
                if (name.length in 4..8 && passesQualityFilter(name) && name != lastRandomName) {
                    val formatted = name.replaceFirstChar { it.uppercase() }
                    if (formatted.lowercase() !in blockedNames && formatted != lastRandomName) {
                        lastRandomName = formatted
                        return formatted
                    }
                }
            }
        }
        return null
    }

    private fun passesQualityFilter(name: String): Boolean {
        if (regexThreeVowels.containsMatchIn(name)) return false
        if (regexThreeConsonants.containsMatchIn(name)) return false
        return true
    }

    private fun pick(array: Array<String>): String = array[(Math.random() * array.size).toInt()]

    fun onRelease() {
        host.quickAppLauncherOverlay.close()
        safeReleaseWakeLock()
        launchablePackageCache.clear()
        activityExistsCache.clear()
    }

    fun onAccessibilityEvent(event: AccessibilityEvent?) {
        host.apply {
            when(event?.eventType){
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val packageName = event.packageName?.toString()
                    val className = event.className?.toString()

                    if (this@SideGestureServiceProxy.isActivity(packageName, className)) {
                        this@SideGestureServiceProxy.currActivityName = className
                    }
                    val prevAppExcludePkgNames = host.actionSettings?.previousApp?.packageNames ?: emptyList()
                    if (packageName !in prevAppExcludePkgNames &&
                        this@SideGestureServiceProxy.hasLaunchIntent(packageName) &&
                        this@SideGestureServiceProxy.currPackageName != packageName
                    ) {
                        this@SideGestureServiceProxy.prevPackageName = this@SideGestureServiceProxy.currPackageName
                        this@SideGestureServiceProxy.currPackageName = packageName
                        if (this@SideGestureServiceProxy.prevPackageName == null) {
                            this@SideGestureServiceProxy.prevPackageName = this@SideGestureServiceProxy.currPackageName
                        }
                    }

                }
                else -> Unit
            }
        }
    }

    fun onAction(action: Action) {
        host.coroutineScope.launch {
            ActionRegistry.execute(action, ActionHandlerContext(
                service = host,
                appContext = host,
                scope = host.coroutineScope,
                actionSettings = host.actionSettings ?: ActionSettings(),
                showToast = { showToast(it) },
                showLongToast = { showToastLong(it) },
                currentPackageName = { currPackageName },
                toggleKeepScreenOn = {
                    if (wakeLock != null) {
                        safeReleaseWakeLock()
                        showToast(R.string.disable_keep_screen_on)
                    } else {
                        val pm = host.getSystemService(Context.POWER_SERVICE) as PowerManager
                        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "gulugulu:KeepScreenOn")
                        wakeLock?.setReferenceCounted(false)
                        wakeLock?.acquire(KEEP_SCREEN_ON_WAKE_LOCK_TIMEOUT_MS)
                        showToast(R.string.enable_keep_screen_on)
                    }
                },
                showVersionTooLowToast = { resId ->
                    showVersionTooLowToastUtil(host, resId)
                },
                previousApp = {
                    previousApp()
                },
            ))
        }
    }

    private suspend fun previousApp() {
        val prevPkgName = prevPackageName
        val curPkgName = currPackageName
        if (prevPkgName.isNullOrEmpty() || curPkgName.isNullOrEmpty()) {
            return
        }
        if (currPackageNameError()) {
            queryLaunchIntentAndStart(curPkgName)
            return
        }
        if (prevPkgName == curPkgName) return
        if (queryLaunchIntentAndStart(prevPkgName)) {
            prevPackageName = curPkgName
            currPackageName = prevPkgName
        }
    }

    private suspend fun queryLaunchIntentAndStart(packageName: String?): Boolean {
        if (packageName.isNullOrEmpty()) {
            return false
        }
        val intent = withContext(Dispatchers.IO) {
            host.packageManager.getLaunchIntentForPackage(packageName)
        } ?: return false
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            host.startActivity(intent)
            true
        } catch (ignored: Exception) {
            false
        }
    }

    private fun currPackageNameError(): Boolean {
        val pkgName = host.rootInActiveWindow?.packageName?.toString()
        return pkgName != currPackageName
    }

    private fun hasLaunchIntent(packageName: String?): Boolean {
        val key = packageName ?: return false
        launchablePackageCache[key]?.let { return it }
        val result = host.packageManager.getLaunchIntentForPackage(key) != null
        launchablePackageCache[key] = result
        return result
    }

    private fun isActivity(packageName: String?, className: String?): Boolean {
        packageName ?: return false
        className ?: return false
        val key = "$packageName/$className"
        activityExistsCache[key]?.let { return it }
        return try {
            val component = ComponentName(packageName, className)
            host.packageManager.getActivityInfo(component, 0)
            cacheActivityExists(key, true)
            true
        } catch (e: Exception) {
            cacheActivityExists(key, false)
            false
        }
    }

    private fun cacheActivityExists(key: String, value: Boolean) {
        if (activityExistsCache.size > 512) {
            activityExistsCache.clear()
        }
        activityExistsCache[key] = value
    }

    private fun safeReleaseWakeLock() {
        val lock = wakeLock
        wakeLock = null
        if (lock?.isHeld == true) {
            try {
                lock.release()
            } catch (ignored: RuntimeException) {
                // Ignore defensively.
            }
        }
    }

    private fun launchAppWithFrozenSupport(appInfo: hunoia.sideleap.launcher.model.AppInfo, miniWindow: Boolean) {
        host.coroutineScope.launch {
            FreezeLaunch.launchWithAutoUnfreeze(
                context = host,
                packageName = appInfo.packageName,
                className = appInfo.className,
                miniWindow = miniWindow
            ) { _, pkg ->
                suspendEnablePackageViaBridge(pkg)
            }
        }
    }

    private fun bridgeOneKeyFreeze(context: Context): Int {
        val intent = Intent(context, ShizukuBridgeService::class.java)
        val latch = CountDownLatch(1)
        val result = AtomicInteger(-1)

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                if (binder == null) { latch.countDown(); return }
                try {
                    val messenger = Messenger(binder)
                    val replyHandler = object : Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            if (msg.what == ShizukuBridgeService.MSG_FREEZE_BATCH_RESULT) {
                                result.set(msg.data.getInt(ShizukuBridgeService.EXTRA_SUCCESS_COUNT, -1))
                                latch.countDown()
                            }
                        }
                    }
                    val replyMessenger = Messenger(replyHandler)
                    val msg = Message.obtain(null, ShizukuBridgeService.MSG_FREEZE_BATCH)
                    msg.replyTo = replyMessenger
                    messenger.send(msg)
                } catch (e: Exception) {
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }

        try {
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
            if (!latch.await(2, TimeUnit.SECONDS)) {
                result.set(-2)
            }
        } catch (e: Exception) {
            result.set(-3)
        } finally {
            try { context.unbindService(conn) } catch (_: Exception) {}
        }

        return result.get()
    }

    private suspend fun suspendEnablePackageViaBridge(packageName: String): Boolean = suspendCancellableCoroutine { cont ->
        host.requestEnableFrozenPackage(packageName) { success ->
            cont.resume(success)
        }
    }
}
