# SideLeap 代码索引
## 元信息
- 源码基准 commit：`65a2fdd`
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
│   │   │   ├── event/                   # 事件定义
│   │   │   ├── action/                  # 动作体系 Handler 层
│   │   │   ├── ktx/                     # Kotlin 扩展函数
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
