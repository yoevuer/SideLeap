# SideLeap 源码索引 - action/

`action/` 承载动作定义、展示元信息、payload、执行分发和 handler 边界。

## 当前职责

- 动作核心模型：`action/Action.kt`、`action/ActionPayload.kt`、`action/ActionRuntimeInfo.kt`、`action/ActionExecutionResult.kt`
- 动作面板长按绑定：`Action.longPressAction` 可为单个面板动作保存长按动作，未设置时运行时回退短按动作
- 动作定义与分类：`action/definition/*`
- 动作常量：`action/GlobalActions.kt`
- 动作分发：`action/ActionRegistry.kt`、`action/ActionHandler.kt`、`action/ActionHandlerContext.kt`
- 动作处理器：`action/handlers/*`
- 动作展示：`action/display/*`
- 密码生成：`action/api/PasswordGenerator.kt`
- 位置移动数据：`action/MoveScreenData.kt`
- 运行时触摸坐标：`action/ActionRuntimeInfo.kt`，用于「模拟点击当前位置」等不持久化的动作执行上下文

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `action/definition/*` | 动作目录、分类、配置类型 |
| `action/handlers/*` | 各动作执行器 |
| `action/display/*` | 动作图标与展示素材 |
| `action/api/PasswordGenerator.kt` | 密码生成、配置修正与熵值计算 |
| `action/ActionRegistry.kt` | 动作注册与分发 |
| `action/ActionPayload.kt` | 动作载荷 |
| `action/ActionRuntimeInfo.kt` | 非持久化运行时动作上下文 |

## 关键入口

- `ActionCatalog` 和 `ActionDefinition` 定义有哪些动作。
- `Action.longPressAction` 用于动作面板内的短按/长按分流；执行时仍通过同一个 `ActionRegistry` 分发。
- `ActionRuntimeInfo` 通过 `Action.extra` 临时传递触发类型和触摸坐标，不进入配置持久化。
- `ActionRegistry` 将配置后的动作分发给对应 handler。
- `SystemActionHandler`、`AppLaunchActionHandler`、`FreezeAppsActionHandler`、`ShortcutActionHandler`、`NavigationActionHandler` 是当前主要执行器。
- `PasswordGeneratorActionHandler` 处理生成密码复制和打开密码生成器浮层动作。
- `ActionHandlerContext` 由 `service/SideGestureServiceProxyActionCoordinator.kt` 统一构造。
- `ActionHandlerContext` 只暴露 action 执行所需的能力、settings 快照和回调，不依赖 `SideGestureService` 具体类型。

## 依赖边界

- `action` 可以依赖 `settings`、`launcher`、`freeze`、`system`。
- `action` 不负责手势识别，不直接读写配置，不让 UI 自己维护动作事实源。
