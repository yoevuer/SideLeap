package hunoia.luno.ui.settings.gesture.button
import hunoia.luno.ui.theme.*

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Swipe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import hunoia.luno.R
import hunoia.luno.config.model.GestureButton
import hunoia.luno.ui.settings.gesture.style.MySideGestureSettings
import hunoia.luno.ui.settings.gesture.style.StyleTrailingButton
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.config.model.TriggerDirection.Center
import hunoia.luno.config.model.TriggerDirection.Center2
import hunoia.luno.config.model.TriggerDirection.Down
import hunoia.luno.config.model.TriggerDirection.Down2
import hunoia.luno.config.model.TriggerDirection.Up
import hunoia.luno.config.model.TriggerDirection.Up2
import hunoia.luno.gesture.GestureFacade
import hunoia.luno.ui.component.actionTextCompose
import hunoia.luno.ui.component.ExpressiveCard
import hunoia.luno.ui.navigation.ActionSelect

@Composable
fun GestureButtonSlideActionsCard(
    gestureButton: GestureButton,
    isSideButton: Boolean,
    onNavToActionSelect: (ActionSelect) -> Unit,
) {
    ExpressiveCard(
        icon = Icons.Default.Swipe,
        title = stringResource(id = R.string.slide_action),
        subtitle = "定义 5 个滑动方向的触发动作",
        onClick = {},
    ) {
        val navToActionSelect: (TriggerDirection) -> Unit = { direction ->
            onNavToActionSelect(
                ActionSelect(
                    gestureButtonId = gestureButton.id,
                    position = gestureButton.position,
                    direction = direction,
                    isLongSlide = false,
                    isSideButton = isSideButton
                )
            )
        }
        MySideGestureSettings(
            onClick = { navToActionSelect(Center) },
            gestureButton = gestureButton,
            direction = Center,
            isLongSlide = false,
            secondaryText = gestureButton.slideActions.center.actionTextCompose()
        )
        MySideGestureSettings(
            onClick = { navToActionSelect(Up) },
            gestureButton = gestureButton,
            direction = Up,
            isLongSlide = false,
            secondaryText = gestureButton.slideActions.up.actionTextCompose()
        )
        MySideGestureSettings(
            onClick = { navToActionSelect(Down) },
            gestureButton = gestureButton,
            direction = Down,
            isLongSlide = false,
            secondaryText = gestureButton.slideActions.down.actionTextCompose()
        )
        MySideGestureSettings(
            onClick = { navToActionSelect(Up2) },
            gestureButton = gestureButton,
            direction = Up2,
            isLongSlide = false,
            secondaryText = gestureButton.slideActions.up2.actionTextCompose()
        )
        MySideGestureSettings(
            onClick = { navToActionSelect(Down2) },
            gestureButton = gestureButton,
            direction = Down2,
            isLongSlide = false,
            secondaryText = gestureButton.slideActions.down2.actionTextCompose()
        )
    }
}

@Composable
fun GestureButtonLongSlideActionsCard(
    gestureButton: GestureButton,
    isSideButton: Boolean,
    onNavToActionSelect: (ActionSelect) -> Unit,
    onStyleSelect: (TriggerDirection) -> Unit,
) {
    ExpressiveCard(
        icon = Icons.Default.Gesture,
        title = stringResource(id = R.string.long_slide_action),
        subtitle = "长滑触发与动作面板样式",
        onClick = {},
    ) {
        val navToActionSelect: (TriggerDirection) -> Unit = { direction ->
            onNavToActionSelect(
                ActionSelect(
                    gestureButtonId = gestureButton.id,
                    position = gestureButton.position,
                    direction = direction,
                    isLongSlide = true,
                    isSideButton = isSideButton
                )
            )
        }
        fun styleTrailing(direction: TriggerDirection): @Composable () -> Unit = {
            StyleTrailingButton(
                currentStyle = GestureFacade.styleBy(gestureButton.longSlideActionPanelStyles, direction),
                onClick = { onStyleSelect(direction) }
            )
        }
        MySideGestureSettings(
            onClick = { navToActionSelect(Center) },
            gestureButton = gestureButton,
            direction = Center,
            isLongSlide = true,
            secondaryText = gestureButton.longSlideActions.center.actionTextCompose(),
            trailing = styleTrailing(Center)
        )
        MySideGestureSettings(
            onClick = { navToActionSelect(Up) },
            gestureButton = gestureButton,
            direction = Up,
            isLongSlide = true,
            secondaryText = gestureButton.longSlideActions.up.actionTextCompose(),
            trailing = styleTrailing(Up)
        )
        MySideGestureSettings(
            onClick = { navToActionSelect(Down) },
            gestureButton = gestureButton,
            direction = Down,
            isLongSlide = true,
            secondaryText = gestureButton.longSlideActions.down.actionTextCompose(),
            trailing = styleTrailing(Down)
        )
        MySideGestureSettings(
            onClick = { navToActionSelect(Up2) },
            gestureButton = gestureButton,
            direction = Up2,
            isLongSlide = true,
            secondaryText = gestureButton.longSlideActions.up2.actionTextCompose(),
            trailing = styleTrailing(Up2)
        )
        MySideGestureSettings(
            onClick = { navToActionSelect(Down2) },
            gestureButton = gestureButton,
            direction = Down2,
            isLongSlide = true,
            secondaryText = gestureButton.longSlideActions.down2.actionTextCompose(),
            trailing = styleTrailing(Down2)
        )
    }
}

@Composable
fun GestureButtonTapActionsCard(
    gestureButton: GestureButton,
    isSideButton: Boolean,
    onNavToActionSelect: (ActionSelect) -> Unit,
) {
    ExpressiveCard(
        icon = Icons.Default.Adjust,
        title = stringResource(id = R.string.tap_and_long_press_action),
        subtitle = "点击与长按触发配置",
        onClick = {},
    ) {
        val navToTapActionSelect: (TriggerDirection) -> Unit = { direction ->
            onNavToActionSelect(
                ActionSelect(
                    gestureButtonId = gestureButton.id,
                    position = gestureButton.position,
                    direction = direction,
                    isLongSlide = false,
                    isSideButton = isSideButton,
                    isTap = true
                )
            )
        }
        MySideGestureSettings(
            onClick = { navToTapActionSelect(Center) },
            gestureButton = gestureButton,
            direction = Center,
            isLongSlide = false,
            secondaryText = gestureButton.tapActions.center.actionTextCompose(),
            text = stringResource(id = R.string.tap_action)
        )
        MySideGestureSettings(
            onClick = {
                onNavToActionSelect(
                    ActionSelect(
                        gestureButtonId = gestureButton.id,
                        position = gestureButton.position,
                        direction = Center2,
                        isLongSlide = false,
                        isSideButton = isSideButton
                    )
                )
            },
            gestureButton = gestureButton,
            direction = Center2,
            isLongSlide = false,
            secondaryText = gestureButton.slideActions.center2.actionTextCompose(),
            text = stringResource(id = R.string.long_press)
        )
    }
}
