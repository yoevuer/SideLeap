package hunoia.sideleap.system.api

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider

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
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    fun requestPermissionIfNeeded(requestCode: Int = 0): Boolean {
        return try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                if (Shizuku.shouldShowRequestPermissionRationale()) {
                    return false
                }
                Shizuku.requestPermission(requestCode)
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun awaitBinderReady(timeoutMs: Long = 5000): Boolean {
        if (Shizuku.pingBinder()) return true
        val latch = java.util.concurrent.CountDownLatch(1)
        val listener = Shizuku.OnBinderReceivedListener {
            latch.countDown()
        }
        @Suppress("DEPRECATION")
        Shizuku.addBinderReceivedListenerSticky(listener)
        try {
            return latch.await(timeoutMs, java.util.concurrent.TimeUnit.MILLISECONDS)
        } finally {
            Shizuku.removeBinderReceivedListener(listener)
        }
    }

    fun awaitBinderReady(context: Context, timeoutMs: Long = 5000): Boolean {
        if (Shizuku.pingBinder()) return true
        runCatching { ShizukuProvider.requestBinderForNonProviderProcess(context.applicationContext) }
        return awaitBinderReady(timeoutMs)
    }
}
