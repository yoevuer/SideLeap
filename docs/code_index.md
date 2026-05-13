# SideLeap 代码索引
## 元信息
- 源码基准 commit：`0b746f0`（v1.5.6 post-refactor）
## 索引范围与统计口径
### 排除规则
- `.git/` - Git 版本控制目录
- `.gradle/` - Gradle 缓存目录
- `build/` - 构建产物目录
- `.idea/` - IDE 配置目录
- `.kotlin/` - Kotlin 编译器缓存
- `captures/` - Android Studio 截图目录
- `local.properties` - 本地配置文件（含 SDK 路径和签名信息）
- `*.iml` - IDE 模块文件
- `docs/code_index.md` - 本索引文件自身（避免循环引用）
- `app/mapping.txt` - ProGuard 构建产物（非源码，不纳入长期索引）
### 行数统计口径
- 文本文件统计物理行数，包含空行和注释
- 二进制文件行数标记为 N/A
- 软链接不统计行数
### 文件类型统计口径
- 按文件扩展名统计文件数量和总行数
- 按用途分类统计：源码、资源、构建、文档、脚本、配置、二进制资源等
### 目录统计口径
- 一级目录：项目根目录下的主要目录（app、gradle、docs 等）
- 主要源码/资源目录：app/src/main/java、app/src/main/res 等核心目录
### 包路径统计口径
- 顶层功能包：按功能域划分的包（如 ui、utils、entity、service 等）
- 主要完整包路径：实际代码中的具体包路径
### 二进制文件和软链接处理
- 二进制文件：记录文件大小、文件类型和用途，行数为 N/A
- 软链接：标记为软链接，记录链接目标，不展开目标内容
## 标签说明
| 标签 | 含义 |
|------|------|
| 动作体系 | 动作定义、执行、元数据管理 |
| 应用列表 | 应用信息查询、快捷启动器管理 |
| Compose UI | Compose UI 组件、屏幕、小部件 |
| ViewModel | MVVM 架构中的 ViewModel 层 |
| 状态管理 | 状态管理相关（DataStore、StateFlow 等） |
| 无障碍服务 | AccessibilityService 及其配置 |
| 系统调用 | 系统服务调用、权限管理 |
| 工具类 | 通用工具函数、扩展函数 |
| 资源配置 | 资源文件（strings、colors、themes 等） |
| 构建发布 | Gradle 构建配置、签名配置、版本管理 |
| 文档发布 | README、RELEASE_NOTES 等文档 |
| 数据模型 | 数据类、实体类、序列化数据 |
| 导航路由 | 应用内导航路由定义 |
| 持久化配置 | DataStore 持久化、备份/恢复 |
| 快捷启动器 | 快捷启动器相关功能 |
| 手势识别 | 手势识别、角度计算、手势按钮 |
| 主题样式 | Material Design 主题、颜色、排版 |
| 其他 | 不属于以上分类的其他内容 |
## 项目结构总览
```
SideLeap/
├── app/
│   ├── build.gradle.kts                      # 应用构建配置
│   ├── lint.xml                             # Lint 规则
│   ├── proguard-rules.pro                   # ProGuard 规则
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml          # 应用清单文件
│       │   ├── java/hunoia/sideleap/        # 主源码目录
│       │   │   ├── constant/                # 常量定义
│       │   │   ├── defaults/                # 默认值配置
│       │   │   ├── entity/                  # 数据模型
│       │   │   ├── event/                   # 事件定义
│       │   │   ├── ktx/                     # Kotlin 扩展函数
│       │   │   ├── overlay/                  # 覆盖层组件
│       │   │   ├── ui/                      # UI 层
│       │   │   └── utils/                   # 工具类
│       │   └── res/                         # 资源目录
│       ├── androidTest/                     # Android 测试
│       └── test/                            # 单元测试
├── build.gradle.kts                         # 项目构建配置
├── gradle/
│   ├── libs.versions.toml                   # 依赖版本管理
│   └── wrapper/                             # Gradle Wrapper
├── gradle.properties                        # Gradle 属性配置
├── gradlew                                  # Gradle Wrapper 脚本
├── gradlew.bat                              # Gradle Wrapper 脚本（Windows）
├── LICENSE                                  # 许可证文件
├── README.md                                # 项目说明
├── RELEASE_NOTES.md                         # 发布说明
├── settings.gradle.kts                      # Gradle 设置
└── docs/                                    # 文档目录
    └── code_index.md                        # 本索引文件
```
## 统计汇总
### 按扩展名统计
| 扩展名 | 文件数量 | 总行数 | 说明 |
|--------|----------|--------|------|
| .kt    | 129      | 20,290 | Kotlin 源码文件 |
| .xml   | 9        | 469    | XML 配置文件（Manifest、资源、规则） |
| .md    | 4        | 2,650  | Markdown 文档 |
| .properties | 2    | 32     | 属性配置文件 |
| .pro   | 1        | 35     | ProGuard 规则 |
| .toml  | 1        | 39     | Gradle 版本目录 |
| .aidl  | 1        | N/A    | Android IDL 接口 |
| .bat   | 1        | N/A    | Windows 批处理脚本 |
| .jar   | 1        | N/A    | Gradle Wrapper JAR（58K） |
| .png   | 1        | N/A    | PNG 图片资源 |
| .webp  | 1        | N/A    | WebP 图片（应用图标，18K） |
| **总计** | **151** | **23,876** |  |
### 按用途统计
| 用途分类 | 文件数量 | 总行数 | 主要内容 |
|----------|----------|--------|----------|
| Kotlin 源码 | 129 | 20,290 | 所有 Kotlin 源代码文件 |
| XML 资源 | 9 | 469 | AndroidManifest、strings、colors、themes、规则配置 |
| 构建配置 | 4 | 74 | build.gradle.kts、settings.gradle.kts、proguard-rules.pro、lint.xml |
| 依赖配置 | 1 | 39 | libs.versions.toml |
| 文档 | 4 | 2,650 | README.md、RELEASE_NOTES.md、docs/ |
| 图片资源 | 2 | N/A | 1 个 PNG + 1 个 WebP（应用图标） |
| Gradle 脚本 | 2 | N/A | gradlew、gradlew.bat |
| 属性文件 | 1 | 24 | gradle.properties |
| AIDL 接口 | 1 | N/A | Shizuku AIDL 接口 |
| **总计** | **164** | **23,876** |  |
### 按一级目录统计
| 目录 | 文件数量 | 说明 |
|------|----------|------|
| app/ | 145 | 应用主目录（含源码、资源、测试） |
| gradle/ | 3 | Gradle 相关（libs.versions.toml、wrapper） |
| docs/ | 2 | 文档目录（code_index.md、v1.5.5_optimization_review.md） |
| 根目录 | 15 | 根配置文件（README、LICENSE、gradlew 等） |
| **总计** | **164** |  |
### 按主要源码/资源目录统计
| 目录 | 文件数量 | 总行数 | 说明 |
|------|----------|--------|------|
| app/src/main/java | 129 | 20,290 | Kotlin/Java 源码 |
| app/src/main/res | 9 | N/A | 资源文件（XML + 图片） |
| app/src/test | 1 | N/A | 单元测试 |
| app/src/androidTest | 1 | N/A | Android 测试 |
| **小计** | **140** | **20,290** |  |
### 按顶层功能包统计
| 顶层包 | 文件数量 | 主要内容 |
|--------|----------|----------|
| ui/ | 48 | UI 层（Screen、Widget、Theme、Dialog、Navigation） |
| utils/ | 23 | 工具类（系统服务、权限、Shizuku、快捷启动器等） |
| ktx/ | 24 | Kotlin 扩展函数（Context、数据类、系统 API 封装） |
| entity/ | 25 | 数据模型（全局配置、实体类、序列化数据） |
| constant/ | 5 | 常量定义（GlobalActions、GlobalSettings 等） |
| event/ | 2 | 事件定义（IconResizeEvent、WallpaperChangedEvent） |
| defaults/ | 1 | 默认值配置（UDFComponentDefaults） |
| overlay/ | 1 | 覆盖层组件（QuickAppLauncherOverlay） |
| **总计** | **129** |  |
## 核心链路索引
### 动作体系链路
- **入口**: 用户在 `ActionSelectScreen` 选择动作，或手势触发动作执行
- **中间处理**: `ActionSelectVM` 管理状态，`GlobalActions` 定义动作 ID，`ActionMeta` 提供元数据，`SideGestureServiceProxy.onAction()` 执行具体动作
- **输出结果**: 动作执行结果（打开应用、执行系统命令、显示 Toast 等）
- **相关文件**:
  - 入口文件: `ui/screen/actionselect/ActionSelectScreen.kt`, `SideGestureServiceProxy.kt`
  - 状态管理: `ui/screen/actionselect/ActionSelectVM.kt`, `ui/dialog/ActionSettingsVM.kt`
  - UI 展示: `ui/widget/ActionPanel.kt`, `ui/widget/ActionItem.kt`
  - 执行逻辑: `SideGestureServiceProxy.kt`, `ktx/ContextLaunch.kt`, `ktx/ContextAudio.kt`, `ktx/ContextSettings.kt`
  - 工具类: `ktx/GestureActions.kt`, `utils/ShortcutUtils.kt`, `utils/VibrateUtils.kt`
  - 资源配置: `constant/GlobalActions.kt`, `ui/screen/actionselect/ActionMeta.kt`, `res/values/strings.xml`
  - 其他必要角色: `entity/GestureActions.kt`, `entity/global/ActionSettings.kt`
