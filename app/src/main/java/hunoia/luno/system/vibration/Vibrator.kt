package hunoia.luno.system.vibration

import android.Manifest.permission.VIBRATE
import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.annotation.RequiresPermission

@RequiresPermission(VIBRATE)
fun vibrate(context: Context, vibrations: Vibrations) {
    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
    val vibrator = vibratorManager.defaultVibrator
    if (vibrations.predefinedEffect != VibrationEffects.None) {
        val effect = when (vibrations.predefinedEffect) {
            VibrationEffects.Tick -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
            VibrationEffects.Click -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
            VibrationEffects.HeavyClick -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
            VibrationEffects.None -> error("Stub!")
        }
        vibrator.vibrate(effect)
    } else if (vibrations.customVibrationMs > 0) {
        val effect = VibrationEffect.createOneShot(vibrations.customVibrationMs, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }
}
