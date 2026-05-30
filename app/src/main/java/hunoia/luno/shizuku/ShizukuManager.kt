package hunoia.luno.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import hunoia.luno.BuildConfig
import hunoia.luno.IShizukuCommandService
import hunoia.luno.core.AppContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

object ShizukuManager {

    private const val RequestCode = 41051
    private const val ServiceTag = "luno_shizuku"
    private const val ShizukuPackageName = "moe.shizuku.privileged.api"

    private val bindMutex = Mutex()
    private val autoPermissionMutex = Mutex()
    private val permissionMutex = Mutex()
    private val statusMutableStateFlow = MutableStateFlow(snapshot())

    @Volatile
    private var service: IShizukuCommandService? = null

    @Volatile
    private var serviceReady: CompletableDeferred<IShizukuCommandService>? = null

    @Volatile
    private var permissionResult: CompletableDeferred<Boolean>? = null

    @Volatile
    private var autoPermissionRequested = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val userServiceArgs by lazy {
        Shizuku.UserServiceArgs(
            ComponentName(AppContext.get(), ShizukuCommandService::class.java)
        ).processNameSuffix("shizuku_shell")
            .tag(ServiceTag)
            .version(BuildConfig.VERSION_CODE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val target = IShizukuCommandService.Stub.asInterface(binder)
            service = target
            serviceReady?.complete(target)
            updateStatus()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            serviceReady = null
            updateStatus()
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        updateStatus()
        scope.launch {
            autoRequestPermissionIfNeeded()
        }
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        service = null
        serviceReady = null
        updateStatus()
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode != RequestCode) return@OnRequestPermissionResultListener
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            permissionResult?.complete(granted)
            permissionResult = null
            if (!granted) {
                autoPermissionRequested = false
            }
            updateStatus()
        }

    init {
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        updateStatus()
    }

    // --- Status ---

    val statusFlow: StateFlow<ShizukuStatus> = statusMutableStateFlow.asStateFlow()

    fun currentStatus(): ShizukuStatus = statusMutableStateFlow.value

    fun updateStatus() {
        statusMutableStateFlow.value = snapshot()
    }

    // --- Permission ---

    suspend fun autoRequestPermissionIfNeeded(): Boolean {
        updateStatus()
        if (!shouldAutoRequest(currentStatus())) return false
        return autoPermissionMutex.withLock {
            updateStatus()
            val status = currentStatus()
            if (!shouldAutoRequest(status)) return@withLock false
            autoPermissionRequested = true
            requestPermission()
        }
    }

    suspend fun requestPermission(): Boolean {
        return permissionMutex.withLock {
            if (!isBinderAlive()) {
                updateStatus()
                return@withLock false
            }
            if (hasPermission()) {
                updateStatus()
                return@withLock true
            }
            val deferred = CompletableDeferred<Boolean>()
            permissionResult = deferred
            Shizuku.requestPermission(RequestCode)
            val granted = deferred.await()
            updateStatus()
            granted
        }
    }

    // --- Shell Execution ---

    suspend fun executeShell(command: String): ShellResult = withContext(Dispatchers.IO) {
        if (command.isBlank()) {
            return@withContext ShellResult(-1, "", "", false, "Command is blank")
        }
        if (!isBinderAlive()) {
            return@withContext ShellResult(-1, "", "", false, "Shizuku binder unavailable")
        }
        if (!hasPermission()) {
            return@withContext ShellResult(-1, "", "", false, "Shizuku permission denied")
        }
        val raw = runCatching {
            ensureService().executeShellCommand(command)
        }.getOrElse { t ->
            service = null
            serviceReady = null
            updateStatus()
            return@withContext ShellResult(-1, "", "", false, t.message ?: t.javaClass.simpleName)
        }
        parseShellResult(raw)
    }

    // --- Package Operations ---

    suspend fun enablePackage(packageName: String): PackageResult = withContext(Dispatchers.IO) {
        if (!isBinderAlive()) {
            return@withContext PackageResult(false, packageName, "Shizuku binder unavailable")
        }
        if (!hasPermission()) {
            return@withContext PackageResult(false, packageName, "Shizuku permission denied")
        }
        val raw = runCatching {
            ensureService().enablePackage(packageName)
        }.getOrElse { t ->
            service = null
            serviceReady = null
            updateStatus()
            return@withContext PackageResult(false, packageName, t.message ?: t.javaClass.simpleName)
        }
        parsePackageResult(packageName, raw)
    }

    suspend fun disablePackage(packageName: String): PackageResult = withContext(Dispatchers.IO) {
        if (!isBinderAlive()) {
            return@withContext PackageResult(false, packageName, "Shizuku binder unavailable")
        }
        if (!hasPermission()) {
            return@withContext PackageResult(false, packageName, "Shizuku permission denied")
        }
        val raw = runCatching {
            ensureService().disablePackage(packageName)
        }.getOrElse { t ->
            service = null
            serviceReady = null
            updateStatus()
            return@withContext PackageResult(false, packageName, t.message ?: t.javaClass.simpleName)
        }
        parsePackageResult(packageName, raw)
    }

