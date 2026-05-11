package hunoia.sideleap.utils

import android.content.Context
import hunoia.sideleap.entity.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun Context.updateQuickAppLauncherStats(app: AppInfo) {
    CoroutineScope(Dispatchers.IO).launch {
        DataStoreHolder.quickAppLauncherSettings.updateData { old ->
            val key = app.key()
            old.copy(recentLaunchTime = old.recentLaunchTime + (key to System.currentTimeMillis()), launchCount = old.launchCount + (key to ((old.launchCount[key] ?: 0L) + 1L)))
        }
    }
}