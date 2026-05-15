# SideLeap 源码索引 - service/

`service/` 承载 `SideGestureService` 的内部协作结构，不是独立业务领域。

## 当前职责

- Runtime bridge：`service/SideGestureRuntime.kt`
- Runtime snapshot：`service/SideGestureRuntimeState.kt`
- Overlay 生命周期协作：`service/SideGestureOverlayLifecycle.kt`
- 手势按钮刷新协作：`service/SideGestureButtonRefreshCoordinator.kt`
- Action 编排协作：`service/SideGestureServiceProxyActionCoordinator.kt`
- 服务代理协作：`service/SideGestureServiceProxy.kt`
- 窗口管理协作：`service/SideGestureWindowController.kt`
- 设置观察协作：`service/SideGestureSettingsObserver.kt`
- 壁纸变化监听：`service/WallpaperChangeObserver.kt`
- 锁屏状态监听：`service/ScreenLockObserver.kt`
- 锁屏音量键切歌：`service/LockScreenVolumeKeyHandler.kt`
- 软键盘 inset 监听：`service/ImeInsetObserver.kt`

## 关键入口

- `SideGestureService`（~260 行）是入口层，负责持有这些协作对象并转发生命周期与运行时请求。
- 协作对象只做编排，不承载独立业务规则。

## 依赖边界

- `service/` 可以依赖 `overlay`、`action`、`settings`、`system`。
- `service/` 不应反向成为 UI 或业务决策层。
