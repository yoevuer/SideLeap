# SideLeap 源码索引 - launcher/

`launcher/` 承载应用、Activity、Shortcut、图标和启动能力。

## 当前职责

- 应用与 Shortcut 查询：`launcher/query/*`
- 应用与 Shortcut 扩展：`launcher/ext/*`
- 启动入口：`launcher/launch/Launcher.kt`
- 启动对象模型：`launcher/model/*`
- 图标缓存：`launcher/util/IconResizeCache.kt`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `launcher/query/AppQuery.kt` | 应用查询 |
| `launcher/query/AppSearch.kt` | 应用搜索 |
| `launcher/query/ShortcutQuery.kt` | Shortcut 查询 |
| `launcher/ext/AppInfoExt.kt` | 应用信息扩展 |
| `launcher/ext/ShortcutInfoExt.kt` | Shortcut 信息扩展 |
| `launcher/launch/Launcher.kt` | 启动能力 |
| `launcher/model/AppInfo.kt` | 应用模型 |
| `launcher/model/LauncherInfo.kt` | 启动信息模型 |
| `launcher/model/ScaleableDefaults.kt` | 尺寸/缩放默认值 |

## 关键入口

- `Launcher` 是启动能力入口。
- `AppQuery`、`ShortcutQuery` 负责元数据查询。
- `IconResizeCache` 属于 launcher 侧的图标缓存，不应回流成通用 utils。

## 依赖边界

- `launcher` 可以依赖 `system`。
- `launcher` 不负责冻结业务，不负责浮窗展示，不负责配置持久化。
