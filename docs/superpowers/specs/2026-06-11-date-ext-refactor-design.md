# 日期/时间扩展重构 — 设计文档

> 日期：2026-06-11
> 状态：待用户复审
> 目标模块：`Basic`（JitPack 库）
> 前置依赖：`docs/superpowers/specs/2026-06-11-app-locale-formatter-design.md`（已落地）

---

## 1. 背景与目标

### 1.1 痛点

- `Basic/.../extend/DateExt.kt`（289 行 22 个扩展）+ `Basic/.../manager/DateManager.kt`（87 行 14 个静态模板）混在一起。
- 业务方反馈"扩展函数混乱，记不清写了哪些 LocalDate 扩展"——本质是"看不见全貌"问题。
- 老的 `toDisplayString(pattern)` / `DateManager.CN_DATE` / `DateManager.US_DATE` / `dayOfWeekChinese` 等 API 鼓励"业务方传中式 / 欧式 pattern"，与"AppLocale 唯一真相源"的设计哲学冲突。

### 1.2 目标

1. 按"接收者类型"把 `DateExt.kt` 拆成 4 个小文件，让 AS 补全即目录。
2. 把 `DateManager` 中"locale 相关"的部分（`US_DATE` / `EU_DATE` / `CN_DATE` / `CN_DATE_TIME`）删除，由上一轮已落地的 `AppDateFormatter` 覆盖。
3. 把 `DateManager` 重命名为 `DatePatterns`，加显眼 KDoc 明确"本类 vs `AppDateFormatter` 的职责边界"。
4. 删除所有"业务方被迫传 pattern"的老 API（A 类）和"硬编码中文"的老 API（B 类），避免"中国用 CN_DATE / 美国用 US_DATE"的地雷模式。

### 1.3 范围外

- 不动 `AppLocale` / `AppDateFormatter` / `BaseCommonMultiStateActivityExt`（上一轮已落地）。
- 不做：`isToday` 内部时区参数化、`dayOfWeekChinese` 多语言替代等"业务行为变更"——本次只整理结构，**不引入新行为**。
- 不动 `app/` 模块（当前 0 调用 `DateExt` / `DateManager`，无影响）。

### 1.4 关键决策前置

| 决策点 | 锁定值 | 理由 |
|---|---|---|
| JitPack 对外兼容 | A. 硬重构，无兼容包袱 | 1.0 仓库无 changelog / 迁移指南，外部用户没"严肃承诺" |
| 老 API 处置 | A/B/D 类全删，C 类保留重排 | 已确认 |
| 文件拆分粒度 | 方案 A：按"接收者类型"拆 | 让 AS 补全即目录 |
| `DateManager` 是否改名 | 改为 `DatePatterns` | 名字更准确 |
| `DatePatterns` KDoc 显眼度 | 显眼 | 防呆上次痛点 |
| 实施顺序 | 先建后拆 | 精确定位编译失败点 |

---

## 2. 目标结构

```
Basic/src/main/java/com/letter/basic/
├── extend/
│   ├── DateParseExt.kt       ← String 解析（4 个扩展，原 DateExt.kt 内的 String 解析部分）
│   ├── LocalDateExt.kt       ← LocalDate 业务扩展（5 个）
│   ├── LocalDateTimeExt.kt   ← LocalDateTime 业务扩展（3 个）
│   ├── TimestampExt.kt       ← Long 时间戳转换（2 个）
│   └── (其他 extend 文件不变：AppCompatActivityExt.kt、FileDirExt.kt、FragmentExt.kt、IntentExt.kt、ListExt.kt、ResultCallbackLauncher.kt)
└── manager/
    ├── AppDateFormatter.kt   ← 不动（上一轮已落地）
    └── DatePatterns.kt        ← 重命名自 DateManager，删除 4 个 locale 相关模板，保留 10 个
```

**删除**：
- `Basic/.../extend/DateExt.kt`（289 行）
- `Basic/.../manager/DateManager.kt`（87 行）

**总计**：删除 376 行 + 新增 270 行，**净减少 106 行**（行数降 28%）。

---

## 3. 详细设计

### 3.1 `extend/DateParseExt.kt`（新增）

**职责**：String → 日期的解析方向，4 个扩展。

| 扩展 | 签名 | 行为 |
|---|---|---|
| `String?.formatDate` | `(pattern = "yyyy-MM-dd"): LocalDate?` | String 解析为 LocalDate，失败/null 时返回 null |
| `String?.formatDateTime` | `(pattern = "yyyy-MM-dd HH:mm:ss"): LocalDateTime?` | String 解析为 LocalDateTime |
| `String?.format` | `(sourcePattern: String, pattern: String): String?` | 字符串 → 字符串（解析+格式化） |
| `String?.daysBetween` | `(other: String?, pattern = "yyyy-MM-dd"): Long?` | 两个 String 算差天数 |

**关键决策**：
- 沿用原 `DateExt.kt` 的"try/catch + 返回 null"容错风格
- 方法签名**一字不差**，老的 `import com.letter.basic.extend.formatDate` 调用方**零迁移**

### 3.2 `extend/LocalDateExt.kt`（新增）

