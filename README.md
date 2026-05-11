# SideLeap

Android 侧边手势与快速启动工具

**当前版本：** v1.5.2

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