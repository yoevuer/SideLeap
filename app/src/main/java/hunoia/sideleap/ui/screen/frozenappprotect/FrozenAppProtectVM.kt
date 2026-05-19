package hunoia.sideleap.ui.screen.frozenappprotect

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.R
import hunoia.sideleap.core.AppContext
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.settings.model.FrozenAppSettings
import hunoia.sideleap.settings.api.SettingsProvider
import hunoia.sideleap.freeze.api.FreezeAction
import hunoia.sideleap.freeze.api.FreezeState
import hunoia.sideleap.system.feedback.showComposeToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrozenAppProtectVM : BaseComposeVM<FrozenAppProtectVM.UiState, FrozenAppProtectVM.UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        viewModelScope.launch {
            SettingsProvider.frozenAppSettings.collectLatest { settings ->
                val showSystemAppsChanged = uiState.showSystemApps != settings.showSystemAppsInProtectPage
                updateUiState {
                    it.copy(
                        showSystemApps = settings.showSystemAppsInProtectPage,
                        oneKeyPackageNames = settings.oneKeyPackageNames,
                        protectedPackageNames = settings.protectedPackageNames,
                        pendingProtectedPackageNames = settings.protectedPackageNames
                    )
                }
                recompute()
                if (showSystemAppsChanged || uiState.apps.isEmpty()) {
                    reloadApps()
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        updateUiState { it.copy(query = query) }
        recompute()
    }

    fun onShowSystemAppsChange(show: Boolean) {
        viewModelScope.launch {
            SettingsProvider.updateFrozenAppSettings {
                it.copy(showSystemAppsInProtectPage = show)
            }
        }
    }

    fun onProtectedChecked(packageName: String, checked: Boolean) {
        updateUiState {
            val next = it.pendingProtectedPackageNames.toMutableSet()
            if (checked) next.add(packageName) else next.remove(packageName)
            it.copy(pendingProtectedPackageNames = next)
        }
    }

    fun commitSelections() {
        viewModelScope.launch {
            val pending = uiState.pendingProtectedPackageNames
            SettingsProvider.updateFrozenAppSettings { settings ->
                val newOneKey = settings.oneKeyPackageNames - pending
                settings.copy(
                    protectedPackageNames = pending,
                    oneKeyPackageNames = newOneKey
                )
            }
        }
    }

    fun reloadApps() {
        viewModelScope.launch {
            commitSelections()
            updateUiState { it.copy(refreshing = true) }
            val apps = withContext(Dispatchers.IO) {
                val context = AppContext.get()
                val normal = hunoia.sideleap.launcher.query.AppQuery.queryLauncherActivities(
                    context = context,
                    allowRepeatPackage = false,
                    showSystemApps = uiState.showSystemApps
                )
                val frozen = FreezeState.queryFrozenApplications(context, uiState.showSystemApps)
                val normalPackageNames = normal.map { it.packageName }.toSet()
                normal + frozen.filter { it.packageName !in normalPackageNames }
            }
            val frozenStateByPackage = withContext(Dispatchers.IO) {
                val context = AppContext.get()
                val packageNames = apps.asSequence().map { it.packageName }.distinct().toList()
                FreezeState.queryFrozenStateByPackage(context, packageNames)
            }
            val shizukuReady = FreezeAction.isShizukuReady()
            updateUiState {
                it.copy(
                    apps = apps,
                    frozenStateByPackage = frozenStateByPackage,
                    shizukuReady = shizukuReady,
                    refreshing = false
                )
            }
            recompute()
        }
    }

    fun onUnfreeze(packageName: String) {
        val isFrozen = uiState.frozenStateByPackage[packageName] == true
        if (!uiState.shizukuReady || !isFrozen || packageName in uiState.runningPackageActions) return
        viewModelScope.launch {
            updateUiState { it.copy(runningPackageActions = it.runningPackageActions + packageName) }
            val result = FreezeAction.checkAndUnfreeze(AppContext.get(), packageName)
            val nowFrozen = result.nowFrozen
            if (!nowFrozen) {
                showComposeToast(R.string.unfrozen_success)
            } else {
                showComposeToast(R.string.frozen_state_unchanged)
            }
            updateUiState {
                it.copy(
                    runningPackageActions = it.runningPackageActions - packageName,
                    frozenStateByPackage = it.frozenStateByPackage + (packageName to nowFrozen)
                )
            }
            recompute()
        }
    }

    private fun recompute() {
        val sorted = uiState.apps
            .asSequence()
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()

        val query = uiState.query.trim()
        val filtered = if (query.isBlank()) {
            sorted
        } else {
            sorted.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }

        val protectedSet = uiState.protectedPackageNames
        val protectedApps = filtered.filter { it.packageName in protectedSet }
        val unprotectedApps = filtered.filter { it.packageName !in protectedSet }

        updateUiState {
            it.copy(
                visibleApps = filtered,
                protectedAppsFirst = protectedApps + unprotectedApps,
                protectedApps = protectedApps,
                unprotectedApps = unprotectedApps,
                hasAnyAppInRange = sorted.isNotEmpty(),
                hasSearchResult = filtered.isNotEmpty()
            )
        }
    }

    @Immutable
    data class UiState(
        val apps: List<AppInfo> = emptyList(),
        val visibleApps: List<AppInfo> = emptyList(),
        val protectedAppsFirst: List<AppInfo> = emptyList(),
        val protectedApps: List<AppInfo> = emptyList(),
        val unprotectedApps: List<AppInfo> = emptyList(),
        val query: String = "",
        val showSystemApps: Boolean = false,
        val oneKeyPackageNames: Set<String> = FrozenAppSettings().oneKeyPackageNames,
        val protectedPackageNames: Set<String> = FrozenAppSettings().protectedPackageNames,
        val pendingProtectedPackageNames: Set<String> = protectedPackageNames,
        val frozenStateByPackage: Map<String, Boolean> = emptyMap(),
        val shizukuReady: Boolean = false,
        val runningPackageActions: Set<String> = emptySet(),
        val hasAnyAppInRange: Boolean = false,
        val hasSearchResult: Boolean = false,
        val refreshing: Boolean = false
    )

    sealed interface UiEvent
}
