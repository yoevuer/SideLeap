package hunoia.luno.config.model

import kotlinx.serialization.Serializable

@Serializable
enum class SnapBackType {
    SPRING, EASE_OUT, SNAP, ELASTIC, FLING
}

object SnapBackDefaults {
    const val SpringStiffness = 0.5f
    const val SpringDamping = 0.5f
    const val EaseOutDurationMs = 300
    const val ElasticCoefficient = 0.3f
    const val FlingDecay = 0.7f
}
