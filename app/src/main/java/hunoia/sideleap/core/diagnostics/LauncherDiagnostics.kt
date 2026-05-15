package hunoia.sideleap.core.diagnostics

import android.content.Context

object LauncherDiagnostics {

    const val TAG = "SideLeapLauncher"

    fun setEnabled(enabled: Boolean) {
    }

    fun d(context: Context, message: String) {
    }

    fun w(context: Context, message: String, throwable: Throwable? = null) {
    }

    fun entries(context: Context): List<String> = emptyList()

    fun clear(context: Context) {
    }

    fun copyText(context: Context): String = ""
}