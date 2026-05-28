package hunoia.luno.ui.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import hunoia.luno.core.Events
import kotlin.reflect.KClass



@Composable
fun <T : Any> SubscribeEvent(eventClass: KClass<T>, subscriber: (T) -> Unit) {
    val curSubscriber by rememberUpdatedState(newValue = subscriber)
    val subscriberObj: Subscriber<T> = remember {
        Subscriber {
            curSubscriber(it)
        }
    }
    DisposableEffect(eventClass, subscriberObj) {
        Events.subscribe(eventClass, subscriberObj)
        onDispose {
            Events.unsubscribe(eventClass, subscriberObj)
        }
    }
}

private fun interface Subscriber<T> : (T) -> Unit
