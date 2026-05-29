package hunoia.luno.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import hunoia.luno.config.model.AnimationStyles
import hunoia.luno.ui.component.ColorPickerBottomSheet
import hunoia.luno.ui.component.ColorSelection

@Composable
fun ColorPickerDialog(
    target: String?,
    state: AnimationStyleVM.UiState,
    vm: AnimationStyleVM,
    onDismiss: () -> Unit,
) {
    target?.let { target ->
        val t = state.currentType
        val ws = state.waveStyle
        val cs = state.capsuleStyle
        val bs = state.bubbleStyle
        val ls = state.lineStyle
        val initial = resolvePreviewColor(
            when (target) {
                "bg" -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.backgroundColorSource
                    AnimationStyles.TYPE_CAPSULE -> cs.backgroundColorSource
                    AnimationStyles.TYPE_LINE -> ls.backgroundColorSource
                    else -> bs.backgroundColorSource
                }
                "stroke" -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.strokeColorSource
                    AnimationStyles.TYPE_CAPSULE -> cs.strokeColorSource
                    AnimationStyles.TYPE_LINE -> ls.strokeColorSource
                    else -> bs.strokeColorSource
                }
                else -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.iconColorSource
                    AnimationStyles.TYPE_CAPSULE -> cs.iconColorSource
                    AnimationStyles.TYPE_LINE -> ls.iconColorSource
                    else -> bs.iconColorSource
                }
            },
            when (target) {
                "bg" -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.backgroundColorThemeKey
                    AnimationStyles.TYPE_CAPSULE -> cs.backgroundColorThemeKey
                    AnimationStyles.TYPE_LINE -> ls.backgroundColorThemeKey
                    else -> bs.backgroundColorThemeKey
                }
                "stroke" -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.strokeColorThemeKey
                    AnimationStyles.TYPE_CAPSULE -> cs.strokeColorThemeKey
                    AnimationStyles.TYPE_LINE -> ls.strokeColorThemeKey
                    else -> bs.strokeColorThemeKey
                }
                else -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.iconColorThemeKey
                    AnimationStyles.TYPE_CAPSULE -> cs.iconColorThemeKey
                    AnimationStyles.TYPE_LINE -> ls.iconColorThemeKey
                    else -> bs.iconColorThemeKey
                }
            },
            when (target) {
                "bg" -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.backgroundColor
                    AnimationStyles.TYPE_CAPSULE -> cs.backgroundColor
                    AnimationStyles.TYPE_LINE -> ls.backgroundColor
                    else -> bs.backgroundColor
                }
                "stroke" -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.strokeColor
                    AnimationStyles.TYPE_CAPSULE -> cs.strokeColor
                    AnimationStyles.TYPE_LINE -> ls.strokeColor
                    else -> bs.strokeColor
                }
                else -> when (t) {
                    AnimationStyles.TYPE_WAVE -> ws.iconColor
                    AnimationStyles.TYPE_CAPSULE -> cs.iconColor
                    AnimationStyles.TYPE_LINE -> ls.iconColor
                    else -> bs.iconColor
                }
            },
        )
        ColorPickerBottomSheet(
            onDismissRequest = onDismiss,
            onColorSelected = { selection ->
                when (selection) {
                    is ColorSelection.Custom -> dispatchColorCustom(vm, t, target, selection.color.toArgb())
                    is ColorSelection.Theme -> dispatchColorThemeKey(vm, t, target, selection.key)
                }
                onDismiss()
            },
            initialColor = initial,
        )
    }
}
