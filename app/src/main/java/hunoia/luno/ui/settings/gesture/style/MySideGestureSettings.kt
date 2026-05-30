package hunoia.luno.ui.settings.gesture.style
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hunoia.luno.R
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.config.model.TriggerDirection.Center
import hunoia.luno.config.model.TriggerDirection.Center2
import hunoia.luno.config.model.TriggerDirection.Down
import hunoia.luno.config.model.TriggerDirection.Down2
import hunoia.luno.config.model.TriggerDirection.Up
import hunoia.luno.config.model.TriggerDirection.Up2
import hunoia.luno.ui.component.ExpressiveRow

@Composable
fun MySideGestureSettings(
    onClick: () -> Unit,
    gestureButton: GestureButton,
    direction: TriggerDirection,
    isLongSlide: Boolean,
    secondaryText: String,
    text: String? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val accent = when (isLongSlide) {
        true -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val onAccent = when (isLongSlide) {
        true -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val label = text ?: when (direction) {
        Center -> when (gestureButton.position) {
            Position.Left -> stringResource(id = R.string.slide_to_right)
            Position.Right -> stringResource(id = R.string.slide_to_left)
            Position.Bottom -> stringResource(id = R.string.slide_to_top)
        }
        Up -> when (gestureButton.position) {
            Position.Left -> stringResource(id = R.string.slide_to_top_right)
            Position.Right -> stringResource(id = R.string.slide_to_top_left)
            Position.Bottom -> stringResource(id = R.string.slide_to_top_left)
        }
        Down -> when (gestureButton.position) {
            Position.Left -> stringResource(id = R.string.slide_to_bottom_right)
            Position.Right -> stringResource(id = R.string.slide_to_bottom_left)
            Position.Bottom -> stringResource(id = R.string.slide_to_top_right)
        }
        Center2 -> stringResource(R.string.long_press)
        Up2 -> when (gestureButton.position) {
            Position.Left, Position.Right -> stringResource(id = R.string.slide_to_top)
            Position.Bottom -> stringResource(id = R.string.slide_to_left)
        }
        Down2 -> when (gestureButton.position) {
            Position.Left, Position.Right -> stringResource(id = R.string.slide_to_bottom)
            Position.Bottom -> stringResource(id = R.string.slide_to_right)
        }
    }
    val imageVector = when (direction) {
        Center2 -> Icons.Default.Adjust
        else -> Icons.AutoMirrored.Filled.ArrowForward
    }
    val rotation = when (direction) {
        Up -> when (gestureButton.position) {
            Position.Left -> -45f
            Position.Right -> -135f
            Position.Bottom -> -135f
        }
        Center -> when (gestureButton.position) {
            Position.Left -> 0f
            Position.Right -> 180f
            Position.Bottom -> -90f
        }
        Down -> when (gestureButton.position) {
            Position.Left -> 45f
            Position.Right -> 135f
            Position.Bottom -> -45f
        }
        Up2 -> when (gestureButton.position) {
            Position.Left, Position.Right -> -90f
            Position.Bottom -> -180f
        }
        Center2 -> 0f
        Down2 -> when (gestureButton.position) {
            Position.Left, Position.Right -> 90f
            Position.Bottom -> 0f
        }
    }
    ExpressiveRow(
        onClick = onClick,
        text = label,
        secondaryText = if (secondaryText.isNotEmpty()) secondaryText
            else stringResource(id = R.string.action_none),
        secondaryTextColor = MaterialTheme.colorScheme.primary,
        trailing = trailing,
        icon = {
            Surface(
                modifier = Modifier.size(Spacing32),
                shape = MaterialTheme.shapes.medium,
                color = accent,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier
                            .graphicsLayer { rotationZ = rotation }
                            .size(Spacing20),
                        imageVector = imageVector,
                        contentDescription = null,
                        tint = onAccent,
                    )
                }
            }
        },
    )
}
