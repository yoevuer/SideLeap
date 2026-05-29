package hunoia.luno.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import hunoia.luno.config.model.AnimationStyles
import hunoia.luno.config.model.ColorSource
import hunoia.luno.config.model.ThemeColorKey
import hunoia.luno.ui.component.resolveColor

fun dispatchColorThemeKey(vm: AnimationStyleVM, type: Int, target: String, key: ThemeColorKey) {
    when (target) {
        "bg" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(backgroundColorThemeKey = key) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(backgroundColorThemeKey = key) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(backgroundColorThemeKey = key) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(backgroundColorThemeKey = key) }; vm.saveBubbleSettings() }
        }
        "stroke" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(strokeColorThemeKey = key) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(strokeColorThemeKey = key) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(strokeColorThemeKey = key) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(strokeColorThemeKey = key) }; vm.saveBubbleSettings() }
        }
        "icon" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(iconColorThemeKey = key) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(iconColorThemeKey = key) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(iconColorThemeKey = key) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(iconColorThemeKey = key) }; vm.saveBubbleSettings() }
        }
    }
}

fun dispatchColorCustom(vm: AnimationStyleVM, type: Int, target: String, argb: Int) {
    when (target) {
        "bg" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(backgroundColor = argb) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(backgroundColor = argb) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(backgroundColor = argb) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(backgroundColor = argb) }; vm.saveBubbleSettings() }
        }
        "stroke" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(strokeColor = argb) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(strokeColor = argb) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(strokeColor = argb) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(strokeColor = argb) }; vm.saveBubbleSettings() }
        }
        "icon" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(iconColor = argb) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(iconColor = argb) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(iconColor = argb) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(iconColor = argb) }; vm.saveBubbleSettings() }
        }
    }
}

fun dispatchColorSource(vm: AnimationStyleVM, type: Int, target: String, source: ColorSource) {
    when (target) {
        "bg" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(backgroundColorSource = source) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(backgroundColorSource = source) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(backgroundColorSource = source) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(backgroundColorSource = source) }; vm.saveBubbleSettings() }
        }
        "stroke" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(strokeColorSource = source) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(strokeColorSource = source) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(strokeColorSource = source) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(strokeColorSource = source) }; vm.saveBubbleSettings() }
        }
        "icon" -> when (type) {
            AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(iconColorSource = source) }; vm.saveWaveSettings() }
            AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(iconColorSource = source) }; vm.saveCapsuleSettings() }
            AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(iconColorSource = source) }; vm.saveLineSettings() }
            AnimationStyles.TYPE_BUBBLE -> { vm.onBubbleStyleChange { it.copy(iconColorSource = source) }; vm.saveBubbleSettings() }
        }
    }
}

@Composable
fun resolvePreviewColor(source: ColorSource, themeKey: ThemeColorKey, customColor: Int): Color = when (source) {
    ColorSource.Custom -> Color(customColor)
    ColorSource.Theme -> themeKey.resolveColor()
}
