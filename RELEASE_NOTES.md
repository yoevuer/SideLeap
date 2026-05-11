# SideLeap Release Notes

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