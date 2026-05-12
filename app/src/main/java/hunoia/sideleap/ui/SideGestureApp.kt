package hunoia.sideleap.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hunoia.sideleap.ui.navigation.About
import hunoia.sideleap.ui.navigation.ActionSelect
import hunoia.sideleap.ui.navigation.AdjustGestureAngles
import hunoia.sideleap.ui.navigation.AdvancedSettings
import hunoia.sideleap.ui.navigation.AppBlacklist
import hunoia.sideleap.ui.navigation.DiagnosticLogs
import hunoia.sideleap.ui.navigation.GestureButtonSettings
import hunoia.sideleap.ui.navigation.QuickAppLauncherHidden
import hunoia.sideleap.ui.navigation.GestureSettings
import hunoia.sideleap.ui.navigation.Home
import hunoia.sideleap.ui.navigation.IconResize
import hunoia.sideleap.ui.navigation.Unlock
import hunoia.sideleap.ui.navigation.WaveAnimationStyle
import hunoia.sideleap.ktx.LocalNavController
import hunoia.sideleap.ui.screen.about.AboutScreen
import hunoia.sideleap.ui.screen.actionselect.ActionSelectScreen
import hunoia.sideleap.ui.screen.advancedsettings.AdvancedSettingsScreen
import hunoia.sideleap.ui.screen.animationstyle.wave.WaveStyleScreen
import hunoia.sideleap.ui.screen.appblacklist.AppBlacklistScreen
import hunoia.sideleap.ui.screen.gestureangles.GestureAnglesScreen
import hunoia.sideleap.ui.screen.gesturebuttonsettings.GestureButtonSettingsScreen
import hunoia.sideleap.ui.screen.gesturesettings.GestureSettingsScreen
import hunoia.sideleap.ui.screen.diagnosticlogs.DiagnosticLogsScreen
import hunoia.sideleap.ui.screen.home.HomeScreen
import hunoia.sideleap.ui.screen.iconresize.IconResizeScreen
import hunoia.sideleap.ui.screen.quickapplaunchermanage.QuickAppLauncherManageScreen
import hunoia.sideleap.ui.screen.unlock.UnlockScreen
import hunoia.sideleap.ui.theme.SideGestureTheme
import kotlin.reflect.KType

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/11/22
 */

@Composable
fun SideGestureApp() {
    SideGestureTheme {
        val navController = rememberNavController()
        val durationMs = ANIMATION_DURATION_MS
        CompositionLocalProvider(
            LocalNavController provides navController
        ) {
            NavHost(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = Home,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(durationMs)) { it }
                },
                exitTransition = {
                    slideOutHorizontally(animationSpec = tween(durationMs)) { -it / 3 }
                },
                popEnterTransition = {
                    slideInHorizontally(animationSpec = tween(durationMs)) { -it / 3 }
                },
                popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(durationMs)) { it }
                }
            ) {
                myComposable<Home> {
                    HomeScreen(
                        onNavToUnlock = { navController.navigate(Unlock) },
                        onNavToAbout = { navController.navigate(About) },
                        onNavToAdvancedSettings = { navController.navigate(AdvancedSettings) },
                        onNavToGestureSettings = { navController.navigate(GestureSettings) },
                        onNavToGestureButtonSettings = { button ->
                            navController.navigate(GestureButtonSettings(button.id, button.position))
                        }
                    )
                }
                myComposable<Unlock> {
                    UnlockScreen(onBack = { navController.navigateUp() })
                }
                myComposable<About> {
                    AboutScreen(
                        onBack = { navController.navigateUp() },
                        onNavToDiagnosticLogs = { navController.navigate(DiagnosticLogs) }
                    )
                }
                myComposable<AdvancedSettings> {
                    AdvancedSettingsScreen(
                        onBack = { navController.navigateUp() },
                        onNavToAppBlacklist = { navController.navigate(AppBlacklist) },
                        onNavToQuickAppHidden = { navController.navigate(QuickAppLauncherHidden) },
                        onNavToAnimationStyle = {
                            navController.navigate(WaveAnimationStyle)
                        }
                    )
                }
                myComposable<DiagnosticLogs> {
                    DiagnosticLogsScreen(onBack = { navController.navigateUp() })
                }
                myComposable<GestureSettings> {
                    GestureSettingsScreen(
                        onNavToGestureAngles = { navController.navigate(AdjustGestureAngles) },
                        onBack = { navController.navigateUp() }
                    )
                }
                myComposable<AdjustGestureAngles> {
                    GestureAnglesScreen(onBack = { navController.navigateUp() })
                }
                myComposable<GestureButtonSettings> {
                    GestureButtonSettingsScreen(
                        onBack = { navController.navigateUp() },
                        onNavToActionSelect = { navController.navigate(it) }
                    )
                }
                myComposable<AppBlacklist> {
                    AppBlacklistScreen(onBack = { navController.navigateUp() })
                }
                myComposable<QuickAppLauncherHidden> {
                    QuickAppLauncherManageScreen(onBack = { navController.navigateUp() })
                }
                myComposable<ActionSelect> {
                    ActionSelectScreen(
                        onBack = { navController.navigateUp() },
                        onNavToIconResize = { navController.navigate(it) }
                    )
                }
                myComposable<IconResize> {
                    IconResizeScreen(onBack = { navController.navigateUp() })
                }
                myComposable<WaveAnimationStyle> {
                    WaveStyleScreen(onBack = { navController.navigateUp() })
                }
            }
        }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.myComposable(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    deepLinks: List<NavDeepLink> = emptyList(),
    noinline enterTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    EnterTransition?)? =
        null,
    noinline exitTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    ExitTransition?)? =
        null,
    noinline popEnterTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    EnterTransition?)? =
        enterTransition,
    noinline popExitTransition:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    ExitTransition?)? =
        exitTransition,
    noinline sizeTransform:
    (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards
    SizeTransform?)? =
        null,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T>(
        typeMap = typeMap,
        deepLinks = deepLinks,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
        sizeTransform = sizeTransform
    ) { navBackStackEntry ->
        Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)) {
            content(navBackStackEntry)
        }
    }
}

private const val ANIMATION_DURATION_MS = 250
