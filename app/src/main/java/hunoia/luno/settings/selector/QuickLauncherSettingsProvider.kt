package hunoia.luno.settings.selector
import hunoia.luno.settings.SettingsProvider

import hunoia.luno.settings.model.FrozenAppSettings
import hunoia.luno.settings.model.QuickAppLauncherSettings
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
