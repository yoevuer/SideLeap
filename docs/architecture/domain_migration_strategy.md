# SideLeap 领域迁移策略

## 1. 迁移原则

SideLeap 架构迁移采用按领域逐步迁移策略。

迁移目标是逐步达到 `domain_boundary_architecture.md` 定义的领域边界，而不是一次性完成全局大重构。

核心原则：

- 每次只迁移一个领域或一条主链路。
- 每个阶段必须可构建、可运行、可回滚。
- `main` 必须始终保持可发布。
- 不允许在一个分支中同时迁移 gesture、action、settings、launcher、freeze、overlay、backup 等多条主链路。
- 不提前设计过细实现。
- 进入某个阶段后，先探索现有代码，再确定该阶段的具体实现。
- 旧代码只能在替代路径稳定后删除。

本策略只约束迁移方法，不规定具体类名、字段、payload、schema 或方法签名。

---

## 2. 分支与提交规则

每轮迁移必须从稳定 `main` 新建独立分支。

建议分支命名：

```text
refactor/<domain-or-chain>-boundary
fix/<specific-chain-consistency>
```

要求：

- 不在不稳定分支上继续叠加新架构迁移。
- 不把实验性大改直接合并回 `main`。
- 每个阶段至少一个独立 commit。
- commit 只包含当前阶段相关修改。
- 提交信息应说明本阶段边界或链路目标。

示例：

```text
fix: stabilize gesture action mapping
refactor: isolate launcher metadata boundary
refactor: move freeze operations behind freeze boundary
refactor: route quick launcher overlay through launcher boundary
```

禁止：

- 一个 commit 同时迁移多个领域。
- 一个 commit 同时做架构迁移、UI 美化、新功能和清理。
- 在构建失败状态下继续下一阶段。
- 为了通过编译随意扩大修改范围。

---

## 3. 阶段推进规则

每个阶段只能选择以下一种目标：

```text
修复一条现有链路一致性
建立一个领域边界入口
迁移一个领域的部分职责
删除一组已有稳定替代的旧代码
更新对应文档
```

阶段开始前必须回答：

```text
本阶段解决哪个领域或哪条链路？
本阶段不解决什么？
本阶段允许修改哪些范围？
本阶段如何验证？
本阶段如何回滚？
```

阶段执行时必须遵守：

- 先探索代码，再制定当前阶段细化方案。
- 不重复询问已确认的顶层原则。
- 不把后续领域的问题提前并入当前阶段。
- 不因为发现旧代码混乱就顺手全局清理。
- 中间态允许存在短期桥接，但桥接不能成为长期公开边界。

---

## 4. 每阶段进入条件

进入任一迁移阶段前，必须满足：

```text
当前分支基于稳定 main
工作区干净或修改范围已明确
当前阶段目标单一
当前阶段不违反领域边界架构
已明确本阶段不做事项
```

建议检查：

```bash
git status
git log --oneline --decorate -10
./gradlew assembleDebug
```

关键阶段开始前建议额外确认：

```bash
./gradlew assembleRelease
```

如果 `main` 本身不稳定，先修复 `main`，不要开始架构迁移。

---

## 5. 每阶段完成条件

每个阶段完成时必须满足：

```text
当前阶段目标完成
没有超出阶段范围的大改
assembleDebug 通过
关键阶段 assembleRelease 通过
人工测试项已列出或完成
回滚方式明确
git diff 可审查
```

提交前建议执行：

```bash
git status
git diff --stat
git diff --check
./gradlew assembleDebug
```

涉及 release、序列化、混淆、备份、启动链路的阶段，还必须执行：

```bash
./gradlew assembleRelease
```

阶段输出应包含：

```text
修改文件
完成目标
未做事项
构建结果
人工测试项
风险点
commit hash
```

---

## 6. 验证与回归要求

验证分三层。

### 6.1 构建验证

普通阶段至少通过：

```bash
./gradlew assembleDebug
```

关键阶段必须通过：

```bash
./gradlew assembleRelease
```

### 6.2 链路验证

根据阶段所属领域选择验证项。

示例：

```text
gesture:
  设置页显示、保存、复制对侧、Service 触发是否一致

action:
  ActionSelect 展示、动作配置、绑定保存、动作执行是否一致

settings:
  配置读取、更新、默认值、重启保留、备份恢复是否一致

launcher:
  应用列表、Activity、Shortcut、图标、启动能力是否正常

freeze:
  单个冻结、一键冻结、保护名单、Shizuku 失败处理是否正常

overlay:
  浮窗显示、应用列表、图标、点击启动、隐藏列表是否正常
```

### 6.3 实机验证

以下阶段必须实机验证：

- 手势识别。
- 动作执行。
- ActionSelect。
- Overlay。
- Freeze。
- Backup。
- Release 包启动。
- 与 AccessibilityService 相关的修改。

实机测试只需记录测试项和结论，不要求写成复杂脚本。

---

## 7. 回滚策略

每个阶段必须可回滚。

推荐规则：

- 每阶段独立 commit。
- 阶段失败时优先 revert 当前阶段 commit。
- 未提交的大改失败时直接丢弃工作区。
- 不在失败分支上继续叠加新阶段。
- 如果一个分支出现多条主链路回归，应废弃该分支，从稳定 `main` 重新开始。

本地实验分支如果未合并、未 push，且问题过多，可以直接：

```text
保留备份分支或 tag
切回 main
删除问题分支
重新开小范围分支
```

禁止：

- 为了保留失败分支而继续打补丁。
- 在多个核心链路同时坏掉时继续推进。
- 用临时兼容层掩盖长期边界错误。
- 在 release 包明显不可用时继续做新迁移。

---

## 8. 禁止事项

迁移过程中禁止：

- 一次性全局迁移动作模型、手势模型、DataStore、ActionSelect、Launcher、Freeze、Overlay、Backup。
- 未验证构建就继续下一阶段。
- 未替代稳定就删除旧代码。
- 把具体实现细节提前写成长期架构约束。
- 为了迁移引入完整 Clean Architecture。
- 拆 Gradle module。
- 引入 Hilt / Dagger / Koin。
- 为每个按钮、每个动作建立独立 UseCase。
- 让 `core` 变成新的 `utils`。
- 让 `entity/`、`utils/`、`ktx/` 换名字继续存在。
- 在 UI / VM / Service / Handler / Overlay 中直接读写 DataStore。
- 让 system 承载业务规则。
- 让 launcher 处理 freeze 规则。
- 让 overlay 保存第二套快捷启动器状态。

---

## 9. 文档关系

长期架构边界以：

```text
docs/architecture/domain_boundary_architecture.md
```

为准。

迁移方法以：

```text
docs/architecture/domain_migration_strategy.md
```

为准。

具体阶段实现方案不写入这两份文档。进入某个领域迁移时，单独根据当前代码分析并形成阶段计划。
