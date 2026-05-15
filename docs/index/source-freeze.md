# SideLeap 源码索引 - freeze/

`freeze/` 承载冻结、解冻、一键名单、保护名单和 Shizuku 冻结链路。

## 当前职责

- 冻结动作：`freeze/FreezeAction.kt`
- 冻结启动与执行：`freeze/FreezeLaunch.kt`
- 冻结状态：`freeze/FreezeState.kt`
- Shizuku 桥接服务：`freeze/ShizukuBridgeService.kt`
- 冻结包 enabled / disabled：`freeze/FrozenPackageEnabler.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `freeze/FreezeAction.kt` | 冻结动作语义 |
| `freeze/FreezeLaunch.kt` | 冻结执行入口 |
| `freeze/FreezeState.kt` | 冻结状态模型 |
| `freeze/ShizukuBridgeService.kt` | Shizuku 桥接服务 |
| `freeze/FrozenPackageEnabler.kt` | 通过 Shizuku 解冻包的能力 |

## 关键入口

- `FreezeAction` 代表冻结业务动作。
- `FreezeLaunch` 负责冻结流程组织。
- `FreezeState` 表示当前冻结态与名单态。
- `ShizukuBridgeService` 负责与底层桥接能力协作。
- `FrozenPackageEnabler` 封装 Shizuku 解冻单包流程，供 Service 调用。

## 依赖边界

- `freeze` 可以依赖 `launcher`、`settings`、`system`。
- `freeze` 不依赖 `ui`，也不把保护名单逻辑下放到 `system/shizuku`。
