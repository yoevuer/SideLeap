# SideLeap 当前架构状态

## 当前状态

### 7 个 Picker 页面 BottomSheet 化（`refactor/bottom-sheet`）

- 将 7 个全屏导航路由改为 ModalBottomSheet：
  - `AppBlacklist` → `AppBlacklistContent`（从 AdvancedSettings 弹出）
  - `QuickAppLauncherHidden` → `QuickAppLauncherManageContent`（从 AdvancedSettings 弹出）
  - `WaveAnimationStyle` → `WaveStyleContent`（从 AdvancedSettings 弹出）
  - `AdjustGestureAngles` → `GestureAnglesContent`（从 GestureSettings 弹出）
  - `ActionSelect` → `ActionSelectContent`（从 GestureButtonSettings 弹出，内部再嵌套 IconResize）
  - `IconResize` → `IconResizeContent`（从 ActionSelect 弹出）
  - `FrozenAppProtect` → `FrozenAppProtectContent`（从 FrozenAppManage 弹出）
- 导航路由数从 13 减少到 6（Home、Unlock、AdvancedSettings、GestureSettings、GestureButtonSettings、FrozenAppManage）
- ActionSelectVM / IconResizeVM 改为直接注入数据（移除 SavedStateHandle 依赖），通过 ViewModelProvider.Factory 创建
- 所有 `finish()` 行为通过 `onBaseEvent` 拦截改为调用 `onDismiss` 关闭 BottomSheet
- TopBar 中的操作按钮（Reset/Done/Save/Refresh）移入 BottomSheet 内容头部
- 构建+人工测试通过

## 待办

- 补充更多单元测试（Shizuku、Backup 等）
- 人工验证密码生成器 overlay 在小屏、横屏和不同输入法下的交互
- 定期更新本文档
