# 功能落位示例

## 密码生成器

推荐拆分：

```text
action
  - GeneratePasswordAction
  - CopyGeneratedPasswordAction
  - PasswordPayload

settings
  - PasswordGeneratorSettings
  - 默认长度
  - 默认字符集
  - 是否包含数字/大小写/符号
  - 配置持久化

ui
  - 密码生成配置面板
  - 密码显示/隐藏
  - 熵值展示
  - 复制按钮
  - 重新生成按钮

system
  - ClipboardGateway.copyText()

core
  - 如果存在完全通用的随机数/熵计算基础工具，可放 core
```

注意：
- UI 不直接写剪贴板。
- 密码生成动作通过 `action` 触发。
- 剪贴板调用通过 `system`。
- 默认规则通过 `settings` 管理。

## 添加新手势

推荐拆分：

```text
gesture
  - 新手势模型
  - 新识别逻辑
  - 新 GestureTrigger

settings
  - 新手势开关
  - 新阈值配置
  - 新动作绑定配置

ui
  - 新配置项
  - 新说明文案
  - 新手势绑定入口

service
  - 接入新 GestureRecognizer
```

## 添加新动作

推荐拆分：

```text
action
  - ActionId
  - ActionDefinition
  - Payload schema

对应领域
  - ActionHandler

ui
  - 动作选择项
  - payload 配置页面

settings
  - 动作绑定配置持久化
```
