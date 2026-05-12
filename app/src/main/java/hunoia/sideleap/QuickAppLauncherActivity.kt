package hunoia.sideleap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import hunoia.sideleap.utils.LauncherDiagnostics

class QuickAppLauncherActivity : Activity() {

    private var activityCreateTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCreateTime = System.currentTimeMillis()
        val intent = intent
        LauncherDiagnostics.d(this, "activity: onCreate taskId=$taskId isTaskRoot=$isTaskRoot action=${intent?.action} flags=${intent?.flags} data=${intent?.dataString} categories=${intent?.categories} extras=${intent?.extras?.keySet()}")
        LauncherDiagnostics.d(this, "activity: onCreate callingPackage=$callingPackage referrer=$referrer source=externalActivity")

        SideGestureService.current?.let { service ->
            LauncherDiagnostics.d(this, "activity: service.current found, calling show()")
            service.quickAppLauncherOverlay?.show()
        } ?: LauncherDiagnostics.w(this, "activity: service.current is null, cannot show")

        LauncherDiagnostics.d(this, "activity: calling finish() reason=immediateAfterShow")
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        LauncherDiagnostics.d(this, "activity: onNewIntent taskId=$taskId isTaskRoot=$isTaskRoot action=${intent.action} flags=${intent.flags} data=${intent.dataString}")
    }

    override fun onResume() {
        super.onResume()
        val elapsed = if (activityCreateTime > 0) System.currentTimeMillis() - activityCreateTime else -1L
        LauncherDiagnostics.d(this, "activity: onResume taskId=$taskId isTaskRoot=$isTaskRoot elapsedSinceCreate=${elapsed}ms")
    }

    override fun onPause() {
        super.onPause()
        LauncherDiagnostics.d(this, "activity: onPause taskId=$taskId")
    }

    override fun onDestroy() {
        LauncherDiagnostics.d(this, "activity: onDestroy taskId=$taskId")
        super.onDestroy()
    }

    override fun finish() {
        LauncherDiagnostics.d(this, "activity: finish called taskId=$taskId")
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
