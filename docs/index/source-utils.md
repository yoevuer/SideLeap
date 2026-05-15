# SideLeap 源码索引 — utils/ (archived)

## 当前状态

- `app/src/main/java/hunoia/sideleap/utils/` 已无 `.kt` 文件。
- `utils/` 的职责已经迁入明确领域包或被删除。

## 主要迁移去向

| 旧职责 | 当前去向 |
|---|---|
| Accessibility helper | 已删除，能力在 `system/accessibility` |
| KeepAlive / system intent helper | `system/intent` |
| DataStore / JSON helper | `settings/internal`、`core/serialization` |
| Event helpers | `ui/event`、`core/event` |
| Package manager helper | `system/packages` |
| Permission helper | `ui/permission` |
| Wave / theme helper | `ui/screen/animationstyle/wave` |
| 旧重复 audio helper | 已删除，能力在 `system/audio` |

## 说明

- 该页仅用于迁移审计。
- 不再包含旧 `utils` 文件清单。
- 不应将其理解为当前源码结构。
