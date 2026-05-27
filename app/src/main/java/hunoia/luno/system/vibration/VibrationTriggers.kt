package hunoia.luno.system.vibration

import android.Manifest.permission.VIBRATE
import androidx.annotation.RequiresPermission
import hunoia.luno.gesture.GestureButton
import hunoia.luno.settings.model.GestureSettings
import hunoia.luno.settings.model.SubGesture
import hunoia.luno.system.vibration.appContext

@RequiresPermission(VIBRATE)
fun GestureButton.tryVibrateForSlide() {
    val ctx = appContext ?: return
    if (slideVibrate) {
        vibrate(ctx, vibrationEffect, customVibrationMs)
    }
}

@RequiresPermission(VIBRATE)
fun GestureButton.tryVibrateForLongSlide() {
    val ctx = appContext ?: return
    if (longSlideVibrate) {
        vibrate(ctx, vibrationEffect, customVibrationMs)
    }
}

@RequiresPermission(VIBRATE)
fun GestureButton.tryVibrateForTap() {
    val ctx = appContext ?: return
    if (tapVibrate) {
        vibrate(ctx, vibrationEffect, customVibrationMs)
    }
}

@RequiresPermission(VIBRATE)
fun GestureButton.tryVibrateForLongPress() {
    val ctx = appContext ?: return
    if (longPressVibrate) {
        vibrate(ctx, vibrationEffect, customVibrationMs)
    }
}

@RequiresPermission(VIBRATE)
fun SubGesture.tryVibrate() {
    val ctx = appContext ?: return
    if (vibrate) {
        vibrate(ctx, vibrationEffect, customVibrationMs)
    }
}

@RequiresPermission(VIBRATE)
fun vibrateForActionPanel(gestureSettings: GestureSettings) {
    val ctx = appContext ?: return
    if (gestureSettings.actionPanelVibrate) {
        vibrate(ctx, DEFAULT_VIBRATION_EFFECT, DEFAULT_VIBRATION_MS)
    }
}

@RequiresPermission(VIBRATE)
fun vibrateForMoveScreen(gestureSettings: GestureSettings) {
    val ctx = appContext ?: return
    if (gestureSettings.moveScreenVibrate) {
        vibrate(ctx, DEFAULT_VIBRATION_EFFECT, DEFAULT_VIBRATION_MS)
    }
}