**职责**：LocalDate 上的 5 个扩展。

| 扩展 | 签名 | 行为 |
|---|---|---|
| `LocalDate.isToday` | `(): Boolean` | 系统时区今天判断 |
| `LocalDate.isYesterday` | `(): Boolean` | 系统时区昨天判断 |
| `LocalDate.isTomorrow` | `(): Boolean` | 系统时区明天判断 |
| `LocalDate?.daysBetween` | `(other: LocalDate?): Long?` | 差天数 |
| `LocalDate?.dayOfWeek` | `(): DayOfWeek?` | 提取 DayOfWeek 枚举 |

**关键决策**：
- 删除 `dayOfWeekChinese` / `dayOfWeekShortChinese`（B 类硬编码中文，已拍板删）
- `isToday` / `isYesterday` / `isTomorrow` 内部用 `LocalDate.now()`，**沿用原行为**（不引入"用户时区"参数化）

### 3.3 `extend/LocalDateTimeExt.kt`（新增）

**职责**：LocalDateTime 上的 3 个扩展。

| 扩展 | 签名 | 行为 |
|---|---|---|
| `LocalDateTime?.daysBetween` | `(other: LocalDateTime?): Long?` | 差天数（按日期部分） |
| `LocalDateTime.date` | `(): LocalDate` | 提取日期部分 |
| `LocalDateTime.time` | `(): LocalTime` | 提取时间部分 |

**关键决策**：
- `date()` / `time()` 是**非 null 重载**，跟原文件一致
- `daysBetween` 是**可空重载** + 容错

### 3.4 `extend/TimestampExt.kt`（新增）

**职责**：Long 时间戳转换，2 个扩展。

| 扩展 | 签名 | 行为 |
|---|---|---|
| `Long.toLocalDate` | `(zoneId = ZoneId.systemDefault()): LocalDate` | 时间戳 → LocalDate |
| `Long.toLocalDateTime` | `(zoneId = ZoneId.systemDefault()): LocalDateTime` | 时间戳 → LocalDateTime |

**关键决策**：沿用原 `DateExt.kt` 实现，`zoneId` 默认系统时区。

### 3.5 `manager/DatePatterns.kt`（新增，重命名自 DateManager）

**职责**：**与 locale 无关**的固定 pattern 模板集合。

| 模板 | pattern | 用途 |
|---|---|---|
| `DATE` | `yyyy-MM-dd` | 标准日期 |
| `DATE_TIME` | `yyyy-MM-dd HH:mm:ss` | 标准日期时间 |
| `ISO_8601` | `yyyy-MM-dd'T'HH:mm:ss'Z'` | 云端 API |
| `COMPACT_DATE` | `yyyyMMdd` | 文件名 / 缓存 |
| `COMPACT_DATE_TIME` | `yyyyMMdd_HHmmss` | 文件名 / 导出 |
| `TIME_FULL` | `HH:mm:ss` | 完整时间 |
| `TIME_SHORT` | `HH:mm` | 短时间 |
| `YEAR_MONTH` | `yyyy-MM` | 月历 / 报表 |
| `MONTH_DAY` | `MM-dd` | 纪念日（不含年） |
| `RFC_2822` | `EEE, dd MMM yyyy HH:mm:ss z` | HTTP Date 头 |

**删除的 4 个模板**（D 类 locale 相关）：
- `US_DATE`（`MM/dd/yyyy`）
- `EU_DATE`（`dd/MM/yyyy`）
- `CN_DATE`（`yyyy年MM月dd日`）
- `CN_DATE_TIME`（`yyyy年MM月dd日 HH:mm:ss`）

**类级 KDoc 显眼度要求**（已确认）：开头直接告诉读者"本类只放固定 pattern 模板，不走 locale；UI 展示请用 `AppDateFormatter`"——防呆上次"美国用 US_DATE / 中国用 CN_DATE"痛点。

### 3.6 删除的文件

| 文件 | 删除理由 |
|---|---|
| `Basic/.../extend/DateExt.kt` | 22 个扩展已按接收者类型拆分到 4 个新文件 |
| `Basic/.../manager/DateManager.kt` | 14 个模板中 4 个 locale 相关删除，剩余 10 个搬入 `DatePatterns` |

---

## 4. 改动清单

| # | 动作 | 文件 | 行数 |
|---|---|---|---|
| 1 | 删除 | `Basic/.../extend/DateExt.kt` | -289 |
| 2 | 删除 | `Basic/.../manager/DateManager.kt` | -87 |
| 3 | 新增 | `Basic/.../extend/DateParseExt.kt` | ~60 |
| 4 | 新增 | `Basic/.../extend/LocalDateExt.kt` | ~50 |
| 5 | 新增 | `Basic/.../extend/LocalDateTimeExt.kt` | ~40 |
| 6 | 新增 | `Basic/.../extend/TimestampExt.kt` | ~30 |
| 7 | 新增 | `Basic/.../manager/DatePatterns.kt` | ~90 |
| - | **不动** | `Basic/.../i18n/AppLocale.kt` | 0 |
| - | **不动** | `Basic/.../manager/AppDateFormatter.kt` | 0 |
| - | **不动** | `Basic/.../extend/BaseCommonMultiStateActivityExt.kt` | 0 |
| - | **不动** | `app/.../MainActivity.kt` | 0 |
| - | **不动** | `app/src/main/res/layout/activity_main.xml` | 0 |

