# 领域职责

## `gesture`

手势模型、输入识别、触发语义。

**可以依赖：**

```text
settings.api
system.api    // 仅输入、坐标、触摸、无障碍事件适配需要时
core
```

**负责：**

- 手势输入模型
- 手势识别状态机
- 手势方向、距离、速度、边缘区域判断
- 手势触发语义
- 手势识别结果

**不负责：**

- 执行动作
- 读取完整配置仓库
- 展示 UI
- 控制浮窗生命周期
- 调用具体业务能力
- 决定某个动作具体做什么

**推荐模型：**

```kotlin
GestureInput
GestureState
GestureRecognizer
GestureTrigger
GestureResult
```

手势识别输出的是触发语义，而不是动作执行。

推荐：
```text
gesture -> GestureTrigger(edge = LEFT, direction = SWIPE_UP)
service -> 根据配置映射到 ActionRequest
action -> 执行动作
```

避免：
```text
gesture -> 直接 launch app
gesture -> 直接 freeze app
gesture -> 直接 show overlay
```

---

## `action`

动作定义、动作请求、payload、动作分发、动作结果。

**可以依赖：**

```text
settings.api
system.api    // 仅通用系统动作需要时
core
```

其他领域可以通过 `action.api` 提供动作处理器。

**负责：**

- 动作 ID
- 动作定义
- 动作 payload
- 动作请求
- 动作结果
- 动作分发
- 动作处理器接口
- 动作注册机制

**不负责：**

- 手势识别
- UI 页面展示
- 应用列表扫描
- 冻结名单策略
- 浮窗生命周期
- 设置持久化
- 直接持有所有业务领域实现

**推荐结构：**

```text
action/
  api/
    ActionId
    ActionDefinition
    ActionPayload
    ActionRequest
    ActionResult
    ActionDispatcher
    ActionHandler
  internal/
    DefaultActionDispatcher
    ActionHandlerRegistry
```

推荐动作执行模式：
```text
action.api 定义 ActionHandler
launcher/freeze/system 相关代码实现具体 handler
service 负责注册 handler
action dispatcher 只负责路由和分发
```

避免：
```text
action -> launcher.internal
action -> freeze.internal
action -> overlay.internal
```

---

## `settings`

配置模型、默认值、持久化、配置导入导出、备份恢复。

**可以依赖：**

```text
system.api    // 文件、备份、权限相关能力需要时
core
```

**负责：**

- 配置模型
- 默认值
- 配置校验
- 配置迁移
- 持久化
- 导入导出
- 备份恢复
- 细粒度配置 provider

**不负责：**

- 执行业务动作
- 调用手势识别
- 控制浮窗实例
- 执行冻结
- 启动应用
- 保存 UI 页面临时状态

---

## `launcher`

应用列表、Shortcut、图标、启动能力。

**可以依赖：**

```text
settings.api
system.api
core
```

**负责：**

- 已安装应用列表
- 应用信息
- 应用图标
- 应用启动
- Shortcut 查询
- Shortcut 启动
- 启动能力封装

**不负责：**

- 决定手势如何触发应用
- 决定动作绑定关系
- 管理冻结状态
- 展示完整 UI 页面
- 直接处理 Service 生命周期

**推荐入口：**

```kotlin
LauncherGateway
InstalledAppRepository
ShortcutRepository
```

---

## `freeze`

冻结业务、冻结状态、名单策略。

**可以依赖：**

```text
settings.api
launcher.api    // 仅需要应用元信息时
system.api
core
```

**负责：**

- 冻结状态
- 冻结名单
- 冻结策略
- 冻结/解冻操作
- 冻结相关动作处理器
- 冻结状态查询

**不负责：**

- 手势识别
- 动作分发框架
- 应用启动
- UI 页面导航
- 直接持久化全局配置

**推荐入口：**

```kotlin
FreezeController
FreezeStateRepository
FreezeRuleRepository
```

