package hunoia.sideleap.action.handlers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import hunoia.sideleap.R
import hunoia.sideleap.action.ActionHandler
import hunoia.sideleap.action.ActionHandlerContext
import hunoia.sideleap.constant.GlobalActions
import hunoia.sideleap.entity.Action
import hunoia.sideleap.utils.ShizukuBridgeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object FreezeAppsActionHandler : ActionHandler {

    override val supportedActions = setOf(GlobalActions.ONE_KEY_FREEZE_APPS)

    override suspend fun handle(action: Action, context: ActionHandlerContext): Boolean {
        when (action.value) {
            GlobalActions.ONE_KEY_FREEZE_APPS -> {
                val successCount = withContext(Dispatchers.IO) {
                    bridgeOneKeyFreeze(context.appContext)
                }
                if (successCount >= 0) {
                    val msg = context.appContext.getString(R.string.bulk_frozen_count, successCount)
                    context.showToast(msg)
                } else {
                    @Suppress("HardCodedStringLiteral")
                    context.showToast("冻结功能暂不可用")
                }
            }
            else -> return false
        }
        return true
    }

    private fun bridgeOneKeyFreeze(context: Context): Int {
        val intent = Intent(context, ShizukuBridgeService::class.java)
        val latch = CountDownLatch(1)
        val result = AtomicInteger(-1)

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                if (binder == null) { latch.countDown(); return }
                try {
                    val messenger = Messenger(binder)
                    val replyHandler = object : Handler(Looper.getMainLooper()) {
                        override fun handleMessage(msg: Message) {
                            if (msg.what == ShizukuBridgeService.MSG_FREEZE_BATCH_RESULT) {
                                result.set(msg.data.getInt(ShizukuBridgeService.EXTRA_SUCCESS_COUNT, -1))
                                latch.countDown()
                            }
                        }
                    }
                    val replyMessenger = Messenger(replyHandler)
                    val msg = Message.obtain(null, ShizukuBridgeService.MSG_FREEZE_BATCH)
                    msg.replyTo = replyMessenger
                    messenger.send(msg)
                } catch (e: Exception) {
                    latch.countDown()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {}
        }

        try {
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
            if (!latch.await(2, TimeUnit.SECONDS)) {
                result.set(-2)
            }
        } catch (e: Exception) {
            result.set(-3)
        } finally {
            try { context.unbindService(conn) } catch (_: Exception) {}
        }

        return result.get()
    }
}
