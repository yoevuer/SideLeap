# SideLeap 代码索引

SideLeap 采用单模块领域分包结构。当前索引只描述真实可维护结构，不再把 `ktx/`、`utils/`、`entity/` 作为当前源码入口。

## 当前领域

| 领域 | 说明 |
|---|---|
| `ui` | 页面、组件、导航、UI 状态 |
| `gesture` | 手势模型、输入识别、手势语义 |
| `action` | 动作定义、展示、payload、执行分发 |
| `settings` | 配置模型、DataStore、默认值、备份恢复 |
| `launcher` | 应用/Shortcut/图标/启动能力 |
| `freeze` | 冻结业务、名单策略、Shizuku 冻结链路 |
| `overlay` | 快捷启动器浮窗 |
| `system` | Android 底层能力封装 |
| `core` | 跨领域基础设施与应用入口类 |

## 索引入口

| 索引页 | 说明 |
|---|---|
| `source-ui.md` | UI、导航、页面、组件、主题 |
| `source-gesture.md` | 手势模型与输入识别 |
| `source-action.md` | 动作定义、展示、执行分发 |
| `source-settings.md` | 配置、DataStore、备份恢复 |
| `source-launcher.md` | 应用、Shortcut、图标、启动能力 |
| `source-freeze.md` | 冻结业务与名单策略 |
| `source-overlay.md` | 快捷启动器浮窗 |
| `source-service.md` | SideGestureService 内部协作结构 |
| `source-system.md` | 系统能力封装 |
| `source-core.md` | 应用入口与基础设施 |

## 使用原则

- 以代码当前结构为准。
- 旧迁移细节只保留在相关领域页的简短说明里，不再作为主索引主体。
- 后续 agent 先看这里，再进入对应领域页。
