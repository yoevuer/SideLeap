# SideLeap 当前架构状态

截至移除音量键切歌功能后。

## 已完成

### 密码生成器功能（`feat/password-generator`）

- 新增两个工具动作：`生成密码并复制`、`打开密码生成器`
- 新增 `ActionSettings.PasswordGenerator` 配置，随 `ActionSettings` 持久化和备份恢复
- 新增 `action.api.PasswordGenerator`，提供安全随机生成、配置修正、熵值计算
- 新增 `system.api.copySensitiveText()`，复制时使用非敏感 label，并在系统支持时标记敏感剪贴板
- 新增 `RuntimePanelOverlay`，由 `SideGestureService` 运行时承载密码生成器 Compose 面板
- 新增密码生成器面板：隐藏/显示、手动编辑、复制、重新生成、长度和字符类型配置
- 密码生成器面板背景透明，并随输入法 inset 自动上移
- `RuntimePanelOverlay` 会按状态栏、导航栏和系统手势 inset 缩小 `ComposeView` 区域，避免覆盖通知栏、底部导航栏和侧边手势热区
- 新增密码生成器单元测试

### 移除音量键切歌功能

- 删除 `service/LockScreenVolumeKeyHandler.kt`（75 行）
- `SideGestureService.kt` 移除 import、实例化、`onKeyEvent` 覆写、`onDestroy` 清理
- `AdvancedSettings.kt` 移除 `volumeButtonSwitchSong` 字段
- `SettingsDefaults.kt` 移除 `VolumeButtonSwitchSong` 常量
- `AdvancedSettingsVM.kt` 移除 `onVolumeButtonSwitchSong`、UI state、save/load 逻辑
- `AdvancedSettingsScreen.kt` 移除对应开关 UI
- `strings.xml` 移除 3 条相关字符串
- `Audio.kt`（`volumeUp`/`volumeDown`/`dispatchMediaKeyEvent`）不受影响，仍被 `MediaActionHandler` 用于手势媒体动作

### 手势热路径优化（`perf/gesture-hotpath`）

- `SideGestureState.onDrag()`: `calcDirection()` 除零保护、finger 显示值从 `Animatable` 改为 `mutableStateOf`（消除每帧协程 launch）、`canDistanceTriggered` 合并调用
- `reset()` 移除弹簧动画协程
- `AppQuery`: 添加 `launcherCache` + `BroadcastReceiver` 自动失效
- `FreezeState`: 添加 `frozenCache` + 同步失效（冻结/解冻操作后）
- 构建+测试通过

### 代码重构清理（`perf/refine`）

- `canDistanceTriggered()`: 3 组方向重复分支平铺为 `when` + 组合函数，-46 行
- `ShizukuCommand`：提取 `createArgs()` + `runWithBinder()` 模板，消除 110 行重复
- 合并 `AppQuery`/`FreezeState` 的广播接收器为共享 `PackageChangeReceiver`
- `QuickAppLauncherOverlay.show()` 拆为 `loadSettingsAsync()` / `cleanupExistingOverlay()` / `showOverlayView()`

### 架构违规修复（`fix/arch-ui-system` + `fix/arch-violations`）

- 创建 `core/AppContext.kt`，消除 system/ 和 ui/ 对根包 `App` 的依赖
- `GestureActions` 从 `gesture/` 移到 `action/`
- `GestureView` 从 `ui/widget/` 移到 `service/`
- `SideGestureContainer` 截图改为回调注入，消除 ui→service 依赖
- 更新架构文档（`dependencies.md`、`rules.md`）以匹配实际依赖

### api 包引入（`feat/api-boundaries`）

- `system/api/`: Toast、Vibration、Intent、Permission、Accessibility、Audio、Packages、Shizuku
- `settings/api/`: SettingsProvider、Defaults、BackupHelper
- `action/api/`: ActionRegistry、ActionHandler、ActionHandlerContext、ActionExecutionResult
- `freeze/api/`: FreezeAction、FreezeLaunch、FreezeState
- `overlay/api/`: QuickAppLauncherOverlay、QuickAppLauncherOverlayHost
- 所有跨域导入已迁移到对应 `api/` 包

## 待办

- 拆分 `ActionSelectScreen`（1326 行）
- 补充更多单元测试（Shizuku、Backup 等）
- 人工验证密码生成器 overlay 在小屏、横屏和不同输入法下的交互
- 定期更新本文档
