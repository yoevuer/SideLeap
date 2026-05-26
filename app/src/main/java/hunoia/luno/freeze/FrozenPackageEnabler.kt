package hunoia.luno.freeze

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import hunoia.luno.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FrozenPackageEnabler(
    private val context: Context,
    private val scopeProvider: () -> CoroutineScope,
    private val log: (String) -> Unit
) {
    private val lock = Any()
    private var inFlightPackageName: String? = null
    private var activeConnection: ServiceConnection? = null

    fun request(packageName: String, onResult: (Boolean) -> Unit) {
        synchronized(lock) {
            if (inFlightPackageName != null) {
                log("enable_package: in-flight $inFlightPackageName, ignoring $packageName")
                onResult(false)
                return
            }
            inFlightPackageName = packageName
        }

        log("enable_package: requesting $packageName")
        val latch = CountDownLatch(1)
        var result = false
        val replyHandler = Handler(Looper.getMainLooper()) { msg ->
            result = msg.data.getBoolean(ShizukuBridgeService.EXTRA_SUCCESS, false)
            val exitCode = msg.data.getInt(ShizukuBridgeService.EXTRA_EXIT_CODE, -1)
            log("enable_package: result=$result exitCode=$exitCode")
            latch.countDown()
            true
        }
        val replyMessenger = Messenger(replyHandler)

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                try {
                    val messenger = Messenger(binder)
                    val msg = Message.obtain(null, ShizukuBridgeService.MSG_ENABLE_PACKAGE)
                    msg.data.putString(ShizukuBridgeService.EXTRA_PACKAGE_NAME, packageName)
                    msg.replyTo = replyMessenger
                    messenger.send(msg)
                } catch (e: Exception) {
                    log("enable_package: send exception ${e.message}")
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {}

            override fun onBindingDied(name: ComponentName?) {
                log("enable_package: binding died")
                latch.countDown()
            }

            override fun onNullBinding(name: ComponentName?) {
                log("enable_package: null binding")
                latch.countDown()
            }
        }

        synchronized(lock) { activeConnection = connection }
        val bound = runCatching {
            context.bindService(
                Intent(context, ShizukuBridgeService::class.java),
                connection,
                Context.BIND_AUTO_CREATE
            )
        }.getOrDefault(false)
        if (!bound) {
            log("enable_package: bind failed for $packageName")
            clearRequest(connection, packageName)
            onResult(false)
            return
        }

        scopeProvider().launch(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            if (!latch.await(10, TimeUnit.SECONDS)) {
                log("enable_package: timeout for $packageName")
                result = false
            }
            if (BuildConfig.DEBUG) {
                android.util.Log.d(
                    "LauncherPerf",
                    "enable_package: shizuku_done pkg=$packageName result=$result elapsed=${System.currentTimeMillis() - start}ms"
                )
            }
            unbind(connection)
            clearRequest(connection, packageName)
            withContext(Dispatchers.Main) {
                onResult(result)
            }
        }
    }

    fun release() {
        val connection = synchronized(lock) {
            val current = activeConnection
            activeConnection = null
            inFlightPackageName = null
            current
        }
        connection?.let { unbind(it) }
    }

    private fun clearRequest(connection: ServiceConnection, packageName: String) {
        synchronized(lock) {
            if (activeConnection === connection) activeConnection = null
            if (inFlightPackageName == packageName) inFlightPackageName = null
        }
    }

    private fun unbind(connection: ServiceConnection) {
        try {
            context.unbindService(connection)
        } catch (_: Exception) {
        }
    }
}
