# 硬性约束

## 配置规则

### 配置归属

配置持久化归 `settings`。业务领域不得直接保存自己的配置到 DataStore、SharedPreferences、Room 或 file。

### 配置模型

配置分为三类：

| 类型 | 说明 | 归属 |
|---|---|---|
| Persisted settings | 实际持久化 schema | `settings` |
| Runtime settings | 运行时消费的配置视图 | `settings.api` |
| UI state | 页面临时状态 | `ui` |

### 默认值

默认值统一由 `settings` 管理。禁止在多个领域散落默认值。
允许领域提供常量建议，但最终默认配置入口必须在 `settings` 中统一落地。

### 配置迁移

配置结构变化必须在 `settings` 中提供迁移逻辑。
迁移逻辑不得放在 `ui`、`service`、`gesture`、`action`、`overlay`。

### 配置访问规则

禁止其他领域直接访问：DataStore、SharedPreferences、Room、文件存储实现、`settings.data`、`settings.impl`。
其他领域只能通过 `settings.api` 访问配置。

推荐通过细粒度提供者访问：
```kotlin
GestureSettingsProvider
OverlaySettingsProvider
ActionSettingsProvider
FreezeSettingsProvider
LauncherSettingsProvider
```

避免：
```kotlin
GlobalSettingsRepository.getAllSettings()
SettingsManager.getBoolean("gesture_left_enabled")
```

完整配置对象只允许在 `settings` 内部、备份恢复、导入导出场景使用。

## Android API 使用规则

### 默认禁止直接调用 Android 系统能力

除以下场景外，业务领域不得直接调用 Android 系统能力：
- 纯模型不涉及 Android API。
- 必须处理 Android 类型的边界适配。
- 调用已经封装好的 `system.api`。

推荐：
```kotlin
system.api.ClipboardGateway.copyText(text)
```

避免：
```kotlin
context.getSystemService(ClipboardManager::class.java)
```

散落在 `gesture`、`action`、`freeze`、`launcher`、`overlay` 中。

### Android Context 使用规则

`Context` 不应随意下传。

允许持有 `Context` 的位置：
- `system` 实现类
- `service` Android Service 边界类
- `ui` Android/Compose 边界类

业务领域内部尽量不直接持有 `Context`。
如果必须使用，优先通过 `system.api` 封装为能力接口。

## UI 规则

`ui` 只负责用户界面和页面状态。

`ui` 可以：
- 展示配置项
- 发起配置修改
- 展示动作列表
- 展示应用列表
- 展示冻结列表
- 展示权限状态
- 发起用户操作
- 调用 `system.feedback`、`system.vibration`、`system.permission`、`system.intent`、`system.window` 这类 UI 级系统能力（toast、振动反馈、权限检查、打开系统设置、获取屏幕尺寸）

`ui` 不可以：
- 直接执行动作
- 直接冻结应用
- 直接启动应用
- 直接识别手势
- 直接管理浮窗窗口
- 直接访问 settings 持久化实现
- 调用 `system/shizuku`、`system/accessibility`、`system/packages` 等业务级系统能力

UI 页面所需的组合数据，应通过 ViewModel 从各领域 `api` 获取，然后转换为 UI state。

## Service 规则

参见 [runtime-flows.md](runtime-flows.md) 的 Service 编排规则部分。

## 命名规则

### 推荐后缀

| 后缀 | 用途 |
|---|---|
| `Gateway` | 对系统或外部能力的封装 |
| `Repository` | 数据读取和写入边界 |
| `Provider` | 只读配置或状态提供 |
| `Controller` | 领域内有状态控制入口 |
| `Dispatcher` | 分发请求 |
| `Handler` | 处理具体请求 |
| `Recognizer` | 识别输入并产生语义 |
| `Runtime` | service 运行时协作对象 |
| `State` | 状态模型 |
| `Request` | 请求模型 |
| `Result` | 结果模型 |

### 避免后缀

避免无语义名称：`Manager`、`Helper`、`Util`、`Common`、`Base`、`Wrapper`、`Processor`。
不是绝对禁止，但使用前必须能说明为什么更具体的名称不合适。

## 重构规则

### 发现跨领域直接调用

如果发现 `ui -> action.internal`、`gesture -> action.impl` 等，应重构为目标领域 api、service 编排、action handler 或 system gateway。

### 发现上帝类

如果某个类同时处理以下三类以上职责，应拆分：生命周期、配置读取、手势识别、动作执行、UI 状态、系统调用、持久化、业务规则。

### 发现重复默认值

默认值必须收敛到 `settings`。

### 发现 Android API 散落

优先提取到 `system.api` 和 `system.impl`。

## 禁止事项

```text
跨领域 import internal/impl/data
在 ui 中直接调用 Android 系统能力执行业务动作
在 gesture 中直接执行 action
在 action 中直接操作 UI 页面状态
在 service 中沉淀业务规则
在 system 中出现业务语义
在 core 中出现 SideLeap 业务概念
在多个领域重复定义同一配置默认值
通过字符串 key 到处读取配置
让一个 GlobalSettingsRepository 被所有领域任意读取
```

## 允许的临时例外

如果为了快速修复 bug 必须临时违反本规范，需要满足：
1. 修改范围足够小。
2. 不扩大现有错误依赖。
3. 在代码附近标注原因。
4. 后续创建明确的 refactor 任务。
5. 不把临时方案继续作为新功能基础。

临时例外不应进入长期架构。

### 当前已知待重构的违规

以下违规已被识别，但尚未修复。需要在后续重构中解决：

- `gesture/GestureActions.kt` → `action/`：手势方向到默认动作的映射系天然耦合，后续可以通过接口提取解耦。
- `settings/` → `gesture/`：配置模型引用了手势类型（`GestureButton`、`GestureAngles`）用于序列化。后续应通过值对象或 ID 引用解耦。
- `action/handlers/` 中 9 处 handler 分布在 `action/` 包下但依赖 `launcher/`、`freeze/`：应当将 handler 实现移到对应领域中。
- `service/SideGestureWindowController` → `ui/widget/GestureView`：窗口控制器直接引用具体 widget，应通过接口抽象。

## 架构检查清单

提交前检查：

```text
新增代码是否有明确领域归属？
是否跨领域 import 了 internal/impl/data？
是否把 Android API 放进了业务领域？
是否把业务语义放进了 system/core？
是否让 service 变胖？
是否让 settings 暴露了过大的全局配置对象？
是否让 gesture 直接执行了 action？
是否让 action 直接控制了 UI？
是否复用了已有 api，而不是调用实现类？
是否有重复默认值？
```

如果任一项为"是"，优先调整结构后再提交。

## 推荐演进顺序

```text
1. 固化包结构和 api 边界
2. 收敛 settings 配置访问
3. 拆薄 SideGestureService
4. 整理 action dispatcher 和 handler
5. 收敛 Android API 到 system
6. 清理 core 中的业务语义
7. 清理 ui 对领域内部实现的依赖
```

每次重构只解决一个明确边界问题。
