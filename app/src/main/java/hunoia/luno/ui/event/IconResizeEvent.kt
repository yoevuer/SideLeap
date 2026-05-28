package hunoia.luno.ui.event


data class IconResizeEvent(
    val scaleFactors: Map<String, Float>,
    val bgColors: Map<String, Int>
)
