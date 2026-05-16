# SideLeap 架构文档

本文档定义 SideLeap 的长期代码组织规则。
所有新增代码、重构和修复都应先判断领域归属，再确定调用方向。
如果现有代码与本文档冲突，以本文档作为后续重构目标。

## 架构目标

- 避免 `SideGestureService` 变成上帝类。
- 避免 `settings` 变成无边界的全局状态中心。
- 避免 `action`、`gesture`、`overlay` 互相直接穿透。
- 避免 Android API 散落在业务代码中。
- 避免 UI 直接依赖领域内部实现。
- 让新功能能稳定落到明确领域中。
- 让后续重构可以分阶段推进，而不是整体推倒重来。

## 总体原则

SideLeap 采用单模块领域边界架构。
代码按业务语义划分领域，通过包结构、入口类、依赖方向和调用边界保持长期可维护性。

- 每个领域负责单一语义。
- 跨领域调用必须走明确入口。
- 配置由统一边界管理。
- 手势识别与动作执行分离。
- Android 系统能力集中封装。
- `service` 负责运行时编排，不沉淀业务规则。
- `core` 保持通用、轻量、无业务语义。
- 单模块不代表可以任意跨包调用。

## 领域

| 领域 | 职责 |
|---|---|
| `ui` | 页面、组件、导航、页面状态、用户配置入口 |
| `gesture` | 手势模型、输入识别、触发语义 |
| `action` | 动作定义、动作请求、payload、动作分发、动作结果 |
| `settings` | 配置模型、默认值、持久化、配置导入导出、备份恢复 |
| `launcher` | 应用列表、Shortcut、图标、启动能力 |
| `freeze` | 冻结业务、冻结状态、名单策略 |
| `overlay` | 浮窗表现、浮窗交互、浮窗生命周期 |
| `service` | `SideGestureService` 内部运行时编排结构 |
| `system` | Android 底层能力封装 |
| `core` | 跨领域基础设施 |

## 文档索引

- [domains.md](domains.md) — 每个领域的具体职责、依赖和边界
- [dependencies.md](dependencies.md) — 层级结构、依赖方向、跨领域调用规则
- [api-boundaries.md](api-boundaries.md) — 包结构、Public API、动作集成规则
- [runtime-flows.md](runtime-flows.md) — 关键运行时流程描述
- [rules.md](rules.md) — 配置、Android API、UI、Service、命名等硬性约束
- [examples.md](examples.md) — 功能落位示例

## 新增代码的基本判断流程

新增功能时按以下顺序判断：

1. 是否是页面或组件？放入 `ui`。
2. 是否是手势识别或触发语义？放入 `gesture`。
3. 是否是"可以被触发的动作"？放入 `action` 的定义或动作集成。如果动作需要具体业务能力，具体执行放到对应领域 handler。
4. 是否是配置、默认值、持久化、导入导出？放入 `settings`。
5. 是否是应用、Shortcut、图标、启动？放入 `launcher`。
6. 是否是冻结业务？放入 `freeze`。
7. 是否是浮窗运行时表现？放入 `overlay`。
8. 是否是 Android 系统能力？放入 `system`。
9. 是否是跨领域通用基础设施？放入 `core`。必须满足：无业务语义、不依赖 Android 业务上下文、不依赖任何 SideLeap 领域。
