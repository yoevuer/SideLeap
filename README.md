# SideLeap

Android 侧边手势与快速启动工具

**当前版本：** v1.5.7

## v1.5.7 更新内容

- 全仓热路径 `Log.d` 加 `BuildConfig.DEBUG` 保护
- 提取共享 `WindowManagerUtils`，消除 Overlay 窗口管理重复代码
- `QuickAppLauncherContent` 状态抽取到 `QuickAppLauncherState`，UI 不再直接执行查询/排序/启动
- 密码面板、快捷启动器渲染迁出 `SideGestureService`，Service 不再持有 Compose 渲染
- `ShizukuCommand` 拆分出 `ShizukuBinderExecutor`（绑定+超时+结果解析）
- 新增 `GestureRuntimeSettingsProvider` / `QuickLauncherSettingsProvider` 领域特定联合 Flow
- `DataStore` 反序列化失败日志增加文件尺寸
- 新增 `ShizukuBinderExecutorTest`（12 cases）、`BatchFrozenResultTest`（4 cases）

## v1.5.6 更新内容

- 动作面板长按动作配置
- 冻结应用管理网格与保护名单页
- 小窗打开位置设置
- 应用冻结优化与小窗改进

- 动作元数据数据化重构，消除冗余 when 分支
- 多项性能优化：DataStore 懒加载、协程替代阻塞、JSON 序列化精简
- **移除支付分类及 4 个支付动作（微信扫一扫、微信付款码、支付宝扫一扫、支付宝付款码）**
- 移除支付宝 / 微信支付专用资源与实现
- 构建工具链升级至 Android 16
- 综合性能、结构、构建优化

## 构建

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## 签名配置

Release 签名通过 `local.properties` 中的 `KEYSTORE_PASSWORD` 和 `KEY_PASSWORD` 读取。本地 `keystore` 文件不应提交到版本控制。

## 安全说明

以下文件不应提交到版本控制（已在 `.gitignore` 中排除）：

- `local.properties` — 本地 SDK 路径与签名密码
- `*.jks` / `*.keystore` / `*.p12` / `*.pfx` — 签名密钥库
- `keystore/` — 密钥库目录
- `signing.properties` / `keystore.properties` — 签名配置
- `build/` / `.gradle/` — 构建产物
- `*.apk` / `*.aab` / `*.idsig` — 分发包

## 开源来源

本项目基于 [SideGesture](https://github.com/aaronzzx/SideGesture) 修改，保留原 `LICENSE` 及作者归属信息。