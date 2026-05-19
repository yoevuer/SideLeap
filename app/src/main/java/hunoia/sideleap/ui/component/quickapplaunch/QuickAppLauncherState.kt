package hunoia.sideleap.ui.component.quickapplaunch

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hunoia.sideleap.freeze.FrozenQuickAppLauncherQuery
import hunoia.sideleap.launcher.launch.QuickAppLaunch
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.launcher.query.AppSearch.key
import hunoia.sideleap.launcher.query.AppSearch.sortApps
import hunoia.sideleap.launcher.query.QuickAppLauncherAppList
import hunoia.sideleap.settings.api.SettingsProvider
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import hunoia.sideleap.ui.theme.AnimPostHideDelay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickAppLauncherState(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    initialSettings: QuickAppLauncherSettings,
    private val quickLauncherAppLongPressLaunchPopup: Boolean,
    private val requestEnableFrozenPackage: (String, (Boolean) -> Unit) -> Unit,
    private val onCloseAnimatedRaw: () -> Unit,
    private val onToggleAdjust: () -> Unit,
    private val onLaunch: (AppInfo, Boolean) -> Boolean,
    private val onRegisterCloseAnimated: ((() -> Unit) -> Unit)?,
) {
    var appListState by mutableStateOf(QuickAppLauncherAppList(emptyList(), emptySet()))
        private set
    var tokens by mutableStateOf(emptyList<String>())
        private set
    var launcherSettings by mutableStateOf(initialSettings)
        private set
    var keyboardExpanded by mutableStateOf(true)
    var panelVisible by mutableStateOf(true)
    var closing by mutableStateOf(false)

    val closeAnimated: () -> Unit = {
        if (!closing) {
            closing = true
            panelVisible = false
            coroutineScope.launch {
                delay(AnimPostHideDelay)
                onCloseAnimatedRaw()
            }
        }
    }

    init {
        coroutineScope.launch {
            SettingsProvider.quickAppLauncherSettings.collectLatest { launcherSettings = it }
        }
        loadApps()
        onRegisterCloseAnimated?.invoke(closeAnimated)
    }

    private fun loadApps() {
        coroutineScope.launch {
            val state = withContext(Dispatchers.IO) {
                FrozenQuickAppLauncherQuery.queryApps(context, launcherSettings.showSystemApps)
            }
            appListState = state
        }
    }

    val visibleApps: List<AppInfo>
        get() {
            val hiddenApps = launcherSettings.hiddenApps
            return appListState.apps.filter { app ->
                if (hiddenApps.contains(app.key())) {
                    false
                } else if (app.className.isEmpty()) {
                    hiddenApps.none { it.startsWith("${app.packageName}/") }
                } else {
                    true
                }
            }
        }

    val filteredApps: List<AppInfo>
        get() = sortApps(context, visibleApps, launcherSettings, tokens)

    fun addToken(token: String) {
        tokens = tokens + token
    }

    fun deleteToken() {
        tokens = tokens.dropLast(1)
    }

    fun clearTokens() {
        tokens = emptyList()
    }

    fun toggleKeyboard() {
        keyboardExpanded = !keyboardExpanded
    }

    fun expandKeyboard() {
        keyboardExpanded = true
    }

    fun launchApp(app: AppInfo, isFrozen: Boolean, miniWindow: Boolean, debugPrefix: String?) {
        QuickAppLaunch.launch(
            context = context,
            coroutineScope = coroutineScope,
            app = app,
            isFrozen = isFrozen,
            miniWindow = miniWindow,
            debugPrefix = debugPrefix,
            requestEnableFrozenPackage = requestEnableFrozenPackage,
            log = { message -> android.util.Log.d("SideLeapLauncher", message) },
            onLaunch = onLaunch,
            onLaunched = closeAnimated
        )
    }
}
