package hunoia.luno.service

import android.os.Build
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hunoia.luno.system.event.WallpaperChangedEvent
import hunoia.luno.settings.model.ActionSettings
import hunoia.luno.settings.model.AdvancedSettings
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.settings.model.SubGestureSettings
import hunoia.luno.settings.SettingsProvider
import hunoia.luno.ui.component.SideGestureContainer
import hunoia.luno.ui.event.SubscribeEvent
import hunoia.luno.ui.theme.SideGestureTheme
import hunoia.luno.gesture.application.VirtualMousePointerAction

@Composable
fun GestureOverlayView(
    screenshotService: hunoia.luno.SideGestureService,
    onSubGestureModeChanged: (Boolean) -> Unit,
    onAction: (hunoia.luno.action.Action, hunoia.luno.gesture.GestureButton?) -> Unit,
    onVirtualMouseStart: () -> Boolean,
    onVirtualMouseEnd: () -> Unit,
    onVirtualMouseSettingsUpdate: (GestureSettings.VirtualMouse) -> Unit,
    virtualMousePreviousPosition: () -> Offset,
    onPointerActionAtPosition: (Int, Int, Boolean, VirtualMousePointerAction) -> Unit,
    onTakeScreenshot: suspend () -> android.graphics.Bitmap?,
    windowController: SideGestureWindowController,
) {
    var lastWallpaperChangeMs by remember { mutableStateOf(0L) }
    SubscribeEvent(eventClass = WallpaperChangedEvent::class) {
        val now = System.currentTimeMillis()
        if (now - lastWallpaperChangeMs < 500L) return@SubscribeEvent
        lastWallpaperChangeMs = now
    }
    val advancedSettingsForTheme by SettingsProvider
        .advancedSettings
        .collectAsStateWithLifecycle(initialValue = AdvancedSettings())
    val themeKey = remember(lastWallpaperChangeMs, advancedSettingsForTheme.animationStyles.json) {
        lastWallpaperChangeMs.toString() + "_" + advancedSettingsForTheme.animationStyles.json
    }
    SideGestureTheme(wallpaperChangeTrigger = themeKey) {
        Box(modifier = Modifier.fillMaxSize()) {
            val sideButtons by SettingsProvider
                .sideGestureButtons
                .collectAsStateWithLifecycle(initialValue = emptyList())
            val bottomButtons by SettingsProvider
                .bottomGestureButtons
                .collectAsStateWithLifecycle(initialValue = emptyList())
            val advancedSettings by SettingsProvider
                .advancedSettings
                .collectAsStateWithLifecycle(initialValue = AdvancedSettings())
            val gestureSettings by SettingsProvider
                .gestureSettings
                .collectAsStateWithLifecycle(initialValue = GestureSettings())
            val actionSettings by SettingsProvider
                .actionSettings
                .collectAsStateWithLifecycle(initialValue = ActionSettings())
            val subGestureSettings by SettingsProvider
                .subGestureSettings
                .collectAsStateWithLifecycle(initialValue = SubGestureSettings())
            SideGestureContainer(
                modifier = Modifier.matchParentSize(),
                buttons = sideButtons + bottomButtons,
                wallpaperChangeTrigger = lastWallpaperChangeMs,
                onSubGestureModeChanged = onSubGestureModeChanged,
                animationStyle = when (advancedSettings.animationStyles.isAnimationEnabled) {
                    true -> advancedSettings.animationStyles.value
                    else -> null
                },
                onAction = onAction,
                onVirtualMouseStart = onVirtualMouseStart,
                onVirtualMouseEnd = onVirtualMouseEnd,
                onVirtualMouseSettingsUpdate = onVirtualMouseSettingsUpdate,
                virtualMousePreviousPosition = virtualMousePreviousPosition,
                onPointerActionAtPosition = onPointerActionAtPosition,
                onTakeScreenshot = onTakeScreenshot,
                actionSettings = actionSettings,
                advancedSettings = advancedSettings,
                gestureSettings = gestureSettings,
                subGestureSettings = subGestureSettings,
            )
        }
    }
}
