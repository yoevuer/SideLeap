# SideLeap 源码索引 - settings/

`settings/` 是全局配置边界，负责默认值、DataStore、配置模型、备份恢复。

## 当前职责

- 配置入口与 provider：`settings/SettingsProvider.kt`
- 默认值：`settings/SettingsDefaults.kt`
- UI 相关设置边界：`settings/SettingsUiDefaults.kt`
- DataStore 访问：`settings/internal/DataStore.kt`
- 备份恢复：`settings/BackupHelper.kt`
- 文件与键名常量：`settings/DataStoreFiles.kt`
- 配置模型：`settings/model/*`
- 手势角度随 `GestureButton` 持久化，不再由 `GestureSettings` 按位置保存
- 密码生成器配置：`ActionSettings.PasswordGenerator`
- 小窗打开位置：`AdvancedSettings.miniWindowHorizontalBias` / `miniWindowVerticalBias` / `miniWindowVerticalEdgeMarginFraction`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `settings/model/GestureSettings.kt` | 手势距离、振动、精确滑动等全局手势配置 |
| `settings/model/ActionSettings.kt` | 动作配置 |
| `settings/model/ActionSettings.kt#PasswordGenerator` | 密码生成器长度和字符类型配置 |
| `settings/model/AdvancedSettings.kt` | 高级设置 |
| `settings/model/QuickAppLauncherSettings.kt` | 快捷启动器配置 |
| `settings/model/FrozenAppSettings.kt` | 冻结配置 |
| `settings/model/Backup.kt` | 备份模型 |
| `settings/model/InitialSettings.kt` | 初始配置 |
| `settings/model/DayNightMode.kt` | 明暗模式模型 |
| `settings/model/AnimationStyles.kt` | 动画样式模型 |
| `settings/model/ActionPanelStyles.kt` | 动作面板样式模型 |

## 关键入口

- `SettingsProvider` 是配置读写入口。
- `BackupHelper` 负责导出与恢复。
- `DataStore` 的实现细节被限制在 `settings/internal`。
- 触钮角度作为 `GestureButton.angle` 随侧边/底部触钮列表保存。

## 依赖边界

- `settings` 是配置权威边界。
- 下游领域通过读取结果、状态快照或 updater 使用配置，不应各自持久化。
- 密码生成器的长度和字符类型配置随 `ActionSettings` 持久化，当前生成密码和显示状态不持久化。
- 应用小窗打开水平/垂直位置和垂直边缘留白随 `AdvancedSettings` 持久化，供打开应用动作、快捷启动器和当前应用小窗动作读取。
- `settings` 的对外依赖应保持最小，优先只依赖 `core`。
