package hunoia.sideleap.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.view.accessibility.AccessibilityEvent
import hunoia.sideleap.R
import hunoia.sideleap.SideGestureService
import hunoia.sideleap.action.Action
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.action.ActionRegistry
import hunoia.sideleap.settings.model.ActionSettings
import hunoia.sideleap.system.feedback.showToast
import hunoia.sideleap.system.feedback.showToastLong
import hunoia.sideleap.system.feedback.showVersionTooLowToast as showVersionTooLowToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class SideGestureServiceProxyActionCoordinator(
    private val host: SideGestureService,
    private val scopeProvider: () -> CoroutineScope,
) {
    private companion object {
        const val KEEP_SCREEN_ON_WAKE_LOCK_TIMEOUT_MS = 2 * 60 * 1000L
        private val regexThreeVowels = Regex("[aeiou]{3}")
        private val regexThreeConsonants = Regex("[bcdfghjklmnpqrstvwxz]{3}")
    }

    private var prevPackageName: String? = null
    private var currPackageName: String? = null
    private val launchablePackageCache = mutableMapOf<String, Boolean>()
    private val activityExistsCache = mutableMapOf<String, Boolean>()

    private var wakeLock: PowerManager.WakeLock? = null

    fun onRelease() {
        safeReleaseWakeLock()
        launchablePackageCache.clear()
        activityExistsCache.clear()
    }

    fun onAccessibilityEvent(event: AccessibilityEvent?) {
        when (event?.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val packageName = event.packageName?.toString()
                val className = event.className?.toString()

                isActivity(packageName, className)
                val prevAppExcludePkgNames = host.actionSettings?.previousApp?.packageNames ?: emptyList()
                if (packageName !in prevAppExcludePkgNames &&
                    hasLaunchIntent(packageName) &&
                    currPackageName != packageName
                ) {
                    prevPackageName = currPackageName
                    currPackageName = packageName
                    if (prevPackageName == null) {
                        prevPackageName = currPackageName
                    }
                }
            }
            else -> Unit
        }
    }

    fun onAction(action: Action) {
        val scope = scopeProvider()
        scope.launch {
            ActionRegistry.execute(action, buildActionHandlerContext())
        }
    }

    private fun buildActionHandlerContext(): ActionHandlerContext {
        return ActionHandlerContext(
            service = host,
            runtime = host,
            appContext = host.applicationContext,
            scope = scopeProvider(),
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
        )
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

}
