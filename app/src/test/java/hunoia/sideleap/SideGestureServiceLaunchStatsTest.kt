package hunoia.sideleap

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SideGestureServiceLaunchStatsTest {

    @Test
    fun recordQuickAppLaunchIfSuccess_recordsOnlyWhenSuccessful() = runBlocking {
        val recorded = mutableListOf<String>()

        recordQuickAppLaunchIfSuccess(true, "pkg.one/MainActivity") {
            recorded.add(it)
        }
        recordQuickAppLaunchIfSuccess(false, "pkg.two/SecondActivity") {
            recorded.add(it)
        }

        assertEquals(listOf("pkg.one/MainActivity"), recorded)
    }

    @Test
    fun recordQuickAppLaunchIfSuccess_doesNotRecordWhenFailed() = runBlocking {
        val recorded = mutableListOf<String>()

        recordQuickAppLaunchIfSuccess(false, "pkg.failed/FailedActivity") {
            recorded.add(it)
        }

        assertEquals(emptyList<String>(), recorded)
    }
}
