package hunoia.sideleap

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import android.os.Build
import android.os.Process
import com.aaron.compose.component.UDFComponentDefaults
import hunoia.sideleap.ui.UDFComponentDefaultsImpl
import hunoia.sideleap.core.AppContext
import hunoia.sideleap.core.DensityProvider
import hunoia.sideleap.core.crash.CrashHandler
import hunoia.sideleap.core.event.Events
import hunoia.sideleap.system.handler.isMainThread
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
class App : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(600)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil"))
                    .maxSizeBytes(50 * 1024 * 1024)
                    .build()
            }
            .build()
    }

    companion object {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

        val applicationScope: CoroutineScope get() = AppContext.applicationScope ?: scope

        fun getContext(): Context = AppContext.get()
    }

    private var isProviderProcess = true

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        isProviderProcess = base?.let { currentProcessName(it) == it.packageName } ?: true
        ShizukuProvider.enableMultiProcessSupport(isProviderProcess)
        if (!isProviderProcess && base != null) {
            ShizukuProvider.requestBinderForNonProviderProcess(base)
        }
        Reflection.unseal(base)
    }

    override fun onCreate() {
        super.onCreate()
        AppContext.init(applicationContext, scope)
        Events.initScope(AppContext.applicationScope!!)
        Events.isMainThread = { isMainThread() }
        initToastScope(AppContext.applicationScope!!)
        initVibrationContext(applicationContext)
        DensityProvider.init(applicationContext)

        UDFComponentDefaults.set(UDFComponentDefaultsImpl())

        Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
    }

    private fun currentProcessName(context: Context): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return getProcessName()
        }
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val pid = Process.myPid()
        return activityManager?.runningAppProcesses
            ?.firstOrNull { it.pid == pid }
            ?.processName
    }
}
