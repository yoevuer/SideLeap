package hunoia.luno.freeze

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import hunoia.luno.freeze.api.FreezeAction
import hunoia.luno.shizuku.ShizukuCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ShizukuBridgeService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_ENABLE_PACKAGE -> {
                    val packageName = msg.data.getString(EXTRA_PACKAGE_NAME, "")
                    val replyTo = msg.replyTo
                    scope.launch {
                        val result = ShizukuCommand.enablePackageForLauncher(this@ShizukuBridgeService, packageName)
                        val reply = Message.obtain(null, MSG_ENABLE_PACKAGE_RESULT)
                        reply.data.putBoolean(EXTRA_SUCCESS, result.success)
                        reply.data.putString(EXTRA_PACKAGE_NAME, result.packageName)
                        reply.data.putInt(EXTRA_EXIT_CODE, result.exitCode)
                        reply.data.putString(EXTRA_OUTPUT, result.output)
                        result.error?.let { reply.data.putString(EXTRA_ERROR, it) }
                        try {
                            replyTo?.send(reply)
                        } catch (_: Exception) {
                        }
                        stopSelf()
                    }
                }
                MSG_FREEZE_BATCH -> {
                    val replyTo = msg.replyTo
                    scope.launch {
                        try {
                            val result = FreezeAction.oneKeyFreeze(this@ShizukuBridgeService)
                            val reply = Message.obtain(null, MSG_FREEZE_BATCH_RESULT)
                            reply.data.putInt(EXTRA_SUCCESS_COUNT, result.successCount)
                            try { replyTo?.send(reply) } catch (_: Exception) {}
                        } catch (e: Exception) {
                            val reply = Message.obtain(null, MSG_FREEZE_BATCH_RESULT)
                            reply.data.putInt(EXTRA_SUCCESS_COUNT, -1)
                            reply.data.putString(EXTRA_ERROR, "${e::class.simpleName} ${e.message}")
                            try { replyTo?.send(reply) } catch (_: Exception) {}
                        } finally {
                            stopSelf()
                        }
                    }
                }

            }
        }
    }

    private val messenger = Messenger(handler)

    override fun onBind(intent: Intent): IBinder = messenger.binder

    override fun onDestroy() {
        scope.cancel()
    }

    companion object {
        const val MSG_ENABLE_PACKAGE = 1
        const val MSG_ENABLE_PACKAGE_RESULT = 2
        const val MSG_FREEZE_BATCH = 3
        const val MSG_FREEZE_BATCH_RESULT = 4
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_SUCCESS = "success"
        const val EXTRA_EXIT_CODE = "exitCode"
        const val EXTRA_OUTPUT = "output"
        const val EXTRA_ERROR = "error"
        const val EXTRA_SUCCESS_COUNT = "successCount"
    }
}