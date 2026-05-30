package hunoia.luno.freeze.api

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import hunoia.luno.core.AppContext
import hunoia.luno.quicklaunch.model.AppInfo
import hunoia.luno.bridge.PackageChangeReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FreezeState {

    private var frozenCache: List<AppInfo>? = null
    private var receiverRegistered = false
    private val frozenResultCache = mutableMapOf<String, Boolean>()

    private fun ensureReceiver() {
        if (receiverRegistered) return
        receiverRegistered = true
        PackageChangeReceiver.register(AppContext.get()) {
            frozenCache = null
            frozenResultCache.clear()
            AppContext.applicationScope?.launch {
                withContext(Dispatchers.IO) {
                    queryFrozenApplications(AppContext.get())
                }
            }
        }
    }

    fun invalidateFrozenCache() {
        frozenCache = null
        frozenResultCache.clear()
    }

    fun isFrozen(context: Context, packageName: String): Boolean {
        frozenResultCache[packageName]?.let { return it }
        val enabledSetting = runCatching {
            context.packageManager.getApplicationEnabledSetting(packageName)
        }.getOrNull() ?: return false.also { frozenResultCache[packageName] = false }
        val result = enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
            enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
            enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED
        frozenResultCache[packageName] = result
        return result
    }

    fun markFrozen(packageName: String) {
        frozenResultCache[packageName] = true
        frozenCache?.let { cache ->
            if (cache.none { it.packageName == packageName }) {
                frozenCache = cache + AppInfo(packageName, "", packageName)
            }
        }
    }

    fun markUnfrozen(packageName: String) {
        frozenResultCache[packageName] = false
        frozenCache?.let { cache ->
            frozenCache = cache.filter { it.packageName != packageName }
        }
    }

    fun markBatchFrozen(packageNames: Collection<String>) {
        packageNames.forEach { frozenResultCache[it] = true }
        frozenCache?.let { cache ->
            val existing = cache.map { it.packageName }.toSet()
            val new = packageNames.filter { it !in existing }
            if (new.isNotEmpty()) {
                frozenCache = cache + new.map { AppInfo(it, "", it) }
            }
        }
    }

    fun markBatchUnfrozen(packageNames: Collection<String>) {
        packageNames.forEach { frozenResultCache[it] = false }
        frozenCache?.let { cache ->
            frozenCache = cache.filter { it.packageName !in packageNames }
        }
    }

    fun queryFrozenStateByPackage(context: Context, packageNames: Collection<String>): Map<String, Boolean> {
        if (packageNames.isEmpty()) return emptyMap()
        val frozenSet = frozenCache?.map { it.packageName }?.toSet()
        if (frozenSet != null) {
            return packageNames.associateWith { it in frozenSet }
        }
        return packageNames.associateWith { isFrozen(context, it) }
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
            if (pkgName in pkgNames) continue

            val enabledSetting = runCatching { pm.getApplicationEnabledSetting(pkgName) }.getOrNull()
            val isDisabled = enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED ||
                enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER ||
                enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED

            val isSuspended = runCatching { pm.isPackageSuspended(pkgName) }.getOrDefault(false)

            if (!isDisabled && !isSuspended) continue
            pkgNames.add(pkgName)

            val label = try {
                app.loadLabel(pm).toString()
            } catch (e: Exception) {
                pkgName
            }

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

