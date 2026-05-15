package hunoia.sideleap.service

import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewConfiguration
import hunoia.sideleap.system.audio.dispatchMediaKeyEvent
import hunoia.sideleap.system.audio.volumeDown
import hunoia.sideleap.system.audio.volumeUp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LockScreenVolumeKeyHandler(
    private val context: Context,
    private val scopeProvider: () -> CoroutineScope
) {
    private var switchSongJob: Job? = null

    fun handle(event: KeyEvent?, enabled: Boolean): Boolean {
        val keyCode = event?.keyCode ?: return false
        if (!enabled || !isSwitchSongKeyEvent(keyCode) || !canHandleOnLockScreen()) return false

        when (event.action) {
            KeyEvent.ACTION_DOWN -> scheduleSwitchSong(keyCode)
            KeyEvent.ACTION_UP -> handleVolumeFallback(keyCode)
            MotionEvent.ACTION_CANCEL -> cancelSwitchSong()
        }
        return true
    }

    fun release() {
        cancelSwitchSong()
    }

    private fun canHandleOnLockScreen(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.isMusicActive && !powerManager.isInteractive
    }

    private fun scheduleSwitchSong(keyCode: Int) {
        cancelSwitchSong()
        switchSongJob = scopeProvider().launch {
            delay(ViewConfiguration.getLongPressTimeout().toLong())
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> context.dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                KeyEvent.KEYCODE_VOLUME_DOWN -> context.dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
            }
        }
    }

    private fun handleVolumeFallback(keyCode: Int) {
        val isCompleted = switchSongJob?.isCompleted == true
        cancelSwitchSong()
        if (isCompleted) return

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> context.volumeUp()
            KeyEvent.KEYCODE_VOLUME_DOWN -> context.volumeDown()
        }
    }

    private fun cancelSwitchSong() {
        switchSongJob?.cancel()
        switchSongJob = null
    }

    private fun isSwitchSongKeyEvent(keyCode: Int): Boolean {
        return keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
    }
}
