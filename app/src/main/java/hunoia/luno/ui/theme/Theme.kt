package hunoia.luno.ui.theme

import android.app.WallpaperManager
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import hunoia.luno.ui.theme.AppTheme
import hunoia.luno.ui.component.ComposeToast

@Composable
fun SideGestureTheme(
    wallpaperChangeTrigger: Any = Any(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val freshContext = remember(wallpaperChangeTrigger, darkTheme) {
        object : ContextWrapper(context) {}
    }
    AppTheme(
        darkTheme = darkTheme,
        dynamicColor = true,
        recomposeTrigger = wallpaperChangeTrigger,
        context = freshContext,
    ) {
        content()
        ComposeToast()
    }
}
