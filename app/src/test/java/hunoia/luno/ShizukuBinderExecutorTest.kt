package hunoia.luno

import hunoia.luno.shizuku.ShellResult
import hunoia.luno.shizuku.PackageResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShizukuBinderExecutorTest {

    @Test
    fun shellResult_success() {
        val result = ShellResult(exitCode = 0, stdout = "ok")
        assertTrue(result.isSuccess)
    }

    @Test
    fun shellResult_failure_nonZeroExit() {
        val result = ShellResult(exitCode = 1, stderr = "error")
        assertFalse(result.isSuccess)
    }

    @Test
    fun shellResult_failure_timeout() {
        val result = ShellResult(timedOut = true)
        assertFalse(result.isSuccess)
    }

    @Test
    fun shellResult_failure_errorMessage() {
        val result = ShellResult(errorMessage = "something went wrong")
        assertFalse(result.isSuccess)
    }

    @Test
    fun shellResult_defaults() {
        val result = ShellResult()
        assertFalse(result.isSuccess)
        assertEquals(-1, result.exitCode)
        assertEquals("", result.stdout)
        assertEquals("", result.stderr)
        assertFalse(result.timedOut)
        assertEquals("", result.errorMessage)
    }

    @Test
    fun packageResult_success() {
        val result = PackageResult(success = true, packageName = "com.example.app")
        assertTrue(result.success)
        assertEquals("com.example.app", result.packageName)
        assertEquals("", result.errorMessage)
    }

    @Test
    fun packageResult_failure() {
        val result = PackageResult(success = false, packageName = "com.example.app", errorMessage = "permission denied")
        assertFalse(result.success)
        assertEquals("permission denied", result.errorMessage)
    }

    @Test
    fun packageResult_defaults() {
        val result = PackageResult(success = false)
        assertFalse(result.success)
        assertEquals("", result.packageName)
        assertEquals("", result.errorMessage)
    }
}
