package hunoia.luno.ui.screen.home

import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aaron.compose.component.UDFComponent
import hunoia.luno.config.model.AnimationStyles
import hunoia.luno.config.model.ColorSource
import hunoia.luno.ui.component.MyColumn

@Composable
fun AnimationStyleContent(
    onDismiss: () -> Unit,
    vm: AnimationStyleVM = viewModel()
) {
    val scrollState = rememberScrollState()
    UDFComponent(component = vm.udfComponent, onEvent = { }) { uiState ->
        var colorPickerTarget by remember { mutableStateOf<String?>(null) }
        ColorPickerDialog(
            target = colorPickerTarget,
            state = uiState,
            vm = vm,
            onDismiss = { colorPickerTarget = null }
        )
        val type = uiState.currentType

        val waveStyle = uiState.waveStyle
        val capsuleStyle = uiState.capsuleStyle
        val bubbleStyle = uiState.bubbleStyle
        val lineStyle = uiState.lineStyle

        var localWaveStrokeWidth by remember(waveStyle.strokeWidth) { mutableStateOf(waveStyle.strokeWidth.toFloat()) }
        var localWaveWidth by remember(waveStyle.width) { mutableStateOf(waveStyle.width.toFloat()) }
        var localCapsuleStrokeWidth by remember(capsuleStyle.strokeWidth) { mutableStateOf(capsuleStyle.strokeWidth.toFloat()) }
        var localCapsuleThickness by remember(capsuleStyle.thickness) { mutableStateOf(capsuleStyle.thickness.toFloat()) }
        var localCapsuleMaxLength by remember(capsuleStyle.maxLength) { mutableStateOf(capsuleStyle.maxLength.toFloat()) }
        var localCapsuleCornerRadius by remember(capsuleStyle.cornerRadius) { mutableStateOf(capsuleStyle.cornerRadius.toFloat()) }
        var localBubbleStrokeWidth by remember(bubbleStyle.strokeWidth) { mutableStateOf(bubbleStyle.strokeWidth.toFloat()) }
        var localBubbleDiameter by remember(bubbleStyle.diameter) { mutableStateOf(bubbleStyle.diameter.toFloat()) }
        var localBubbleMaxOffset by remember(bubbleStyle.maxOffset) { mutableStateOf(bubbleStyle.maxOffset.toFloat()) }
        var localLineStrokeWidth by remember(lineStyle.strokeWidth) { mutableStateOf(lineStyle.strokeWidth.toFloat()) }
        var localLineWidth by remember(lineStyle.width) { mutableStateOf(lineStyle.width.toFloat()) }
        var localLineMaxLength by remember(lineStyle.maxLength) { mutableStateOf(lineStyle.maxLength.toFloat()) }
        var localLineMaxOffset by remember(lineStyle.maxOffset) { mutableStateOf(lineStyle.maxOffset.toFloat()) }
        var localLineCornerRadius by remember(lineStyle.cornerRadius) { mutableStateOf(lineStyle.cornerRadius.toFloat()) }

        val currentSnapType = when (type) {
            AnimationStyles.TYPE_WAVE -> waveStyle.snapBackType
            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.snapBackType
            AnimationStyles.TYPE_BUBBLE -> bubbleStyle.snapBackType
            else -> lineStyle.snapBackType
        }
        val currentSnapStiffness = when (type) {
            AnimationStyles.TYPE_WAVE -> waveStyle.snapBackSpringStiffness
            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.snapBackSpringStiffness
            AnimationStyles.TYPE_BUBBLE -> bubbleStyle.snapBackSpringStiffness
            else -> lineStyle.snapBackSpringStiffness
        }
        val currentSnapDamping = when (type) {
            AnimationStyles.TYPE_WAVE -> waveStyle.snapBackSpringDamping
            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.snapBackSpringDamping
            AnimationStyles.TYPE_BUBBLE -> bubbleStyle.snapBackSpringDamping
            else -> lineStyle.snapBackSpringDamping
        }
        val currentSnapDuration = when (type) {
            AnimationStyles.TYPE_WAVE -> waveStyle.snapBackEaseOutDurationMs
            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.snapBackEaseOutDurationMs
            AnimationStyles.TYPE_BUBBLE -> bubbleStyle.snapBackEaseOutDurationMs
            else -> lineStyle.snapBackEaseOutDurationMs
        }
        val currentSnapElastic = when (type) {
            AnimationStyles.TYPE_WAVE -> waveStyle.snapBackElasticCoefficient
            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.snapBackElasticCoefficient
            AnimationStyles.TYPE_BUBBLE -> bubbleStyle.snapBackElasticCoefficient
            else -> lineStyle.snapBackElasticCoefficient
        }
        val currentSnapFling = when (type) {
            AnimationStyles.TYPE_WAVE -> waveStyle.snapBackFlingDecay
            AnimationStyles.TYPE_CAPSULE -> capsuleStyle.snapBackFlingDecay
            AnimationStyles.TYPE_BUBBLE -> bubbleStyle.snapBackFlingDecay
            else -> lineStyle.snapBackFlingDecay
        }

        var localSnapStiffness by remember(currentSnapStiffness) { mutableStateOf(currentSnapStiffness) }
        var localSnapDamping by remember(currentSnapDamping) { mutableStateOf(currentSnapDamping) }
        var localSnapDuration by remember(currentSnapDuration) { mutableStateOf(currentSnapDuration.toFloat()) }
        var localSnapElastic by remember(currentSnapElastic) { mutableStateOf(currentSnapElastic) }
        var localSnapFling by remember(currentSnapFling) { mutableStateOf(currentSnapFling) }

        MyColumn(scrollState = scrollState) {
            ColorSettingsSection(
                type = type,
                waveStyle = waveStyle,
                capsuleStyle = capsuleStyle,
                lineStyle = lineStyle,
                bubbleStyle = bubbleStyle,
                localWaveStrokeWidth = localWaveStrokeWidth,
                localCapsuleStrokeWidth = localCapsuleStrokeWidth,
                localLineStrokeWidth = localLineStrokeWidth,
                localBubbleStrokeWidth = localBubbleStrokeWidth,
                onWaveStrokeWidthChange = { localWaveStrokeWidth = it },
                onCapsuleStrokeWidthChange = { localCapsuleStrokeWidth = it },
                onLineStrokeWidthChange = { localLineStrokeWidth = it },
                onBubbleStrokeWidthChange = { localBubbleStrokeWidth = it },
                onWaveStrokeWidthFinished = { vm.onWaveStyleChange { it.copy(strokeWidth = localWaveStrokeWidth.toInt()) }; vm.saveWaveSettings() },
                onCapsuleStrokeWidthFinished = { vm.onCapsuleStyleChange { it.copy(strokeWidth = localCapsuleStrokeWidth.toInt()) }; vm.saveCapsuleSettings() },
                onLineStrokeWidthFinished = { vm.onLineStyleChange { it.copy(strokeWidth = localLineStrokeWidth.toInt()) }; vm.saveLineSettings() },
                onBubbleStrokeWidthFinished = { vm.onBubbleStyleChange { it.copy(strokeWidth = localBubbleStrokeWidth.toInt()) }; vm.saveBubbleSettings() },
                onBackgroundColorClick = { colorPickerTarget = "bg" },
                onStrokeColorClick = { colorPickerTarget = "stroke" },
                onBackgroundColorSourceChange = { checked ->
                    dispatchColorSource(vm, type, "bg", if (checked) ColorSource.Theme else ColorSource.Custom)
                },
                onStrokeColorSourceChange = { checked ->
                    dispatchColorSource(vm, type, "stroke", if (checked) ColorSource.Theme else ColorSource.Custom)
                },
            )

            ShapeStyleSection(
                type = type,
                vm = vm,
            )

            ShapeSizeSection(
                type = type,
                vm = vm,
                waveStyle = waveStyle,
                localWaveWidth = localWaveWidth,
                onWaveWidthChange = { localWaveWidth = it },
                onWaveWidthFinished = { vm.onWaveStyleChange { it.copy(width = localWaveWidth.toInt()) }; vm.saveWaveSettings() },
                localCapsuleThickness = localCapsuleThickness,
                onCapsuleThicknessChange = { localCapsuleThickness = it },
                onCapsuleThicknessFinished = { vm.onCapsuleStyleChange { it.copy(thickness = localCapsuleThickness.toInt()) }; vm.saveCapsuleSettings() },
                localCapsuleMaxLength = localCapsuleMaxLength,
                onCapsuleMaxLengthChange = { localCapsuleMaxLength = it },
                onCapsuleMaxLengthFinished = { vm.onCapsuleStyleChange { it.copy(maxLength = localCapsuleMaxLength.toInt()) }; vm.saveCapsuleSettings() },
                localCapsuleCornerRadius = localCapsuleCornerRadius,
                onCapsuleCornerRadiusChange = { localCapsuleCornerRadius = it },
                onCapsuleCornerRadiusFinished = { vm.onCapsuleStyleChange { it.copy(cornerRadius = localCapsuleCornerRadius.toInt()) }; vm.saveCapsuleSettings() },
                localLineWidth = localLineWidth,
                onLineWidthChange = { localLineWidth = it },
                onLineWidthFinished = { vm.onLineStyleChange { it.copy(width = localLineWidth.toInt()) }; vm.saveLineSettings() },
                localLineMaxLength = localLineMaxLength,
                onLineMaxLengthChange = { localLineMaxLength = it },
                onLineMaxLengthFinished = { vm.onLineStyleChange { it.copy(maxLength = localLineMaxLength.toInt()) }; vm.saveLineSettings() },
                localLineMaxOffset = localLineMaxOffset,
                onLineMaxOffsetChange = { localLineMaxOffset = it },
                onLineMaxOffsetFinished = { vm.onLineStyleChange { it.copy(maxOffset = localLineMaxOffset.toInt()) }; vm.saveLineSettings() },
                localLineCornerRadius = localLineCornerRadius,
                onLineCornerRadiusChange = { localLineCornerRadius = it },
                onLineCornerRadiusFinished = { vm.onLineStyleChange { it.copy(cornerRadius = localLineCornerRadius.toInt()) }; vm.saveLineSettings() },
                localBubbleDiameter = localBubbleDiameter,
                onBubbleDiameterChange = { localBubbleDiameter = it },
                onBubbleDiameterFinished = { vm.onBubbleStyleChange { it.copy(diameter = localBubbleDiameter.toInt()) }; vm.saveBubbleSettings() },
                localBubbleMaxOffset = localBubbleMaxOffset,
                onBubbleMaxOffsetChange = { localBubbleMaxOffset = it },
                onBubbleMaxOffsetFinished = { vm.onBubbleStyleChange { it.copy(maxOffset = localBubbleMaxOffset.toInt()) }; vm.saveBubbleSettings() },
            )

            SnapBackSection(
                currentType = currentSnapType,
                springStiffness = localSnapStiffness,
                springDamping = localSnapDamping,
                easeOutDurationMs = localSnapDuration.toInt(),
                elasticCoefficient = localSnapElastic,
                flingDecay = localSnapFling,
                onTypeChange = { newType ->
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> vm.onWaveStyleChange { it.copy(snapBackType = newType) }
                        AnimationStyles.TYPE_CAPSULE -> vm.onCapsuleStyleChange { it.copy(snapBackType = newType) }
                        AnimationStyles.TYPE_BUBBLE -> vm.onBubbleStyleChange { it.copy(snapBackType = newType) }
                        AnimationStyles.TYPE_LINE -> vm.onLineStyleChange { it.copy(snapBackType = newType) }
                    }
                    vm.saveCurrentStyle(type)
                },
                onSpringStiffnessChange = { localSnapStiffness = it },
                onSpringDampingChange = { localSnapDamping = it },
                onEaseOutDurationChange = { localSnapDuration = it.toFloat() },
                onElasticCoefficientChange = { localSnapElastic = it },
                onFlingDecayChange = { localSnapFling = it },
            )

            IconSection(
                iconColor = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconColor
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColor
                    AnimationStyles.TYPE_LINE -> lineStyle.iconColor
                    else -> bubbleStyle.iconColor
                },
                iconColorSource = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconColorSource
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColorSource
                    AnimationStyles.TYPE_LINE -> lineStyle.iconColorSource
                    else -> bubbleStyle.iconColorSource
                },
                iconColorThemeKey = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconColorThemeKey
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColorThemeKey
                    AnimationStyles.TYPE_LINE -> lineStyle.iconColorThemeKey
                    else -> bubbleStyle.iconColorThemeKey
                },
                iconScale = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconScale
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconScale
                    AnimationStyles.TYPE_LINE -> lineStyle.iconScale
                    else -> bubbleStyle.iconScale
                },
                iconType = when (type) {
                    AnimationStyles.TYPE_WAVE -> waveStyle.iconType
                    AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconType
                    AnimationStyles.TYPE_LINE -> lineStyle.iconType
                    else -> bubbleStyle.iconType
                },
                isCustomIconExpanded = when (type) {
                    AnimationStyles.TYPE_WAVE -> uiState.waveCustomIconExpanded
                    AnimationStyles.TYPE_CAPSULE -> uiState.capsuleCustomIconExpanded
                    else -> uiState.bubbleCustomIconExpanded
                },
                onColorClick = {
                    val iconSource = when (type) {
                        AnimationStyles.TYPE_WAVE -> waveStyle.iconColorSource
                        AnimationStyles.TYPE_CAPSULE -> capsuleStyle.iconColorSource
                        AnimationStyles.TYPE_LINE -> lineStyle.iconColorSource
                    else -> bubbleStyle.iconColorSource
                    }
                    colorPickerTarget = "icon"
                },
                onColorSourceChange = { checked ->
                    val source = if (checked) ColorSource.Theme else ColorSource.Custom
                    dispatchColorSource(vm, type, "icon", source)
                },
                onIconScaleChange = { scale ->
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> vm.onWaveStyleChange { it.copy(iconScale = scale) }
                        AnimationStyles.TYPE_CAPSULE -> vm.onCapsuleStyleChange { it.copy(iconScale = scale) }
                        AnimationStyles.TYPE_LINE -> vm.onLineStyleChange { it.copy(iconScale = scale) }
                        else -> vm.onBubbleStyleChange { it.copy(iconScale = scale) }
                    }
                },
                onIconScaleChangeFinished = {
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> vm.saveWaveSettings()
                        AnimationStyles.TYPE_CAPSULE -> vm.saveCapsuleSettings()
                        AnimationStyles.TYPE_LINE -> vm.saveLineSettings()
                        else -> vm.saveBubbleSettings()
                    }
                },
                onIconTypeChange = { iconType ->
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> { vm.onWaveStyleChange { it.copy(iconType = iconType) }; vm.saveWaveSettings() }
                        AnimationStyles.TYPE_CAPSULE -> { vm.onCapsuleStyleChange { it.copy(iconType = iconType) }; vm.saveCapsuleSettings() }
                        AnimationStyles.TYPE_LINE -> { vm.onLineStyleChange { it.copy(iconType = iconType) }; vm.saveLineSettings() }
                        else -> { vm.onBubbleStyleChange { it.copy(iconType = iconType) }; vm.saveBubbleSettings() }
                    }
                },
                onCustomIconExpandedChange = { expanded ->
                    when (type) {
                        AnimationStyles.TYPE_WAVE -> vm.onWaveCustomIconExpandedChange(expanded)
                        AnimationStyles.TYPE_CAPSULE -> vm.onCapsuleCustomIconExpandedChange(expanded)
                        AnimationStyles.TYPE_LINE -> vm.onLineCustomIconExpandedChange(expanded)
                        else -> vm.onBubbleCustomIconExpandedChange(expanded)
                    }
                }
            )
        }
    }
}
