# API 与包结构边界规则

## 包结构规则

每个领域应优先采用以下结构：

```text
<domain>/
  api/
  internal/
  model/
  data/
  impl/
```

各目录含义：

| 目录 | 含义 |
|---|---|
| `api` | 对外稳定入口，其他领域只允许依赖这里 |
| `internal` | 领域内部协作类，不允许跨领域直接使用 |
| `model` | 领域内部模型 |
| `data` | 数据来源、缓存、持久化适配 |
| `impl` | API 的默认实现 |

并非每个领域都必须拥有所有目录。小领域可以只保留 `api` 和 `internal`。

示例：
```text
action/
  api/
    ActionId
    ActionRequest
    ActionPayload
    ActionResult
    ActionDispatcher
    ActionHandler
  internal/
    DefaultActionDispatcher
    BuiltInActionRegistry
    ActionPayloadParser
```

外部允许：
```kotlin
import sideleap.action.api.ActionDispatcher
```

外部禁止：
```kotlin
import sideleap.action.internal.DefaultActionDispatcher
import sideleap.action.impl.DefaultActionDispatcher
```

## Public API 规则

### 领域对外只暴露 `api`

其他领域只能依赖目标领域的 `api` 包。

允许：
```text
ui -> action.api
service -> gesture.api
action integration -> launcher.api
```

禁止：
```text
ui -> action.internal
service -> gesture.impl
overlay -> settings.data
freeze -> system.impl
```

### `api` 中只放稳定语义

`api` 可以包含：
- 领域入口接口
- 请求模型
- 结果模型
- 稳定枚举
- 稳定值对象
- 对外事件
- 只读状态模型

`api` 不应包含：
- 具体实现类
- Android 细节实现
- DataStore / SharedPreferences 细节
- Compose 页面状态
- 临时工具函数
- 只服务单个页面的 UI model

### API 命名原则

API 命名应表达领域能力，而不是实现方式。

推荐：
```kotlin
ActionDispatcher
GestureRecognizer
GestureTrigger
FreezeController
LauncherGateway
OverlayController
SettingsRepository
```

避免：
```kotlin
ActionManagerImpl
GestureUtil
ServiceHelper
DataStoreHelper
CommonManager
```

## 动作集成规则

### 动作定义

动作定义归 `action` 管理。

动作定义应包含：
```text
ActionId
Action category
Payload schema
是否需要配置
是否需要权限
是否可作为手势动作
是否可作为浮窗动作
```

动作定义不应直接包含 Compose UI。

### 动作展示

动作展示分两层：

| 内容 | 归属 |
|---|---|
| 动作 ID、分类、参数 schema | `action` |
| 文案、图标映射、页面展示 | `ui` |
| 运行时执行 | `action` + 具体领域 handler |

### 动作处理器

动作处理器应实现 `action.api.ActionHandler`。

```kotlin
interface ActionHandler {
    val supportedActionIds: Set<ActionId>
    suspend fun handle(request: ActionRequest): ActionResult
}
```

业务领域可以提供自己的 handler：
```text
launcher -> LaunchAppActionHandler
freeze -> FreezeActionHandler
overlay -> OverlayActionHandler
system/action integration -> ClipboardActionHandler
```

`service` 或依赖注入装配层负责注册这些 handler。
