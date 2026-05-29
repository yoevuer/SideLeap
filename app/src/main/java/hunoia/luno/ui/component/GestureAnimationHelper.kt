package hunoia.luno.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import hunoia.luno.config.model.ColorSource
import hunoia.luno.config.model.Position
import hunoia.luno.config.model.ThemeColorKey
import hunoia.luno.config.model.TriggerDirection
import hunoia.luno.config.model.TriggerDirection.Center
import hunoia.luno.config.model.TriggerDirection.Center2
import hunoia.luno.config.model.TriggerDirection.Down
import hunoia.luno.config.model.TriggerDirection.Down2
import hunoia.luno.config.model.TriggerDirection.Up
import hunoia.luno.config.model.TriggerDirection.Up2
import hunoia.luno.ui.component.resolveColor

@Composable
fun resolveColor(source: ColorSource, themeKey: ThemeColorKey, customColor: Int): Color = when (source) {
    ColorSource.Custom -> Color(customColor)
    ColorSource.Theme -> themeKey.resolveColor()
}

fun triggerRotationOffset(triggerDirection: TriggerDirection, position: Position): Float {
    return when (triggerDirection) {
        Up -> when (position) {
            Position.Left -> -45f
            Position.Right -> 45f
            Position.Bottom -> -45f
        }
        Center, Center2 -> 0f
        Down -> when (position) {
            Position.Left -> 45f
            Position.Right -> -45f
            Position.Bottom -> 45f
        }
        Up2 -> when (position) {
            Position.Left -> -90f
            Position.Right -> 90f
            Position.Bottom -> -90f
        }
        Down2 -> when (position) {
            Position.Left -> 90f
            Position.Right -> -90f
            Position.Bottom -> 90f
        }
    }
}

fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
