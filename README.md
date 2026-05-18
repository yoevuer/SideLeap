# SideLeap

SideLeap 是一个面向 Android 的侧边手势、快捷启动与系统动作增强工具。它通过可配置的侧边/底部触钮，将返回、主页、最近任务、通知面板、应用启动、快捷方式、冻结应用、虚拟鼠标等操作绑定到滑动、长滑、单击和长按手势上。

当前版本：`v1.5.7`

## 功能特性

- 侧边与底部触钮：支持左侧、右侧和底部触发区域。
- 多方向手势：支持短滑、长滑、单击、长按，以及按触钮独立配置的手势角度。
- 动作面板：长滑即时触发时可展开多动作面板，支持弧形和网格样式。
- 快速启动：支持启动应用、启动快捷方式、隐藏应用和最近/频率记录。
- 小窗启动：支持应用小窗打开位置、边缘留白和位置补偿配置。
- 系统动作：支持返回、主页、最近任务、通知面板、快捷面板、音量、静音、媒体控制等动作。
- 工具动作：支持密码生成器、模拟点击当前位置、虚拟鼠标、移动屏幕等增强操作。
- 冻结管理：配合 Shizuku 管理应用冻结、一键冻结和保护名单。
- 个性化显示：支持动态配色、深色模式、动画风格、图标缩放和图标背景色。
- 配置备份：支持本地备份和恢复应用配置。

## 系统要求

- Android 13 及以上，即 API 33+。
- 需要启用无障碍服务以执行手势动作。
- 冻结应用相关功能需要 Shizuku 环境支持。
- 构建环境需要 JDK 17、Android SDK，并使用项目指定的 Gradle/AGP/Kotlin 版本。

## 权限说明

SideLeap 需要较多系统权限来完成手势识别、系统动作、应用查询和后台保活等功能。核心权限用途如下：

- 无障碍服务：用于执行返回、主页、最近任务、点击、打开系统面板等动作。应用不会通过无障碍服务收集个人数据。
- 查询应用列表：用于选择应用、快捷方式、黑名单、隐藏应用和冻结管理。
- 通知、免打扰、相机、网络、Wi-Fi、蓝牙等权限：用于对应系统动作或状态控制。
- Shizuku Provider：用于冻结/解冻应用等需要更高系统能力的功能。
- 电池优化与自启动相关权限：用于提高后台手势服务存活率。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- AndroidX DataStore
- Kotlinx Serialization
- Accessibility Service
- Shizuku
- Coil

## 构建

Debug 构建：

```bash
./gradlew assembleDebug
```

Release 构建：

```bash
./gradlew assembleRelease
```

默认输出路径：

```text
app/build/outputs/apk/debug/
app/build/outputs/apk/release/
```

## 项目结构

```text
app/src/main/java/hunoia/sideleap/
├── action/      动作定义、展示、payload、执行分发和 handler
├── freeze/      冻结应用与 Shizuku 相关能力
├── gesture/     手势按钮、触发方向、角度和识别语义
├── launcher/    应用与快捷方式查询、图标和启动信息
├── overlay/     运行时浮层和窗口承载
├── service/     无障碍服务、运行态协调和动作入口
├── settings/    配置模型、默认值、DataStore、备份恢复
├── system/      系统能力封装
└── ui/          Compose 页面、组件、主题和交互层
```

更多当前架构说明见 `docs/`。

## 开源来源

本项目基于 [SideGesture](https://github.com/aaronzzx/SideGesture) 修改，保留原 `LICENSE` 及作者归属信息。

## License

请查看仓库根目录的 `LICENSE` 文件。
