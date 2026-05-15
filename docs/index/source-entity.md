# SideLeap 源码索引 — entity/ (archived)

## 当前状态

- `app/src/main/java/hunoia/sideleap/entity/` 已无 `.kt` 文件。
- `entity/` 的职责已经迁入明确领域包或被删除。

## 主要迁移去向

| 旧职责 | 当前去向 |
|---|---|
| 动作面板样式 / 动画样式 / 主题模式 | `settings/model`、`ui/screen/animationstyle/wave` |
| 手势按钮 / 方向 / 角度 | `gesture` |
| Launcher 应用 / Shortcut 模型 | `launcher/model` |
| MoveScreen payload | `action` |
| 振动配置 / 效果 | `system/vibration` |
| 旧全局配置模型 | `settings/model` |

## 说明

- 该页仅用于迁移审计。
- 不再包含旧 `entity` 文件清单。
- 不应将其理解为当前源码结构。
