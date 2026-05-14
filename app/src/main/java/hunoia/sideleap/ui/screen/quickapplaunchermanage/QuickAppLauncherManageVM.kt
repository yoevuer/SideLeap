package hunoia.sideleap.ui.screen.quickapplaunchermanage

import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.entity.QuickAppLauncherSettings
import hunoia.sideleap.utils.AppInfoUtils
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.freeze.FreezeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickAppLauncherManageVM : BaseComposeVM<QuickAppLauncherManageVM.UiState, QuickAppLauncherManageVM.UiEvent>() {
    override val initialState = UiState()
    init { load() }
    fun load() = viewModelScope.launch {
        val context = hunoia.sideleap.App.getContext()
        val (settings, normalApps, frozenApps) = withContext(Dispatchers.IO) {
            val s = SettingsProvider.getQuickAppLauncherSettings()
            val normal = AppInfoUtils.queryLauncherActivities(context, false, s.showSystemApps)
            val frozen = FreezeState.queryFrozenApplications(context, s.showSystemApps)
            Triple(s, normal, frozen)
        }
        val normalPackageNames = normalApps.map { it.packageName }.toSet()
        val filteredFrozenApps = frozenApps.filter { it.packageName !in normalPackageNames }
        val mergedApps = mutableListOf<AppInfo>()
        mergedApps.addAll(normalApps)
        mergedApps.addAll(filteredFrozenApps)
        updateUiState { it.copy(apps = mergedApps, settings = settings) }
    }
    fun setHidden(app: AppInfo, hide: Boolean) = viewModelScope.launch {
        SettingsProvider.updateQuickAppLauncherSettings {
            it.copy(hiddenApps = if (hide) it.hiddenApps + app.key() else it.hiddenApps - app.key())
        }
        load()
    }

    fun clearStats() = viewModelScope.launch {
        SettingsProvider.updateQuickAppLauncherSettings { it.copy(recentLaunchTime = emptyMap(), launchCount = emptyMap()) }
        load()
    }
    private fun AppInfo.key() = "$packageName/$className"
    data class UiState(val apps: List<AppInfo> = emptyList(), val settings: QuickAppLauncherSettings = QuickAppLauncherSettings())
    sealed interface UiEvent
}
