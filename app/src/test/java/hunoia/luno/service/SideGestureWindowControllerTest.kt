package hunoia.luno.service

import android.view.Gravity
import android.view.WindowManager
import hunoia.luno.config.model.GestureButton
import hunoia.luno.config.model.GestureButtonBounds
import org.junit.Assert.assertEquals
import org.junit.Test

class SideGestureWindowControllerTest {

    @Test
    fun updateGestureButton_bounds_matchesExpectedLayout() {
        val layout = WindowManager.LayoutParams().apply {
            updateGestureButton(
                button = GestureButton(
                    id = "button",
                    bounds = GestureButtonBounds(
                        x = 0.25f,
                        y = 0.1f,
                        width = 0.5f,
                        height = 0.75f,
                    ),
                ),
                rootWidth = 1080,
                rootHeight = 2400,
            )
        }
        assertEquals(540, layout.width)
        assertEquals(1800, layout.height)
        assertEquals(270, layout.x)
        assertEquals(240, layout.y)
        assertEquals(Gravity.LEFT or Gravity.TOP, layout.gravity)
    }
}
