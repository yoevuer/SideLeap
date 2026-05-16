# 运行时流程

## 手势触发动作

标准流程：

```text
SideGestureService
  -> service.runtime.GestureRuntime
  -> gesture.api.GestureRecognizer
  -> gesture.api.GestureTrigger
  -> settings.api.ActionBindingSettingsProvider
  -> action.api.ActionRequest
  -> action.api.ActionDispatcher
  -> ActionHandler
  -> launcher/freeze/system/overlay 等领域 API
  -> ActionResult
```

规则：

- `gesture` 只负责识别触发。
- `settings` 只负责提供绑定配置。
- `action` 只负责动作请求和分发。
- 具体业务能力由对应领域执行。
- `service` 负责把这些步骤串起来。

## 用户修改配置

标准流程：

```text
ui
  -> settings.api
  -> settings.internal validation
  -> settings.data persistence
  -> settings.api typed Flow
  -> service / overlay / gesture / action runtime
```

规则：

- UI 不直接写 DataStore。
- UI 不直接修改运行时对象内部状态。
- 配置变更通过 `settings.api` 生效。
- 运行时组件监听细粒度配置流或由 `service` 统一更新。

## 启动应用动作

标准流程：

```text
GestureTrigger
  -> ActionRequest(actionId = LaunchApp)
  -> ActionDispatcher
  -> LaunchAppActionHandler
  -> launcher.api.LauncherGateway
  -> system.api.PackageGateway / IntentGateway
```

规则：

- `gesture` 不知道启动应用。
- `action` 不直接扫描应用列表。
- `launcher` 不知道手势来源。
- `system` 不知道这是哪个业务动作。

## 冻结动作

标准流程：

```text
GestureTrigger / UI
  -> ActionRequest(actionId = FreezeApp)
  -> ActionDispatcher
  -> FreezeActionHandler
  -> freeze.api.FreezeController
  -> system.api
```

规则：

- 冻结规则属于 `freeze`。
- 动作请求属于 `action`。
- 系统调用属于 `system`。
- 运行时组合属于 `service`。

## Service 运行时编排规则

`SideGestureService` 必须保持薄边界。

允许它处理：
- Android Service 生命周期
- 权限状态入口
- runtime 初始化
- runtime 销毁
- 输入事件转发
- 前台服务通知边界

不允许它长期沉淀：
- 手势算法
- 动作执行细节
- 配置解析细节
- 冻结策略
- 应用启动策略
- 浮窗业务规则

当 `SideGestureService` 中出现连续多段业务逻辑时，应优先拆入：
```text
service.runtime
gesture
action
overlay
settings
launcher
freeze
system
```
