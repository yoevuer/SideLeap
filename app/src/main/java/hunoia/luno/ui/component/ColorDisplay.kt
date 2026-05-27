package hunoia.luno.ui.component
import hunoia.luno.ui.theme.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.unit.dp
import hunoia.luno.ui.theme.MinIconSize

@Composable
fun MyColorDisplay(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = MinIconSize, minHeight = MinIconSize)
            .background(
                color = color,
                shape = CircleShape
            )
            .border(
                width = Spacing1,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            )
    ) {
        if (color.isUnspecified || color == Color.Transparent) {
            Icon(
                modifier = Modifier.matchParentSize(),
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}