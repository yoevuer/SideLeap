package hunoia.sideleap.ui.screen.animationstyle

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import hunoia.sideleap.gesture.Position
import hunoia.sideleap.settings.model.WaveStyle

/**
 * @author DS-Z
 * @since 2025/11/5
 */

@Composable
fun WaveStyle.getIcon(): Painter {
    return getWaveStyleIcon(iconType)
}

@Composable
fun getWaveStyleIcon(iconType: Int): Painter {
    if (iconType == WaveStyle.ICON_TYPE_TRIANGLE) {
        return rememberVectorPainter(Icons.Default.PlayArrow)
    } else if (iconType == WaveStyle.ICON_TYPE_ANGLE) {
        return rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowForwardIos)
    } else if (iconType == WaveStyle.ICON_TYPE_ARROW_NEW) {
        return rememberVectorPainter(Icons.AutoMirrored.Filled.Forward)
    }
    return rememberVectorPainter(Icons.AutoMirrored.Filled.ArrowForward)
}

fun WaveStyle.getIconInitialRotation(position: Position): Float {
    return when (position) {
        Position.Left -> 0f
        Position.Right -> 180f
        Position.Bottom -> 270f
    }
}
