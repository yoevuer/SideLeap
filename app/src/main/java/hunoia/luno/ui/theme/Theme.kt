package hunoia.luno.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import hunoia.luno.ui.component.ComposeToast

@Composable
fun SideGestureTheme(
    wallpaperChangeTrigger: Any = Any(),
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    AppTheme(darkTheme = darkTheme) {
        content()
        ComposeToast()
    }
}
