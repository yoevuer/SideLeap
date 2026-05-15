package hunoia.sideleap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

class QuickAppLauncherActivity : Activity() {

    private var activityCreateTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCreateTime = System.currentTimeMillis()
        val intent = intent
        Log.d("SideLeapLauncher", "activity: onCreate taskId=$taskId isTaskRoot=$isTaskRoot action=${intent?.action} flags=${intent?.flags} data=${intent?.dataString} categories=${intent?.categories} extras=${intent?.extras?.keySet()}")
        Log.d("SideLeapLauncher", "activity: onCreate callingPackage=$callingPackage referrer=$referrer source=externalActivity")

        SideGestureService.current?.let { service ->
            Log.d("SideLeapLauncher", "activity: service.current found, calling show()")
            service.quickAppLauncherOverlay?.show()
        } ?: Log.w("SideLeapLauncher", "activity: service.current is null, cannot show")

        Log.d("SideLeapLauncher", "activity: calling finish() reason=immediateAfterShow")
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("SideLeapLauncher", "activity: onNewIntent taskId=$taskId isTaskRoot=$isTaskRoot action=${intent.action} flags=${intent.flags} data=${intent.dataString}")
    }

    override fun onResume() {
        super.onResume()
        val elapsed = if (activityCreateTime > 0) System.currentTimeMillis() - activityCreateTime else -1L
        Log.d("SideLeapLauncher", "activity: onResume taskId=$taskId isTaskRoot=$isTaskRoot elapsedSinceCreate=${elapsed}ms")
    }

    override fun onPause() {
        super.onPause()
        Log.d("SideLeapLauncher", "activity: onPause taskId=$taskId")
    }

    override fun onDestroy() {
        Log.d("SideLeapLauncher", "activity: onDestroy taskId=$taskId")
        super.onDestroy()
    }

    override fun finish() {
        Log.d("SideLeapLauncher", "activity: finish called taskId=$taskId")
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
