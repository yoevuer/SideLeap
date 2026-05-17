# SideLeap 源码索引 - system/

`system/` 承载 Android 底层能力封装，是业务领域的能力提供者，不是业务决策层。

## 当前职责

- Accessibility：`system/accessibility/*`
- Intent：`system/intent/*`
- Permission：`system/permission/*`
- Window：`system/window/*`
- Shizuku：`system/shizuku/*`
- Vibration：`system/vibration/*`
- Toast / Compose toast：`system/feedback/*`
- Clipboard：`system/api/Clipboard.kt`
- Package / API 兼容封装：`system/packages/Compat.kt`
- Audio：`system/audio/Audio.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `system/accessibility/Accessibility.kt` | 无障碍能力封装 |
| `system/accessibility/Screenshot.kt` | 截图能力 |
| `system/intent/Intents.kt` | Intent 启动封装 |
| `system/permission/PermissionChecks.kt` | 权限状态判断 |
| `system/intent/KeepAliveHelper.kt` | 保活辅助 |
| `system/window/WindowManager.kt` | Window 能力 |
| `system/window/WindowLayout.kt` | 窗口布局 |
| `system/shizuku/*` | Shizuku 调用与服务入口 |
| `system/vibration/*` | 震动封装与效果定义 |
| `system/feedback/*` | Toast 封装 |
| `system/api/Clipboard.kt` | 敏感剪贴板写入封装 |
| `system/api/ShizukuBinderExecutor.kt` | Shizuku 绑定执行器（bind/call/unbind/超时/结果解析） |

## 关键入口

- `Accessibility`、`Intents`、`WindowManager`、`ShizukuCommand`、`ShizukuBinderExecutor`、`Vibrator` 是主要能力入口。
- `copySensitiveText()` 封装敏感剪贴板写入。
- `system` 只提供能力，不承载业务规则。
- `ContextSettings.kt` 已删除，Intent 跳转和权限判断已拆到 `system/intent` 与 `system/permission`。

## 依赖边界

- `system` 可以依赖 `core`。
- `system` 不依赖 `ui`、不依赖 `settings`，也不判断冻结名单或动作绑定。
