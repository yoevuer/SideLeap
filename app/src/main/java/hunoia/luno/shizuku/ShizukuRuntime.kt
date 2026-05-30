package hunoia.luno.shizuku

import android.content.Context
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

@Deprecated("Use ShizukuManager instead. Will be removed in a future release.")
object ShizukuRuntime {

    fun isAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Exception) {
            false
        }
    }

    fun getVersion(): Int {
        return try {
            Shizuku.getVersion()
        } catch (e: Exception) {
            -1
        }
    }

    fun isPreV11OrUnsupported(): Boolean {
        val version = getVersion()
        return version < 11
    }

    fun checkPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermissionIfNeeded(requestCode: Int = 0): Boolean {
        return ShizukuManager.runCatching {
            kotlinx.coroutines.runBlocking {
                ShizukuManager.requestPermission()
            }
        }.getOrDefault(false)
    }

    fun awaitBinderReady(timeoutMs: Long = 5000): Boolean {
        return ShizukuManager.currentStatus().binderAlive
    }

    fun awaitBinderReady(context: Context, timeoutMs: Long = 5000): Boolean {
        return ShizukuManager.currentStatus().binderAlive
    }
}
