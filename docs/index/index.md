# SideLeap 代码索引

## 当前状态

SideLeap 采用单模块领域分包结构，当前主领域为：

- `ui`
- `gesture`
- `action`
- `settings`
- `launcher`
- `freeze`
- `overlay`
- `system`
- `core`

`ktx/`、`utils/`、`entity/` 已完成清理，不再作为活跃源码包。

## 索引入口

| 索引页 | 说明 |
|---|---|
| `source-ui.md` | UI、导航、页面、VM |
| `source-action.md` | 动作定义、执行、展示 |
| `source-core.md` | 应用入口与顶层服务/Activity |
| `source-misc.md` | 常量、事件、浮窗等辅助包 |
| `source-ktx.md` | 历史归档页 |
| `source-utils.md` | 历史归档页 |
| `source-entity.md` | 历史归档页 |
| `resources.md` | 资源索引 |

## 领域摘要

| 领域 | 说明 |
|---|---|
| `ui` | 页面、组件、导航、UI ViewModel |
| `gesture` | 手势按钮、方向、识别语义 |
| `action` | 动作定义、payload、执行分发 |
| `settings` | 默认值、DataStore、备份恢复 |
| `launcher` | 应用/Shortcut/图标/启动能力 |
| `freeze` | 冻结业务规则与 Shizuku 冻结链路 |
| `overlay` | 快捷启动器浮窗表现 |
| `system` | Android 底层能力封装 |
| `core` | 跨领域基础设施 |

## 归档说明

- `source-ktx.md`、`source-utils.md`、`source-entity.md` 仅保留历史迁移记录。
- 这些页面顶部均已标记 `archived`，不应再被视为当前结构说明。
