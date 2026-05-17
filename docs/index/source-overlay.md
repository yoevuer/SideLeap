# SideLeap 源码索引 - overlay/

`overlay/` 负责运行时浮窗表现。

## 当前职责

- 快捷启动器浮窗：`overlay/api/QuickAppLauncherOverlay.kt`
- 通用运行时面板浮窗：`overlay/api/RuntimePanelOverlay.kt`
- 共享窗口管理工具：`overlay/api/WindowManagerUtils.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `overlay/api/QuickAppLauncherOverlay.kt` | 快捷启动器浮窗 |
| `overlay/api/RuntimePanelOverlay.kt` | 通用运行时面板浮窗 |
| `overlay/api/WindowManagerUtils.kt` | 共享 WindowManager 获取、ViewTree owner 设置、通用 overlay LayoutParams |

## 关键入口

- `QuickAppLauncherOverlay` 是快捷启动器浮窗入口。
- `RuntimePanelOverlay` 是工具类运行时面板入口，当前用于密码生成器面板。
- `QuickAppLauncherOverlayHost` 由入口层实现，负责提供 context、协程、settings、解冻请求。Compose 内容渲染已迁入 Overlay 内部，不再通过 Host 接口回调。
- 锁屏关闭走立即关闭路径，普通关闭保留动画路径。

## 依赖边界

- `overlay` 可以依赖 `settings`、`launcher`、`system`、`ui.theme`、`ui.widget.quickapplaunch`，不直接依赖具体 service 或 UI 组件实现。
- `overlay` 现在直接调用 `QuickAppLauncherContent` / `QuickAppLauncherAdjustPanel` Compose 函数，不再通过 Host 接口转发渲染。
