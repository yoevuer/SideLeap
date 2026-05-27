package hunoia.luno.ui.component.quickapplaunch

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import hunoia.luno.freeze.FreezeFacade
import hunoia.luno.launcher.LauncherFacade
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.query.AppSearch.key
import hunoia.luno.launcher.query.AppSearch.sortApps
import hunoia.luno.launcher.query.QuickAppLauncherAppList
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.settings.model.QuickAppLauncherSettings
import hunoia.luno.ui.theme.AnimPostHideDelay
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
    private val requestEnableFrozenPackage: (String, (Boolean) -> Unit) -> Unit,
    private val onCloseAnimatedRaw: () -> Unit,
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
    var panelVisible by mutableStateOf(false)
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
                FreezeFacade.queryQuickAppLauncherApps(context)
            }
            appListState = state
            prewarmIcons()
        }
    }

    val visibleApps: List<AppInfo>
        get() = appListState.apps

    val filteredApps: List<AppInfo> by derivedStateOf {
        sortApps(visibleApps, launcherSettings, tokens)
    }

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

    private fun prewarmIcons() {
        coroutineScope.launch(Dispatchers.IO) {
            val apps = appListState.apps
            for (app in apps.take(25)) {
                if (LauncherFacade.getCachedIcon(app.packageName) == null) {
                    LauncherFacade.loadIcon(context, app.packageName)?.let {
                        LauncherFacade.cacheIcon(app.packageName, it)
                    }
                }
            }
        }
    }

    fun launchApp(app: AppInfo, isFrozen: Boolean, miniWindow: Boolean, debugPrefix: String?) {
        LauncherFacade.launchApp(
            context = context,
            coroutineScope = coroutineScope,
            app = app,
            isFrozen = isFrozen,
            miniWindow = miniWindow,
            debugPrefix = debugPrefix,
            requestEnableFrozenPackage = requestEnableFrozenPackage,
            log = { message -> android.util.Log.d("LunoLauncher", message) },
            onLaunch = onLaunch,
            onLaunched = closeAnimated
        )
    }
}
