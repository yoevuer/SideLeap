# SideLeap 源码索引 - launcher/

`launcher/` 承载应用、Activity、Shortcut、图标和启动能力。

## 当前职责

- 应用与 Shortcut 查询：`launcher/query/*`
- 应用与 Shortcut 扩展：`launcher/ext/*`
- 启动入口：`launcher/launch/Launcher.kt`
- 应用小窗启动位置由调用方传入水平/垂直 bias、垂直边缘留白和垂直补偿，`Launcher` 只负责转换为 `ActivityOptions.setLaunchBounds`
- 快捷启动器基础应用列表：`launcher/query/QuickAppLauncherBaseQuery.kt`
- 快捷启动器启动流程：`launcher/launch/QuickAppLaunch.kt`
- 启动对象模型：`launcher/model/*`
- 打开应用/URL payload：`launcher/model/OpenAppOrUrlData.kt`
- 图标缓存：`launcher/util/IconResizeCache.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `launcher/query/AppQuery.kt` | 应用查询 |
| `launcher/query/AppSearch.kt` | 应用搜索、排序、key 生成 |
| `launcher/query/ShortcutQuery.kt` | Shortcut 查询 |
| `launcher/query/LauncherIconQuery.kt` | 应用图标和 Shortcut icon resource 解析 |
| `launcher/query/LauncherEnvironment.kt` | 判断当前包是否为桌面启动器 |
| `launcher/query/OpenAppOrUrlQuery.kt` | 打开应用/URL 的查询入口 |
| `launcher/query/QuickAppLauncherBaseQuery.kt` | 快捷启动器基础应用列表 |
| `launcher/ext/AppInfoExt.kt` | 应用信息扩展 |
| `launcher/ext/ShortcutInfoExt.kt` | Shortcut 信息扩展 |
| `launcher/launch/Launcher.kt` | 启动能力 |
| `launcher/launch/QuickAppLaunch.kt` | 快捷启动器冻结后启动流程 |
| `launcher/model/AppInfo.kt` | 应用模型 |
| `launcher/model/LauncherInfo.kt` | 启动信息模型 |
| `launcher/model/OpenAppOrUrlData.kt` | 打开应用/URL payload 模型 |
| `launcher/model/ScaleableDefaults.kt` | 尺寸/缩放默认值 |

## 关键入口

- `Launcher` 是启动能力入口。
- `Launcher.launchAppInPopup` 支持小窗初始位置 bias；部分厂商 windowing mode 可能忽略 bounds。
- `AppQuery`、`ShortcutQuery` 负责元数据查询。
- `LauncherIconQuery`、`LauncherEnvironment`、`OpenAppOrUrlQuery` 负责按需查询。
- `QuickAppLauncherBaseQuery` 提供不含冻结业务的快捷启动器基础应用列表。
- `QuickAppLaunch` 封装快捷启动器的冻结后启动流程。
- `OpenAppOrUrlData` 属于 launcher payload 模型，action 侧只负责解析并分发执行。
- `IconResizeCache` 属于 launcher 侧的图标缓存，不应回流成通用 utils。

## 依赖边界

- `launcher` 可以依赖 `system`。
- `launcher` 不负责冻结业务，不负责浮窗展示，不负责配置持久化。
- `launcher` 不直接读取 settings，小窗位置由 action/overlay 等调用方传入。
