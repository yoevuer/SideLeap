package hunoia.luno.ui.screen.home

import hunoia.luno.ui.theme.*
import hunoia.luno.config.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.luno.gesture.bounds

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEach

@Composable
fun GestureButtonOverlay(
    showSide: Boolean,
    showBottom: Boolean,
    sideGestureButtons: List<hunoia.luno.config.model.GestureButton>,
    bottomGestureButtons: List<hunoia.luno.config.model.GestureButton>,
) {
    AnimatedVisibility(
        visible = showSide || showBottom,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium))
    ) {
        val colorScheme = MaterialTheme.colorScheme
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val buttons = if (showSide) {
                        sideGestureButtons
                    } else {
                        bottomGestureButtons
                    }
                    buttons.fastForEach { button ->
                        if (!button.enabled) {
                            return@fastForEach
                        }
                        val bounds = button.bounds()
                        drawRoundRect(
                            color = when (button.color == android.graphics.Color.TRANSPARENT) {
                                true -> colorScheme.primary.copy(alpha = GestureButtonColorAlpha)
                                else -> Color(button.color).copy(alpha = GestureButtonColorAlpha)
                            },
                            topLeft = bounds.topLeft,
                            size = bounds.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(Spacing4.toPx(), Spacing4.toPx())
                        )
                        drawRoundRect(
                            color = colorScheme.outlineVariant,
                            topLeft = bounds.topLeft,
                            size = bounds.size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(Spacing4.toPx(), Spacing4.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = Spacing1.toPx())
                        )
                    }
                }
        )
    }
}
