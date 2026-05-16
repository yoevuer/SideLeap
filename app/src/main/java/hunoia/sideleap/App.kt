package hunoia.sideleap

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.aaron.compose.component.UDFComponentDefaults
import hunoia.sideleap.ui.UDFComponentDefaultsImpl
import hunoia.sideleap.core.AppContext
import hunoia.sideleap.core.crash.CrashHandler
import hunoia.sideleap.core.event.Events
import hunoia.sideleap.system.feedback.initToastScope
import hunoia.sideleap.system.vibration.initVibrationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.weishu.reflection.Reflection
import rikka.shizuku.ShizukuProvider

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/17
 */
class App : Application() {

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val applicationScope: CoroutineScope get() = AppContext.applicationScope ?: scope

        fun getContext(): Context = AppContext.get()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ShizukuProvider.enableMultiProcessSupport(true)
        Reflection.unseal(base)
    }

    override fun onCreate() {
        super.onCreate()
        AppContext.init(applicationContext, scope)
        Events.initScope(AppContext.applicationScope!!)
        initToastScope(AppContext.applicationScope!!)
        initVibrationContext(applicationContext)

        UDFComponentDefaults.set(UDFComponentDefaultsImpl())

        Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
    }
}
