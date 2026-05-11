# SideLeap Release Notes

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