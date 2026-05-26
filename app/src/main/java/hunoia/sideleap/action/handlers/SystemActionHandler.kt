package hunoia.sideleap.action.handlers

import android.Manifest
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_POWER_DIALOG
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import hunoia.sideleap.R
import hunoia.sideleap.action.api.ActionExecutionResult
import hunoia.sideleap.action.api.ActionHandler
import hunoia.sideleap.action.api.ActionHandlerContext
import hunoia.sideleap.action.GlobalActions
import hunoia.sideleap.action.Action
import hunoia.sideleap.overlay.api.ElementPickerOverlay
import hunoia.sideleap.overlay.api.PickerCandidate
import hunoia.sideleap.settings.SettingsProvider
import hunoia.sideleap.settings.model.SavedFocusTarget
import hunoia.sideleap.system.intent.launchAssist
import hunoia.sideleap.system.intent.gotoAppDetailSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

object SystemActionHandler : ActionHandler {

    override val supportedActions = setOf(
        GlobalActions.POWER_BUTTON,
        GlobalActions.LOCK_SCREEN,
        GlobalActions.FLASHLIGHT,
        GlobalActions.SPLIT_SCREEN,
        GlobalActions.ASSIST_APP,
        GlobalActions.SCREENSHOT,
        GlobalActions.KEEP_SCREEN_ON,
        GlobalActions.HIDE_GESTURE_BUTTON,
        GlobalActions.FOCUS_INPUT,
    )

    override suspend fun handle(action: Action, context: ActionHandlerContext): ActionExecutionResult {
        when (action.value) {
            GlobalActions.POWER_BUTTON -> context.accessibilityService.performGlobalAction(GLOBAL_ACTION_POWER_DIALOG)
            GlobalActions.LOCK_SCREEN -> handleLockScreen(context)
            GlobalActions.FLASHLIGHT -> handleFlashlight(context)
            GlobalActions.SPLIT_SCREEN -> handleSplitScreen(context)
            GlobalActions.ASSIST_APP -> context.appContext.launchAssist()
            GlobalActions.SCREENSHOT -> handleScreenshot(context)
            GlobalActions.KEEP_SCREEN_ON -> context.toggleKeepScreenOn()
            GlobalActions.HIDE_GESTURE_BUTTON -> context.hideGestureButton(context.actionSettings.hideGestureButton.delayMs)
            GlobalActions.FOCUS_INPUT -> handleFocusInput(context)
            else -> return ActionExecutionResult.Ignored
        }
        return ActionExecutionResult.Success
    }

