package hunoia.sideleap.ui.screen.quickapplaunchermanage

import androidx.lifecycle.viewModelScope
import com.aaron.compose.base.BaseComposeVM
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.freeze.QuickAppLauncherQuery
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
        val (settings, mergedApps) = withContext(Dispatchers.IO) {
            val s = SettingsProvider.getQuickAppLauncherSettings()
            val appList = QuickAppLauncherQuery.queryApps(context, s.showSystemApps)
            s to appList.apps
        }
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
