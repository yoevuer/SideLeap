package hunoia.luno.ui.theme

import android.app.WallpaperManager
import android.content.ContextWrapper
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hunoia.luno.settings.model.AdvancedSettings
import hunoia.luno.settings.model.DayNightMode
import hunoia.luno.ui.theme.generator.AppTheme
import hunoia.luno.ui.component.ComposeToast
import hunoia.luno.settings.SettingsProvider

@Composable
fun SideGestureTheme(
    wallpaperChangeTrigger: Any = Any(),
    content: @Composable () -> Unit
) {
    val advancedSettings by SettingsProvider
        .advancedSettings
        .collectAsStateWithLifecycle(initialValue = AdvancedSettings())
    val darkTheme = when (advancedSettings.dayNightMode) {
        DayNightMode.Auto -> isSystemInDarkTheme()
        DayNightMode.Day -> false
        DayNightMode.Night -> true
    }
    val dynamicColor = advancedSettings.dynamicColor
    val context = LocalContext.current
    val freshContext = remember(wallpaperChangeTrigger, darkTheme) {
        object : ContextWrapper(context) {}
    }
    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        recomposeTrigger = wallpaperChangeTrigger,
        context = if (dynamicColor) freshContext else null,
    ) {
        content()
        ComposeToast()
    }
}
