package hunoia.sideleap.ui.widget.quickapplaunch

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blankj.utilcode.util.ScreenUtils
import hunoia.sideleap.BuildConfig
import hunoia.sideleap.SideGestureService
import hunoia.sideleap.entity.AppInfo
import hunoia.sideleap.entity.QuickAppLauncherSettings
import hunoia.sideleap.utils.AppInfoUtils
import hunoia.sideleap.utils.DataStoreHolder
import hunoia.sideleap.utils.LauncherDiagnostics
import hunoia.sideleap.utils.key
import hunoia.sideleap.freeze.FreezeState
import hunoia.sideleap.utils.sortApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
internal fun QuickAppLauncherContent(
    service: SideGestureService,
    initialSettings: QuickAppLauncherSettings,
    quickLauncherAppLongPressLaunchPopup: Boolean,
    onClose: () -> Unit,
    onCloseAnimated: () -> Unit,
    onSettingsChanged: (QuickAppLauncherSettings) -> Unit,
    onToggleAdjust: () -> Unit,
    onLaunch: (AppInfo, Boolean) -> Boolean,
    onRegisterCloseAnimated: ((() -> Unit) -> Unit)? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    var appListState by remember { mutableStateOf(AppListState(emptyList(), emptySet())) }
    var tokens by remember { mutableStateOf(emptyList<String>()) }
    var launcherSettings by remember { mutableStateOf(initialSettings) }
    var keyboardExpanded by remember { mutableStateOf(true) }
    var panelVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        DataStoreHolder.quickAppLauncherSettings.data.collectLatest { launcherSettings = it }
    }
    LaunchedEffect(launcherSettings.showSystemApps) {
        val state = kotlinx.coroutines.withContext(Dispatchers.IO) {
            val fa = FreezeState.queryFrozenApplications(context, launcherSettings.showSystemApps)
            val frozenPkgSet = fa.map { it.packageName }.toSet()
            val la = AppInfoUtils.queryLauncherActivities(context, allowRepeatPackage = false, showSystemApps = launcherSettings.showSystemApps)
            val normalPkgNames = la.map { it.packageName }.toSet()
            val merged = la + fa.filter { it.packageName !in normalPkgNames }
            AppListState(merged, frozenPkgSet)
        }
        appListState = state
    }
    val hiddenApps = launcherSettings.hiddenApps
    val visibleApps = remember(appListState.apps, hiddenApps) {
        appListState.apps.filter { app ->
            if (hiddenApps.contains(app.key())) {
                false
            } else if (app.className.isEmpty()) {
                hiddenApps.none { it.startsWith("${app.packageName}/") }
            } else {
                true
            }
        }
    }
    val filteredApps = remember(visibleApps, launcherSettings.recentLaunchTime, launcherSettings.launchCount, tokens) {
        sortApps(context, visibleApps, launcherSettings, tokens)
    }
    val density = LocalDensity.current
    val screenWidthPx = ScreenUtils.getScreenWidth()
    val screenHeightPx = ScreenUtils.getScreenHeight()
    val panelWidthDp = with(density) { (screenWidthPx * launcherSettings.panelWidthFraction).toDp() }
    val panelAlpha by animateFloatAsState(if (panelVisible) 1f else 0f, animationSpec = tween(200), label = "panelAlpha")
    val panelShiftY by animateFloatAsState(if (panelVisible) 0f else 18f, animationSpec = tween(180), label = "panelShiftY")
    LaunchedEffect(Unit) { panelVisible = true }
    var panelBounds by remember { mutableStateOf<androidx.compose.ui.geometry.Rect?>(null) }
    var gridAtTop by remember { mutableStateOf(true) }
    var closing by remember { mutableStateOf(false) }
    val closeAnimated = {
        if (!closing) {
            closing = true
            panelVisible = false
            coroutineScope.launch {
                delay(220)
                onCloseAnimated()
            }
        }
    }
    LaunchedEffect(Unit) { onRegisterCloseAnimated?.invoke(closeAnimated) }
    val gridState = rememberLazyGridState()
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collectLatest { (index, offset) -> gridAtTop = index == 0 && offset == 0 }
    }
    val expandFromTopConnection = remember(gridAtTop, keyboardExpanded) {
        object : NestedScrollConnection {
            private var totalPull = 0f

            private fun updatePull(delta: Float) {
                if (!keyboardExpanded && gridAtTop && delta > 0f) {
                    totalPull += delta
                    if (totalPull > 32f) {
                        keyboardExpanded = true
                        totalPull = 0f
                    }
                } else if (delta <= 0f) {
                    totalPull = 0f
                }
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                updatePull(available.y)
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                updatePull(available.y)
                return Offset.Zero
            }
        }
    }
        Box(modifier = Modifier.width(panelWidthDp)) {
            Box(modifier = Modifier.fillMaxWidth().graphicsLayer { alpha = panelAlpha; translationY = panelShiftY }) {
            Column(modifier = Modifier.fillMaxWidth().nestedScroll(expandFromTopConnection).pointerInput(key1 = keyboardExpanded, key2 = gridAtTop) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount
                        if (keyboardExpanded && totalDrag < -32f) {
                            keyboardExpanded = false
                        }
                        if (!keyboardExpanded && gridAtTop && totalDrag > 32f) {
                            keyboardExpanded = true
                        }
                    }
                )
            }) {
            Card(modifier = Modifier.width(panelWidthDp).onGloballyPositioned { panelBounds = it.boundsInWindow() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    val contentHeightFraction = launcherSettings.contentHeightFraction
                    val candidateRows = launcherSettings.candidateRows.coerceIn(1, 3)
                    val chunkedApps = remember(filteredApps, candidateRows) { filteredApps.chunked(candidateRows) }
                    val candidateHeight = candidateHeightFor(contentHeightFraction, candidateRows)
                    AnimatedContent(
                        targetState = keyboardExpanded,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(120)) togetherWith fadeOut(animationSpec = tween(90)) using SizeTransform(clip = false)
                        },
                        label = "keyboardExpand"
                    ) { expanded ->
                        if (expanded) {
                            Column {
Box(modifier = Modifier.fillMaxWidth().height(candidateHeight)) {
                                      val columnCount = maxOf(1, (filteredApps.size + candidateRows - 1) / candidateRows)
                                      LazyRow(
                                          horizontalArrangement = Arrangement.spacedBy(2.dp),
                                          modifier = Modifier.fillMaxSize()
                                      ) {
                                          items(chunkedApps, key = { chunk -> chunk.firstOrNull()?.key() ?: "empty" }) { columnApps ->
Column(
                                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                                    modifier = Modifier.width(64.dp)
                                                ) {
columnApps.forEach { app ->
                                                              key(app.key()) {
val isFrozen = app.packageName in appListState.frozenPkgs
                                                          AppItem(app = app, isFrozen = isFrozen, onClick = {
                                                             if (isFrozen) {
                                                                 service.requestEnableFrozenPackage(app.packageName) { success ->
                                                                     if (success) {
                                                                         LauncherDiagnostics.d(service, "enable_package: request launch pkg=${app.packageName} miniWindow=false")
                                                                         coroutineScope.launch {
                                                                             val found = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                                                 AppInfoUtils.findLauncherActivity(context, app.packageName)
                                                                             }
                                                                             if (found != null) {
                                                                                 LauncherDiagnostics.d(service, "enable_package: launcher found pkg=${found.packageName} cls=${found.className}")
                                                                                 val result = onLaunch(found, !quickLauncherAppLongPressLaunchPopup)
                                                                                 LauncherDiagnostics.d(service, "enable_package: launch after enable result=$result pkg=${app.packageName}")
                                                                                 if (result) closeAnimated()
                                                                             } else {
                                                                                 LauncherDiagnostics.d(service, "enable_package: launcher activity not found pkg=${app.packageName}")
                                                                             }
                                                                         }
                                                                     }
                                                                 }
                                                             } else if (onLaunch(app, !quickLauncherAppLongPressLaunchPopup)) closeAnimated()
}, onLongPress = { _, _ ->
                                                   if (isFrozen) {
                                                       val tFrozenStart = System.currentTimeMillis()
                                                        if (BuildConfig.DEBUG) {
                                                                       val beforeState = runCatching { context.packageManager.getApplicationEnabledSetting(app.packageName) }.getOrDefault(-1)
                                                                       android.util.Log.d("LauncherPerf", "longPress: frozen start pkg=${app.packageName} beforeEnable=$beforeState")
                                                                   }
                                                                   service.requestEnableFrozenPackage(app.packageName) { success ->
                                                                       if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress: enable_end pkg=${app.packageName} success=$success elapsed=${System.currentTimeMillis() - tFrozenStart}ms")
                                                                       if (success) {
                                                                           LauncherDiagnostics.d(service, "enable_package: request launch pkg=${app.packageName} miniWindow=true")
                                                                           if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress: resolve_intent start pkg=${app.packageName}")
                                                                           val tResolve = System.currentTimeMillis()
                                                                           coroutineScope.launch {
                                                                               val found = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                                                   AppInfoUtils.findLauncherActivity(context, app.packageName)
                                                                               }
                                                                               if (found != null) {
                                                                                   if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress: resolve_intent found pkg=${app.packageName} elapsed=${System.currentTimeMillis() - tResolve}ms")
                                                                                   LauncherDiagnostics.d(service, "enable_package: launcher found pkg=${found.packageName} cls=${found.className}")
                                                                                   val tLaunch = System.currentTimeMillis()
                                                                                   val result = onLaunch(found, quickLauncherAppLongPressLaunchPopup)
                                                                                   if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress: startActivity pkg=${app.packageName} result=$result elapsed=${System.currentTimeMillis() - tLaunch}ms total=${System.currentTimeMillis() - tFrozenStart}ms")
                                                                                   LauncherDiagnostics.d(service, "enable_package: launch after enable result=$result pkg=${app.packageName}")
                                                                                   if (result) closeAnimated()
                                                                               } else {
                                                                                   if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress: resolve_intent not_found pkg=${app.packageName} elapsed=${System.currentTimeMillis() - tResolve}ms total=${System.currentTimeMillis() - tFrozenStart}ms")
                                                                                   LauncherDiagnostics.d(service, "enable_package: launcher activity not found pkg=${app.packageName}")
                                                                               }
                                                                           }
                                                                       } else {
                                                                           if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress: enable_failed pkg=${app.packageName} total=${System.currentTimeMillis() - tFrozenStart}ms")
                                                                       }
                                                                   }
} else if (onLaunch(app, quickLauncherAppLongPressLaunchPopup)) closeAnimated()
                                                            })
                                                      }
                                                  }
                                              }
                                            }
                                        }
                                    }
                                  Spacer(modifier = Modifier.height(8.dp))
                                  KeyboardRow(view, listOf("QW" to "qw", "ER" to "er", "TY" to "ty", "UI" to "ui", "OP" to "op"), keyHeight = keyHeightFor(contentHeightFraction)) { token -> tokens = tokens + token }
                                  Spacer(modifier = Modifier.height(6.dp))
                                  KeyboardRow(view, listOf("AS" to "as", "DF" to "df", "GH" to "gh", "JK" to "jk", "L" to "l"), keyHeight = keyHeightFor(contentHeightFraction)) { token -> tokens = tokens + token }
                                  Spacer(modifier = Modifier.height(6.dp))
                                  KeyboardRow(view, listOf("调整" to null, "ZX" to "zx", "CV" to "cv", "BN" to "bn", "M" to "m", "删除" to null), onDelete = { tokens = tokens.dropLast(1) }, onClear = { tokens = emptyList() }, onAdjust = onToggleAdjust, keyHeight = keyHeightFor(contentHeightFraction)) { token -> tokens = tokens + token }
                              }
                          } else {
                              Box(modifier = Modifier.height(gridHeightFor(contentHeightFraction)).fillMaxWidth().nestedScroll(expandFromTopConnection).pointerInput(gridAtTop) {
                                 var totalDrag = 0f
                                 detectVerticalDragGestures(
                                     onDragStart = { totalDrag = 0f },
                                     onVerticalDrag = { change, dragAmount ->
                                         if (gridAtTop && dragAmount > 0f) {
                                             change.consume()
                                             totalDrag += dragAmount
                                             if (totalDrag > 32f) keyboardExpanded = true
                                         }
                                     }
                                 )
                             }) {
                                     LazyVerticalGrid(state = gridState, columns = GridCells.Adaptive(minSize = 64.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), contentPadding = PaddingValues(bottom = 36.dp), modifier = Modifier.fillMaxSize()) {
                                         if (filteredApps.isEmpty()) item { Box(modifier = Modifier.height(88.dp).fillMaxWidth()) }
                                         items(filteredApps, key = { it.key() }) { app ->
                                             val isFrozen = app.packageName in appListState.frozenPkgs
                                             AppItem(app = app, isFrozen = isFrozen, onClick = {
                                                 if (isFrozen) {
                                                     service.requestEnableFrozenPackage(app.packageName) { success ->
                                                         if (success) {
                                                             LauncherDiagnostics.d(service, "enable_package: request launch pkg=${app.packageName} miniWindow=false")
                                                             coroutineScope.launch {
                                                                 val found = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                                     AppInfoUtils.findLauncherActivity(context, app.packageName)
                                                                 }
                                                                 if (found != null) {
                                                                     LauncherDiagnostics.d(service, "enable_package: launcher found pkg=${found.packageName} cls=${found.className}")
                                                                     val result = onLaunch(found, !quickLauncherAppLongPressLaunchPopup)
                                                                     LauncherDiagnostics.d(service, "enable_package: launch after enable result=$result pkg=${app.packageName}")
                                                                     if (result) closeAnimated()
                                                                 } else {
                                                                     LauncherDiagnostics.d(service, "enable_package: launcher activity not found pkg=${app.packageName}")
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 } else if (onLaunch(app, !quickLauncherAppLongPressLaunchPopup)) closeAnimated()
}, onLongPress = { _, _ ->
                                                  if (isFrozen) {
val tFrozenStart = System.currentTimeMillis()
                                                       if (BuildConfig.DEBUG) {
                                                           val beforeState = runCatching { context.packageManager.getApplicationEnabledSetting(app.packageName) }.getOrDefault(-1)
                                                           android.util.Log.d("LauncherPerf", "longPress_grid: frozen start pkg=${app.packageName} beforeEnable=$beforeState")
                                                       }
                                                       service.requestEnableFrozenPackage(app.packageName) { success ->
                                                           if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress_grid: enable_end pkg=${app.packageName} success=$success elapsed=${System.currentTimeMillis() - tFrozenStart}ms")
                                                           if (success) {
                                                               LauncherDiagnostics.d(service, "enable_package: request launch pkg=${app.packageName} miniWindow=true")
                                                               if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress_grid: resolve_intent start pkg=${app.packageName}")
                                                               val tResolve = System.currentTimeMillis()
                                                               coroutineScope.launch {
                                                                   val found = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                                                       AppInfoUtils.findLauncherActivity(context, app.packageName)
                                                                   }
                                                                   if (found != null) {
                                                                       if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress_grid: resolve_intent found pkg=${app.packageName} elapsed=${System.currentTimeMillis() - tResolve}ms")
                                                                       LauncherDiagnostics.d(service, "enable_package: launcher found pkg=${found.packageName} cls=${found.className}")
                                                                       val tLaunch = System.currentTimeMillis()
                                                                       val result = onLaunch(found, quickLauncherAppLongPressLaunchPopup)
                                                                       if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress_grid: startActivity pkg=${app.packageName} result=$result elapsed=${System.currentTimeMillis() - tLaunch}ms total=${System.currentTimeMillis() - tFrozenStart}ms")
                                                                       LauncherDiagnostics.d(service, "enable_package: launch after enable result=$result pkg=${app.packageName}")
                                                                       if (result) closeAnimated()
                                                                   } else {
                                                                       if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress_grid: resolve_intent not_found pkg=${app.packageName} elapsed=${System.currentTimeMillis() - tResolve}ms total=${System.currentTimeMillis() - tFrozenStart}ms")
                                                                       LauncherDiagnostics.d(service, "enable_package: launcher activity not found pkg=${app.packageName}")
                                                                   }
                                                               }
                                                           } else {
                                                               if (BuildConfig.DEBUG) android.util.Log.d("LauncherPerf", "longPress_grid: enable_failed pkg=${app.packageName} total=${System.currentTimeMillis() - tFrozenStart}ms")
                                                           }
                                                       }
                                                   } else if (onLaunch(app, quickLauncherAppLongPressLaunchPopup)) closeAnimated()
                                              })
                                         }
                                     }
                                 Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().pointerInput(keyboardExpanded) {
                                     var totalDrag = 0f
                                     detectVerticalDragGestures(
                                         onDragStart = { totalDrag = 0f },
                                         onVerticalDrag = { change, dragAmount ->
                                             change.consume()
                                             totalDrag += dragAmount
                                             if (!keyboardExpanded && totalDrag > 32f) keyboardExpanded = true
                                         }
                                     )
                                 }, contentAlignment = Alignment.Center) {
                                     Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 6.dp).clickable { view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); keyboardExpanded = true }) {
                                         Box(modifier = Modifier.width(44.dp).height(5.dp).clip(RoundedCornerShape(99.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)))
                                         Spacer(modifier = Modifier.height(4.dp))
                                         Text(text = "⌨", fontSize = 18.sp)
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             }
             }
         }
     }

private fun keyHeightFor(panelHeightFraction: Float) = when {
    panelHeightFraction < 0.5f -> 34.dp
    panelHeightFraction < 0.75f -> 36.dp
    else -> 40.dp
}

private fun candidateHeightFor(panelHeightFraction: Float, rows: Int): Dp {
    val rowHeight = when {
        panelHeightFraction < 0.5f -> 48.dp
        panelHeightFraction < 0.75f -> 52.dp
        else -> 56.dp
    }
    return rowHeight * rows.coerceIn(1, 3)
}

private fun gridHeightFor(panelHeightFraction: Float) = when {
    panelHeightFraction < 0.5f -> 180.dp
    panelHeightFraction < 0.75f -> 220.dp
    else -> 260.dp
}

private data class AppListState(val apps: List<AppInfo>, val frozenPkgs: Set<String>)