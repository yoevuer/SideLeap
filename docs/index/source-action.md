# SideLeap 源码索引 - action/

`action/` 承载动作定义、展示元信息、payload、执行分发和 handler 边界。

## 当前职责

- 动作核心模型：`action/Action.kt`、`action/ActionPayload.kt`、`action/ActionExecutionResult.kt`
- 动作定义与分类：`action/definition/*`
- 动作分发：`action/ActionRegistry.kt`、`action/ActionHandler.kt`、`action/ActionHandlerContext.kt`
- 动作处理器：`action/handlers/*`
- 动作展示：`action/display/*`
- 位置移动数据：`action/MoveScreenData.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `action/definition/*` | 动作目录、分类、配置类型 |
| `action/handlers/*` | 各动作执行器 |
| `action/display/*` | 动作图标与展示素材 |
| `action/ActionRegistry.kt` | 动作注册与分发 |
| `action/ActionPayload.kt` | 动作载荷 |

## 关键入口

- `ActionCatalog` 和 `ActionDefinition` 定义有哪些动作。
- `ActionRegistry` 将配置后的动作分发给对应 handler。
- `SystemActionHandler`、`AppLaunchActionHandler`、`FreezeAppsActionHandler`、`ShortcutActionHandler`、`NavigationActionHandler` 是当前主要执行器。

## 依赖边界

- `action` 可以依赖 `settings`、`launcher`、`freeze`、`system`。
- `action` 不负责手势识别，不直接读写配置，不让 UI 自己维护动作事实源。
