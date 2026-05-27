package hunoia.luno.ui.ext

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import hunoia.luno.R
import hunoia.luno.settings.model.ThemeColorKey

@get:StringRes
val ThemeColorKey.displayNameRes: Int
    get() = when (this) {
        ThemeColorKey.Primary -> R.string.theme_color_primary
        ThemeColorKey.PrimaryContainer -> R.string.theme_color_primary_container
        ThemeColorKey.Secondary -> R.string.theme_color_secondary
        ThemeColorKey.SecondaryContainer -> R.string.theme_color_secondary_container
        ThemeColorKey.Tertiary -> R.string.theme_color_tertiary
        ThemeColorKey.TertiaryContainer -> R.string.theme_color_tertiary_container
        ThemeColorKey.Surface -> R.string.theme_color_surface
        ThemeColorKey.SurfaceVariant -> R.string.theme_color_surface_variant
        ThemeColorKey.OnSurface -> R.string.theme_color_on_surface
        ThemeColorKey.OnSurfaceVariant -> R.string.theme_color_on_surface_variant
        ThemeColorKey.Outline -> R.string.theme_color_outline
        ThemeColorKey.OutlineVariant -> R.string.theme_color_outline_variant
        ThemeColorKey.SurfaceContainerLow -> R.string.theme_color_surface_container_low
        ThemeColorKey.SurfaceContainer -> R.string.theme_color_surface_container
        ThemeColorKey.SurfaceContainerHigh -> R.string.theme_color_surface_container_high
    }

@androidx.compose.runtime.Composable
fun ThemeColorKey.resolveColor(): Color = when (this) {
    ThemeColorKey.Primary -> MaterialTheme.colorScheme.primary
    ThemeColorKey.PrimaryContainer -> MaterialTheme.colorScheme.primaryContainer
    ThemeColorKey.Secondary -> MaterialTheme.colorScheme.secondary
    ThemeColorKey.SecondaryContainer -> MaterialTheme.colorScheme.secondaryContainer
    ThemeColorKey.Tertiary -> MaterialTheme.colorScheme.tertiary
    ThemeColorKey.TertiaryContainer -> MaterialTheme.colorScheme.tertiaryContainer
    ThemeColorKey.Surface -> MaterialTheme.colorScheme.surface
    ThemeColorKey.SurfaceVariant -> MaterialTheme.colorScheme.surfaceVariant
    ThemeColorKey.OnSurface -> MaterialTheme.colorScheme.onSurface
    ThemeColorKey.OnSurfaceVariant -> MaterialTheme.colorScheme.onSurfaceVariant
    ThemeColorKey.Outline -> MaterialTheme.colorScheme.outline
    ThemeColorKey.OutlineVariant -> MaterialTheme.colorScheme.outlineVariant
    ThemeColorKey.SurfaceContainerLow -> MaterialTheme.colorScheme.surfaceContainerLow
    ThemeColorKey.SurfaceContainer -> MaterialTheme.colorScheme.surfaceContainer
    ThemeColorKey.SurfaceContainerHigh -> MaterialTheme.colorScheme.surfaceContainerHigh
}
