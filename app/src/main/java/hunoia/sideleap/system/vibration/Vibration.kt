package hunoia.sideleap.system.vibration

import android.Manifest.permission.VIBRATE
import androidx.annotation.RequiresPermission
import hunoia.sideleap.App
import hunoia.sideleap.entity.Vibrations

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForSlide() {
    if (slideEnabled) {
        vibrate(App.getContext(), this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForLongSlide() {
    if (longSlideEnabled) {
        vibrate(App.getContext(), this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForActionPanel() {
    if (actionPanelEnabled) {
        vibrate(App.getContext(), this)
    }
}

@RequiresPermission(VIBRATE)
fun Vibrations.tryVibrateForMoveScreen() {
    if (moveScreenEnabled) {
        vibrate(App.getContext(), this)
    }
}