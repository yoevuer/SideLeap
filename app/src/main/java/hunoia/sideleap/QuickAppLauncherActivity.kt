package hunoia.sideleap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import hunoia.sideleap.BuildConfig

class QuickAppLauncherActivity : Activity() {

    private var activityCreateTime: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityCreateTime = System.currentTimeMillis()
        val intent = intent
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: onCreate taskId=$taskId isTaskRoot=$isTaskRoot action=${intent?.action} flags=${intent?.flags} data=${intent?.dataString} categories=${intent?.categories} extras=${intent?.extras?.keySet()}")
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: onCreate callingPackage=$callingPackage referrer=$referrer source=externalActivity")

        SideGestureService.current?.let { service ->
            if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: service.current found, calling show()")
            service.quickAppLauncherOverlay?.show()
        } ?: Log.w("SideLeapLauncher", "activity: service.current is null, cannot show")

        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: calling finish() reason=immediateAfterShow")
        finish()
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: onNewIntent taskId=$taskId isTaskRoot=$isTaskRoot action=${intent.action} flags=${intent.flags} data=${intent.dataString}")
    }

    override fun onResume() {
        super.onResume()
        val elapsed = if (activityCreateTime > 0) System.currentTimeMillis() - activityCreateTime else -1L
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: onResume taskId=$taskId isTaskRoot=$isTaskRoot elapsedSinceCreate=${elapsed}ms")
    }

    override fun onPause() {
        super.onPause()
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: onPause taskId=$taskId")
    }

    override fun onDestroy() {
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: onDestroy taskId=$taskId")
        super.onDestroy()
    }

    override fun finish() {
        if (BuildConfig.DEBUG) Log.d("SideLeapLauncher", "activity: finish called taskId=$taskId")
        super.finish()
        overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
