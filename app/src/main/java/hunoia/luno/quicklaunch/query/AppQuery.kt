package hunoia.luno.quicklaunch.query

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import hunoia.luno.bridge.PackageChangeReceiver
import hunoia.luno.bridge.queryIntentActivitiesCompat
import hunoia.luno.core.AppContext
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.quicklaunch.model.LauncherInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AppQuery {

    private var launcherCache: MutableMap<Boolean, List<AppInfo>>? = null
    private var receiverRegistered = false

    private fun ensureReceiver() {
        if (receiverRegistered) return
        receiverRegistered = true
        PackageChangeReceiver.register(AppContext.get()) {
            launcherCache?.clear()
            AppContext.applicationScope?.launch {
                withContext(Dispatchers.IO) {
                    try {
                        queryLauncherActivities(AppContext.get(), false)
                    } catch (_: Exception) {
                        // PackageManager may be busy during package change processing;
                        // cache will be rebuilt on next query request
                    }
                }
            }
        }
    }

    fun invalidateLauncherCache() {
        launcherCache?.clear()
    }

    fun queryLauncherActivities(context: Context, allowRepeatPackage: Boolean = true): List<AppInfo> {
        launcherCache?.get(allowRepeatPackage)?.let { return it }
        ensureReceiver()

        val list = mutableListOf<AppInfo>()
        val pkgList = mutableListOf<String>()
        val packageManager = context.packageManager
        val intent = Intent().apply {
            setAction(Intent.ACTION_MAIN)
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val activities = packageManager.queryIntentActivitiesCompat(intent, PackageManager.MATCH_ALL)
        for (resolveInfo in activities) {
            val activityInfo = resolveInfo.activityInfo
            val packageName = activityInfo?.packageName
            if (packageName.isNullOrEmpty()) continue
            if (!allowRepeatPackage && packageName in pkgList) continue
            val item = AppInfo(
                packageName = packageName,
                className = activityInfo.name,
                label = activityInfo.loadLabel(packageManager).toString()
            )
            list.add(item)
            pkgList.add(packageName)
        }
        val cache = launcherCache ?: mutableMapOf<Boolean, List<AppInfo>>().also { launcherCache = it }
        cache[allowRepeatPackage] = list
        return list
    }

    fun queryCreateShortcutActivities(context: Context, allowRepeatPackage: Boolean = true): List<LauncherInfo> {
        val list = mutableListOf<LauncherInfo>()
        val pkgList = mutableListOf<String>()
        val packageManager = context.packageManager
        val intent = Intent().apply {
            setAction(Intent.ACTION_CREATE_SHORTCUT)
        }
        val activities = packageManager.queryIntentActivitiesCompat(intent, PackageManager.MATCH_ALL)
        for (resolveInfo in activities) {
            val activityInfo = resolveInfo.activityInfo
            val packageName = activityInfo?.packageName
            if (!activityInfo.exported) continue
            if (packageName.isNullOrEmpty()) continue
            if (!allowRepeatPackage && packageName in pkgList) continue
            val item = LauncherInfo(
                packageName = packageName,
                className = activityInfo.name,
                label = activityInfo.loadLabel(packageManager).toString()
            )
            list.add(item)
            pkgList.add(packageName)
        }
        return list
    }

    suspend fun findLauncherActivity(context: Context, packageName: String, maxRetries: Int = 5, retryDelayMs: Long = 200): AppInfo? {
        repeat(maxRetries) { attempt ->
            if (attempt > 0) delay(retryDelayMs)
            try {
                val pm = context.packageManager
                val intent = pm.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    val component = intent.component
                    if (component != null) {
                        val ai = pm.getApplicationInfo(packageName, 0)
                        val label = try { pm.getApplicationLabel(ai).toString() } catch (_: Exception) { packageName }
                        return AppInfo(packageName, component.className, label)
                    }
                }
                val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                val resolves = pm.queryIntentActivitiesCompat(mainIntent, 0)
                for (resolve in resolves) {
                    if (resolve.activityInfo.packageName == packageName) {
                        val ai = pm.getApplicationInfo(packageName, 0)
                        val label = try { pm.getApplicationLabel(ai).toString() } catch (_: Exception) { packageName }
                        return AppInfo(packageName, resolve.activityInfo.name, label)
                    }
                }
            } catch (e: CancellationException) { throw e } catch (_: Exception) {}
        }
        return null
    }

    private fun isSystemApp(applicationInfo: android.content.pm.ApplicationInfo?): Boolean {
        if (applicationInfo == null) return false
        val flags = applicationInfo.flags
        return (flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 ||
               (flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }
}