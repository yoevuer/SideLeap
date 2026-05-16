# SideLeap 源码索引 - overlay/

`overlay/` 负责运行时浮窗表现。

## 当前职责

- 快捷启动器浮窗：`overlay/api/QuickAppLauncherOverlay.kt`
- 密码生成器浮窗：`overlay/api/PasswordGeneratorOverlay.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `overlay/api/QuickAppLauncherOverlay.kt` | 快捷启动器浮窗 |
| `overlay/api/PasswordGeneratorOverlay.kt` | 密码生成器浮窗 |

## 关键入口

- `QuickAppLauncherOverlay` 是快捷启动器浮窗入口。
- `PasswordGeneratorOverlay` 是密码生成器运行时面板入口。
- `QuickAppLauncherOverlayHost` 由入口层实现，负责提供 context、协程、settings、解冻请求和 Compose 内容装配。
- 锁屏关闭走立即关闭路径，普通关闭保留动画路径。

## 依赖边界

- `overlay` 可以依赖 `settings`、`launcher`、`system`，不直接依赖具体 service 或 UI 组件实现。
- `overlay` 不负责配置持久化，也不维护自己的应用列表。
