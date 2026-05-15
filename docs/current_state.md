# SideLeap 当前架构状态

截至 `refactor/quick-launcher-boundary` 合并到 main 后（2025/2026 年）。

## 已完成

- `ktx/`、`utils/`、`entity/` 已无 `.kt` 文件。
- 代码主边界已稳定为 `ui`、`gesture`、`action`、`settings`、`launcher`、`freeze`、`overlay`、`system`、`core`。

### SideGestureService 瘦身

从 ~670 行缩减至 ~260 行，拆出 9 个协作对象：

| 协作者 | 文件 | 职责 |
|---|---|---|
| `SideGestureWindowController` | `service/SideGestureWindowController.kt` | 主 overlay 和手势按钮窗口创建/更新 |
| `SideGestureSettingsObserver` | `service/SideGestureSettingsObserver.kt` | settings flow 订阅分发 |
| `WallpaperChangeObserver` | `service/WallpaperChangeObserver.kt` | 壁纸变化广播监听 |
| `ScreenLockObserver` | `service/ScreenLockObserver.kt` | 锁屏/解锁广播监听 |
| `LockScreenVolumeKeyHandler` | `service/LockScreenVolumeKeyHandler.kt` | 锁屏音量键切歌/音量回退 |
| `ImeInsetObserver` | `service/ImeInsetObserver.kt` | 软键盘 inset 监听 |
| `FrozenPackageEnabler` | `freeze/FrozenPackageEnabler.kt` | Shizuku 解冻单包流程 |
| `SideGestureServiceProxy` | `service/SideGestureServiceProxy.kt` | Action 编排协作 |
| `SideGestureButtonRefreshCoordinator` | `service/SideGestureButtonRefreshCoordinator.kt` | 手势按钮刷新 |

### 配置边界收敛

- `SettingsProvider` 新增 `recordQuickAppLaunch()`、`updateQuickAppLauncherLayout()`、`resetQuickAppLauncherLayout()`
- Overlay / AdjustPanel 不再直接拼 DataStore 更新

### launcher 边界收敛

- `launcher/query/` 新增：
  - `LauncherIconQuery.kt` — 应用图标和 Shortcut icon resource 解析
  - `LauncherEnvironment.kt` — 判断当前包是否为桌面启动器
  - `OpenAppOrUrlQuery.kt` — 打开应用/URL 的查询入口
  - `QuickAppLauncherQuery.kt` — 快捷启动器应用列表合并
- `launcher/launch/` 新增 `QuickAppLaunch.kt` — 快捷启动器冻结后启动流程
- UI 不再直接调用 `PackageManager` 读取图标和 shortcut resource
- `ActionSettingsDialog` 的 PackageManager 查询迁至 `launcher/query`

### freeze 边界收敛

- `FrozenPackageEnabler.kt` 封装 Shizuku 解冻单包流程，从 `SideGestureService` 移出

### 协程安全

- `App.applicationScope` 替代 `GlobalScope`
- `Events` 使用 `ConcurrentHashMap + CopyOnWriteArrayList` 替代 `MutableMap + MutableList`

### 清理

- `printStackTrace` 已全部清理（仅保留 CrashHandler 的崩溃文件写入）
- `LauncherDiagnostics` 已移除（原为空实现）

### 测试

- 共 24 个单元测试
- 覆盖：`FreezeAction.computeOneKeyTargetsInRange`、`FreezeState.isSystemApp`、`AppInfo.key`、`Events.subscribe/unsubscribe/dispatch`
- 运行：`./gradlew testDebugUnitTest`

### CI

- `.github/workflows/ci.yml`
- 在 push/PR 时运行 `testDebugUnitTest` 和 `assembleDebug`

## 待办

- 拆分 `ShizukuCommand`（535 行）
- 拆分 `ActionSelectScreen`（1326 行）
- 补充更多单元测试（Shizuku、Backup 等）
- 定期更新本文档
