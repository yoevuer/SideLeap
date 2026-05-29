package hunoia.luno.config.selector
import hunoia.luno.config.ConfigProvider

import hunoia.luno.config.model.FrozenAppSettings
import hunoia.luno.config.model.QuickAppLauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class QuickAppLauncherRuntimeSettings(
    val launcherSettings: QuickAppLauncherSettings,
    val frozenAppSettings: FrozenAppSettings,
)

object QuickLauncherSettingsProvider {

    val flow: Flow<QuickAppLauncherRuntimeSettings> = combine(
        ConfigProvider.quickAppLauncherSettings,
        ConfigProvider.frozenAppSettings,
    ) { launcher, frozen ->
        QuickAppLauncherRuntimeSettings(
            launcherSettings = launcher,
            frozenAppSettings = frozen,
        )
    }
}
