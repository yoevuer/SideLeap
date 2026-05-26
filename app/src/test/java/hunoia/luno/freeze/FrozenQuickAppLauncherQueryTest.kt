package hunoia.luno.freeze

import android.content.Context
import hunoia.luno.freeze.api.FreezeState
import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.query.AppQuery
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class FrozenQuickAppLauncherQueryTest {

    private val context = mockk<Context>(relaxed = true)

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun queryApps_mergesLauncherAndFrozenApps() {
        val launcherApps = listOf(
            AppInfo("pkg.normal", "MainActivity", "Normal"),
            AppInfo("pkg.shared", "LauncherActivity", "Shared Launcher"),
        )
        val frozenApps = listOf(
            AppInfo("pkg.shared", "FrozenActivity", "Shared Frozen"),
            AppInfo("pkg.frozen", "FrozenOnlyActivity", "Frozen Only"),
        )

        mockkObject(AppQuery, FreezeState)
        every {
            AppQuery.queryLauncherActivities(context = context, allowRepeatPackage = false)
        } returns launcherApps
        every {
            FreezeState.queryFrozenApplications(context)
        } returns frozenApps

        val result = FrozenQuickAppLauncherQuery.queryApps(context)

        assertEquals(listOf(launcherApps[0], launcherApps[1], frozenApps[1]), result.apps)
        assertEquals(setOf("pkg.shared", "pkg.frozen"), result.frozenPkgs)
    }
}
