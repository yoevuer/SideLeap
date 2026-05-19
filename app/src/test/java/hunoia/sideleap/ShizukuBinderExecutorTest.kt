package hunoia.sideleap

import hunoia.sideleap.system.shizuku.ShizukuBinderExecutor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShizukuBinderExecutorTest {

    @Test
    fun parseFrozenActionResult_exitCodeZero_isSuccess() {
        val result = ShizukuBinderExecutor.parseFrozenActionResult("com.example.app", "exitCode=0")
        assertTrue(result.success)
        assertEquals("com.example.app", result.packageName)
        assertEquals(0, result.exitCode)
        assertNull(result.error)
    }

    @Test
    fun parseFrozenActionResult_exitCodeNonZero_isFailure() {
        val result = ShizukuBinderExecutor.parseFrozenActionResult("com.example.app", "exitCode=1")
        assertEquals(false, result.success)
        assertEquals(1, result.exitCode)
    }

    @Test
    fun parseFrozenActionResult_errorPrefix_setsError() {
        val result = ShizukuBinderExecutor.parseFrozenActionResult("com.example.app", "error: binder timeout")
        assertEquals(false, result.success)
        assertEquals("binder timeout", result.error)
    }

    @Test
    fun parseFrozenActionResult_errorPrefixTrimmed() {
        val result = ShizukuBinderExecutor.parseFrozenActionResult("com.example.app", "error:  ")
        assertEquals(false, result.success)
        assertEquals("", result.error)
    }

    @Test
    fun parseFrozenActionResult_noExitCode_defaultsToMinusOne() {
        val result = ShizukuBinderExecutor.parseFrozenActionResult("com.example.app", "output=ok")
        assertEquals(false, result.success)
        assertEquals(-1, result.exitCode)
    }

    @Test
    fun parseFrozenActionResult_multipleLines_exitCodeParsed() {
        val result = ShizukuBinderExecutor.parseFrozenActionResult("com.example.app", "output=success\nexitCode=0\n")
        assertTrue(result.success)
        assertEquals(0, result.exitCode)
    }

    @Test
    fun parseFrozenActionResult_emptyString_defaultsFailure() {
        val result = ShizukuBinderExecutor.parseFrozenActionResult("com.example.app", "")
        assertEquals(false, result.success)
        assertEquals(-1, result.exitCode)
    }

    @Test
    fun parseFrozenActionResult_invalidExitCode_defaultsFailure() {
        val result = ShizukuBinderExecutor.parseFrozenActionResult("com.example.app", "exitCode=abc")
        assertEquals(false, result.success)
        assertEquals(-1, result.exitCode)
    }

    @Test
    fun parseLauncherResult_exitCodeZero_isSuccess() {
        val result = ShizukuBinderExecutor.parseLauncherResult("exitCode=0", "com.example.app")
        assertTrue(result.success)
        assertEquals("com.example.app", result.packageName)
        assertEquals(0, result.exitCode)
        assertEquals("", result.output)
    }

    @Test
    fun parseLauncherResult_withOutput() {
        val result = ShizukuBinderExecutor.parseLauncherResult("exitCode=0\noutput=Package com.example.app enabled", "com.example.app")
        assertTrue(result.success)
        assertEquals("Package com.example.app enabled", result.output)
    }

    @Test
    fun parseLauncherResult_exitCodeNonZero_isFailure() {
        val result = ShizukuBinderExecutor.parseLauncherResult("exitCode=1", "com.example.app")
        assertEquals(false, result.success)
        assertEquals(1, result.exitCode)
    }

    @Test
    fun parseLauncherResult_noExitCode_defaultsMinusOne() {
        val result = ShizukuBinderExecutor.parseLauncherResult("", "com.example.app")
        assertEquals(false, result.success)
        assertEquals(-1, result.exitCode)
    }
}
