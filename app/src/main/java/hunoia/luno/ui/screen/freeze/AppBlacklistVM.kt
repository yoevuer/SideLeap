package hunoia.luno.ui.screen.freeze

import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.luno.core.AppContext
import hunoia.luno.launcher.LauncherFacade
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.freeze.FreezeFacade
import hunoia.luno.ui.screen.freeze.AppBlacklistVM.UiEvent
import hunoia.luno.ui.screen.freeze.AppBlacklistVM.UiState
import hunoia.luno.settings.SettingsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AppBlacklistVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        loadData()
    }

    fun selectApp(appInfo: AppInfo, selected: Boolean) {
        updateUiState {
            val mutableList = it.excludeApps.toMutableList()
            if (selected) {
                mutableList.add(appInfo.packageName)
            } else {
                mutableList.remove(appInfo.packageName)
            }
            it.copy(excludeApps = mutableList)
        }
        save()
    }

    fun reset() {
        updateUiState {
            it.copy(excludeApps = emptyList())
        }
        save()
        reloadApps()
    }

    fun reloadApps() {
        viewModelScope.launchWithLoading {
            save()
            val appInfos = withContext(Dispatchers.IO) {
                try {
                    LauncherFacade
                        .queryApps(AppContext.get())
                        .filter { it.packageName != AppContext.get().packageName }
                } catch (_: Exception) {
                    emptyList()
                }
            }
            val frozenApps = withContext(Dispatchers.IO) {
                FreezeFacade.queryFrozenAppsOnIo(AppContext.get())
            }
            val normalPackageNames = appInfos.map { it.packageName }.toSet()
            val filteredFrozenApps = frozenApps.filter {
                it.packageName !in normalPackageNames && it.packageName != AppContext.get().packageName
            }
            val mergedApps = mutableListOf<AppInfo>()
            mergedApps.addAll(appInfos)
            mergedApps.addAll(filteredFrozenApps)
            arrangeAppInfos(mergedApps)
        }
    }

    private fun save() {
        viewModelScope.launch {
            SettingsProvider.updateAdvancedSettings {
                it.copy(excludeApps = uiState.excludeApps)
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            SettingsProvider
                .advancedSettings
                .take(1)
                .collectLatest { item ->
                    updateUiState {
                        it.copy(excludeApps = item.excludeApps)
                    }
                }
        }
    }

    private suspend fun arrangeAppInfos(appInfos: List<AppInfo>) {
        val selectedList = mutableListOf<AppInfo>()
        val unselectedList = mutableListOf<AppInfo>()
        val excludeApps = uiState.excludeApps.toMutableList()
        withContext(Dispatchers.Default) {
            excludeApps.apply {
                val packageNames = appInfos.map { app -> app.packageName }
                removeAll { packageName -> packageName !in packageNames }
            }
            appInfos.forEach { info ->
                if (info.packageName in excludeApps) {
                    selectedList.add(info)
                } else {
                    unselectedList.add(info)
                }
            }
        }
        updateUiState {
            it.copy(
                excludeApps = excludeApps,
                selectedAppInfos = selectedList,
                unselectedAppInfos = unselectedList
            )
        }
    }

    data class UiState(
        val selectedAppInfos: List<AppInfo> = emptyList(),
        val unselectedAppInfos: List<AppInfo> = emptyList(),
        val excludeApps: List<String> = emptyList(),
    )

    sealed interface UiEvent
}
