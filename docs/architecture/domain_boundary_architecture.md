# SideLeap 领域边界架构

## 1. 架构目标

SideLeap 采用单模块领域边界架构。

本架构不采用完整 Clean Architecture，不拆 Gradle module，不引入 Hilt / Dagger / Koin。项目仍保留在 `app` 模块内，通过领域分包、依赖方向和边界入口约束代码结构。

目标不是增加层数，而是解决以下问题：

- 动作、手势、配置、应用列表、冻结、浮窗等职责混杂。
- `entity/`、`utils/`、`ktx/` 承载过多无边界逻辑。
- UI、VM、Service、Handler、DataStore、系统 API 调用互相穿透。
- 设置页显示、配置保存、手势触发、动作执行容易不一致。
- 大范围重构风险过高，后续维护成本不可控。

最终架构应做到：

- 每个领域只负责自己的业务语义。
- 配置只有一个权威边界。
- 手势与动作解耦。
- Android 系统能力集中封装。
- 跨领域调用只走明确入口。
- 具体实现细节在迁移对应领域时再设计。

---

## 2. 顶层领域

项目顶层领域固定为：

```text
ui
gesture
action
settings
launcher
freeze
overlay
system
core
```

各领域含义：

| 领域 | 职责概述 |
|---|---|
| `ui` | 页面、组件、导航、用户交互 |
| `gesture` | 手势语义、槽位识别、手势配置语义 |
| `action` | 动作定义、动作绑定、动作执行 |
| `settings` | 配置、默认值、持久化、备份恢复 |
| `launcher` | 应用 / Activity / Shortcut / 图标 / 启动能力 |
| `freeze` | 冻结 / 解冻 / 一键名单 / 保护名单 |
| `overlay` | 快捷启动器浮窗表现 |
| `system` | Android API、Shizuku、Accessibility、Intent 等底层能力 |
| `core` | 跨领域基础设施 |

---

## 3. 各领域职责与非职责

### 3.1 ui

`ui` 负责：

- 页面展示。
- 用户输入。
- 导航。
- Dialog / Sheet / List / Button 等 Compose 组件。
- 调用 VM 暴露的状态和事件。

`ui` 不负责：

- 直接读写 DataStore。
- 直接调用 PackageManager。
- 直接调用 Shizuku。
- 直接执行 action。
- 直接维护动作事实源。
- 直接解析手势槽位到动作的底层映射。

VM 负责组合页面状态，订阅 settings selector / repository，调用 updater / repository，并处理一次性事件。

---

### 3.2 gesture

`gesture` 负责：

- 手势按钮语义。
- 手势方向语义。
- 短滑、长滑、长按等手势语义。
- 将用户操作识别为手势槽位。
- 提供手势配置相关语义。

`gesture` 不负责：

- 决定执行什么动作。
- 保存动作配置。
- 展示动作列表。
- 启动应用。
- 冻结应用。
- 处理动作执行。

手势域只回答一个问题：

```text
哪个手势槽位被触发？
```

---

### 3.3 action

`action` 负责：

- 动作定义。
- 动作展示元信息。
- 动作配置入口。
- 动作绑定表达。
- 动作执行分发。
- 动作 Handler 边界。

`action` 内部按三个子边界组织：

```text
definition   # 有哪些动作、动作标题、分类、是否需要配置
binding      # 某个手势槽位绑定了哪个动作；实际持久化归 settings
execution    # 动作执行分发与 Handler
```

`action` 不负责：

- 手势识别。
- 直接持久化配置。
- 直接管理应用列表。
- 直接管理冻结名单。
- 直接暴露 Android API 细节。
- 让 UI、ActionPanel、Service 各自维护一套动作事实源。

动作执行需要的能力应委托给对应领域：

```text
打开应用 / Activity / Shortcut -> launcher
冻结 / 解冻 -> freeze
系统动作 / Accessibility / Intent -> system
随机名称等纯动作 -> action 内部处理
```

---

### 3.4 settings

`settings` 是全局唯一配置边界。

`settings` 负责：

- 用户配置模型。
- 默认值。
- 配置读取。
- 配置更新。
- 配置持久化。
- 配置备份恢复。
- 配置派生状态。

`settings` 不负责：

- 手势识别。
- 动作执行。
- 应用列表查询。
- Shizuku 底层调用。
- 浮窗展示。
- Android API 业务封装。

其他领域不能私自持久化配置。UI、VM、Service、Handler、Overlay 不直接读写 DataStore。

其他领域只能通过 settings 暴露的读取结果、配置快照、selector 或 updater 使用配置。

---

### 3.5 launcher

`launcher` 负责可启动对象。

`launcher` 负责：

- 应用列表。
- Activity 列表。
- Shortcut 列表。
- 应用搜索。
- 图标加载。
- 应用 / Activity / URL / Shortcut 启动能力。

`launcher` 不负责：

- 冻结 / 解冻。
- 一键冻结名单。
- 保护名单。
- 快捷启动器配置保存。
- 浮窗展示。
- 动作绑定保存。

`launcher` 的边界是“可启动对象元数据与启动能力”，不是“所有和应用有关的功能”。

---

### 3.6 freeze

`freeze` 是唯一冻结业务边界。

`freeze` 负责：

- 冻结应用。
- 解冻应用。
- 查询冻结状态。
- 一键冻结名单。
- 保护名单。
- 冻结策略判断。
- 批量冻结 / 解冻结果聚合。

`freeze` 不负责：

- 应用启动。
- 快捷启动器展示。
- UI 组件。
- Shizuku 底层连接细节。

Shizuku 只是 system 提供的底层能力。冻结业务规则必须留在 `freeze`，不能下沉到 `system/shizuku`。

