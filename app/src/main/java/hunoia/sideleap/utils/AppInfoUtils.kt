package hunoia.sideleap.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.query.AppQuery
import hunoia.sideleap.settings.model.FrozenAppSettings
import hunoia.sideleap.freeze.FreezeState

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
        val normal = AppQuery.queryLauncherActivities(
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
        val normal = AppQuery.queryLauncherActivities(
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
            "disabledUntilUse=$totalDisabledUntilUsed suspended=$totalSuspended " +
            "labelOk=$totalLabelOk iconOk=$totalIconOk"
        )
    }
}