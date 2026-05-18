package hunoia.sideleap.ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VirtualMouseCursor(
    position: Offset,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        if (!position.x.isFinite() || !position.y.isFinite()) return@Canvas
        drawCircle(
            color = Color(0xFF2196F3),
            radius = 12.dp.toPx(),
            center = position,
        )
    }
}
