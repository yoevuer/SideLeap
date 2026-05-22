package hunoia.sideleap.core.crash

import hunoia.sideleap.core.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object CrashHandler : Thread.UncaughtExceptionHandler {

    private const val SEPARATOR = "gulugulu_CRASH_REPORT"
    private const val CRASH_FILE_NAME = "crashlog"

    private val defaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    private val crashDir = "${Paths.AppCache}/crash"
    private val crashFilePath = "$crashDir/$CRASH_FILE_NAME"

    fun getCrashFile(): File? {
        val path = crashFilePath
        if (!File(path).exists()) {
            return null
        }
        return File(path)
    }

    fun getCrashList(): List<String> {
        val string = File(crashFilePath).readText()
        if (string.isNullOrEmpty()) {
            return emptyList()
        }
        return string.split(SEPARATOR).map { it.trimIndent() }.filter { it.isNotEmpty() }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        saveErrorInfo(e)
        defaultHandler?.uncaughtException(t, e)
    }

    fun reset() {
        File(crashFilePath).delete()
    }

    private fun saveErrorInfo(e: Throwable) {
        val stringBuffer = StringBuffer()
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        e.printStackTrace(printWriter)
        printWriter.close()
        val errorStackInfo = stringWriter.toString()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        stringBuffer.append("Time: ${dateFormat.format(Date(System.currentTimeMillis()))}\n")
        stringBuffer.append(errorStackInfo)
        stringBuffer.append("$SEPARATOR\n")

        val cache = File(crashFilePath).readText()
        if (!cache.isNullOrEmpty()) {
            stringBuffer.append(cache)
        }

        File(crashDir).mkdirs()
        File(crashFilePath).createNewFile()
        File(crashFilePath).writeText(stringBuffer.toString())
    }
}