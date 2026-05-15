# SideLeap 源码索引 - overlay/

`overlay/` 只负责快捷启动器浮窗表现。

## 当前职责

- 浮窗实现：`overlay/QuickAppLauncherOverlay.kt`
- 浮窗宿主接口：`overlay/QuickAppLauncherOverlayHost`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `overlay/QuickAppLauncherOverlay.kt` | 快捷启动器浮窗 |

## 关键入口

- `QuickAppLauncherOverlay` 是当前唯一浮窗入口。
- `QuickAppLauncherOverlayHost` 由入口层实现，负责提供 context、协程、settings、解冻请求和 Compose 内容装配。
- 锁屏关闭走立即关闭路径，普通关闭保留动画路径。

## 依赖边界

- `overlay` 可以依赖 `settings`、`launcher`、`system`，不直接依赖具体 service 或 UI 组件实现。
- `overlay` 不负责配置持久化，也不维护自己的应用列表。