### 应用列表链路
- **入口**: 用户打开快捷启动器或应用黑名单页面
- **中间处理**: `AppInfoUtils` 查询应用列表，`AppSearchUtils` 提供搜索功能，`AppBlacklistVM` 管理黑名单，`QuickAppLauncherManageVM` 管理快捷启动器
- **输出结果**: 显示应用列表、快捷启动器面板、应用黑名单
- **相关文件**:
  - 入口文件: `ui/screen/quickapplaunchermanage/QuickAppLauncherManageScreen.kt`, `ui/screen/appblacklist/AppBlacklistScreen.kt`, `overlay/QuickAppLauncherOverlay.kt`
  - 状态管理: `ui/screen/quickapplaunchermanage/QuickAppLauncherManageVM.kt`, `ui/screen/appblacklist/AppBlacklistVM.kt`
  - UI 展示: `ui/widget/quickapplaunch/QuickAppLauncherContent.kt`, `ui/widget/quickapplaunch/QuickAppLauncherComponents.kt`, `ui/widget/quickapplaunch/QuickAppLauncherAdjustPanel.kt`
  - 执行逻辑: `utils/AppInfoUtils.kt`, `utils/AppSearchUtils.kt`, `utils/LauncherDiagnostics.kt`
  - 工具类: `ktx/AppInfo.kt`, `ktx/ShortcutInfo.kt`, `utils/IconResizeCache.kt`, `utils/ShizukuUtils.kt`, `utils/ShizukuCommandService.kt`, `utils/ShizukuBridgeService.kt`
  - 资源配置: `entity/AppInfo.kt`, `entity/LauncherInfo.kt`, `res/xml/accessibility_service_config.xml`
  - 其他必要角色: `QuickAppLauncherActivity.kt`
