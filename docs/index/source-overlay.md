# SideLeap 源码索引 - overlay/

`overlay/` 只负责快捷启动器浮窗表现。

## 当前职责

- 浮窗实现：`overlay/QuickAppLauncherOverlay.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `overlay/QuickAppLauncherOverlay.kt` | 快捷启动器浮窗 |

## 关键入口

- `QuickAppLauncherOverlay` 是当前唯一浮窗入口。

## 依赖边界

- `overlay` 可以依赖 `settings`、`launcher`、`system`。
- `overlay` 不负责配置持久化，也不维护自己的应用列表。
