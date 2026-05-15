# SideLeap 源码索引 — ktx/ (archived)

## 当前状态

- `app/src/main/java/hunoia/sideleap/ktx/` 已无 `.kt` 文件。
- `ktx/` 的职责已经迁入明确领域包或被删除。

## 主要迁移去向

| 旧职责 | 当前去向 |
|---|---|
| Navigation CompositionLocal | `ui/navigation` |
| DataStore helper | `settings/internal` |
| Context settings / permissions | `system/intent`、`ui/permission` |
| Event subscribe helpers | `ui/event` |
| WaveStyle helper | `ui/screen/animationstyle/wave` |
| Offset helper | 已内联删除 |
| 旧重复 audio / package manager helper | 已删除，保留 `system/audio`、`system/packages` |

## 说明

- 该页仅用于迁移审计。
- 不再包含旧 `ktx` 文件清单。
- 不应将其理解为当前源码结构。
