# SideLeap 当前架构状态

截至移除音量键切歌功能后。

## 已完成

### 移除音量键切歌功能

- 删除 `service/LockScreenVolumeKeyHandler.kt`（75 行）
- `SideGestureService.kt` 移除 import、实例化、`onKeyEvent` 覆写、`onDestroy` 清理
- `AdvancedSettings.kt` 移除 `volumeButtonSwitchSong` 字段
- `SettingsDefaults.kt` 移除 `VolumeButtonSwitchSong` 常量
- `AdvancedSettingsVM.kt` 移除 `onVolumeButtonSwitchSong`、UI state、save/load 逻辑
- `AdvancedSettingsScreen.kt` 移除对应开关 UI
- `strings.xml` 移除 3 条相关字符串
- `Audio.kt`（`volumeUp`/`volumeDown`/`dispatchMediaKeyEvent`）不受影响，仍被 `MediaActionHandler` 用于手势媒体动作

## 待办

- 拆分 `ShizukuCommand`（535 行）
- 拆分 `ActionSelectScreen`（1326 行）
- 补充更多单元测试（Shizuku、Backup 等）
- 定期更新本文档
