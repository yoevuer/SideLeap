package hunoia.luno.ui.settings.gesture.angle
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import hunoia.luno.R
import hunoia.luno.config.model.GestureAngle
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.defaultGestureAngleFor
import hunoia.luno.config.model.getArcDegrees
import hunoia.luno.config.model.getDegrees
import hunoia.luno.config.defaults.SettingsUiDefaults.GestureButtonColorAlpha
import hunoia.luno.ui.theme.ContentPaddingHorizontal
import hunoia.luno.ui.theme.ContentPaddingVertical
import hunoia.luno.ui.theme.ItemPadding
import hunoia.luno.ui.theme.SectionPadding
import kotlin.math.roundToInt

@Composable
fun GestureButtonAngleContent(
    gestureButton: GestureButton,
    onDismiss: () -> Unit,
    onSave: (GestureAngle) -> Unit
) {
    var angle by remember(gestureButton.id, gestureButton.position, gestureButton.angle) {
        mutableStateOf(gestureButton.angle)
    }
    val color = when (gestureButton.color == android.graphics.Color.TRANSPARENT) {
        true -> MaterialTheme.colorScheme.primary
        else -> Color(gestureButton.color)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ContentPaddingHorizontal, vertical = ContentPaddingVertical)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(color.copy(alpha = GestureButtonColorAlpha), CircleShape)
                    .border(Spacing1, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.gesture_angles),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = gestureButtonTitle(gestureButton.position),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TextButton(onClick = { angle = defaultGestureAngleFor(gestureButton.position) }) {
                Icon(Icons.Default.Restore, contentDescription = null)
                Text(text = stringResource(id = R.string.reset))
            }
        }

        Spacer(modifier = Modifier.height(SectionPadding))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .border(
                    width = Spacing1,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(horizontal = Spacing24, vertical = Spacing16)
        ) {
            Column {
                AdjustAngle(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    angle = angle,
                    onAngleChange = { angle = it },
                    position = gestureButton.position,
                    color = color
                )
                Spacer(modifier = Modifier.height(Spacing12))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val sectorNames = gestureButtonSectorNames(gestureButton.position)
                    val arcDegrees = angle.getArcDegrees()
                    sectorNames.forEachIndexed { index: Int, name: String ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${arcDegrees[index].roundToInt()}°",
                                color = color,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SectionPadding),
            horizontalArrangement = Arrangement.spacedBy(ItemPadding)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = onDismiss
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(
                modifier = Modifier.weight(1f),
                onClick = { onSave(angle) }
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    }
}

@Composable
private fun gestureButtonTitle(position: Position): String {
    return when (position) {
        Position.Left -> stringResource(id = R.string.left_gesture_button)
        Position.Right -> stringResource(id = R.string.right_gesture_button)
        Position.Bottom -> stringResource(id = R.string.bottom_gesture_button)
    }
}

@Composable
private fun gestureButtonSectorNames(position: Position): List<String> {
    return when (position) {
        Position.Bottom -> listOf(
            stringResource(R.string.gesture_angle_left2),
            stringResource(R.string.left),
            stringResource(R.string.direction_center),
            stringResource(R.string.right),
            stringResource(R.string.gesture_angle_right2)
        )
        else -> listOf(
            stringResource(R.string.gesture_angle_top2),
            stringResource(R.string.top),
            stringResource(R.string.direction_center),
            stringResource(R.string.bottom),
            stringResource(R.string.gesture_angle_bottom2)
        )
    }
}


