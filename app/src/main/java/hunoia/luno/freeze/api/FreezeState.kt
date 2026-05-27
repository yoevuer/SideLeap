package hunoia.luno.freeze.api

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import hunoia.luno.App
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.system.packages.PackageChangeReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FreezeState {

    private var frozenCache: List<AppInfo>? = null
    private var receiverRegistered = false

    private fun ensureReceiver() {
        if (receiverRegistered) return
        receiverRegistered = true
        PackageChangeReceiver.register(App.getContext()) {
            frozenCache = null
            App.applicationScope.launch {
                withContext(Dispatchers.IO) {
                    queryFrozenApplications(App.getContext())
                }
            }
        }
    }

    fun invalidateFrozenCache() {
        frozenCache = null
    }

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

    fun queryFrozenApplications(context: Context): List<AppInfo> {
        frozenCache?.let { return it }
        ensureReceiver()

        val pm = context.packageManager
        val allApps = try {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } catch (e: Exception) {
            return emptyList()
        }

        val result = mutableListOf<AppInfo>()
        val pkgNames = mutableSetOf<String>()
        for (app in allApps) {
            val pkgName = app.packageName
            if (pkgName.isBlank()) continue

            val enabledSetting = try {
                pm.getApplicationEnabledSetting(pkgName)
            } catch (e: Exception) {
                continue
            }

            val suspended = runCatching { pm.isPackageSuspended(pkgName) }.getOrDefault(false)

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
        frozenCache = result
        return result
    }

    suspend fun queryFrozenApplicationsOnIo(context: Context): List<AppInfo> {
        return queryFrozenApplications(context)
    }

    fun isSystemApp(ai: ApplicationInfo?): Boolean {
        if (ai == null) return false
        val flags = ai.flags
        return (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
            (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
    }
}