---

## `overlay`

浮窗表现、浮窗交互、浮窗生命周期。

**可以依赖：**

```text
settings.api
gesture.api     // 仅需要展示手势状态或区域时
action.api      // 仅浮窗触发动作时
system.api
core
```

**负责：**

- 浮窗显示
- 浮窗隐藏
- 浮窗位置
- 浮窗尺寸
- 浮窗交互
- 浮窗生命周期
- 浮窗运行时状态

**不负责：**

- 常规设置页 UI
- 手势识别算法
- 动作业务实现
- 配置持久化
- Service 总编排

`ui` 与 `overlay` 边界：
- `ui` 是常规 App 页面，`overlay` 是运行时浮窗界面。
- `ui` 可以通过 `overlay.api` 修改浮窗配置或请求预览。
- `ui` 不直接持有 overlay window 实例。
- `overlay` 不依赖 `ui` 页面状态。

---

## `service`

`SideGestureService` 内部运行时编排结构。

**可以依赖：**

```text
gesture.api
action.api
settings.api
launcher.api
freeze.api
overlay.api
system.api
core
```

**负责：**

- `SideGestureService` 生命周期拆分
- 运行时对象组装
- 手势输入到动作触发的流程编排
- 监听配置变化并更新运行时组件
- 协调 overlay、gesture、action 的生命周期
- 处理服务启动、销毁、重建

**不负责：**

- 定义手势识别算法
- 定义动作业务规则
- 直接读写 DataStore
- 直接实现冻结策略
- 直接实现应用启动策略
- 直接持有复杂 UI 状态

**推荐结构：**

```text
service/
  SideGestureService
  runtime/
    GestureRuntime
    ActionRuntime
    OverlayRuntime
    ServiceRuntimeController
  wiring/
    RuntimeGraph
```

---

## `system`

Android 底层能力封装。

**可以依赖：**

```text
core
Android SDK
```

**负责封装 Android 能力：**

- Accessibility
- PackageManager
- ShortcutManager
- WindowManager
- Clipboard
- Vibrator
- Intent
- PendingIntent
- Notification
- Permission
- Storage
- Backup 文件访问
- 系统版本差异

**不负责：**

- 手势业务
- 动作业务
- 冻结策略
- 默认配置
- UI 页面状态
- 浮窗业务语义
- 运行时编排

`system` 命名规则：
- 方法命名必须是系统能力语义，不是业务语义。

推荐：
```kotlin
ClipboardGateway.copyText(text)
PackageGateway.launchPackage(packageName)
```

避免：
```kotlin
SystemActionHelper.executeFreezeAction()
SystemGestureHelper.handleBackSwipe()
```

---

## `core`

跨领域基础设施。

**可以依赖：** 无 SideLeap 业务依赖。

**可以包含：**

- `Result`
- `Error`
- `Logger`
- `DispatcherProvider`
- `TimeProvider`
- `Id`
- 通用扩展函数
- 通用校验工具
- 通用 coroutine 工具

**不应包含：**

- 手势语义
- 动作语义
- 配置项语义
- 冻结语义
- 启动应用语义
- 浮窗语义
- Android 业务封装

如果某个工具依赖 Android SDK，优先放入 `system`，而不是 `core`。

---

## `ui`

页面、组件、导航、页面状态、用户配置入口。

**可以依赖：**

```text
gesture.api
action.api
settings.api
launcher.api
freeze.api
overlay.api
core
```

**禁止依赖：**

```text
任何领域 internal/impl/data
system.impl
service 内部实现
```

**负责：**

- 设置页面
- 列表页面
- 配置页面
- Compose 组件
- 导航
- 页面状态
- 用户输入校验
- UI 显示模型转换

**不负责：**

- 手势识别规则
- 动作执行规则
- 配置持久化细节
- Android 系统能力调用
- 浮窗运行时生命周期
