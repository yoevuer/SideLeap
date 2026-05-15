package hunoia.sideleap.freeze

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import hunoia.sideleap.launcher.model.AppInfo

object FreezeState {

    fun isFrozen(context: Context, packageName: String): Boolean {
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

    suspend fun queryFrozenApplicationsOnIo(context: Context, showSystemApps: Boolean): List<AppInfo> {
        return queryFrozenApplications(context, showSystemApps)
    }

    fun isSystemApp(ai: ApplicationInfo?): Boolean {
        if (ai == null) return false
        val flags = ai.flags
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }
}
