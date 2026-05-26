package hunoia.luno.ui.component.quickapplaunch

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import hunoia.luno.launcher.model.AppInfo
import hunoia.luno.launcher.query.AppSearch.key
import hunoia.luno.settings.model.QuickAppLauncherSettings
import hunoia.luno.ui.theme.AnimOverlayFade
import hunoia.luno.ui.theme.AnimPanelShift
import hunoia.luno.ui.theme.AnimPostHideDelay
import hunoia.luno.ui.theme.ScrollBottomPadding
import hunoia.luno.ui.theme.ShapeExtraLarge
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun QuickAppLauncherContent(
    initialSettings: QuickAppLauncherSettings,
    quickLauncherAppLongPressLaunchPopup: Boolean,
    requestEnableFrozenPackage: (String, (Boolean) -> Unit) -> Unit,
    onCloseAnimated: () -> Unit,
    onToggleAdjust: () -> Unit,
    onLaunch: (AppInfo, Boolean) -> Boolean,
    onRegisterCloseAnimated: ((() -> Unit) -> Unit)? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val state = remember(coroutineScope) {
        QuickAppLauncherState(
            context = context,
            coroutineScope = coroutineScope,
            initialSettings = initialSettings,
            quickLauncherAppLongPressLaunchPopup = quickLauncherAppLongPressLaunchPopup,
            requestEnableFrozenPackage = requestEnableFrozenPackage,
            onCloseAnimatedRaw = onCloseAnimated,
            onToggleAdjust = onToggleAdjust,
            onLaunch = onLaunch,
            onRegisterCloseAnimated = onRegisterCloseAnimated,
        )
    }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val screenWidthPx = remember(orientation) { with(density) { configuration.screenWidthDp.dp.toPx().roundToInt() } }
    val panelWidthDp = with(density) { (screenWidthPx * state.launcherSettings.panelWidthFraction).toDp() }
    val panelAlpha by animateFloatAsState(if (state.panelVisible) 1f else 0f, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "panelAlpha")
    val panelShiftY by animateFloatAsState(if (state.panelVisible) 0f else 18f, animationSpec = spring(stiffness = Spring.StiffnessMedium), label = "panelShiftY")
    var gridAtTop by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { onRegisterCloseAnimated?.invoke(state.closeAnimated) }
    LaunchedEffect(Unit) { state.panelVisible = true }
    val gridState = rememberLazyGridState()
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collectLatest { (index, offset) -> gridAtTop = index == 0 && offset == 0 }
    }
    val expandFromTopConnection = remember(gridAtTop, state.keyboardExpanded) {
        object : NestedScrollConnection {
            private var totalPull = 0f

            private fun updatePull(delta: Float) {
                if (!state.keyboardExpanded && gridAtTop && delta > 0f) {
                    totalPull += delta
                    if (totalPull > 32f) {
                        state.expandKeyboard()
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = panelAlpha
                    translationY = panelShiftY
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(expandFromTopConnection)
                    .pointerInput(key1 = state.keyboardExpanded, key2 = gridAtTop) {
                        var totalDrag = 0f
                        detectVerticalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                                if (state.keyboardExpanded && totalDrag < -32f) state.toggleKeyboard()
                                if (!state.keyboardExpanded && gridAtTop && totalDrag > 32f) state.expandKeyboard()
                            }
                        )
                    }
            ) {
                Card(
                    modifier = Modifier.width(panelWidthDp),
                    shape = RoundedCornerShape(ShapeExtraLarge),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        val contentHeightFraction = state.launcherSettings.contentHeightFraction
                        val candidateRows = state.launcherSettings.candidateRows.coerceIn(1, 3)
                        val chunkedApps = remember(state.filteredApps, candidateRows) { state.filteredApps.chunked(candidateRows) }
                        val candidateHeight = candidateHeightFor(contentHeightFraction, candidateRows)
                        val keyboardHeight = keyHeightFor(contentHeightFraction) * 3 + 20.dp
                        val contentHeight = candidateHeight + keyboardHeight
                        AnimatedContent(
                            targetState = state.keyboardExpanded,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150)) using SizeTransform(clip = false)
                            },
                            label = "keyboardExpand"
                        ) { expanded ->
                            if (expanded) {
                                Column {
                                    CandidateAppRows(
                                        chunkedApps = chunkedApps,
                                        frozenPkgs = state.appListState.frozenPkgs,
                                        candidateHeight = candidateHeight,
                                        onClick = { app, isFrozen ->
                                            state.launchApp(app, isFrozen, !quickLauncherAppLongPressLaunchPopup, null)
                                        },
                                        onLongPress = { app, isFrozen ->
                                            state.launchApp(app, isFrozen, quickLauncherAppLongPressLaunchPopup, "longPress")
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    KeyboardRow(
                                        view,
                                        listOf("QW" to "qw", "ER" to "er", "TY" to "ty", "UI" to "ui", "OP" to "op"),
                                        keyHeight = keyHeightFor(contentHeightFraction)
                                    ) { token -> state.addToken(token) }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    KeyboardRow(
                                        view,
                                        listOf("AS" to "as", "DF" to "df", "GH" to "gh", "JK" to "jk", "L" to "l"),
                                        keyHeight = keyHeightFor(contentHeightFraction)
                                    ) { token -> state.addToken(token) }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    KeyboardRow(
                                        view,
                                        listOf("调整" to null, "ZX" to "zx", "CV" to "cv", "BN" to "bn", "M" to "m", "删除" to null),
                                        onDelete = { state.deleteToken() },
                                        onClear = { state.clearTokens() },
                                        onAdjust = onToggleAdjust,
                                        keyHeight = keyHeightFor(contentHeightFraction)
                                    ) { token -> state.addToken(token) }
                                }
                            } else {
                                AppGrid(
                                    apps = state.filteredApps,
                                    frozenPkgs = state.appListState.frozenPkgs,
                                    gridState = gridState,
                                    gridAtTop = gridAtTop,
                                    keyboardExpanded = state.keyboardExpanded,
                                    gridHeight = contentHeight,
                                    onExpandKeyboard = { state.expandKeyboard() },
                                    onClick = { app, isFrozen ->
                                        state.launchApp(app, isFrozen, !quickLauncherAppLongPressLaunchPopup, null)
                                    },
                                    onLongPress = { app, isFrozen ->
                                        state.launchApp(app, isFrozen, quickLauncherAppLongPressLaunchPopup, "longPress_grid")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateAppRows(
    chunkedApps: List<List<AppInfo>>,
    frozenPkgs: Set<String>,
    candidateHeight: Dp,
    onClick: (AppInfo, Boolean) -> Unit,
    onLongPress: (AppInfo, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(candidateHeight)
    ) {
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
                            val isFrozen = app.packageName in frozenPkgs
                            AppItem(
                                app = app,
                                onClick = { onClick(app, isFrozen) },
                                onLongPress = { onLongPress(app, isFrozen) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGrid(
    apps: List<AppInfo>,
    frozenPkgs: Set<String>,
    gridState: LazyGridState,
    gridAtTop: Boolean,
    keyboardExpanded: Boolean,
    gridHeight: Dp,
    onExpandKeyboard: () -> Unit,
    onClick: (AppInfo, Boolean) -> Unit,
    onLongPress: (AppInfo, Boolean) -> Unit
) {
    val view = LocalView.current

    Box(
        modifier = Modifier
            .height(gridHeight)
            .fillMaxWidth()
            .pointerInput(gridAtTop) {
                var totalDrag = 0f
                detectVerticalDragGestures(
                    onDragStart = { totalDrag = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        if (gridAtTop && dragAmount > 0f) {
                            change.consume()
                            totalDrag += dragAmount
                            if (totalDrag > 32f) onExpandKeyboard()
                        }
                    }
                )
            }
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = ScrollBottomPadding),
            modifier = Modifier.fillMaxSize()
        ) {
            if (apps.isEmpty()) {
                item { Box(modifier = Modifier.height(88.dp).fillMaxWidth()) }
            }
            items(apps, key = { it.key() }, contentType = { "app" }) { app ->
                val isFrozen = app.packageName in frozenPkgs
                AppItem(
                    app = app,
                    onClick = { onClick(app, isFrozen) },
                    onLongPress = { onLongPress(app, isFrozen) }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .pointerInput(keyboardExpanded) {
                    var totalDrag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { totalDrag = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            totalDrag += dragAmount
                            if (!keyboardExpanded && totalDrag > 32f) onExpandKeyboard()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(vertical = 6.dp)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onExpandKeyboard()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "⌨", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun heightFractionLerp(f: Float): Float {
    return ((f.coerceIn(0.35f, 0.75f) - 0.35f) / 0.4f).coerceIn(0f, 1f)
}

private fun keyHeightFor(f: Float): Dp {
    val t = heightFractionLerp(f)
    return (34 + 6 * t).dp
}

private fun candidateHeightFor(f: Float, rows: Int): Dp {
    val t = heightFractionLerp(f)
    return ((48 + 8 * t) * rows.coerceIn(1, 3)).dp
}
