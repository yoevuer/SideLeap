package hunoia.sideleap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hunoia.sideleap.entity.DayNightMode
import hunoia.sideleap.entity.global.AdvancedSettings
import hunoia.sideleap.ui.theme.generator.AppTheme
import hunoia.sideleap.ui.widget.ComposeToast
import hunoia.sideleap.utils.DataStoreHolder

@Composable
fun SideGestureTheme(content: @Composable () -> Unit) {
    val advancedSettings by DataStoreHolder
        .advancedSettings
        .data
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