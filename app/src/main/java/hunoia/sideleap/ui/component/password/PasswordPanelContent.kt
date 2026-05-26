package hunoia.sideleap.ui.component.password

import android.content.Context
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import hunoia.sideleap.overlay.api.RuntimePanelScope
import hunoia.sideleap.system.copySensitiveText
import hunoia.sideleap.ui.theme.AnimOverlayFade
import hunoia.sideleap.ui.theme.AnimPanelShift
import hunoia.sideleap.ui.theme.AnimPostHideDelay
import hunoia.sideleap.ui.theme.ShapeExtraLarge
import hunoia.sideleap.ui.theme.SideGestureTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RuntimePanelScope.PasswordPanelContent(applicationContext: Context) {
    SideGestureTheme {
        val coroutineScope = rememberCoroutineScope()
        var panelVisible by remember { mutableStateOf(false) }
        var closing by remember { mutableStateOf(false) }
        val panelAlpha by animateFloatAsState(
            targetValue = if (panelVisible) 1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label = "passwordPanelAlpha"
        )
        val panelShiftY by animateFloatAsState(
            targetValue = if (panelVisible) 0f else 18f,
            animationSpec = spring(stiffness = Spring.StiffnessMedium),
            label = "passwordPanelShiftY"
        )
        val closeAnimated = {
            if (!closing) {
                closing = true
                panelVisible = false
                coroutineScope.launch {
                    delay(AnimPostHideDelay)
                    onCloseAnimated()
                }
            }
        }
        LaunchedEffect(Unit) {
            onRegisterCloseAnimated?.invoke(closeAnimated)
            panelVisible = true
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .graphicsLayer {
                    alpha = panelAlpha
                    translationY = panelShiftY
                }
                .onSizeChanged { size ->
                    updatePanelSize(size.width, size.height)
                },
            shape = RoundedCornerShape(ShapeExtraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            PasswordGeneratorPanel(
                onClose = closeAnimated,
                onCopyPassword = { password ->
                    copySensitiveText(
                        context = applicationContext,
                        label = "Generated Password",
                        text = password,
                    )
                }
            )
        }
    }
}