    private fun handleLockScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.accessibilityService.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            context.showVersionTooLowToast(R.string.action_lock_screen)
        }
    }

    private var flashlightOn = false

    private fun handleFlashlight(context: ActionHandlerContext) {
        try {
            val cameraManager = context.appContext.getSystemService(android.content.Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull()
            if (cameraId != null) {
                val block = {
                    context.scope.launch(Dispatchers.Default) {
                        flashlightOn = !flashlightOn
                        cameraManager.setTorchMode(cameraId, flashlightOn)
                    }
                }
                if (ContextCompat.checkSelfPermission(
                        context.appContext, Manifest.permission.CAMERA
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    block()
                } else {
                    context.showToast(context.appContext.getString(R.string.grant_camera_permission))
                    context.showToast(context.appContext.getString(R.string.goto_grant_camera_permission))
                    context.appContext.gotoAppDetailSettings()
                }
            } else {
                context.showToast(context.appContext.getString(R.string.flashlight_failed))
            }
        } catch (e: Exception) {
            context.showToast(context.appContext.getString(R.string.flashlight_failed))
        }
    }

    private fun handleSplitScreen(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.accessibilityService.performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        } else {
            context.showVersionTooLowToast(R.string.action_split_screen)
        }
    }

    private fun handleScreenshot(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.scope.launch {
                delay(200)
                context.accessibilityService.performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            }
        } else {
            context.showVersionTooLowToast(R.string.action_screenshot)
        }
    }

    private suspend fun handleFocusInput(context: ActionHandlerContext) {
        val pkg = context.currentPackageName() ?: ""
        if (pkg.isNotEmpty()) {
            val targets = SettingsProvider.getFocusTargets()
            val entries = targets[pkg].orEmpty()
            val best = entries.filter { !it.disabled }.maxByOrNull { it.timestamp }
            if (best != null) {
                val node = savedRootNode(context)?.let { root ->
                    findNodeByViewId(root, best.viewId).also { root.recycle() }
                }
                if (node != null) {
                    activateNode(context, node)
                    node.recycle()
                    return
                }
                SettingsProvider.updateFocusTargets { map ->
                    map + (pkg to entries.filter { it.viewId != best.viewId })
                }
            }
        }

        val autoNode = scanActivate(context)
        if (autoNode != null) {
            recordFocusTarget(context, pkg, autoNode)
            autoNode.recycle()
            return
        }

        val root = context.accessibilityService.rootInActiveWindow ?: return
        val clicked = findAndClickSearchNode(root)
        if (clicked) {
            delay(600)
            val clickNode = scanActivate(context)
            if (clickNode != null) {
                recordFocusTarget(context, pkg, clickNode)
                clickNode.recycle()
                return
            }
            val newRoot = context.accessibilityService.rootInActiveWindow
            if (newRoot != null) {
                showPickerPhase(context, newRoot, pkg)
                newRoot.recycle()
                return
            }
        }
        showPickerPhase(context, root, pkg)
        root.recycle()
    }

    private suspend fun recordFocusTarget(context: ActionHandlerContext, pkg: String, node: AccessibilityNodeInfo) {
        if (pkg.isEmpty()) return
        val viewId = node.viewIdResourceName ?: return
        if (viewId.isEmpty()) return
        SettingsProvider.updateFocusTargets { targets ->
            val list = targets[pkg].orEmpty().toMutableList()
            list.add(SavedFocusTarget(
                packageName = pkg,
                viewId = viewId,
                className = node.className?.toString() ?: "",
                appName = pkg,
                timestamp = System.currentTimeMillis(),
            ))
            targets + (pkg to list)
        }
    }

    private suspend fun showPickerPhase(context: ActionHandlerContext, root: AccessibilityNodeInfo, pkg: String) {
        val raw = mutableListOf<Triple<Rect, Int, String>>()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.isVisibleToUser && (isEditableNode(node) || node.isClickable)) {
                val r = Rect().also { node.getBoundsInScreen(it) }
                if (!r.isEmpty) {
                    raw.add(Triple(r, scoreSearchRelevance(node, root), node.viewIdResourceName ?: ""))
                }
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
            if (node !== root) node.recycle()
        }
        if (raw.isEmpty()) {
            context.showToast("No interactive controls found on screen")
            return
        }

        val candidates = raw.mapIndexed { i, (b, s, v) ->
            PickerCandidate(b, s, i, v)
        }

        val selected = withTimeoutOrNull(3_000) {
            suspendCancellableCoroutine<Int> { cont ->
                val overlay = ElementPickerOverlay(
                    context = context.appContext,
                    candidates = candidates,
                    onSelected = { cont.resume(it) },
                    onDismiss = { cont.resume(-1) },
                )
                overlay.show()
                cont.invokeOnCancellation { overlay.closeImmediately() }
            }
        }
        if (selected == null || selected < 0 || selected >= candidates.size) return
        val target = candidates[selected]

        val targetNode = if (target.viewId.isNotEmpty()) {
            findNodeByViewId(root, target.viewId)
        } else null

        if (targetNode != null) {
            recordFocusTarget(context, pkg, targetNode)
            activateNode(context, targetNode)
            targetNode.recycle()
        }
    }

    private suspend fun scanActivate(context: ActionHandlerContext): AccessibilityNodeInfo? {
        for (win in context.accessibilityService.windows) {
            if (win.type != AccessibilityWindowInfo.TYPE_APPLICATION) continue
            val root = win.root ?: continue
            val node = findAndActivateEditable(context, root)
            if (node != null) return node
        }
        return null
    }

    private fun savedRootNode(context: ActionHandlerContext): AccessibilityNodeInfo? {
        return context.accessibilityService.rootInActiveWindow
    }

    private fun findNodeByViewId(root: AccessibilityNodeInfo, viewId: String): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (viewId == node.viewIdResourceName && node.isVisibleToUser) {
                queue.forEach { if (it !== root) it.recycle() }
                return node
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
            if (node !== root) node.recycle()
        }
        return null
    }

    private val hintWords = setOf("搜索", "search", "查找", "搜寻")

    private suspend fun findAndActivateEditable(context: ActionHandlerContext, root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val candidates = mutableListOf<AccessibilityNodeInfo>()
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && isEditableNode(focused)) {
            candidates.add(focused)
        }
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node !== focused && isEditableNode(node) && node.isVisibleToUser) {
                candidates.add(node)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
            if (node !== root) node.recycle()
        }
        val best = candidates
            .filter { scoreSearchRelevance(it, root) >= 5 }
            .maxByOrNull { scoreSearchRelevance(it, root) }
        if (best != null) {
            activateNode(context, best, alreadyFocused = best === focused && focused != null)
            return best
        }
        return null
    }

    private fun scoreSearchRelevance(node: AccessibilityNodeInfo, root: AccessibilityNodeInfo): Int {
        var score = 0
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (viewId.contains("search")) score += 25
        val className = node.className?.toString().orEmpty()
        val hint = node.hintText?.toString()?.lowercase() ?: ""
        if (hint.isNotEmpty() && hintWords.any { hint.contains(it) }) score += 20
        if (node.isShowingHintText) score += 10
        val cd = node.contentDescription?.toString()?.lowercase() ?: ""
        if (cd.isNotEmpty() && hintWords.any { cd.contains(it) }) score += 15
        val isRealInput = node.isEditable || className.endsWith("EditText") ||
            node.actionList.any { it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT.id } ||
            (hint.isNotEmpty() && hintWords.any { hint.contains(it) })
        if (!isRealInput && node.isClickable && cd.isNotEmpty() && hintWords.any { cd.contains(it) }) score -= 15
        val rootRect = android.graphics.Rect()
        root.getBoundsInScreen(rootRect)
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        if (rootRect.height() > 0) {
            if (rect.top < rootRect.height() * 0.3f) score += 8
            if (rect.top > rootRect.height() * 0.6f) score -= 15
        }
        if (node.isClickable) score += 5
        if (!node.text.isNullOrEmpty()) score += 3
        return score
    }

    private fun findAndClickSearchNode(root: AccessibilityNodeInfo): Boolean {
        val rootRect = android.graphics.Rect()
        root.getBoundsInScreen(rootRect)

        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            val cd = node.contentDescription?.toString()?.lowercase() ?: ""
            val hint = node.hintText?.toString()?.lowercase() ?: ""
            val viewId = node.viewIdResourceName?.lowercase() ?: ""
            val isSearch = hintWords.any { cd.contains(it) } || hintWords.any { hint.contains(it) } || viewId.contains("search")
            if (isSearch) {
                if (node.isClickable) return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                val parent = node.parent
                if (parent != null && parent.isClickable) {
                    val result = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    parent.recycle()
                    return result
                }
                parent?.recycle()
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
            if (node !== root) node.recycle()
        }

        val h = rootRect.height()
        val w = rootRect.width()
        if (h <= 0 || w <= 0) return false

        val queue2 = ArrayDeque<AccessibilityNodeInfo>()
        queue2.add(root)
        while (queue2.isNotEmpty()) {
            val node = queue2.removeFirst()
            val rect = android.graphics.Rect()
            node.getBoundsInScreen(rect)
            if (node.isClickable && node.isVisibleToUser &&
                rect.top < h * 0.20f &&
                rect.width() > w * 0.20f &&
                rect.height() > h * 0.03f
            ) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue2.add(it) }
            }
            if (node !== root) node.recycle()
        }

        val queue3 = ArrayDeque<AccessibilityNodeInfo>()
        queue3.add(root)
        while (queue3.isNotEmpty()) {
            val node = queue3.removeFirst()
            val cn = node.className?.toString()?.lowercase() ?: ""
            val rect = android.graphics.Rect()
            node.getBoundsInScreen(rect)
            if (node.isClickable && node.isVisibleToUser &&
                rect.top < h * 0.15f &&
                (cn.contains("imagebutton") || cn.contains("imageview"))
            ) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue3.add(it) }
            }
            if (node !== root) node.recycle()
        }

        return false
    }

    private suspend fun activateNode(context: ActionHandlerContext, node: AccessibilityNodeInfo, alreadyFocused: Boolean = false) {
        if (alreadyFocused || hasText(node)) {
            node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            delay(50)
            cursorToEnd(node)
            softShowIme(context)
            return
        }
        if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            delay(50)
            cursorToEnd(node)
            softShowIme(context)
            return
        }
        val parent = node.parent
        if (parent != null && parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            parent.recycle()
            delay(50)
            cursorToEnd(node)
            softShowIme(context)
            return
        }
        parent?.recycle()
        gestureTapCenter(context, node)
        softShowIme(context)
    }

    private fun gestureTapCenter(context: ActionHandlerContext, node: AccessibilityNodeInfo) {
        val rect = android.graphics.Rect()
        node.getBoundsInScreen(rect)
        if (rect.isEmpty) return
        val cx = (rect.left + rect.right) / 2f
        val cy = (rect.top + rect.bottom) / 2f
        val path = android.graphics.Path()
        path.moveTo(cx, cy)
        val stroke = android.accessibilityservice.GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = android.accessibilityservice.GestureDescription.Builder()
            .addStroke(stroke)
            .build()
        context.accessibilityService.dispatchGesture(gesture, null, null)
    }

    private fun hasText(node: AccessibilityNodeInfo): Boolean {
        if (!node.text.isNullOrEmpty()) return true
        for (i in 0 until node.childCount.coerceAtMost(5)) {
            val child = node.getChild(i) ?: continue
            val r = !child.text.isNullOrEmpty()
            child.recycle()
            if (r) return true
        }
        return false
    }

    private fun cursorToEnd(node: AccessibilityNodeInfo) {
        val len = node.text?.length ?: 0
        val pos = if (len > 0) len else Int.MAX_VALUE
        val args = android.os.Bundle()
        args.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT, pos)
        args.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT, pos)
        node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, args)
    }

    private fun softShowIme(context: ActionHandlerContext) {
        if (Build.VERSION.SDK_INT >= 35) {
            context.accessibilityService.performGlobalAction(0x00000010)
        } else {
            val imm = context.appContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }

    private fun isEditableNode(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false
        val className = node.className?.toString().orEmpty()
        if (node.isEditable) return true
        if (className.endsWith("EditText")) return true
        if (node.actionList.any { it.id == AccessibilityNodeInfo.AccessibilityAction.ACTION_SET_TEXT.id }) return true
        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        if (viewId.contains("search") && node.isClickable) return true
        val hint = node.hintText?.toString()?.lowercase() ?: ""
        if (hint.isNotEmpty() && hintWords.any { hint.contains(it) }) return true
        val cd = node.contentDescription?.toString()?.lowercase() ?: ""
        return cd.isNotEmpty() && hintWords.any { cd.contains(it) }
    }
}
