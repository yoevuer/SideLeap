# SideLeap 源码索引 - core/

`core/` 承载跨领域基础设施、应用入口类和少量真正通用的基础能力。

## 当前职责

- 应用与服务入口：`App.kt`、`MainActivity.kt`、`QuickAppLauncherActivity.kt`、`SideGestureService.kt`、`SideGestureServiceProxy.kt`
- 底层基础设施：`core/serialization/*`、`core/crash/*`、`core/diagnostics/*`、`core/event/*`
- 跨领域常量与默认值：`constant/*`、`defaults/*`、`event/*`
- 服务内部协作只保留在 `service/*`，不回塞进 `core`。

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `App.kt` | 应用入口 |
| `MainActivity.kt` | 主 Activity |
| `QuickAppLauncherActivity.kt` | 快捷启动器 Activity |
| `SideGestureService.kt` | 无障碍服务入口 |
| `SideGestureServiceProxy.kt` | 服务代理入口 |
| `core/serialization/JsonHelper.kt` | JSON 辅助 |
| `core/crash/CrashHandler.kt` | 崩溃处理 |
| `core/diagnostics/LauncherDiagnostics.kt` | 诊断辅助 |
| `core/event/Events.kt` | 事件总线基础 |

## 关键入口

- 上层运行入口集中在根包。
- `CrashHandler`、`JsonHelper`、`LauncherDiagnostics`、`Events` 是当前基础设施核心。

## 依赖边界

- `core` 不依赖 `action`、`gesture`、`settings`、`launcher`、`freeze`、`overlay`、`system` 这类业务领域。
- 其他领域可以依赖 `core`，但不要把业务工具重新塞回 `core`。
