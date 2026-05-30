package hunoia.luno.ui.component.panel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState.Visible
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import hunoia.luno.ui.component.container.LongSlideState
import hunoia.luno.ui.component.actionText
import hunoia.luno.ui.component.actionIcon
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastForEachIndexed
import coil.compose.AsyncImage
import coil.imageLoader
import com.aaron.compose.ktx.clipToBackground
import com.aaron.compose.ktx.toDp
import com.aaron.compose.ktx.toPx

import hunoia.luno.action.api.ActionFacade
import hunoia.luno.config.defaults.SettingsUiDefaults.DimAlpha
import hunoia.luno.action.TriggerType
import hunoia.luno.config.model.Action
import hunoia.luno.config.model.ActionPanelStyle
import hunoia.luno.config.model.ArcStyle
import hunoia.luno.action.api.appInfo
import hunoia.luno.action.api.shortcutInfo
import hunoia.luno.config.model.GestureSettings
import hunoia.luno.gesture.GestureFacade
import hunoia.luno.ui.theme.AnimNormal
import hunoia.luno.ui.theme.AnimPanelResize
import hunoia.luno.ui.theme.MiniWindowDefaultHeight
import hunoia.luno.ui.theme.MiniWindowWidth
import hunoia.luno.ui.theme.RootPadding
import hunoia.luno.ui.theme.ShapeSmall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch



@Composable
fun ActionPanel(
    actionPanelStyle: ActionPanelStyle,
    actionPanelState: ActionPanelState,
    modifier: Modifier = Modifier,
    gestureSettings: GestureSettings? = null
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = actionPanelState.visible,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(spring(stiffness = Spring.StiffnessMedium))
    ) {
        Box {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = DimAlpha))
            )

            val selectedAction = actionPanelState.selectedAction
            val selectedLabel = actionText(selectedAction)
            val animationSpec = spring<Float>(stiffness = Spring.StiffnessHigh)
            val enter = fadeIn(animationSpec) + scaleIn(animationSpec, 0.9f)
            val exit = fadeOut(animationSpec) + scaleOut(animationSpec, 0.9f)


            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.Center)
                    .displayCutoutPadding()
                    .padding(RootPadding),
                visible = selectedAction.value == ActionFacade.EXTRA_LAUNCH_APP,
                enter = enter,
                exit = ExitTransition.None
            ) {
                    BoxWithConstraints {
                    Box(
                        modifier = Modifier
                            .let { thisModifier ->
                                val miniWindow = selectedAction.appInfo?.miniWindow ?: false
                                val boxMaxWidth = maxWidth
                                val boxMaxHeight = maxHeight
                                val spec = tween<Dp>(AnimPanelResize.toInt())
                                val width by animateDpAsState(
                                    targetValue = if (miniWindow) MiniWindowWidth else boxMaxWidth,
                                    animationSpec = spec
                                )
                                val height by animateDpAsState(
                                    targetValue = if (miniWindow) MiniWindowDefaultHeight else boxMaxHeight,
                                    animationSpec = spec
                                )
                                thisModifier.size(width = width, height = height)
                            }
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = MaterialTheme.shapes.small
                            )
                    )
                }
            }

            when (val style = actionPanelState.actionPanelStyle ?: actionPanelStyle) {
                is ArcStyle -> {
                    ArcActionPanel(
                        modifier = Modifier.fillMaxSize(),
                        actionPanelStyle = style,
                        actionPanelState = actionPanelState,
                        gestureSettings = gestureSettings
                    )
                }

                else -> {}
            }

            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .displayCutoutPadding()
                    .padding(RootPadding),
                visible = selectedLabel.isNotEmpty(),
                enter = enter,
                exit = exit
            ) {
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        shadow = Shadow(
                            color = Color.Black, offset = Offset(2.0f, 2.0f), blurRadius = 3f
                        )
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}







@Composable
internal fun AnimatedVisibilityScope.ActionPanelSelectableItem(
    actionPanelState: ActionPanelState,
    index: Int,
    action: Action,
    targetAnimOffset: Offset,
    panelOrigin: Offset,
    itemSizePx: Float,
    gestureSettings: GestureSettings?,
    modifier: Modifier,
    shape: androidx.compose.ui.graphics.Shape,
    content: @Composable () -> Unit
) {
    val transition = transition
    var isHovered by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isHovered) 1.15f else 1f, spring(stiffness = Spring.StiffnessHigh), label = "actionScale")
    LaunchedEffect(transition, actionPanelState, index, action, panelOrigin, targetAnimOffset) {
        snapshotFlow { actionPanelState.finger }
            .filter { it.isSpecified && !transition.isRunning && transition.currentState == Visible }
            .collect { finger ->
                if (actionPanelHitContains(finger, panelOrigin, targetAnimOffset, itemSizePx)) {
                    if (!actionPanelState.isSelected(action)) {
                        isHovered = true
                        actionPanelState.select(index, action)
                        gestureSettings?.let { GestureFacade.vibrateForActionPanel(it) }
                    }
                } else if (actionPanelState.isSelected(action)) {
                    isHovered = false
                    actionPanelState.select(index, Action.NONE)
                }
            }
    }
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = targetAnimOffset.x
                translationY = targetAnimOffset.y
                scaleX = scale
                scaleY = scale
            }
            .clipToBackground(color = actionPanelItemColor(action), shape = shape),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun ActionPanelIcon(action: Action, iconSize: Dp, bitmapIconSize: Dp = iconSize) {
    val actionIcon = actionIcon(action = action)
    if (actionIcon is ImageVector) {
        Image(
            modifier = Modifier.size(iconSize),
            imageVector = actionIcon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
        )
    } else {
        AsyncImage(
            modifier = Modifier
                .size(bitmapIconSize)
                .graphicsLayer {
                    val appInfo = action.appInfo
                    if (appInfo != null) {
                        scaleX = appInfo.iconScale
                        scaleY = appInfo.iconScale
                        return@graphicsLayer
                    }
                    val shortcutInfo = action.shortcutInfo
                    if (shortcutInfo != null) {
                        scaleX = shortcutInfo.iconScale
                        scaleY = shortcutInfo.iconScale
                    }
                },
            model = actionIcon,
            contentDescription = null,
            imageLoader = LocalContext.current.imageLoader,
        )
    }
}

@Composable
internal fun actionPanelItemColor(action: Action): Color {
    val actionIcon = actionIcon(action = action)
    return when (action.value) {
        ActionFacade.EXTRA_LAUNCH_APP -> action.appInfo?.iconBgColor.toActionPanelColor()
        ActionFacade.EXTRA_LAUNCH_SHORTCUT -> action.shortcutInfo?.iconBgColor.toActionPanelColor()

        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
internal fun Int?.toActionPanelColor(): Color {
    return if (this == null || this == 0) MaterialTheme.colorScheme.primary else Color(this)
}


