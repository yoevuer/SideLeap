package hunoia.sideleap.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.entity.LauncherInfo
import hunoia.sideleap.settings.model.FrozenAppSettings
import hunoia.sideleap.freeze.FreezeState
import hunoia.sideleap.system.packages.queryIntentActivitiesCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

internal fun queryFrozenApplicationsOnIo(context: Context, showSystemApps: Boolean): List<AppInfo> {
    return FreezeState.queryFrozenApplications(context, showSystemApps)
}

object AppInfoUtils {

    data class FrozenOneKeySnapshot(
        val oneKeyPackageCount: Int,
        val protectedPackageCount: Int,
        val inScopePackageCount: Int,
        val targets: List<String>,
        val candidates: List<String>,
        val executeBatchCalled: Boolean
    )

    fun queryOneKeyFrozenTargets(context: Context, settings: FrozenAppSettings): List<String> {
        val normal = queryLauncherActivities(
            context = context,
            allowRepeatPackage = false,
            showSystemApps = settings.showSystemAppsInManagePage
        )
        val frozen = FreezeState.queryFrozenApplications(
            context = context,
            showSystemApps = settings.showSystemAppsInManagePage
        )
        val allPackages = (normal + frozen)
            .asSequence()
            .map { it.packageName }
            .filter { it.isNotBlank() }
            .distinct()
        val oneKeySet = settings.oneKeyPackageNames
        val protectedSet = settings.protectedPackageNames
        return allPackages.filter { it in oneKeySet && it !in protectedSet }.toList()
    }

    fun snapshotOneKeyFreezeTargets(
        context: Context,
        settings: FrozenAppSettings
    ): FrozenOneKeySnapshot {
        val normal = queryLauncherActivities(
            context = context,
            allowRepeatPackage = false,
            showSystemApps = settings.showSystemAppsInManagePage
        )
        val frozen = FreezeState.queryFrozenApplications(
            context = context,
            showSystemApps = settings.showSystemAppsInManagePage
        )
        val inScope = (normal + frozen)
            .asSequence()
            .map { it.packageName }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()

        val oneKeySet = settings.oneKeyPackageNames
        val protectedSet = settings.protectedPackageNames
        val targets = inScope.filter { it in oneKeySet && it !in protectedSet }
        val frozenState = FreezeState.queryFrozenStateByPackage(context, targets)
        val candidates = targets.filter { frozenState[it] != true }

        return FrozenOneKeySnapshot(
            oneKeyPackageCount = oneKeySet.size,
            protectedPackageCount = protectedSet.size,
            inScopePackageCount = inScope.size,
            targets = targets,
            candidates = candidates,
            executeBatchCalled = candidates.isNotEmpty()
        )
    }

    fun isFrozenDisabledUser(context: Context, packageName: String): Boolean {
        return FreezeState.isFrozen(context, packageName)
    }

    fun queryFrozenStateByPackage(context: Context, packageNames: Collection<String>): Map<String, Boolean> {
        return FreezeState.queryFrozenStateByPackage(context, packageNames)
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
        return FreezeState.isSystemApp(applicationInfo)
    }

    fun queryFrozenApplications(context: Context, showSystemApps: Boolean = true): List<AppInfo> {
        return FreezeState.queryFrozenApplications(context, showSystemApps)
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

            val isSystem = FreezeState.isSystemApp(app)

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