    suspend fun executeBatch(
        packageNames: List<String>,
        disable: Boolean
    ): BatchFrozenResult = withContext(Dispatchers.IO) {
        val requestedCount = packageNames.size
        if (packageNames.isEmpty()) return@withContext BatchFrozenResult(0, 0, 0, 0, false)

        if (!isBinderAlive()) {
            return@withContext BatchFrozenResult(
                requestedCount = requestedCount, attemptedCount = 0,
                successCount = 0, failedCount = requestedCount,
                fallbackTriggered = false, errorSummary = "shizuku binder unavailable"
            )
        }
        if (!hasPermission()) {
            return@withContext BatchFrozenResult(
                requestedCount = requestedCount, attemptedCount = 0,
                successCount = 0, failedCount = requestedCount,
                fallbackTriggered = false, errorSummary = "permission denied"
            )
        }

        try {
            val svc = ensureService()
            val results = mutableListOf<PackageResult>()
            for (pkg in packageNames) {
                val raw = if (disable) {
                    svc.disablePackageApi(pkg)
                } else {
                    svc.enablePackageApi(pkg)
                }
                results += parsePackageResult(pkg, raw)
            }
            val successCount = results.count { it.success }
            val failedCount = results.count { !it.success }
            if (failedCount == 0) {
                return@withContext BatchFrozenResult(
                    requestedCount = requestedCount,
                    attemptedCount = results.size,
                    successCount = successCount,
                    failedCount = 0,
                    fallbackTriggered = false
                )
            }
            runCatching { svc.destroy() }
            executeSingleFallback(packageNames, disable, "batch failed: $failedCount/$requestedCount")
        } catch (e: Exception) {
            service = null
            serviceReady = null
            updateStatus()
            executeSingleFallback(packageNames, disable, e.message ?: e.javaClass.simpleName)
        }
    }

    private suspend fun executeSingleFallback(
        packageNames: List<String>, disable: Boolean, reason: String
    ): BatchFrozenResult {
        var fallbackAttempted = 0
        var fallbackSuccess = 0
        var fallbackFailed = 0
        for (pkg in packageNames) {
            val result = if (disable) disablePackage(pkg) else enablePackage(pkg)
            fallbackAttempted++
            if (result.success) fallbackSuccess++ else fallbackFailed++
        }
        return BatchFrozenResult(
            requestedCount = packageNames.size,
            attemptedCount = 0,
            successCount = 0,
            failedCount = packageNames.size,
            fallbackTriggered = true,
            fallbackAttemptedCount = fallbackAttempted,
            fallbackSuccessCount = fallbackSuccess,
            fallbackFailedCount = fallbackFailed,
            errorSummary = reason
        )
    }

    // --- Lifecycle ---

    suspend fun releaseService() {
        bindMutex.withLock {
            runCatching { service?.destroy() }
            runCatching { Shizuku.unbindUserService(userServiceArgs, serviceConnection, true) }
            service = null
            serviceReady = null
            updateStatus()
        }
    }

    // --- Parser ---

    private fun parseShellResult(raw: String): ShellResult {
        if (raw.startsWith("error:")) {
            return ShellResult(-1, "", "", false, raw.removePrefix("error:").trim())
        }
        val lines = raw.lines()
        val timedOut = lines.any { it == "timedOut=true" }
        val exitCode = lines.firstOrNull { it.startsWith("exitCode=") }
            ?.removePrefix("exitCode=")?.toIntOrNull() ?: -1
        val stdout = lines.firstOrNull { it.startsWith("stdout=") }
            ?.removePrefix("stdout=") ?: ""
        val stderr = lines.firstOrNull { it.startsWith("stderr=") }
            ?.removePrefix("stderr=") ?: ""
        return ShellResult(exitCode, stdout, stderr, timedOut, "")
    }

    private fun parsePackageResult(packageName: String, raw: String): PackageResult {
        if (raw.startsWith("error:")) {
            return PackageResult(false, packageName, raw.removePrefix("error:").trim())
        }
        val success = raw.contains("success=true")
        return PackageResult(success, packageName, if (success) "" else raw)
    }

    // --- Internal ---

    private suspend fun ensureService(): IShizukuCommandService {
        service?.let { return it }
        return bindMutex.withLock {
            val currentService = service
            if (currentService != null) return@withLock currentService
            val waiter = serviceReady ?: CompletableDeferred<IShizukuCommandService>().also { deferred ->
                serviceReady = deferred
                Shizuku.bindUserService(userServiceArgs, serviceConnection)
            }
            waiter.await()
        }
    }

    private fun snapshot(): ShizukuStatus {
        val context = runCatching { AppContext.get() }.getOrNull()
        if (context == null) {
            return ShizukuStatus(
                installed = false, binderAlive = false,
                permissionGranted = false, serviceBound = false,
                uid = null
            )
        }
        val binderAlive = isBinderAlive()
        val permissionGranted = binderAlive && hasPermission()
        val uid = if (binderAlive) runCatching { Shizuku.getUid() }.getOrNull() else null
        val installed = runCatching {
            context.packageManager.getPackageInfo(ShizukuPackageName, 0)
            true
        }.getOrDefault(false)
        return ShizukuStatus(
            installed = installed,
            binderAlive = binderAlive,
            permissionGranted = permissionGranted,
            serviceBound = service != null,
            uid = uid
        )
    }

    private fun isBinderAlive(): Boolean {
        return runCatching { Shizuku.pingBinder() }.getOrDefault(false)
    }

    private fun hasPermission(): Boolean {
        return runCatching {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
    }

    private fun shouldAutoRequest(status: ShizukuStatus): Boolean {
        return status.installed &&
            status.binderAlive &&
            !status.permissionGranted &&
            !autoPermissionRequested
    }
}