---

## 5. 风险点与边界

### 5.1 兼容性

- `Basic` 模块是 JitPack 库（`com.github.mstains:android-basic:1.0`），**当前 0 外部用户**（已确认 JitPack 1.0 仓库无 changelog / 迁移指南）。
- 仓库内 `app/` 模块 0 调用 `DateExt` / `DateManager`，**重构零爆炸半径**。
- 删除的 4 个 `DateManager` 模板（`US_DATE` / `EU_DATE` / `CN_DATE` / `CN_DATE_TIME`）已由 `AppDateFormatter.medium(date)` / `short(date)` 在不同 locale 下覆盖。
- 删除的 `toDisplayString(pattern)` / `format(pattern)` 等 A 类方法已由 `AppDateFormatter.format(date, pattern)` 兜底覆盖。
- 删除的 `dayOfWeekChinese` / `dayOfWeekShortChinese` 是硬编码中文，违反 locale 原则。

### 5.2 状态 / 线程

- 拆分纯结构性变化，**无并发模型改动**。
- 5 个新文件相互独立，**0 循环依赖**。

### 5.3 可观察行为变化

- C 类方法签名**一字不差**，调用方零迁移。
- 老的 `toDisplayString(pattern)` 删了，业务方需要强 pattern 格式化时改用 `AppDateFormatter.format(date, pattern)`。
- 老的 `DateManager.CN_DATE` 等 locale 模板删了，UI 展示改用 `AppDateFormatter.medium(date)`。
- **不引入新行为**：`isToday` 仍用 `LocalDate.now()`，未做时区参数化。

### 5.4 边界条件

- 拆分后 `daysBetween` 在 String / LocalDate / LocalDateTime 三种接收者上的 3 个重载分散在 3 个文件——按"接收者拆"是设计意图，**调用方通常知道自己要在哪个类型上做差天数**。
- 老的 `DateManager.CN_DATE` 删除是**硬删**，无 `@Deprecated` 兼容（已确认 0 外部用户）。

---

## 6. 验证方式

```bash
# 1. 编译 Basic Release（关键：发到 JitPack 的 AAR）
./gradlew :Basic:assembleRelease

# 2. 编译 app Debug
./gradlew :app:assembleDebug

# 3. 静态检查
./gradlew :Basic:lintDebug
# 期望：5 个新增文件 0 warning（项目原有 lint 错误在 AppCompatActivityExt.kt:199，与本次无关）
```

**不可运行的验证**（AGENTS.md 已明确仓库无测试）：
- 静态检查：编译通过
- 5 个新文件 lint 0 warning
- 老的 `DateExt.kt` / `DateManager.kt` 已被物理删除（不是注释掉）

---

## 7. 实施顺序（"先建后拆"原则）

```
步骤 1: 新建 DateParseExt.kt        （4 个 String 扩展）
步骤 2: 新建 LocalDateExt.kt        （5 个 LocalDate 扩展）
步骤 3: 新建 LocalDateTimeExt.kt    （3 个 LocalDateTime 扩展）
步骤 4: 新建 TimestampExt.kt        （2 个 Long 扩展）
步骤 5: 新建 DatePatterns.kt        （10 个静态模板 + 显眼 KDoc）
步骤 6: 编译 :Basic:assembleRelease —— 验证 5 个新文件 OK
步骤 7: 删除 DateExt.kt             （rm）
步骤 8: 删除 DateManager.kt         （rm）
步骤 9: 编译 :Basic:assembleRelease + :app:assembleDebug —— 验证无残留引用
步骤 10: 跑 :Basic:lintDebug        —— 验证 5 个新文件 0 warning
步骤 11: 自检交付清单               （KDoc 完整、注释质量、TODO 格式、中英文空格）
```

**为什么"先建后拆"**：
- 步骤 6 编译失败时，**能精确定位"是哪个新文件错"**
- 步骤 9 编译失败时，**能精确定位"是哪个老 import 没清干净"**
- "建一个删一个"的循环方式会让两个错误叠加，无法定位

---

## 8. 自检交付清单（按 android-comment-style 第 7 节）

- [ ] 所有 public extension fun / object / val 有 KDoc（用途 + `@param` + `@return`，全中文）
- [ ] 行内注释回答"为什么"而非"做什么"
- [ ] 核心算法 / 关键分支 / 魔法值有行内说明
- [ ] TODO 按 `// TODO(作者/issue号): 原因 → 计划方案` 格式（本轮无 TODO 引入）
- [ ] 中英文混排正确空格化
- [ ] 无变动类 / 自明类 / 鼓励型 / 空 TODO 注释
- [ ] 无注释掉的旧代码
- [ ] `DatePatterns` KDoc 显眼，开头 1-2 句明确"本类 vs `AppDateFormatter` 的职责边界"

---

## 9. 待确认问题

无。设计阶段所有 3 段已取得用户确认。
