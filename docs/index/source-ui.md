# SideLeap 源码索引 - ui/

`ui/` 承载页面、组件、导航、主题和 UI 事件层。

## 当前职责

- 应用壳与入口 UI：`ui/SideGestureApp.kt`
- 导航与路由：`ui/navigation/*`
- 页面与 VM：`ui/screen/*`
- 共享组件：`ui/widget/*`
- 密码生成器面板：`ui/widget/password/PasswordGeneratorPanel.kt`
- Dialog 与权限辅助：`ui/dialog/*`、`ui/permission/Permissions.kt`
- 主题与样式：`ui/theme/*`
- UI 事件与 ViewModel 基类：`ui/event/*`、`ui/UDFComponentDefaultsImpl.kt`
- 手势展示扩展：`ui/gesture/*`

## 主要源码路径

| 路径 | 作用 |
|---|---|
| `ui/SideGestureApp.kt` | 应用级 Compose 入口 |
| `ui/navigation/Routes.kt` | 路由定义 |
| `ui/navigation/CompositionLocals.kt` | Compose 注入对象 |
| `ui/screen/home/*` | 首页 |
| `ui/screen/actionselect/*` | 动作选择面板（BottomSheet） |
| `ui/screen/gesture*/*` | 手势配置页 |
| `ui/screen/frozen*/*` | 冻结管理与保护面板（BottomSheet） |
| `ui/screen/quickapplaunchermanage/*` | 快捷启动器管理面板（BottomSheet） |
| `ui/screen/iconresize/*` | 图标尺寸设置面板（BottomSheet） |
| `ui/widget/*` | 按钮、面板、弹层、拖拽等通用组件 |
| `ui/widget/password/*` | 密码生成器运行时面板 |
| `ui/theme/*` | 主题与配色 |
| `ui/gesture/GestureButtonDisplay.kt` | GestureButton 的 Compose 文案展示 |

## 关键入口

- `SideGestureApp` 负责装配 UI 栈。
- 导航路由精简为 6 个全屏目的地（Home、Unlock、AdvancedSettings、GestureSettings、GestureButtonSettings、FrozenAppManage）。
- 其余 Picker 页面（AppBlacklist、ActionSelect、IconResize、WaveAnimationStyle、AdjustGestureAngles、QuickAppLauncherHidden、FrozenAppProtect）改为 ModalBottomSheet，由父屏管理状态；GestureButtonSettings 中的 ActionSelect 禁用 sheet 下滑关闭。
- 各 `*VM` 承担页面/面板状态入口。
- `ActionSelectContent` 的已选栏保持 chip 概览；已选栏下方的已选动作设置区负责长按动作配置和排序。
- 长滑动多动作 `ActionPanel` 支持弧形、自适应列表和自适应网格布局，样式由触钮长滑方向配置决定。
- `VirtualMouseCursor` 绘制动态色圆环准星、点击反馈和拖尾样式；`SideGestureContainer` 承接手势未断触路径的虚拟鼠标移动、防抖、连续位置和停留长按。

## 依赖边界

- `ui` 可以依赖 `gesture`、`action`、`settings`、`launcher`、`freeze`、`overlay`。
- `ui` 通过 VM、状态和事件调用下游领域，不直接读写 DataStore，不直接调用 Shizuku；PackageManager 查询应通过领域边界，权限辅助中的平台权限探测除外。
