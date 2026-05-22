package hunoia.sideleap.system.vibration

import android.Manifest.permission.VIBRATE
import androidx.annotation.RequiresPermission
import hunoia.sideleap.system.vibration.Vibrations
import hunoia.sideleap.system.vibration.appContext
import hunoia.sideleap.system.vibration.vibrate

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForSlide() {
    val ctx = appContext ?: return
    if (slideEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForLongSlide() {
    val ctx = appContext ?: return
    if (longSlideEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForActionPanel() {
    val ctx = appContext ?: return
    if (actionPanelEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForMoveScreen() {
    val ctx = appContext ?: return
    if (moveScreenEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForTap() {
    val ctx = appContext ?: return
    if (tapEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForLongPress() {
    val ctx = appContext ?: return
    if (longPressEnabled) {
        vibrate(ctx, this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForSubGesture() {
    val ctx = appContext ?: return
    if (subGestureEnabled) {
        vibrate(ctx, this)
    }
}
