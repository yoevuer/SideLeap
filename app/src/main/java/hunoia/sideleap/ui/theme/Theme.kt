package hunoia.sideleap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hunoia.sideleap.settings.model.AdvancedSettings
import hunoia.sideleap.settings.model.DayNightMode
import hunoia.sideleap.ui.theme.generator.AppTheme
import hunoia.sideleap.ui.component.ComposeToast
import hunoia.sideleap.settings.api.SettingsProvider

@Composable
fun SideGestureTheme(content: @Composable () -> Unit) {
    val advancedSettings by SettingsProvider
        .advancedSettings
        .collectAsStateWithLifecycle(initialValue = AdvancedSettings())
    val darkTheme = when (advancedSettings.dayNightMode) {
        DayNightMode.Auto -> isSystemInDarkTheme()
        DayNightMode.Day -> false
        DayNightMode.Night -> true
    }
    val dynamicColor = advancedSettings.dynamicColor
    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor
    ) {
        content()
        ComposeToast()
    }
}
