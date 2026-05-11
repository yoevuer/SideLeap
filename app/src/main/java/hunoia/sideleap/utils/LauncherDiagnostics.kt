package hunoia.sideleap.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LauncherDiagnostics {

    private const val MAX_ENTRIES = 200
    private const val FILE_NAME = "launcher_diagnostics.log"
    val TAG = "SideLeapLauncher"

    @Volatile
    private var enabled = true

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    private fun file(context: Context): File {
        val ctx = context.applicationContext
        val dir = ctx.filesDir
        return File(dir, FILE_NAME)
    }

    private fun readLines(context: Context): MutableList<String> {
        val f = file(context)
        if (!f.exists()) return mutableListOf()
        return try {
            f.readLines().toMutableList()
        } catch (e: Exception) {
            Log.w(TAG, "readLines failed", e)
            mutableListOf()
        }
    }

    private fun writeLines(context: Context, lines: List<String>) {
        try {
            file(context).writeText(lines.joinToString("\n"))
        } catch (e: Exception) {
            Log.w(TAG, "writeLines failed", e)
        }
    }

    fun d(context: Context, message: String) {
        if (!enabled) return
        Log.d(TAG, message)
        val timestamp = dateFormat.format(Date())
        val safeMsg = message.replace('\n', ' ')
        val entry = "[$timestamp] $safeMsg"
        synchronized(this) {
            val lines = readLines(context)
            if (lines.size >= MAX_ENTRIES) lines.removeAt(0)
            lines.add(entry)
            writeLines(context, lines)
        }
    }

    fun w(context: Context, message: String, throwable: Throwable? = null) {
        if (!enabled) return
        val msg = if (throwable != null) "$message : ${throwable::class.simpleName} ${throwable.message}" else message
        Log.w(TAG, msg, throwable)
        d(context, msg)
    }

    fun entries(context: Context): List<String> {
        synchronized(this) {
            return readLines(context)
        }
    }

    fun clear(context: Context) {
        synchronized(this) {
            writeLines(context, emptyList())
            Log.d(TAG, "diagnostics log cleared")
        }
    }

    fun copyText(context: Context): String {
        synchronized(this) {
            return entries(context).joinToString("\n")
        }
    }
}