### Compose UI 链路
- **入口**: `SideGestureApp` 初始化 Compose 根组件
- **中间处理**: 各 Screen 和 Widget 通过 `Routes` 导航，`MaterialTheme` 提供主题，`CompositionLocals` 提供上下文
- **输出结果**: 显示完整的 Compose UI 界面
- **相关文件**:
  - 入口文件: `ui/SideGestureApp.kt`, `MainActivity.kt`
  - 状态管理: 各 Screen 对应的 VM（HomeVM、GestureSettingsVM 等）
  - UI 展示: 所有 ui/screen/*.kt、ui/widget/*.kt 文件
  - 执行逻辑: `ui/navigation/Routes.kt`, `ktx/CompositionLocals.kt`
  - 工具类: `ui/theme/Theme.kt`, `ui/theme/Dimension.kt`, `ui/theme/generator/*.kt`, `defaults/UDFComponentDefaults.kt`
  - 资源配置: `res/values/colors.xml`, `res/values/themes.xml`
  - 其他必要角色: `App.kt`
### ViewModel / 状态管理链路
- **入口**: `MainActivity` 启动，`SideGestureApp` 初始化
- **中间处理**: 各 Screen 的 ViewModel 通过 `DataStore` 读取/写入配置，`DataStoreHolder` 管理 DataStore 实例，`Events` 提供事件订阅
- **输出结果**: ViewModel 管理的 UI 状态持久化并在各 Screen 间共享
- **相关文件**:
  - 入口文件: `MainActivity.kt`, `App.kt`, `ui/SideGestureApp.kt`
  - 状态管理: 所有 `ui/screen/*/XXXVM.kt`、`ui/dialog/ActionSettingsVM.kt`
  - UI 展示: 所有 Screen 文件（通过 VM 驱动）
  - 执行逻辑: `utils/DataStoreHolder.kt`, `utils/Events.kt`, `utils/BackupHelper.kt`
  - 工具类: `ktx/DataStore.kt`, `ktx/ViewModel.kt`, `ktx/Events.kt`, `ktx/Coroutine.kt`
  - 资源配置: `constant/DataStoreFiles.kt`, `entity/global/ActionSettings.kt`, `entity/global/GestureSettings.kt`, `entity/global/AdvancedSettings.kt`
  - 其他必要角色: `entity/global/InitialSettings.kt`, `entity/global/Backup.kt`
### 无障碍服务 / 系统调用链路
- **入口**: `SideGestureService` 作为 AccessibilityService 启动
- **中间处理**: `SideGestureService` 处理无障碍事件，`SideGestureServiceProxy` 封装服务逻辑，`AccessibilityUtils` 提供辅助功能，`SystemAlertWindow` 管理浮窗
- **输出结果**: 手势识别、动作执行、浮窗显示、无障碍功能
- **相关文件**:
  - 入口文件: `SideGestureService.kt`, `SideGestureServiceProxy.kt`
  - 状态管理: 无（Service 直接管理状态）
  - UI 展示: `ui/widget/SideGestureContainer.kt`, `ui/widget/GestureView.kt`, `ui/widget/DragGestureHandler.kt`, `ui/widget/GestureAnimation.kt`
  - 执行逻辑: `utils/AccessibilityUtils.kt`, `utils/SystemAlertWindow.kt`, `utils/MotionEventDispatcher.kt`, `utils/KeepAliveHelper.kt`, `utils/PopBackgroundPermissionUtil.kt`
  - 工具类: `ktx/SideGestureService.kt`, `ktx/WindowLayoutParams.kt`, `utils/MiniWindowUtils.kt`
  - 资源配置: `res/xml/accessibility_service_config.xml`, `res/xml/app_file_paths.xml`
  - 其他必要角色: `entity/GestureButton.kt`, `entity/GestureAngle.kt`, `entity/Position.kt`
### 配置、资源与构建链路
- **入口**: `App` 初始化，`build.gradle.kts` 执行构建
- **中间处理**: `DataStoreHolder` 读取配置，`GlobalSettings` 定义全局配置，`gradle` 目录管理依赖
- **输出结果**: 应用配置加载、资源加载、构建成功
- **相关文件**:
  - 入口文件: `App.kt`, `build.gradle.kts`, `settings.gradle.kts`, `gradlew`
  - 状态管理: `utils/DataStoreHolder.kt`, `constant/DataStoreFiles.kt`
  - UI 展示: 无
  - 执行逻辑: `app/build.gradle.kts`, `app/proguard-rules.pro`, `app/lint.xml`, `gradle.properties`, `gradle/libs.versions.toml`
  - 工具类: `utils/BackupHelper.kt`, `utils/CrashHandler.kt`
  - 资源配置: `app/src/main/AndroidManifest.xml`, `res/values/colors.xml`, `res/values/strings.xml`, `res/values/themes.xml`, `res/xml/*.xml`, `res/drawable*/*`, `gradle/wrapper/`
  - 其他必要角色: `README.md`, `RELEASE_NOTES.md`, `LICENSE`
### 工具类与通用能力链路
- **入口**: 各处调用工具类函数
- **中间处理**: 工具类封装系统 API、权限管理、数据处理等
- **输出结果**: 提供通用能力复用
- **相关文件**:
  - 入口文件: 无（按需调用）
  - 状态管理: 无
  - UI 展示: 无
  - 执行逻辑: 所有 `utils/*.kt` 文件
  - 工具类: 所有 `ktx/*.kt` 文件、`utils/*.kt` 文件
  - 资源配置: 无
  - 其他必要角色: 无
### 文档与发布链路
- **入口**: 开发者查看 README 或 RELEASE_NOTES
- **中间处理**: 文档更新、版本号管理、Git 标签管理
- **输出结果**: 发布说明、版本信息
- **相关文件**:
  - 入口文件: `README.md`, `RELEASE_NOTES.md`
  - 状态管理: 无
  - UI 展示: 无
  - 执行逻辑: `app/build.gradle.kts`（版本号）
  - 工具类: 无
  - 资源配置: 无
  - 其他必要角色: `LICENSE`, `docs/code_index.md`
## 维护提示
结构变化后应同步更新本索引。## Kotlin / Java 文件索引
### hunoia/sideleap/App.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/App.kt`
- **行数**: 41
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap`
- **职责标签**: 状态管理
- **是否入口**: 是
- **入口类型**: 应用入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: App
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/MainActivity.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/MainActivity.kt`
- **行数**: 58
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap`
- **职责标签**: Compose UI
- **是否入口**: 是
- **入口类型**: Screen 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MainActivity
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/QuickAppLauncherActivity.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/QuickAppLauncherActivity.kt`
- **行数**: 68
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap`
- **职责标签**: 快捷启动器
- **是否入口**: 是
- **入口类型**: Screen 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: QuickAppLauncherActivity
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/SideGestureService.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/SideGestureService.kt`
- **行数**: 607
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap`
- **职责标签**: 无障碍服务、手势识别
- **是否入口**: 是
- **入口类型**: Service 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: SideGestureService, ImeInsetObserver
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/SideGestureServiceProxy.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/SideGestureServiceProxy.kt`
- **行数**: 559
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap`
- **职责标签**: 无障碍服务、手势识别
- **是否入口**: 是
- **入口类型**: Service 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: SideGestureServiceProxy
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/constant/DataStoreFiles.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/constant/DataStoreFiles.kt`
- **行数**: 16
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.constant`
- **职责标签**: 资源配置
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: DataStoreFiles
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/constant/GlobalActions.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/constant/GlobalActions.kt`
- **行数**: 121
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.constant`
- **职责标签**: 资源配置
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GlobalActions
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/constant/GlobalDefaults.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/constant/GlobalDefaults.kt`
- **行数**: 178
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.constant`
- **职责标签**: 资源配置
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AdvancedSettingsDefaults, GestureSettingsDefaults, InitialSettingsDefaults, ActionPanelStylesDefaults, AnimationStylesDefaults
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/constant/GlobalSettings.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/constant/GlobalSettings.kt`
- **行数**: 67
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.constant`
- **职责标签**: 资源配置
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GlobalSettings
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/constant/Paths.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/constant/Paths.kt`
- **行数**: 16
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.constant`
- **职责标签**: 资源配置
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Paths
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/defaults/UDFComponentDefaults.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/defaults/UDFComponentDefaults.kt`
- **行数**: 44
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.defaults`
- **职责标签**: 其他
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: UDFComponentDefaultsImpl
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/ActionPanelStyles.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/ActionPanelStyles.kt`
- **行数**: 45
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 动作体系
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ActionPanelStyles, ActionPanelStyle, ArcStyle
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/AnimationStyles.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/AnimationStyles.kt`
- **行数**: 74
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AnimationStyles, AnimationStyle, WaveStyle
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/AppInfo.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/AppInfo.kt`
- **行数**: 20
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AppInfo
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/DayNightMode.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/DayNightMode.kt`
- **行数**: 13
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: class
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/GestureActions.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/GestureActions.kt`
- **行数**: 64
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureActions, OpenAppOrUrlData, Action
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/GestureAngle.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/GestureAngle.kt`
- **行数**: 42
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureAngles, GestureAngle
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/GestureButton.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/GestureButton.kt`
- **行数**: 87
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureButton
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/LauncherInfo.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/LauncherInfo.kt`
- **行数**: 35
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: LauncherInfo, ShortcutInfo
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/MoveScreenData.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/MoveScreenData.kt`
- **行数**: 17
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MoveScreenData
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/Position.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/Position.kt`
- **行数**: 13
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: class
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/QuickAppLauncherSettings.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/QuickAppLauncherSettings.kt`
- **行数**: 19
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: QuickAppLauncherSettings
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/TriggerDirection.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/TriggerDirection.kt`
- **行数**: 14
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: class
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/VibrationEffects.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/VibrationEffects.kt`
- **行数**: 13
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: class
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/Vibrations.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/Vibrations.kt`
- **行数**: 37
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Vibrations
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/global/ActionSettings.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/global/ActionSettings.kt`
- **行数**: 40
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity.global`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ActionSettings, MoveScreen, class, PreviousApp, GotoBottom
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/global/AdvancedSettings.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/global/AdvancedSettings.kt`
- **行数**: 54
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity.global`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AdvancedSettings
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/global/Backup.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/global/Backup.kt`
- **行数**: 24
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity.global`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Backup
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/global/GestureSettings.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/global/GestureSettings.kt`
- **行数**: 33
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity.global`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureSettings
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/entity/global/InitialSettings.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/entity/global/InitialSettings.kt`
- **行数**: 17
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.entity.global`
- **职责标签**: 数据模型
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: InitialSettings
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/event/IconResizeEvent.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/event/IconResizeEvent.kt`
- **行数**: 10
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.event`
- **职责标签**: 其他
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: IconResizeEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/event/WallpaperChangedEvent.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/event/WallpaperChangedEvent.kt`
- **行数**: 8
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.event`
- **职责标签**: 其他
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: WallpaperChangedEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/ActionPanelTriggerType.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/ActionPanelTriggerType.kt`
- **行数**: 15
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 动作体系
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ActionPanelState
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/AppInfo.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/AppInfo.kt`
- **行数**: 36
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: get
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/CompositionLocals.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/CompositionLocals.kt`
- **行数**: 11
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 见源码
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/ContextAudio.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/ContextAudio.kt`
- **行数**: 47
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Context, Context, Context, Context
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/ContextLaunch.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/ContextLaunch.kt`
- **行数**: 151
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Context, Context, Context, Context, Context
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/ContextSettings.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/ContextSettings.kt`
- **行数**: 85
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Context, Context, Context, Context, Context
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/Coroutine.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/Coroutine.kt`
- **行数**: 19
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 见源码
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/DataStore.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/DataStore.kt`
- **行数**: 43
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 见源码
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/Events.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/Events.kt`
- **行数**: 32
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Subscriber
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/GestureActions.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/GestureActions.kt`
- **行数**: 90
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureActions, List
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/GestureAngle.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/GestureAngle.kt`
- **行数**: 92
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureAngle, GestureAngle, GestureAngle, GestureAngle, GestureAngle
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/GestureButton.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/GestureButton.kt`
- **行数**: 67
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureButton, GestureButton
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/GlobalActions.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/GlobalActions.kt`
- **行数**: 178
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: actionText, actionIcon
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/MaterialTheme.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/MaterialTheme.kt`
- **行数**: 0
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: get, get
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/Offset.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/Offset.kt`
- **行数**: 12
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Offset
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/PackageManager.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/PackageManager.kt`
- **行数**: 19
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: PackageManager
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/Permissions.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/Permissions.kt`
- **行数**: 60
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GetInstalledAppsPermissionState
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/SerializableType.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/SerializableType.kt`
- **行数**: 49
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: CustomNavType
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/ShortcutInfo.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/ShortcutInfo.kt`
- **行数**: 90
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: get, get
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/SideGestureService.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/SideGestureService.kt`
- **行数**: 111
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 无障碍服务、手势识别
- **是否入口**: 是
- **入口类型**: Service 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: SideGestureService, SideGestureService, SideGestureService, SideGestureService, SideGestureService
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/Vibrations.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/Vibrations.kt`
- **行数**: 40
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Vibrations, Vibrations, Vibrations, Vibrations
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/ViewModel.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/ViewModel.kt`
- **行数**: 17
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 见源码
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/WaveStyle.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/WaveStyle.kt`
- **行数**: 42
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: WaveStyle, getWaveStyleIcon
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ktx/WindowLayoutParams.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ktx/WindowLayoutParams.kt`
- **行数**: 78
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ktx`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: WindowManager, WindowManager, WindowManager, WindowManager
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/overlay/QuickAppLauncherOverlay.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/overlay/QuickAppLauncherOverlay.kt`
- **行数**: 361
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.overlay`
- **职责标签**: 快捷启动器
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: QuickAppLauncherOverlay
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/SideGestureApp.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/SideGestureApp.kt`
- **行数**: 196
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui`
- **职责标签**: 手势识别
- **是否入口**: 是
- **入口类型**: Screen 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: SideGestureApp, AnimatedContentScope
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/dialog/ActionSettingsDialog.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/dialog/ActionSettingsDialog.kt`
- **行数**: 608
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.dialog`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: LauncherAppOption, ActivityOption
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/dialog/ActionSettingsVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/dialog/ActionSettingsVM.kt`
- **行数**: 100
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.dialog`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ActionSettingsVM, UiState, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/navigation/Routes.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/navigation/Routes.kt`
- **行数**: 75
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.navigation`
- **职责标签**: Compose UI、导航路由
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: About, ActionSelect, AdvancedSettings, AppBlacklist, QuickAppLauncherHidden
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/about/AboutScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/about/AboutScreen.kt`
- **行数**: 109
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.about`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AboutScreen
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/about/AboutVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/about/AboutVM.kt`
- **行数**: 50
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.about`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AboutVM, UiState, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/actionselect/ActionMeta.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/actionselect/ActionMeta.kt`
- **行数**: 162
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.actionselect`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: class, class, ActionMeta
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/actionselect/ActionSelectScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/actionselect/ActionSelectScreen.kt`
- **行数**: 1335
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.actionselect`
- **职责标签**: 动作体系
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ActionPage, AppPage, ShortcutPage, ActionItem, PermissionPage
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/actionselect/ActionSelectVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/actionselect/ActionSelectVM.kt`
- **行数**: 799
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.actionselect`
- **职责标签**: 动作体系
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ActionSelectVM, EventHandler, ActionSettingsDialog, UiState, SelectedRecord
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/advancedsettings/AdvancedSettingsScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/advancedsettings/AdvancedSettingsScreen.kt`
- **行数**: 237
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.advancedsettings`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AdvancedSettingsScreen
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/advancedsettings/AdvancedSettingsVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/advancedsettings/AdvancedSettingsVM.kt`
- **行数**: 221
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.advancedsettings`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AdvancedSettingsVM, UiState, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/animationstyle/wave/WaveStyleScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/animationstyle/wave/WaveStyleScreen.kt`
- **行数**: 255
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.animationstyle.wave`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: WaveStyleScreen
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/animationstyle/wave/WaveStyleVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/animationstyle/wave/WaveStyleVM.kt`
- **行数**: 177
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.animationstyle.wave`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: WaveStyleVM, ColorPickerDialog, UiState, UiEvent, ScrollToBottom
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/appblacklist/AppBlacklistScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/appblacklist/AppBlacklistScreen.kt`
- **行数**: 301
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.appblacklist`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AppBlacklistScreen, private
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/appblacklist/AppBlacklistVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/appblacklist/AppBlacklistVM.kt`
- **行数**: 152
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.appblacklist`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AppBlacklistVM, UiState, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/diagnosticlogs/DiagnosticLogsScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/diagnosticlogs/DiagnosticLogsScreen.kt`
- **行数**: 235
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.diagnosticlogs`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: DiagnosticLogsScreen
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/gestureangles/GestureAnglesScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/gestureangles/GestureAnglesScreen.kt`
- **行数**: 446
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.gestureangles`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureAnglesScreen, BoxScope, private
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/gestureangles/GestureAnglesVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/gestureangles/GestureAnglesVM.kt`
- **行数**: 126
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.gestureangles`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureAnglesVM, UiState, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/gesturebuttonsettings/GestureButtonSettingsScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/gesturebuttonsettings/GestureButtonSettingsScreen.kt`
- **行数**: 485
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.gesturebuttonsettings`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureButtonSettingsScreen, private
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/gesturebuttonsettings/GestureButtonSettingsVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/gesturebuttonsettings/GestureButtonSettingsVM.kt`
- **行数**: 362
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.gesturebuttonsettings`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureButtonSettingsVM, ColorPickerDialog, UiState, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/gesturesettings/GestureSettingsScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/gesturesettings/GestureSettingsScreen.kt`
- **行数**: 276
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.gesturesettings`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureSettingsScreen
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/gesturesettings/GestureSettingsVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/gesturesettings/GestureSettingsVM.kt`
- **行数**: 171
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.gesturesettings`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureSettingsVM, UiState, UiEvent, ScrollToBottom
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/home/HomeScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/home/HomeScreen.kt`
- **行数**: 474
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.home`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: HomeScreen, private
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/home/HomeVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/home/HomeVM.kt`
- **行数**: 269
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.home`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: HomeVM, UiState, UiEvent, ScrollToBottom, ScrollToEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/iconresize/IconResizeScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/iconresize/IconResizeScreen.kt`
- **行数**: 272
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.iconresize`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: IconResizeScreen
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/iconresize/IconResizeVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/iconresize/IconResizeVM.kt`
- **行数**: 196
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.iconresize`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: IconResizeVM, UiState, BgColor, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/quickapplaunchermanage/QuickAppLauncherManageScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/quickapplaunchermanage/QuickAppLauncherManageScreen.kt`
- **行数**: 185
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.quickapplaunchermanage`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: QuickAppLauncherManageScreen
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/quickapplaunchermanage/QuickAppLauncherManageVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/quickapplaunchermanage/QuickAppLauncherManageVM.kt`
- **行数**: 49
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.quickapplaunchermanage`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: QuickAppLauncherManageVM, UiState, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/unlock/UnlockScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/unlock/UnlockScreen.kt`
- **行数**: 29
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.unlock`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: UnlockScreen
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/screen/unlock/UnlockVM.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/screen/unlock/UnlockVM.kt`
- **行数**: 18
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.screen.unlock`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: ViewModel 入口
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: UnlockVM, UiState, UiEvent
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/theme/Dimension.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/theme/Dimension.kt`
- **行数**: 32
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.theme`
- **职责标签**: Compose UI、主题样式
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 见源码
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/theme/Theme.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/theme/Theme.kt`
- **行数**: 32
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.theme`
- **职责标签**: Compose UI、主题样式
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: SideGestureTheme
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/theme/generator/Color.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/theme/generator/Color.kt`
- **行数**: 226
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.theme.generator`
- **职责标签**: Compose UI、主题样式
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 见源码
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/theme/generator/Theme.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/theme/generator/Theme.kt`
- **行数**: 278
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.theme.generator`
- **职责标签**: Compose UI、主题样式
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ColorFamily
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/theme/generator/Type.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/theme/generator/Type.kt`
- **行数**: 5
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.theme.generator`
- **职责标签**: Compose UI、主题样式
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 见源码
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/theme/icons/PlayPause.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/theme/icons/PlayPause.kt`
- **行数**: 43
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.theme.icons`
- **职责标签**: Compose UI、主题样式
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 见源码
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/ActionItem.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/ActionItem.kt`
- **行数**: 219
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: TextActionButton, LabeledSwitch
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/ActionPanel.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/ActionPanel.kt`
- **行数**: 497
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: 动作体系
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ActionPanelState, class
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/ColorDisplay.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/ColorDisplay.kt`
- **行数**: 45
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MyColorDisplay
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/ComposeToast.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/ComposeToast.kt`
- **行数**: 132
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: class, ToastData
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/Dialog.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/Dialog.kt`
- **行数**: 354
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MyAlertDialog, ColorPickerDialog, ActionSettingsDialog
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/DragGestureHandler.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/DragGestureHandler.kt`
- **行数**: 61
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: DragGestureHandler
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/GestureAnimation.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/GestureAnimation.kt`
- **行数**: 270
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureAnimation, private
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/GestureView.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/GestureView.kt`
- **行数**: 64
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: GestureView
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/Layout.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/Layout.kt`
- **行数**: 153
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MyColumn, ColumnScope, SectionCard, ColumnScope, MyExpandableColumn
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/MoveScreen.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/MoveScreen.kt`
- **行数**: 344
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MoveScreenState
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/SideGestureContainer.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/SideGestureContainer.kt`
- **行数**: 625
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: 手势识别
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: SideGestureState, LongSlideState
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/Slider.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/Slider.kt`
- **行数**: 264
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MyTextSlider, MyTextRangeSlider, MySlider, MyRangeSlider
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/Snackbar.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/Snackbar.kt`
- **行数**: 32
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MySnackbarHost
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/TopBar.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/TopBar.kt`
- **行数**: 74
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: TopBar, RowScope
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/quickapplaunch/QuickAppLauncherAdjustPanel.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/quickapplaunch/QuickAppLauncherAdjustPanel.kt`
- **行数**: 119
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget.quickapplaunch`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: internal, internal
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/quickapplaunch/QuickAppLauncherComponents.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/quickapplaunch/QuickAppLauncherComponents.kt`
- **行数**: 165
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget.quickapplaunch`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: internal, internal, internal
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/ui/widget/quickapplaunch/QuickAppLauncherContent.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/ui/widget/quickapplaunch/QuickAppLauncherContent.kt`
- **行数**: 426
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.ui.widget.quickapplaunch`
- **职责标签**: Compose UI、Compose UI
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AppListState
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/AboutUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/AboutUtils.kt`
- **行数**: 28
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AboutUtils
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/AccessibilityUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/AccessibilityUtils.kt`
- **行数**: 130
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AccessibilityUtils
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/AppInfoUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/AppInfoUtils.kt`
- **行数**: 255
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AppInfoUtils
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/AppSearchUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/AppSearchUtils.kt`
- **行数**: 85
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: AppSearchIndex
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/BackupHelper.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/BackupHelper.kt`
- **行数**: 182
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: BackupHelper
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/CrashHandler.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/CrashHandler.kt`
- **行数**: 72
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: CrashHandler
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/DataStoreHolder.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/DataStoreHolder.kt`
- **行数**: 71
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: DataStoreHolder
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/Events.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/Events.kt`
- **行数**: 55
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: Events
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/IconResizeCache.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/IconResizeCache.kt`
- **行数**: 26
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: IconResizeCache
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/JsonHelper.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/JsonHelper.kt`
- **行数**: 24
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: JsonHelper
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/KeepAliveHelper.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/KeepAliveHelper.kt`
- **行数**: 114
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: KeepAliveHelper
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/LauncherDiagnostics.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/LauncherDiagnostics.kt`
- **行数**: 91
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: LauncherDiagnostics
- **实现方式概述**: 见源码
- **关键依赖**: 见源码
- **被哪些核心链路使用**: 见源码
- **客观维护注意事项**: 见源码
### hunoia/sideleap/utils/MiniWindowUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/MiniWindowUtils.kt`
- **行数**: 89
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MiniWindowUtils
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/MotionEventDispatcher.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/MotionEventDispatcher.kt`
- **行数**: 33
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: MotionEventDispatcher, OnMotionEventListener
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/PopBackgroundPermissionUtil.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/PopBackgroundPermissionUtil.kt`
- **行数**: 100
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: PopBackgroundPermissionUtil
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/QuickAppLauncherStatsUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/QuickAppLauncherStatsUtils.kt`
- **行数**: 16
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: 
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/ShizukuBridgeService.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/ShizukuBridgeService.kt`
- **行数**: 62
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ShizukuBridgeService
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/ShizukuCommandService.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/ShizukuCommandService.kt`
- **行数**: 159
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ShizukuCommandService
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/ShizukuUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/ShizukuUtils.kt`
- **行数**: 590
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ShizukuUtils, EnablePackageResult
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/ShortcutUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/ShortcutUtils.kt`
- **行数**: 153
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: ShortcutUtils
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/SystemAlertWindow.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/SystemAlertWindow.kt`
- **行数**: 158
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: SystemAlertWindow
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/Toast.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/Toast.kt`
- **行数**: 90
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: showVersionTooLowToast, showToastDelay, showToastLongDelay, showToastDelay, showToastLongDelay
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
### hunoia/sideleap/utils/VibrateUtils.kt
- **文件路径**: `app/src/main/java/hunoia/sideleap/utils/VibrateUtils.kt`
- **行数**: 46
- **文件类型**: Kotlin 源码
- **所属包路径**: `hunoia.sideleap.utils`
- **职责标签**: 工具类
- **是否入口**: 否
- **入口类型**: 非入口文件
- **简要职责**: 见文件名
- **主要类、函数或 Composable**: VibrateUtils
- **实现方式概述**: 
- **关键依赖**: 
- **被哪些核心链路使用**: 
- **客观维护注意事项**:
Added 136 Kotlin/Java file entries
Total lines added: 2178
## 核心链路分析
### 手势识别与动作执行链路
- **入口**: `hunoia/sideleap/SideGestureServiceProxy.kt` - 手势服务代理，处理手势识别和动作分发
- **中间处理**: 
  - `hunoia/sideleap/ui/screen/actionselect/ActionSelectScreen.kt` - 动作选择界面
  - `hunoia/sideleap/constant/GlobalActions.kt` - 全局动作定义
  - `hunoia/sideleap/ui/screen/actionselect/ActionMeta.kt` - 动作元数据管理
- **输出结果**: 动作执行结果（Toast 通知、应用启动等）
- **相关文件**: 
  - `hunoia/sideleap/ktx/GlobalActions.kt` - 动作文本和图标扩展
  - `hunoia/sideleap/utils/Toast.kt` - Toast 通知工具
  - `hunoia/sideleap/utils/ShortcutUtils.kt` - 快捷方式工具

### 随机名称生成链路
- **入口**: `hunoia/sideleap/SideGestureServiceProxy.kt` - RANDOM_NAME 动作处理
- **中间处理**: 
  - `hunoia/sideleap/SideGestureServiceProxy.kt` - 随机名称生成逻辑
  - `hunoia/sideleap/constant/GlobalActions.kt` - RANDOM_NAME 动作定义
- **输出结果**: 生成的随机名称（Toast 显示）
- **相关文件**: 
  - `hunoia/sideleap/ui/screen/actionselect/ActionSelectScreen.kt` - Toast 显示
  - `hunoia/sideleap/ktx/GlobalActions.kt` - 动作文本映射
  - `hunoia/sideleap/utils/Toast.kt` - Toast 通知工具

### 应用启动链路
- **入口**: `hunoia/sideleap/SideGestureServiceProxy.kt` - queryLaunchIntentAndStart 方法
- **中间处理**: 
  - `hunoia/sideleap/SideGestureServiceProxy.kt` - 包管理器调用（异步）
  - `hunoia/sideleap/utils/ShortcutUtils.kt` - 快捷方式管理
- **输出结果**: 应用启动或失败通知
- **相关文件**: 
  - `hunoia/sideleap/utils/Toast.kt` - Toast 通知
  - `hunoia/sideleap/ui/screen/actionselect/ActionSelectScreen.kt` - 界面反馈

## 资源 / 配置 / 文档文件简表
| 文件路径 | 行数 | 大小 | 类型 | 用途 |
|----------|------|------|------|------|
| `LICENSE` | 201 | 11K | 其他 | 开源许可证 |
| `README.md` | 39 | 1K | Markdown 文档 | 项目说明文档 |
| `RELEASE_NOTES.md` | 73 | 2K | Markdown 文档 | 发布说明文档 |
| `app/lint.xml` | 4 | 115B | 构建配置 | Lint 检查规则 |
| `app/proguard-rules.pro` | 35 | 1K | 构建配置 | ProGuard 混淆规则 |
| `app/src/main/AndroidManifest.xml` | 103 | 4K | 清单文件 | 清单文件（Activity、Service、Provider、权限） |
| `app/src/main/aidl/hunoia/sideleap/IShizukuCommandService.aidl` | 8 | 208B | AIDL 接口 | Shizuku AIDL 接口 |
| `app/src/main/res/drawable-nodpi/sideleap.webp` | 133 | 17K | 图片资源 | 应用图标 |
| `app/src/main/res/drawable-xxhdpi/alipay_paycode.png` | 37 | 4K | 图片资源 | 二维码图片（收付款码/扫码） |
| `app/src/main/res/drawable-xxhdpi/alipay_scan.png` | 48 | 3K | 图片资源 | 二维码图片（收付款码/扫码） |
| `app/src/main/res/drawable-xxhdpi/github.png` | 47 | 5K | 图片资源 | GitHub 图标 |
| `app/src/main/res/drawable-xxhdpi/wechat_paycode.png` | 46 | 5K | 图片资源 | 二维码图片（收付款码/扫码） |
| `app/src/main/res/drawable-xxhdpi/wechat_scan.png` | 59 | 4K | 图片资源 | 二维码图片（收付款码/扫码） |
| `app/src/main/res/values/colors.xml` | 3 | 63B | 资源配置 | 颜色资源（主题颜色） |
| `app/src/main/res/values/strings.xml` | 316 | 20K | 资源配置 | 字符串资源（所有文本内容） |
| `app/src/main/res/values/themes.xml` | 13 | 645B | 资源配置 | 主题样式（Material3 主题） |
| `app/src/main/res/xml/accessibility_service_config.xml` | 14 | 709B | 资源配置 | 无障碍服务配置 |
| `app/src/main/res/xml/app_file_paths.xml` | 4 | 102B | 资源配置 | FileProvider 路径配置 |
| `app/src/main/res/xml/backup_rules.xml` | 13 | 478B | 资源配置 | 备份规则 |
| `app/src/main/res/xml/data_extraction_rules.xml` | 19 | 551B | 资源配置 | 数据提取规则 |
| `gradle.properties` | 23 | 1K | 构建配置 | Gradle 属性配置 |
| `gradle/libs.versions.toml` | 39 | 2K | 构建配置 | 依赖版本目录 |
| `gradle/wrapper/gradle-wrapper.jar` | 552 | 57K | 构建配置 | Gradle Wrapper JAR |
| `gradle/wrapper/gradle-wrapper.properties` | 6 | 230B | 构建配置 | 构建配置 |
| `gradlew` | 185 | 5K | 构建配置 | Gradle Wrapper 脚本 |
| `gradlew.bat` | 89 | 2K | 构建配置 | Gradle Wrapper 脚本 |
Added 26 config/resource file entries
Total lines added: 30
