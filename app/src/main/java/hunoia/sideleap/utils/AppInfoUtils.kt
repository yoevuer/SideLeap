package hunoia.sideleap.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.entity.LauncherInfo
import hunoia.sideleap.ktx.queryIntentActivitiesCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

internal fun queryFrozenApplicationsOnIo(context: Context, showSystemApps: Boolean): List<AppInfo> {
    return AppInfoUtils.queryFrozenApplications(context, showSystemApps)
}

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/2
 */
object AppInfoUtils {

    fun isFrozenDisabledUser(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        val enabledSetting = runCatching { pm.getApplicationEnabledSetting(packageName) }.getOrNull()
        return enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
    }

    fun queryFrozenStateByPackage(context: Context, packageNames: Collection<String>): Map<String, Boolean> {
        if (packageNames.isEmpty()) return emptyMap()
        val pm = context.packageManager
        val result = LinkedHashMap<String, Boolean>(packageNames.size)
        packageNames.forEach { packageName ->
            val enabledSetting = runCatching { pm.getApplicationEnabledSetting(packageName) }.getOrNull()
            result[packageName] = enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
        }
        return result
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

    fun queryLauncherActivities(context: Context, allowRepeatPackage: Boolean = true, showSystemApps: Boolean = true): List<AppInfo> {
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
            if (!showSystemApps && isSystemApp(activityInfo.applicationInfo)) continue
            if (!allowRepeatPackage && packageName in pkgList) continue
            val item = AppInfo(
                packageName = packageName,
                className = activityInfo.name,
                label = activityInfo.loadLabel(packageManager).toString()
            )
            list.add(item)
            pkgList.add(packageName)
        }
        return list
    }

    private fun isSystemApp(applicationInfo: ApplicationInfo?): Boolean {
        if (applicationInfo == null) return false
        val flags = applicationInfo.flags
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0 || (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }

    fun queryFrozenApplications(context: Context, showSystemApps: Boolean = true): List<AppInfo> {
        val pm = context.packageManager
        val allApps = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(0)
            }
        } catch (e: Exception) {
            return emptyList()
        }

        val result = mutableListOf<AppInfo>()
        val pkgNames = mutableSetOf<String>()
        for (app in allApps) {
            val pkgName = app.packageName
            if (pkgName.isBlank()) continue
            if (!showSystemApps && isSystemApp(app)) continue

            val enabledSetting = try {
                pm.getApplicationEnabledSetting(pkgName)
            } catch (e: Exception) {
                continue
            }

            val suspended = if (Build.VERSION.SDK_INT >= 24) {
                runCatching { pm.isPackageSuspended(pkgName) }.getOrDefault(false)
            } else {
                false
            }

            if (enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED &&
                enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER &&
                enabledSetting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED &&
                app.enabled &&
                !suspended
            ) continue

            val label = try {
                app.loadLabel(pm).toString()
            } catch (e: Exception) {
                pkgName
            }

            if (pkgName in pkgNames) continue
            pkgNames.add(pkgName)

            result.add(AppInfo(
                packageName = pkgName,
                className = "",
                label = label
            ))
        }
        return result
    }

    fun inspectDisabledAppsByPackageManager(context: Context) {
        val pm = context.packageManager
        val allApps = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(0)
            }
        } catch (e: Exception) {
            LauncherDiagnostics.d(context, "pm_frozen_probe: getInstalledApplications failed: ${e::class.simpleName} ${e.message}")
            return
        }

        var totalDisabled = 0
        var totalDisabledUser = 0
        var totalDisabledUntilUsed = 0
        var totalSuspended = 0
        var totalLabelOk = 0
        var totalIconOk = 0

        for (app in allApps) {
            val pkgName = app.packageName
            if (pkgName.isBlank()) continue

            val enabledSetting = try {
                pm.getApplicationEnabledSetting(pkgName)
            } catch (e: Exception) {
                continue
            }

            val appEnabled = app.enabled
            val suspended = if (Build.VERSION.SDK_INT >= 24) {
                runCatching { pm.isPackageSuspended(pkgName) }.getOrDefault(false)
            } else {
                false
            }

            val label = try {
                app.loadLabel(pm).toString()
            } catch (e: Exception) {
                ""
            }
            val labelOk = label.isNotBlank()
            val iconOk = runCatching { pm.getApplicationIcon(pkgName) }.isSuccess

            val isSystem = isSystemApp(app)

            when (enabledSetting) {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> totalDisabled++
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> totalDisabledUser++
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> totalDisabledUntilUsed++
            }
            if (suspended) totalSuspended++
            if (labelOk) totalLabelOk++
            if (iconOk) totalIconOk++

            if (enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
                enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED ||
                !appEnabled ||
                suspended
            ) {
                val settingStr = when (enabledSetting) {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> "disabled"
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> "disabled_user"
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> "disabled_until_used"
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> "enabled"
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> "default"
                    else -> "unknown($enabledSetting)"
                }
                LauncherDiagnostics.d(context,
                    "pm_frozen_probe: pkg=$pkgName label=$label " +
                    "enabledSetting=$settingStr appEnabled=$appEnabled " +
                    "suspended=$suspended labelOk=$labelOk iconOk=$iconOk " +
                    "system=$isSystem"
                )
            }
        }

        LauncherDiagnostics.d(context,
            "pm_frozen_probe: totalInstalled=${allApps.size} " +
            "disabled=$totalDisabled disabledUser=$totalDisabledUser " +
            "disabledUntilUsed=$totalDisabledUntilUsed suspended=$totalSuspended " +
            "labelOk=$totalLabelOk iconOk=$totalIconOk"
        )
    }

    suspend fun findLauncherActivity(context: Context, packageName: String, maxRetries: Int = 5, retryDelayMs: Long = 200): AppInfo? {
        repeat(maxRetries) { attempt ->
            if (attempt > 0) {
                delay(retryDelayMs)
            }
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
            } catch (e: CancellationException) { throw e } catch (_: Exception) {
            }
        }
        return null
    }
}
