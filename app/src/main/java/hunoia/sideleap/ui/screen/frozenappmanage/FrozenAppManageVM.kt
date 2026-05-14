package hunoia.sideleap.ui.screen.frozenappmanage

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.App
import hunoia.sideleap.R
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.settings.model.FrozenAppSettings
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.freeze.FreezeAction
import hunoia.sideleap.freeze.FreezeState
import hunoia.sideleap.ui.widget.showComposeToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrozenAppManageVM : BaseComposeVM<FrozenAppManageVM.UiState, FrozenAppManageVM.UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        viewModelScope.launch {
            SettingsProvider.frozenAppSettings.collectLatest { settings ->
                val showSystemAppsChanged = uiState.showSystemApps != settings.showSystemAppsInManagePage
                updateUiState {
                    it.copy(
                        showSystemApps = settings.showSystemAppsInManagePage,
                        oneKeyPackageNames = settings.oneKeyPackageNames,
                        protectedPackageNames = settings.protectedPackageNames
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
        updateUiState {
            it.copy(query = query)
        }
        recompute()
    }

    fun onShowSystemAppsChange(show: Boolean) {
        viewModelScope.launch {
            SettingsProvider.updateFrozenAppSettings {
                it.copy(showSystemAppsInManagePage = show)
            }
        }
    }

    fun onOneKeyChecked(packageName: String, checked: Boolean) {
        viewModelScope.launch {
            SettingsProvider.updateFrozenAppSettings { settings ->
                val next = settings.oneKeyPackageNames.toMutableSet()
                if (checked) {
                    next.add(packageName)
                } else {
                    next.remove(packageName)
                }
                settings.copy(oneKeyPackageNames = next)
            }
        }
    }

    fun reloadApps() {
        viewModelScope.launch {
            updateUiState { it.copy(refreshing = true) }
            val apps = withContext(Dispatchers.IO) {
                val context = App.getContext()
                val normal = hunoia.sideleap.utils.AppInfoUtils.queryLauncherActivities(
                    context = context,
                    allowRepeatPackage = false,
                    showSystemApps = uiState.showSystemApps
                )
                val frozen = FreezeState.queryFrozenApplications(context, uiState.showSystemApps)
                val normalPackageNames = normal.map { it.packageName }.toSet()
                normal + frozen.filter { it.packageName !in normalPackageNames }
            }
            val frozenStateByPackage = withContext(Dispatchers.IO) {
                val context = App.getContext()
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

    fun onToggleFrozen(packageName: String) {
        if (!uiState.shizukuReady || packageName in uiState.runningPackageActions) return
        viewModelScope.launch {
            updateUiState { it.copy(runningPackageActions = it.runningPackageActions + packageName) }
            val wasFrozen = uiState.frozenStateByPackage[packageName] == true
            val result = if (wasFrozen) {
                FreezeAction.checkAndUnfreeze(App.getContext(), packageName)
            } else {
                FreezeAction.checkAndFreeze(App.getContext(), packageName)
            }
            val nowFrozen = result.nowFrozen
            val expectedFrozen = !wasFrozen
            if (nowFrozen == expectedFrozen) {
                showComposeToast(if (nowFrozen) R.string.frozen_success else R.string.unfrozen_success)
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

    fun onOneKeyFreezeAll() {
        if (!uiState.shizukuReady || uiState.bulkActionRunning) return
        viewModelScope.launch {
            updateUiState { it.copy(bulkActionRunning = true) }
            val result = FreezeAction.oneKeyFreeze(App.getContext())
            showComposeToast(App.getContext().getString(R.string.bulk_frozen_count, result.successCount))
            val refreshedFrozenState = withContext(Dispatchers.IO) {
                FreezeState.queryFrozenStateByPackage(
                    App.getContext(),
                    uiState.apps.asSequence().map { it.packageName }.distinct().toList()
                )
            }
            updateUiState {
                it.copy(
                    frozenStateByPackage = refreshedFrozenState,
                    bulkActionRunning = false
                )
            }
            recompute()
        }
    }

    fun onOneKeyUnfreezeAll() {
        if (!uiState.shizukuReady || uiState.bulkActionRunning) return
        viewModelScope.launch {
            val targets = currentOneKeyTargetsInRange()
            if (targets.isEmpty()) return@launch
            updateUiState { it.copy(bulkActionRunning = true) }
            val result = FreezeAction.oneKeyUnfreeze(App.getContext(), targets)
            val successCount = result.successCount
            val latestState = withContext(Dispatchers.IO) {
                FreezeState.queryFrozenStateByPackage(App.getContext(), targets)
            }
            showComposeToast(App.getContext().getString(R.string.bulk_unfrozen_count, successCount))
            updateUiState {
                it.copy(
                    frozenStateByPackage = it.frozenStateByPackage + latestState,
                    bulkActionRunning = false
                )
            }
            recompute()
        }
    }

    private fun currentOneKeyTargetsInRange(): List<String> {
        return FreezeAction.computeOneKeyTargetsInRange(
            uiState.apps, uiState.oneKeyPackageNames, uiState.protectedPackageNames
        )
    }

    private fun recompute() {
        val protectedSet = uiState.protectedPackageNames
        val inRange = uiState.apps
            .asSequence()
            .filter { it.packageName !in protectedSet }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
            .toList()

        val query = uiState.query.trim()
        val filtered = if (query.isBlank()) {
            inRange
        } else {
            inRange.filter {
                it.label.contains(query, ignoreCase = true) ||
                    it.packageName.contains(query, ignoreCase = true)
            }
        }

        val oneKeySet = uiState.oneKeyPackageNames
        val oneKeyApps = filtered.filter { it.packageName in oneKeySet }
        val otherApps = filtered.filter { it.packageName !in oneKeySet }

        val selectedCount = inRange.count { it.packageName in oneKeySet }
        val frozenState = uiState.frozenStateByPackage
        val frozenCount = inRange.count { frozenState[it.packageName] == true }
        val oneKeyTargetsInRange = inRange.count { it.packageName in oneKeySet }

        updateUiState {
            it.copy(
                visibleApps = filtered,
                oneKeyApps = oneKeyApps,
                otherApps = otherApps,
                selectedCount = selectedCount,
                frozenCount = frozenCount,
                oneKeyTargetCount = oneKeyTargetsInRange,
                hasAnyAppInRange = inRange.isNotEmpty(),
                hasSearchResult = filtered.isNotEmpty()
            )
        }
    }

    @Immutable
    data class UiState(
        val apps: List<AppInfo> = emptyList(),
        val visibleApps: List<AppInfo> = emptyList(),
        val oneKeyApps: List<AppInfo> = emptyList(),
        val otherApps: List<AppInfo> = emptyList(),
        val query: String = "",
        val showSystemApps: Boolean = false,
        val oneKeyPackageNames: Set<String> = FrozenAppSettings().oneKeyPackageNames,
        val protectedPackageNames: Set<String> = FrozenAppSettings().protectedPackageNames,
        val selectedCount: Int = 0,
        val frozenCount: Int = 0,
        val frozenStateByPackage: Map<String, Boolean> = emptyMap(),
        val shizukuReady: Boolean = false,
        val runningPackageActions: Set<String> = emptySet(),
        val bulkActionRunning: Boolean = false,
        val oneKeyTargetCount: Int = 0,
        val hasAnyAppInRange: Boolean = false,
        val hasSearchResult: Boolean = false,
        val refreshing: Boolean = false
    )

    sealed interface UiEvent
}
