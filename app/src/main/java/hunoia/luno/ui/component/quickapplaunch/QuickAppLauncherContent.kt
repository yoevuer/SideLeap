package hunoia.luno.ui.component.quickapplaunch
import hunoia.luno.ui.theme.*

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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

import hunoia.luno.core.DensityProvider
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

private enum class Page { App, Settings }

@Composable
internal fun QuickAppLauncherContent(
    initialSettings: QuickAppLauncherSettings,
    requestEnableFrozenPackage: (String, (Boolean) -> Unit) -> Unit,
    onCloseAnimated: () -> Unit,
    onUpdateLayout: ((QuickAppLauncherSettings) -> Unit)? = null,
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
            requestEnableFrozenPackage = requestEnableFrozenPackage,
            onCloseAnimatedRaw = onCloseAnimated,
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
    var currentPage by remember { mutableStateOf(Page.App) }
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
                    .then(
                        if (currentPage == Page.App) Modifier.nestedScroll(expandFromTopConnection) else Modifier
                    )
                    .pointerInput(currentPage, state.keyboardExpanded, gridAtTop) {
                        if (currentPage == Page.Settings) return@pointerInput
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
                    Column(modifier = Modifier.padding(horizontal = Spacing12, vertical = Spacing10)) {
                        val contentHeightFraction = state.launcherSettings.contentHeightFraction
                        val candidateRows = state.launcherSettings.candidateRows.coerceIn(1, 3)
                        val chunkedApps = remember(state.filteredApps, candidateRows) { state.filteredApps.chunked(candidateRows) }
                        val density = LocalDensity.current
                        val screenHeightPx = DensityProvider.screenHeightPx
                        val panelHeightDp = with(density) { (screenHeightPx * contentHeightFraction).toDp() }
                        val contentDp = (panelHeightDp - Spacing10 * 2).coerceAtLeast(80.dp)
                        val rowUnit = ((contentDp - Spacing20) / (candidateRows + 2.25f)).coerceIn(24.dp, 80.dp)
                        val candidateHeight = rowUnit * candidateRows
                        val keyHeight = rowUnit * 0.75f
                        val keyboardHeight = keyHeight * 3 + Spacing20
                        val contentHeight = candidateHeight + keyboardHeight
                        AnimatedContent(
                            targetState = currentPage,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                            },
                            label = "pageSwitch"
                        ) { page ->
                            when (page) {
                                Page.App -> {
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
                                                    rows = candidateRows,
                                                    onClick = { app, isFrozen ->
                                                        state.launchApp(app, isFrozen, state.launcherSettings.tapOpensMiniWindow, null)
                                                    },
                                                    onLongPress = { app, isFrozen ->
                                                        state.launchApp(app, isFrozen, !state.launcherSettings.tapOpensMiniWindow, "longPress")
                                                    }
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                KeyboardRow(
                                                    view,
                                                    listOf("QW" to "qw", "ER" to "er", "TY" to "ty", "UI" to "ui", "OP" to "op"),
                                                    keyHeight = keyHeight
                                                ) { token -> state.addToken(token) }
                                                Spacer(modifier = Modifier.height(Spacing6))
                                                KeyboardRow(
                                                    view,
                                                    listOf("AS" to "as", "DF" to "df", "GH" to "gh", "JK" to "jk", "L" to "l"),
                                                    keyHeight = keyHeight
                                                ) { token -> state.addToken(token) }
                                                Spacer(modifier = Modifier.height(Spacing6))
                                                KeyboardRow(
                                                    view,
                                                    listOf("调整" to null, "ZX" to "zx", "CV" to "cv", "BN" to "bn", "M" to "m", "删除" to null),
                                                    onDelete = { state.deleteToken() },
                                                    onClear = { state.clearTokens() },
                                                    onAdjust = { currentPage = Page.Settings },
                                                    keyHeight = keyHeight
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
                                        gridColumns = state.launcherSettings.gridColumns,
                                                onExpandKeyboard = { state.expandKeyboard() },
                                                    onClick = { app, isFrozen ->
                                                        state.launchApp(app, isFrozen, state.launcherSettings.tapOpensMiniWindow, null)
                                                    },
                                                    onLongPress = { app, isFrozen ->
                                                        state.launchApp(app, isFrozen, !state.launcherSettings.tapOpensMiniWindow, "longPress_grid")
                                                }
                                            )
                                        }
                                    }
                                }
                                Page.Settings -> {
                                    Column(
                                        modifier = Modifier
                                            .height(contentHeight)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .verticalScroll(rememberScrollState())
                                        ) {
                                            QuickAppLauncherAdjustPanel(
                                                onSettingsChanged = { onUpdateLayout?.invoke(it) },
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .pointerInput(Unit) {
                                                    var drag = 0f
                                                    detectVerticalDragGestures(
                                                        onDragStart = { drag = 0f },
                                                        onVerticalDrag = { change, amount ->
                                                            change.consume()
                                                            drag += amount
                                                            if (drag > 32f) currentPage = Page.App
                                                        }
                                                    )
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .padding(vertical = Spacing6)
                                                    .clickable { currentPage = Page.App }
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(44.dp)
                                                        .height(Spacing5)
                                                        .clip(RoundedCornerShape(99.dp))
                                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                                                )
                                                Spacer(modifier = Modifier.height(Spacing4))
                                                Icon(
                                                    imageVector = Icons.Outlined.Tune,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.width(20.dp).height(20.dp),
                                                )
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
}

@Composable
private fun CandidateAppRows(
    chunkedApps: List<List<AppInfo>>,
    frozenPkgs: Set<String>,
    candidateHeight: Dp,
    rows: Int = 1,
    onClick: (AppInfo, Boolean) -> Unit,
    onLongPress: (AppInfo, Boolean) -> Unit
) {
    val iconHeight = (candidateHeight - Spacing2 * (rows - 1)) / rows - Spacing4 * 2
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(candidateHeight)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(chunkedApps, key = { chunk -> chunk.firstOrNull()?.key() ?: "empty" }) { columnApps ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing2),
                    modifier = Modifier.width(64.dp)
                ) {
                    columnApps.forEach { app ->
                        key(app.key()) {
                            val isFrozen = app.packageName in frozenPkgs
                            AppItem(
                                app = app,
                                iconHeight = iconHeight,
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
    gridColumns: Int = 4,
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
            columns = GridCells.Fixed(gridColumns),
            verticalArrangement = Arrangement.spacedBy(Spacing4),
            horizontalArrangement = Arrangement.spacedBy(Spacing4),
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
                    .padding(vertical = Spacing6)
                    .clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onExpandKeyboard()
                    }
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(Spacing5)
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                )
                Spacer(modifier = Modifier.height(Spacing4))
                Text(text = "⌨", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}


