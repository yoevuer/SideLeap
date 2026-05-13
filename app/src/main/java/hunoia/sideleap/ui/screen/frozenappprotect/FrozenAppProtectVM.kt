package hunoia.sideleap.ui.screen.frozenappprotect

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.App
import hunoia.sideleap.R
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.entity.global.FrozenAppSettings
import hunoia.sideleap.utils.AppInfoUtils
import hunoia.sideleap.utils.DataStoreHolder
import hunoia.sideleap.utils.ShizukuUtils
import hunoia.sideleap.utils.queryFrozenApplicationsOnIo
import hunoia.sideleap.ui.widget.showComposeToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrozenAppProtectVM : BaseComposeVM<FrozenAppProtectVM.UiState, FrozenAppProtectVM.UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        viewModelScope.launch {
            DataStoreHolder.frozenAppSettings.data.collectLatest { settings ->
                val showSystemAppsChanged = uiState.showSystemApps != settings.showSystemAppsInProtectPage
                updateUiState {
                    it.copy(
                        showSystemApps = settings.showSystemAppsInProtectPage,
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
        updateUiState { it.copy(query = query) }
        recompute()
    }

    fun onShowSystemAppsChange(show: Boolean) {
        viewModelScope.launch {
            DataStoreHolder.frozenAppSettings.updateData {
                it.copy(showSystemAppsInProtectPage = show)
            }
        }
    }

    fun onProtectedChecked(packageName: String, checked: Boolean) {
        viewModelScope.launch {
            DataStoreHolder.frozenAppSettings.updateData { settings ->
                val protected = settings.protectedPackageNames.toMutableSet()
                val oneKey = settings.oneKeyPackageNames.toMutableSet()
                if (checked) {
                    protected.add(packageName)
                    oneKey.remove(packageName)
                } else {
                    protected.remove(packageName)
                }
                settings.copy(
                    protectedPackageNames = protected,
                    oneKeyPackageNames = oneKey
                )
            }
        }
    }

    fun reloadApps() {
        viewModelScope.launch {
            updateUiState { it.copy(refreshing = true) }
            val apps = withContext(Dispatchers.IO) {
                val context = App.getContext()
                val normal = AppInfoUtils.queryLauncherActivities(
                    context = context,
                    allowRepeatPackage = false,
                    showSystemApps = uiState.showSystemApps
                )
                val frozen = queryFrozenApplicationsOnIo(context, uiState.showSystemApps)
                val normalPackageNames = normal.map { it.packageName }.toSet()
                normal + frozen.filter { it.packageName !in normalPackageNames }
            }
            val frozenStateByPackage = withContext(Dispatchers.IO) {
                val context = App.getContext()
                val packageNames = apps.asSequence().map { it.packageName }.distinct().toList()
                AppInfoUtils.queryFrozenStateByPackage(context, packageNames)
            }
            val shizukuReady = withContext(Dispatchers.IO) {
                ShizukuUtils.isUsableForFrozenAppActions()
            }
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
            withContext(Dispatchers.IO) {
                ShizukuUtils.enablePackageForFrozenApp(App.getContext(), packageName)
            }
            delay(100)
            val nowFrozen = withContext(Dispatchers.IO) {
                AppInfoUtils.isFrozenDisabledUser(App.getContext(), packageName)
            }
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
        val protectedFirst = filtered.sortedWith(
            compareByDescending<AppInfo> { it.packageName in protectedSet }
                .thenBy { it.label.lowercase() }
        )

        updateUiState {
            it.copy(
                visibleApps = filtered,
                protectedAppsFirst = protectedFirst,
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
        val query: String = "",
        val showSystemApps: Boolean = false,
        val oneKeyPackageNames: Set<String> = FrozenAppSettings().oneKeyPackageNames,
        val protectedPackageNames: Set<String> = FrozenAppSettings().protectedPackageNames,
        val frozenStateByPackage: Map<String, Boolean> = emptyMap(),
        val shizukuReady: Boolean = false,
        val runningPackageActions: Set<String> = emptySet(),
        val hasAnyAppInRange: Boolean = false,
        val hasSearchResult: Boolean = false,
        val refreshing: Boolean = false
    )

    sealed interface UiEvent
}
