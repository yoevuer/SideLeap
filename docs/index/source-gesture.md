# SideLeap 源码索引 - gesture/

`gesture/` 承载手势按钮、方向、角度、触发槽位和输入识别语义。

## 当前职责

- 手势按钮与扩展语义：`gesture/GestureButton.kt`、`gesture/GestureButtonExt.kt`
- 手势角度与方向：`gesture/GestureAngle.kt`、`gesture/TriggerDirection.kt`
- 手势动作语义：`gesture/GestureActions.kt`
- 位置与槽位：`gesture/Position.kt`
- 输入分发：`gesture/input/MotionEventDispatcher.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `gesture/GestureButton.kt` | 手势按钮模型 |
| `gesture/GestureAngle.kt` | 角度语义 |
| `gesture/GestureActions.kt` | 手势动作语义 |
| `gesture/input/MotionEventDispatcher.kt` | 触摸事件分发 |

## 关键入口

- `MotionEventDispatcher` 是手势输入到槽位识别的核心入口。
- `GestureButton`、`GestureAngle`、`TriggerDirection`、`Position` 提供手势配置和解析所需的语义模型。

## 依赖边界

- `gesture` 允许依赖 `settings` 和 `system`。
- `gesture` 不决定动作执行，不持久化配置，不直接展示 UI。
