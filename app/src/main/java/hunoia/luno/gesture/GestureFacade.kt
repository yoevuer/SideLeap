package hunoia.luno.gesture

import androidx.compose.ui.geometry.Rect
import hunoia.luno.config.model.GestureButton

object GestureFacade {

    fun bounds(button: GestureButton, imePadding: Int = 0): Rect =
        button.bounds(imePadding)

    fun find(buttons: List<GestureButton>, offset: androidx.compose.ui.geometry.Offset, imePadding: Int = 0): GestureButton? =
        buttons.find(offset, imePadding)
}
