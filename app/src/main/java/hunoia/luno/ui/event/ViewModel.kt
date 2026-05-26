package hunoia.luno.ui.event

import androidx.lifecycle.ViewModel
import hunoia.luno.core.event.Events
import kotlin.reflect.KClass

/**
 * @author aaronzzxup@gmail.com
 * @since 2024/12/4
 */

fun <T : Any> ViewModel.subscribeEvent(eventClass: KClass<T>, block: (T) -> Unit) {
    addCloseable {
        Events.unsubscribe(eventClass, block)
    }
    Events.subscribe(eventClass, block)
}
