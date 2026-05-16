# 依赖方向

## 层级

```text
入口层
  ui
  service

业务领域层
  gesture
  action
  settings
  launcher
  freeze
  overlay

系统能力层
  system

基础设施层
  core
```

## 总体依赖方向

```text
ui
  -> *.api
  -> core

service
  -> *.api
  -> system.api
  -> core

business domain
  -> 允许依赖的 *.api
  -> system.api
  -> core

system
  -> core

core
  -> 无 SideLeap 业务依赖
```

## 禁止反向依赖

```text
core -> system
core -> business domain
core -> ui
core -> service

system -> gesture/action/settings/launcher/freeze/overlay
system -> ui
system -> service

business domain -> ui
business domain -> service

business domain -> 其他领域 internal/impl/data
```

## 允许依赖表

| 领域 | 可以依赖 | 禁止依赖 |
|---|---|---|
| `ui` | `gesture.api`, `action.api`, `settings.api`, `launcher.api`, `freeze.api`, `overlay.api`, `system.feedback`, `system.vibration`, `system.permission`, `system.intent`, `system.window`, `core` | 任何领域 internal/impl/data, system.impl (上述列出的除外), service 内部实现 |
| `service` | `gesture.api`, `action.api`, `settings.api`, `launcher.api`, `freeze.api`, `overlay.api`, `system.api`, `core` | — |
| `gesture` | `settings.api`, `system.api`, `core` | 执行动作、读取完整配置仓库、展示 UI、控制浮窗生命周期 |
| `action` | `settings.api`, `launcher`, `freeze`, `system.api`, `core` | 手势识别、UI 页面展示、浮窗生命周期、设置持久化 |
| `settings` | `system.api`, `core` | 执行业务动作、调用手势识别、控制浮窗实例、执行冻结、启动应用 |
| `launcher` | `settings.api`, `system.api`, `core` | 决定手势如何触发应用、决定动作绑定关系、管理冻结状态、展示完整 UI 页面 |
| `freeze` | `settings.api`, `launcher.api`, `system.api`, `core` | 手势识别、动作分发框架、应用启动、UI 页面导航、直接持久化全局配置 |
| `overlay` | `settings.api`, `gesture.api`, `action.api`, `launcher`, `system.api`, `core` | 常规设置页 UI、手势识别算法、动作业务实现、配置持久化、Service 总编排 |
| `system` | `core`, Android SDK | 手势业务、动作业务、冻结策略、默认配置、UI 页面状态、浮窗业务语义、运行时编排 |
| `core` | 无 SideLeap 业务依赖 | 手势语义、动作语义、配置项语义、冻结语义、启动应用语义、浮窗语义、Android 业务封装 |

## 跨领域调用规则

### 默认规则

业务领域之间默认不直接调用对方实现。
跨领域协作只能通过以下方式之一完成：

1. 依赖对方 `api`。
2. 由 `service` 在运行时编排。
3. 通过 `action.api` 定义的动作请求和动作处理器协作。
4. 通过 `settings.api` 暴露的细粒度配置视图协作。
5. 通过 `system.api` 调用 Android 能力。

### 稳定能力可以进 API

如果某个领域长期提供稳定能力，可以暴露到 `api`。

例如：
```text
launcher.api.LauncherGateway
freeze.api.FreezeController
overlay.api.OverlayController
settings.api.GestureSettingsProvider
```

### 一次性协作不要进领域依赖

如果只是某个运行流程中的一次性组合，不应让领域互相依赖。
应放在 `service` 编排层。

推荐：
```text
service
  -> gesture.api 识别手势
  -> settings.api 读取绑定配置
  -> action.api 分发动作
```

避免：
```text
gesture -> action
gesture -> overlay
overlay -> action.internal
```
