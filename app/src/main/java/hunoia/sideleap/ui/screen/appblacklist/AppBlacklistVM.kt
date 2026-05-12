package hunoia.sideleap.ui.screen.appblacklist

import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.App
import hunoia.sideleap.R
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.ui.screen.appblacklist.AppBlacklistVM.UiEvent
import hunoia.sideleap.ui.screen.appblacklist.AppBlacklistVM.UiState
import hunoia.sideleap.utils.AppInfoUtils
import hunoia.sideleap.utils.DataStoreHolder
import hunoia.sideleap.utils.queryFrozenApplicationsOnIo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/1
 */
class AppBlacklistVM : BaseComposeVM<UiState, UiEvent>() {

    override val initialState: UiState = UiState()

    init {
        loadData()
    }

    fun showResetWarningDialog(show: Boolean) {
        updateUiState {
            it.copy(showResetWarningDialog = show)
        }
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
    }

    fun done() {
        viewModelScope.launch {
            DataStoreHolder.advancedSettings.updateData {
                it.copy(excludeApps = uiState.excludeApps)
            }
        }.invokeOnCompletion {
            if (it == null) {
                toast(R.string.save_success)
                finish()
            } else {
                toast(R.string.save_failure)
            }
        }
    }

    fun reset() {
        viewModelScope.launch {
            DataStoreHolder.advancedSettings.updateData {
                it.copy(excludeApps = emptyList())
            }
        }.invokeOnCompletion {
            if (it == null) {
                toast(R.string.reset_success)
            } else {
                toast(R.string.reset_failure)
            }
        }
    }

    fun updateAppInfos() {
        viewModelScope.launchWithLoading {
            val appInfos = withContext(Dispatchers.IO) {
                AppInfoUtils
                    .queryLauncherActivities(App.getContext())
                    .filter {
                        it.packageName != App.getContext().packageName
                    }
            }
            val frozenApps = withContext(Dispatchers.IO) {
                queryFrozenApplicationsOnIo(App.getContext(), true)
            }
            val normalPackageNames = appInfos.map { it.packageName }.toSet()
            val filteredFrozenApps = frozenApps.filter {
                it.packageName !in normalPackageNames && it.packageName != App.getContext().packageName
            }
            val mergedApps = mutableListOf<AppInfo>()
            mergedApps.addAll(appInfos)
            mergedApps.addAll(filteredFrozenApps)
            arrangeAppInfos(mergedApps)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            DataStoreHolder
                .advancedSettings
                .data
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
        val showResetWarningDialog: Boolean = false
    )

    sealed interface UiEvent
}
