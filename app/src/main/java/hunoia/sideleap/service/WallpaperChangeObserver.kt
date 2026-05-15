package hunoia.sideleap.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import hunoia.sideleap.core.event.Events
import hunoia.sideleap.core.event.WallpaperChangedEvent

@Suppress("DEPRECATION")
class WallpaperChangeObserver(private val context: Context) {
    private var registered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_WALLPAPER_CHANGED) {
                Events.post(WallpaperChangedEvent())
            }
        }
    }

    fun register() {
        if (registered) return
        context.registerReceiver(
            receiver,
            IntentFilter().apply { addAction(Intent.ACTION_WALLPAPER_CHANGED) }
        )
        registered = true
    }

    fun unregister() {
        if (!registered) return
        context.unregisterReceiver(receiver)
        registered = false
    }
}
