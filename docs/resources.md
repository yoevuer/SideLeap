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

### 手势识别与动作执行链路
- **入口**: `hunoia/sideleap/SideGestureServiceProxy.kt` - 手势服务代理，处理手势识别，通过 `ActionRegistry` 分发动作
- **中间处理**: 
  - `hunoia/sideleap/action/ActionRegistry.kt` - 动作注册中心，将动作名路由到对应的 `ActionHandler`
  - `hunoia/sideleap/action/handlers/` - 各 `ActionHandler` 实现（NavigationActionHandler、MediaActionHandler、SystemActionHandler 等）
  - `hunoia/sideleap/ui/screen/actionselect/ActionSelectScreen.kt` - 动作选择界面
  - `hunoia/sideleap/constant/GlobalActions.kt` - 全局动作定义
  - `hunoia/sideleap/ui/screen/actionselect/ActionMeta.kt` - 动作元数据管理
- **输出结果**: 动作执行结果（Toast 通知、应用启动等）
- **相关文件**: 
  - `hunoia/sideleap/action/ActionHandler.kt` - 动作处理接口
  - `hunoia/sideleap/action/ActionHandlerContext.kt` - 动作处理上下文
  - `hunoia/sideleap/ktx/GlobalActions.kt` - 动作文本和图标扩展
  - `hunoia/sideleap/utils/Toast.kt` - Toast 通知工具
  - `hunoia/sideleap/utils/ShortcutUtils.kt` - 快捷方式工具

### 随机名称生成链路
- **入口**: `hunoia/sideleap/action/ActionRegistry.kt` - RANDOM_NAME 路由到 RandomNameActionHandler
- **中间处理**: 
  - `hunoia/sideleap/action/handlers/RandomNameActionHandler.kt` - 随机名称生成逻辑
  - `hunoia/sideleap/constant/GlobalActions.kt` - RANDOM_NAME 动作定义
- **输出结果**: 生成的随机名称（Toast 显示）
- **相关文件**: 
  - `hunoia/sideleap/ui/screen/actionselect/ActionSelectScreen.kt` - Toast 显示
  - `hunoia/sideleap/ktx/GlobalActions.kt` - 动作文本映射
  - `hunoia/sideleap/utils/Toast.kt` - Toast 通知工具

### 应用启动链路
- **入口**: `hunoia/sideleap/action/handlers/AppLaunchActionHandler.kt` - action/handler 封装的应用启动逻辑
- **中间处理**: 
  - `hunoia/sideleap/action/handlers/AppLaunchActionHandler.kt` - 包管理器调用（异步）
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
