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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.blankj.utilcode.util.ScreenUtils
import hunoia.sideleap.launcher.model.AppInfo
import hunoia.sideleap.freeze.FrozenQuickAppLauncherQuery
import hunoia.sideleap.settings.model.QuickAppLauncherSettings
import hunoia.sideleap.settings.api.SettingsProvider
import hunoia.sideleap.launcher.launch.QuickAppLaunch
import hunoia.sideleap.launcher.query.AppSearch.key
import hunoia.sideleap.launcher.query.AppSearch.sortApps
import hunoia.sideleap.launcher.query.QuickAppLauncherAppList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

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
    var appListState by remember { mutableStateOf(QuickAppLauncherAppList(emptyList(), emptySet())) }
    var tokens by remember { mutableStateOf(emptyList<String>()) }
    var launcherSettings by remember { mutableStateOf(initialSettings) }
    var keyboardExpanded by remember { mutableStateOf(true) }
    var panelVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        SettingsProvider.quickAppLauncherSettings.collectLatest { launcherSettings = it }
    }
    LaunchedEffect(launcherSettings.showSystemApps) {
        val state = kotlinx.coroutines.withContext(Dispatchers.IO) {
            FrozenQuickAppLauncherQuery.queryApps(context, launcherSettings.showSystemApps)
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
    val panelWidthDp = with(density) { (screenWidthPx * launcherSettings.panelWidthFraction).toDp() }
    val panelAlpha by animateFloatAsState(if (panelVisible) 1f else 0f, animationSpec = tween(200), label = "panelAlpha")
    val panelShiftY by animateFloatAsState(if (panelVisible) 0f else 18f, animationSpec = tween(180), label = "panelShiftY")
    LaunchedEffect(Unit) { panelVisible = true }
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
    val launchApp = { app: AppInfo, isFrozen: Boolean, miniWindow: Boolean, debugPrefix: String? ->
        QuickAppLaunch.launch(
            context = context,
            coroutineScope = coroutineScope,
            app = app,
            isFrozen = isFrozen,
            miniWindow = miniWindow,
            debugPrefix = debugPrefix,
            requestEnableFrozenPackage = requestEnableFrozenPackage,
            log = { message -> android.util.Log.d("SideLeapLauncher", message) },
            onLaunch = onLaunch,
            onLaunched = closeAnimated
        )
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
                    .pointerInput(key1 = keyboardExpanded, key2 = gridAtTop) {
                        var totalDrag = 0f
                        detectVerticalDragGestures(
                            onDragStart = { totalDrag = 0f },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                                if (keyboardExpanded && totalDrag < -32f) keyboardExpanded = false
                                if (!keyboardExpanded && gridAtTop && totalDrag > 32f) keyboardExpanded = true
                            }
                        )
                    }
            ) {
                Card(
                    modifier = Modifier.width(panelWidthDp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
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
                                    CandidateAppRows(
                                        chunkedApps = chunkedApps,
                                        frozenPkgs = appListState.frozenPkgs,
                                        candidateHeight = candidateHeight,
                                        onClick = { app, isFrozen ->
                                            launchApp(app, isFrozen, !quickLauncherAppLongPressLaunchPopup, null)
                                        },
                                        onLongPress = { app, isFrozen ->
                                            launchApp(app, isFrozen, quickLauncherAppLongPressLaunchPopup, "longPress")
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    KeyboardRow(
                                        view,
                                        listOf("QW" to "qw", "ER" to "er", "TY" to "ty", "UI" to "ui", "OP" to "op"),
                                        keyHeight = keyHeightFor(contentHeightFraction)
                                    ) { token -> tokens = tokens + token }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    KeyboardRow(
                                        view,
                                        listOf("AS" to "as", "DF" to "df", "GH" to "gh", "JK" to "jk", "L" to "l"),
                                        keyHeight = keyHeightFor(contentHeightFraction)
                                    ) { token -> tokens = tokens + token }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    KeyboardRow(
                                        view,
                                        listOf("调整" to null, "ZX" to "zx", "CV" to "cv", "BN" to "bn", "M" to "m", "删除" to null),
                                        onDelete = { tokens = tokens.dropLast(1) },
                                        onClear = { tokens = emptyList() },
                                        onAdjust = onToggleAdjust,
                                        keyHeight = keyHeightFor(contentHeightFraction)
                                    ) { token -> tokens = tokens + token }
                                }
                            } else {
                                AppGrid(
                                    apps = filteredApps,
                                    frozenPkgs = appListState.frozenPkgs,
                                    gridState = gridState,
                                    gridAtTop = gridAtTop,
                                    keyboardExpanded = keyboardExpanded,
                                    contentHeightFraction = contentHeightFraction,
                                    onExpandKeyboard = { keyboardExpanded = true },
                                    onClick = { app, isFrozen ->
                                        launchApp(app, isFrozen, !quickLauncherAppLongPressLaunchPopup, null)
                                    },
                                    onLongPress = { app, isFrozen ->
                                        launchApp(app, isFrozen, quickLauncherAppLongPressLaunchPopup, "longPress_grid")
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
                                isFrozen = isFrozen,
                                onClick = { onClick(app, isFrozen) },
                                onLongPress = { _, _ -> onLongPress(app, isFrozen) }
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
    contentHeightFraction: Float,
    onExpandKeyboard: () -> Unit,
    onClick: (AppInfo, Boolean) -> Unit,
    onLongPress: (AppInfo, Boolean) -> Unit
) {
    val view = LocalView.current

    Box(
        modifier = Modifier
            .height(gridHeightFor(contentHeightFraction))
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
            columns = GridCells.Adaptive(minSize = 64.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 36.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (apps.isEmpty()) {
                item { Box(modifier = Modifier.height(88.dp).fillMaxWidth()) }
            }
            items(apps, key = { it.key() }) { app ->
                val isFrozen = app.packageName in frozenPkgs
                AppItem(
                    app = app,
                    isFrozen = isFrozen,
                    onClick = { onClick(app, isFrozen) },
                    onLongPress = { _, _ -> onLongPress(app, isFrozen) }
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
                Text(text = "⌨", fontSize = 18.sp)
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
