# SideLeap 架构规范

架构文档已拆分到 `docs/architecture/`，本文件仅作为索引入口。

## 文档列表

- [architecture/README.md](architecture/README.md) — 架构总览、原则、领域列表、新功能判断流程
- [architecture/domains.md](architecture/domains.md) — 每个领域的具体职责、依赖和边界
- [architecture/dependencies.md](architecture/dependencies.md) — 层级结构、依赖方向、跨领域调用规则
- [architecture/api-boundaries.md](architecture/api-boundaries.md) — 包结构、Public API、动作集成规则
- [architecture/runtime-flows.md](architecture/runtime-flows.md) — 关键运行时流程描述
- [architecture/rules.md](architecture/rules.md) — 配置、Android API、UI、Service、命名等硬性约束
- [architecture/examples.md](architecture/examples.md) — 功能落位示例

## 修改规范

- 新增架构规则应写入 `rules.md`。
- 修改领域职责应更新 `domains.md`。
- 修改依赖方向应更新 `dependencies.md`。
- 新增运行时流程应更新 `runtime-flows.md`。
- 新增功能示例应写入 `examples.md`。
- 包结构或 API 约定变更应更新 `api-boundaries.md`。
- 新增原则或架构目标应更新 `README.md`。
