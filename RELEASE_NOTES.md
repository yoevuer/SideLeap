# SideLeap Release Notes

## v1.5.4

### 新功能
- 新增「生成随机名称」动作：一键生成 4–8 字母轻幻想昵称，自动复制到系统剪贴板；支持重试和失败提示

### 体验调整
- 将动作选择器中的设置提示和权限提示由 Snackbar 改为 Toast，统一短反馈方式
- 删除选择器中无用的标签 Chip 展示，简化界面

### 功能变更
- 移除支付分类及 4 个支付动作（微信扫一扫、微信付款码、支付宝扫一扫、支付宝付款码）
- 删除支付专用 helper 和资源文件

### 性能优化
- 将 PackageManager 的 `getLaunchIntentForPackage` 调用移至 `Dispatchers.IO`，减少主线程阻塞
- 将 `getResourcesForApplication` 调用移至 `Dispatchers.IO`，减少主线程阻塞

### 资源清理
- 删除无引用的 Play Store 图标文件

## v1.5.3

### 新功能
- 新增「生成随机名称」动作：一键生成 4–8 字母轻幻想昵称，自动复制到系统剪贴板；支持重试和失败提示

### 体验调整
- 将动作选择器中的设置提示和权限提示由 Snackbar 改为 Toast，统一短反馈方式
- 删除选择器中无用的标签 Chip 展示，简化界面

### 性能优化
- 将 PackageManager 的 `getLaunchIntentForPackage` 调用移至 `Dispatchers.IO`，减少主线程阻塞
- 将 `getResourcesForApplication` 调用移至 `Dispatchers.IO`，减少主线程阻塞

### 资源清理
- 删除无引用的 Play Store 图标文件

## v1.5.3

### 启动器性能优化
- 减少快捷启动器重组与阻塞 IO
- 优化应用图标异步加载
- 缓存搜索计算结果
- 优化快捷启动器管理页列表加载
- 将冻结应用查询迁出主线程

### 冻结应用启动优化
- 复用 Shizuku UserService Binder 连接，减少重复绑定开销
- 优先使用直接 PackageManager/IPackageManager API 启用冻结应用
- 保留 pm enable fallback，提高兼容性

### 深色模式修复
- 修复深色 MediumContrast 主题下部分 on-container 文本为黑色的问题
- 修复隐藏应用页应用名和包名在深色模式下可读性不足的问题

## v1.5.2

### 签名配置
- 保留 v2 签名并新增 v3 签名支持
- release 构建显式配置 v1/v2/v3/v4 签名开关

### 结构优化
- 将 quick launcher widget 相关文件整理到 ui/widget/quickapplaunch/
- 将 IconResizeCache 移至 utils
- 拆分 Basic.kt 为按职责划分的基础 widget 文件
- 拆分 Context.kt 为按领域划分的 Context 扩展文件
- 删除无引用的 animationstyle placeholder 文件

### 代码复用
- 统一部分 Toast 调用
- 使用 queryIntentActivitiesCompat 替换直接调用
- 提取 WeChat 包名常量

### 验证
- assembleDebug 通过
- assembleRelease 通过
- v2 签名通过、v3 签名通过
- 结构整理残留检查通过

## v1.5.1

### 代码结构优化
- 拆分 DragGestureHandler，收敛手势事件分发与 Compose 手势处理职责
- 移动快速启动器非 Screen 组件，区分页面内容与通用浮层组件
- 将 LauncherInfo 文件名与顶层类名保持一致
- 拆分 IconResize 缓存为独立对象，降低 Routes 职责
- 清理 Toast、SystemAlertWindow、GestureButtonDefaults 中的冗余结构代码

### 验证
- assembleDebug 通过
- assembleRelease 通过
- 结构整理残留检查通过