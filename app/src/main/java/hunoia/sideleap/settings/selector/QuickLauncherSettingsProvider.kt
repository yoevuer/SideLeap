package hunoia.sideleap.settings.selector
import hunoia.sideleap.settings.SettingsProvider

import hunoia.sideleap.settings.model.FrozenAppSettings
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class QuickAppLauncherRuntimeSettings(
    val launcherSettings: QuickAppLauncherSettings,
    val frozenAppSettings: FrozenAppSettings,
)

object QuickLauncherSettingsProvider {

    val flow: Flow<QuickAppLauncherRuntimeSettings> = combine(
        SettingsProvider.quickAppLauncherSettings,
        SettingsProvider.frozenAppSettings,
    ) { launcher, frozen ->
        QuickAppLauncherRuntimeSettings(
            launcherSettings = launcher,
            frozenAppSettings = frozen,
        )
    }
}
