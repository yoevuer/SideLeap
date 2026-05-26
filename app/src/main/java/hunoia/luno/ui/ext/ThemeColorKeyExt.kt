package hunoia.luno.ui.ext

import androidx.annotation.StringRes
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
