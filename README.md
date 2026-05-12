# SideLeap

Android 侧边手势与快速启动工具

**当前版本：** v1.5.4

## v1.5.4 更新内容

- 新增「生成随机名称」动作，一键生成轻幻想昵称并自动复制到剪贴板
- 将动作选择器中的设置提示和权限提示由 Snackbar 改为 Toast，统一短反馈方式
- 删除选择器中无用的标签 Chip 展示，简化界面
- 优化 PackageManager 调用（`getLaunchIntentForPackage`、`getResourcesForApplication`），减少主线程阻塞
- 清理无引用的 Play Store 图标

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