保护名单是 freeze 领域的硬规则，不能只作为 UI 隐藏逻辑存在。

---

### 3.7 overlay

`overlay` 只负责浮窗表现。

`overlay` 负责：

- 浮窗生命周期。
- 浮窗显示。
- 浮窗隐藏。
- 点击事件。
- 动画、展开、收起。
- 根据外部输入渲染快捷启动器。

`overlay` 不负责：

- 保存快捷启动器应用列表。
- 直接读写 DataStore。
- 查询 PackageManager。
- 维护独立应用缓存。
- 通过 ActionExecutor 启动应用。
- 处理冻结业务规则。

快捷启动器配置归 `settings`，应用信息、图标和启动能力归 `launcher`。

---

### 3.8 system

`system` 是底层 Android 能力封装层。

`system` 负责：

- Accessibility 全局动作封装。
- Intent 启动封装。
- PackageManager 封装。
- ShortcutManager 封装。
- Shizuku 底层调用。
- Toast。
- Vibration。
- WindowManager。
- 权限检查。
- Android API 兼容处理。

`system` 不负责：

- 保存用户配置。
- 读取 settings。
- 判断动作绑定。
- 判断冻结保护名单。
- 处理快捷启动器顺序。
- 依赖 UI。
- 依赖 ViewModel。
- 承载业务规则。

`system` 是能力提供者，不是业务决策者。

---

### 3.9 core

`core` 只放真正跨领域的基础设施。

根包只保留 Android / App 入口类，服务协作下沉到 `service/`。

`core` 可以放：

- 日志封装。
- 崩溃处理。
- 通用 Result / Error 类型。
- 通用时间、调度、协程辅助。
- AppGraph / 手动依赖装配。
- 纯 Kotlin 基础工具。

`core` 不放：

- PackageManager 工具。
- Shizuku 工具。
- Shortcut 工具。
- Toast 工具。
- Freeze 工具。
- Launcher 工具。
- Action 工具。
- Gesture 工具。
- UI 组件。

`core` 不能变成新的 `utils`。

---

## 4. 依赖方向白名单

允许的顶层依赖方向：

```text
ui -> gesture / action / settings / launcher / freeze / overlay

gesture -> settings / system

action -> settings / launcher / freeze / system

launcher -> system

freeze -> launcher / settings / system

overlay -> settings / launcher / system

settings -> core

system -> core

core -> 无业务领域
```

说明：

- 依赖白名单只约束顶层方向。
- 具体实现细节在迁移对应领域时再设计。
- 某个领域依赖另一个领域时，只能依赖对方暴露的边界入口，不能随意引用内部实现。

---

## 5. 禁止事项

禁止以下依赖和行为：

```text
system -> settings
system -> ui
launcher -> freeze
launcher -> overlay
freeze -> ui
action -> ui
gesture -> ui
core -> action / gesture / settings / launcher / freeze / overlay / system
```

禁止以下实现方式：

- UI 直接读写 DataStore。
- VM 直接调用 PackageManager。
- VM 直接调用 Shizuku。
- Handler 直接读 DataStore。
- Service 解析旧动作数据结构。
- Overlay 保存自己的快捷启动器列表。
- Launcher 处理冻结策略。
- System 判断业务规则。
- ActionSelect 自己维护动作事实源。
- ActionPanel 自己解析动作执行逻辑。
- 其他领域随意 import 对方内部工具类。

---

## 6. 取消 entity / utils / ktx 大杂烩

后续迁移应逐步取消顶层 `entity/`、`utils/`、`ktx/` 大杂烩。

模型按业务语义归属：

```text
gesture 相关模型 -> gesture
action 相关模型 -> action
settings 配置模型 -> settings
launcher 应用模型 -> launcher
freeze 冻结模型 -> freeze
overlay 浮窗状态模型 -> overlay
system 返回结果模型 -> system
真正通用模型 -> core
```

工具函数按能力归属：

```text
PackageManager -> system 或 launcher
ShortcutManager -> system 或 launcher
Intent 启动 -> system 或 launcher
Shizuku -> system
Toast -> system
Vibration -> system
WindowManager -> system
Accessibility -> system
应用搜索 -> launcher
图标缓存 -> launcher
冻结策略 -> freeze
动作相关辅助 -> action
手势相关辅助 -> gesture
DataStore / 备份 / 默认值 -> settings
```

禁止继续把不同领域的模型和工具集中放入 `entity/`、`utils/`、`ktx/`。

---

## 7. 后续细化原则

本文件只定义长期架构边界，不绑定具体版本、分支、commit 或迁移阶段。

本文件不提前规定：

- ActionPayload 具体结构。
- GestureSlot 具体字段。
- AppSettings 具体字段。
- Backup JSON 格式。
- Repository 方法签名。
- Catalog 字段。
- 完整包路径树。
- 某个旧类何时删除。

具体实现应在迁移对应领域时，根据当前代码状态单独分析和设计。

后续任何具体方案必须满足：

- 不破坏本文件定义的领域职责。
- 不违反依赖方向白名单。
- 不让旧 `entity/`、`utils/`、`ktx/` 以新名字继续存在。
- 不为了当前实现方便牺牲长期边界。

## 8. 当前状态摘要

截至当前分支状态：

- `ktx/`、`utils/`、`entity/` 已无 `.kt` 文件。
- `docs/index/` 已重写为当前领域索引，不再把旧包作为主入口。
- 代码主边界已稳定为 `ui`、`gesture`、`action`、`settings`、`launcher`、`freeze`、`overlay`、`system`、`core`。
- `SideGestureService` 的内部协作已拆成 runtime、overlay lifecycle、button refresh 和 action coordinator。
