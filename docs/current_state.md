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

### 触钮独立手势角度（`feat/per-button-gesture-angle`）

- 手势角度从 `GestureSettings` 的按位置共享配置迁移到 `GestureButton.angle`
- 运行时方向判定直接使用当前触钮角度
- 全局手势角度入口移除，角度调整入口放入单个触钮设置页
- 角度调整界面改为当前触钮专用底部弹窗

### 密码生成器功能（`feat/password-generator`）

### AdvancedSettings 分节重组

- 将 AdvancedSettings 从 4 节重构为清晰的 4 节：
  - **应用管理**: 应用黑名单、管理隐藏应用、清除统计、显示系统应用、最近任务隐藏、启动方式 ×2
  - **触钮行为**: 适配软键盘
  - **隐藏触钮**: 4 个子项（不变）
  - **显示**: 动画风格、动态配色、深色模式
- 消除"触钮扩展"节名下视觉设置与手势行为混放的混乱

### 密码生成器运行时面板

- 密码生成器面板由 `RuntimePanelOverlay` 承载，窗口使用 `TYPE_ACCESSIBILITY_OVERLAY`、透明背景和内容实测尺寸。
- 可见 Card 在 `SideGestureTheme` 内渲染，使用动态取色；窗口尺寸随 Card 实测宽高回写到 `WindowManager.LayoutParams`。
- `PasswordGeneratorPanel` 只保留内容层，关闭、外部触摸、圆角和底部定位由运行时 overlay 负责；打开/关闭复用 QLA 的内容层淡入淡出与位移动画。

### 应用小窗打开位置设置（`feat/mini-window-position-settings`）

- 高级设置新增小窗打开水平/垂直位置滑杆，默认水平 50%、垂直 70%。
- 新增小窗垂直边缘留白滑杆，默认 5%；垂直位置 0% 表示上边缘距顶部留白，100% 表示下边缘距底部留白。
- 新增小窗垂直位置补偿滑杆，默认 0%，范围 -100%..100%；正值向下补偿，负值向上补偿。
- 打开应用动作、快捷启动器打开应用和当前应用小窗动作共用同一位置配置。
- 小窗位置通过 `ActivityOptions.setLaunchBounds` 传入；部分厂商小窗模式可能由系统决定实际位置。

### 动作面板长按动作配置（`feat/action-panel-long-press`）

- 动作面板多动作列表支持给单个动作配置长按动作；长按未配置时继续执行短按动作。
- 动作面板运行时根据 `TriggerType.Press / LongPress` 选择短按动作或 `longPressAction`，并保留原有打开应用长按小窗逻辑。
- ActionSelect 的已选栏保持原 chip 样式，同时显示普通动作、应用和快捷方式。
- 已选栏下方新增已选动作设置区，支持设置/清除长按动作和上移/下移排序。
- 已选记录统一保存为 `Action`，应用和快捷方式通过 `EXTRA_LAUNCH_APP` / `EXTRA_LAUNCH_SHORTCUT` payload 表示。

### 手势按钮单击动作（`feat/gesture-button-tap`）

- 触钮设置页新增“单击/长按”分组；单击使用 `GestureButton.tapActions.center`，长按沿用原 `slideActions.center2`。
- 运行时在未达到滑动阈值时触发单击动作，长按触发逻辑保持原有配置与时长。
- “隐藏触钮”改为可选动作，执行后按 `ActionSettings.hideGestureButton.delayMs` 临时隐藏触钮，到期自动恢复。
- 隐藏触钮动作设置弹窗显示当前时长，并提供 500..5000 ms 滑杆。

### 冻结应用管理网格（`feat/frozen-app-grid`）

- 冻结应用管理页和保护名单页改为自适应网格展示。
- 一键冻结名单和保护名单使用待提交选择状态，离开或关闭页面时统一写入配置。
- 顶部搜索/筛选控件悬浮在网格上方，网格用顶部 padding 预留控件高度，避免控件显隐时改变列表高度。
- 用户继续上滑时会先隐藏控件，释放顶部可视空间，避免底部距离不足时出现回弹阻塞。
- 一键名单和保护名单默认折叠；保护名单页弹窗高度为屏幕 70%，控件固定显示，默认不显示其他应用，仅搜索时展示匹配的其他应用。

### 代码优化与架构清理（`refactor/optimize-logging-overlay`）

- 全仓热路径 `Log.d` 加 `BuildConfig.DEBUG` 保护：`QuickAppLauncherOverlay`、`QuickAppLauncherActivity`、`ShizukuCommand.enablePackageForLauncher`。
- 提取 `WindowManagerUtils.kt`（`Context.windowManager()`、`applyOverlayViewTreeOwners()`、`overlayLayoutParams()`）消除两个 Overlay 的窗口管理重复代码。
- `QuickAppLauncherOverlayHost` 接口移除 `RenderQuickAppLauncherContent` / `RenderQuickAppLauncherAdjustPanel`，Overlay 直接调用 Compose 内容层。
- `PasswordGeneratorPanel` 动画/状态封装为 `PasswordPanelContent`，`SideGestureService` 不再持有 Compose 渲染。
- `QuickAppLauncherContent` 状态抽取到 `QuickAppLauncherState`，UI 不再直接执行查询/排序/启动。
- 从 `ShizukuCommand`（476 行）拆分出 `ShizukuBinderExecutor`（绑定+超时+结果解析），减少 ShizukuCommand 到 372 行。
- 新增 `GestureRuntimeSettingsProvider` / `QuickLauncherSettingsProvider` 提供领域特定联合 Flow。
- `DataStore` 反序列化失败日志增加文件尺寸，空白文件跳过解码。
- 新增单元测试：`ShizukuBinderExecutorTest`（12 cases）、`BatchFrozenResultTest`（4 cases）。

## 待办

- 实机验证快捷启动器、密码面板、手势识别、冻结功能正常
- 人工验证密码生成器 overlay 在小屏、横屏和不同输入法下的交互
- 定期更新本文档
