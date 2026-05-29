package hunoia.luno.ui

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.navigation.toRoute
import hunoia.luno.ui.theme.AnimNavTransition
import hunoia.luno.ui.theme.NavExitOffsetDivisor

import hunoia.luno.ui.navigation.ActionSelect

import hunoia.luno.ui.navigation.GestureButtonSettings

import hunoia.luno.ui.navigation.Home
import hunoia.luno.ui.navigation.SubGestureActionSelect
import hunoia.luno.ui.navigation.SubGestureEditor

import hunoia.luno.ui.actionselect.ActionSelectContent

import hunoia.luno.ui.settings.gesture.button.GestureButtonSettingsScreen

import hunoia.luno.ui.settings.gesture.subgesture.SubGestureActionSelectContent
import hunoia.luno.ui.settings.gesture.subgesture.SubGestureSettingsScreen
import hunoia.luno.ui.theme.SideGestureTheme
import hunoia.luno.ui.navigation.LocalNavController
import hunoia.luno.ui.home.HomeScreen
import kotlin.reflect.KType



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
                    slideOutHorizontally(animationSpec = tween(durationMs)) { -it / NavExitOffsetDivisor }
                },
                popEnterTransition = {
                    slideInHorizontally(animationSpec = tween(durationMs)) { -it / NavExitOffsetDivisor }
                },
                popExitTransition = {
                    slideOutHorizontally(animationSpec = tween(durationMs)) { it }
                }
            ) {
                myComposable<Home> {
                    HomeScreen(
                        onNavToGestureButtonSettings = { button ->
                            navController.navigate(GestureButtonSettings(button.id, button.position))
                        },
                        onNavToSubGestureEditor = { subGestureId ->
                            navController.navigate(SubGestureEditor(subGestureId))
                        }
                    )
                }
                myComposable<GestureButtonSettings> {
                    GestureButtonSettingsScreen(
                        onBack = { navController.navigateUp() },
                        onNavToActionSelect = { navController.navigate(it) }
                    )
                }
                myComposable<ActionSelect> {
                    ActionSelectContent(
                        onDismiss = { navController.popBackStack() },
                        actionSelect = it.toRoute()
                    )
                }
                myComposable<SubGestureEditor> {
                    SubGestureSettingsScreen(
                        onBack = { navController.navigateUp() },
                        onNavToSubGestureActionSelect = { navController.navigate(it) }
                    )
                }
                myComposable<SubGestureActionSelect> {
                    SubGestureActionSelectContent(
                        onDismiss = { navController.popBackStack() },
                        subGestureId = it.toRoute<SubGestureActionSelect>().id,
                        direction = it.toRoute<SubGestureActionSelect>().direction
                    )
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
        Surface(color = MaterialTheme.colorScheme.surface) {
            content(navBackStackEntry)
        }
    }
}

private const val ANIMATION_DURATION_MS = AnimNavTransition
