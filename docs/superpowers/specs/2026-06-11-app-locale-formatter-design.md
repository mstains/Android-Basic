# App 内部语言环境 + Locale 感知时间格式化 — 设计文档

> 日期：2026-06-11
> 状态：待用户复审
> 目标模块：`Basic`（JitPack 库，坐标 `com.github.mstains:android-basic:1.0`）
> 演示模块：`app`

---

## 1. 背景与目标

### 1.1 现状

- 业务侧在不同语言环境下展示日期时，被迫传入"中式 pattern"或"欧美 pattern"，调用方需要根据当前语言环境自行选 pattern（不自然、易错）。
- 已有 `Basic/.../manager/DateManager.kt` 提供 `CN_DATE` / `US_DATE` / `EU_DATE` 等"语言分立"的 `DateTimeFormatter` 模板 —— 这正是要解决的痛点。
- 已有 `Basic/.../extend/DateExt.kt` 提供 `toDisplayString(pattern)` 扩展，pattern 由调用方传入 —— 同样是痛点源头。
- `app` 模块已经有 `values/`（默认中文）和 `values-en/` 两套字符串资源，但 **没有运行时"在 App 内手动切语言"的能力**。
- `Basic` 模块已依赖 `androidx.appcompat`，`AppCompatDelegate.setApplicationLocales` 可用。
- `BaseCommonMultiStateActivity` 现有 4 个 `open fun`（`initStatusBar` / `initView` / `initData` / `initListener`）**全部是生命周期钩子**，没有"业务动作"扩展点。

### 1.2 目标

建立 **App 级别"语言环境"唯一真相源**（`AppLocale`），并提供：

1. 在 `BaseCommonMultiStateActivity` 上暴露"切换 App 语言"的能力（2 个扩展函数），调用方一行代码完成切换。
2. 提供 **Locale 感知的时间格式化工具**（`AppDateFormatter`），业务层只传时间，不传 pattern。
3. **不破坏**现有 `DateManager` / `DateExt`，老调用方零迁移成本。
4. **不绑架**宿主 App：`AppLocale` 在 `restore` 未调用时回退到系统 Locale，所有时间格式化方法自动降级为"等同系统 Locale 的行为"。

### 1.3 范围外

- 不做：自定义切换动画、相对时间（"3 天前"）、农历、时区转换（调用方负责）。
- 不修改：`Basic/.../manager/DateManager.kt`（保留向后兼容）、`Basic/.../extend/DateExt.kt`、`BaseCommonMultiStateActivity` 本体、`BaseMultiStateVBActivity` 本体。

---

## 2. 架构

```
┌──────────────────────────────────────────────────────────┐
│  业务层                                                   │
│  switchLanguage(Locale.SIMPLIFIED_CHINESE)               │
│  AppDateFormatter.medium(orderDate)                      │
└──────────────────────────────────────────────────────────┘
                         ▲
┌──────────────────────────────────────────────────────────┐
│  extend/BaseCommonMultiStateActivityExt.kt（新增）       │
│  switchLanguage(locale)                                  │
│  applyLanguage(locale, recreate = true)                  │
└──────────────────────────────────────────────────────────┘
                         ▲
┌──────────────────────────────────────────────────────────┐
│  i18n/AppLocale.kt（新增）                               │
│  current: Locale                                         │
│  set(locale, context) → 写 prefs + 通知 AppCompatDelegate │
│  restore(context) → 启动时回放                            │
└──────────────────────────────────────────────────────────┘
                         ▲
┌──────────────────────────────────────────────────────────┐
│  manager/AppDateFormatter.kt（新增）                     │
│  short/medium/long/full(date)  ← 默认走 AppLocale.current│
│  shortDateTime/mediumDateTime                             │
│  format(date, pattern)             ← 强 pattern 兜底      │
└──────────────────────────────────────────────────────────┘
```

**依赖方向**：业务层 → extend 层 → i18n 层 → manager 层。i18n 不依赖 extend（避免循环）。manager 只依赖 i18n 的 `AppLocale.current`，不依赖 extend。

---

## 3. 详细设计

### 3.1 `Basic/.../i18n/AppLocale.kt`（新增）

**职责**：App 级别"语言环境"唯一真相源。

**API**：

```kotlin
object AppLocale {
    /** 当前 App 生效的 Locale，未显式设置时回退到系统 Locale */
    val current: Locale

    /**
     * 切换 App 语言：
     * 1. 写入 SharedPreferences("app_locale") 的 key "app_locale_tag"
     * 2. 通知 AppCompatDelegate.setApplicationLocales 触发系统级 locale 切换
     * 3. 立刻更新内部引用（让 recreate 期间的代码也拿到新值）
     */
    fun set(locale: Locale?, context: Context)

    /**
     * 启动时回放：从持久化中读出上次保存的 locale，写入 current。
     * 未调用时 current 始终等于 Locale.getDefault()，行为与本次改造前完全一致。
     */
    fun restore(context: Context)
}
```

**关键决策**：

- **持久化用 SharedPreferences 而不是 DataStore**：Basic 库保持零外部依赖，`SharedPreferences` 是 Android SDK 自带。
- **不暴露 prefs 注入接口**：locale 持久化只服务于 Basic 库自己；宿主 App 不需要读这份 prefs（字符串资源由 `AppCompatDelegate` 接管）。
- **`restore()` 是可选的**：未调用不影响其他功能，current 走系统 Locale 兜底。
- **`restore()` 建议在 `Application.onCreate()` 第一时间调用**，先于任何 Activity 创建。
- **`set()` 不触发 Activity 重建**：recreate 的责任完全落在 `switchLanguage` / `applyLanguage` 这一层，避免重复触发。

### 3.2 `Basic/.../manager/AppDateFormatter.kt`（新增）

**职责**：Locale 感知的时间格式化工具，业务层只传时间。

**API（4 档内置 + 2 档日期时间 + 1 个兜底）**：

| 方法 | 输入 | 输出风格 | locale 默认值 |
|---|---|---|---|
| `short(date)` | `LocalDate` | `FormatStyle.SHORT` | `AppLocale.current` |
| `medium(date)` | `LocalDate` | `FormatStyle.MEDIUM` | 同上 |
| `long(date)` | `LocalDate` | `FormatStyle.LONG` | 同上 |
| `full(date)` | `LocalDate` | `FormatStyle.FULL` | 同上 |
| `shortDateTime(date)` | `LocalDateTime` | SHORT 日期 + SHORT 时间 | 同上 |
| `mediumDateTime(date)` | `LocalDateTime` | MEDIUM 日期 + SHORT 时间 | 同上 |
| `format(date, pattern)` | `LocalDate` | 自定义 pattern（兜底） | 同上 |

**关键决策**：

- **默认 locale 走 `AppLocale.current` 而非 `Locale.getDefault()`**：让"系统中文 + App 内切英文"生效。
- **接收类型选 `LocalDate` / `LocalDateTime`**：immutable、无时区混淆风险。`Date` / `Long` 转换由调用方负责（`DateExt.toLocalDateTime()` 扩展已提供）。
- **formatter 实例缓存**：内部 `ConcurrentHashMap<Pair<FormatStyle, Locale>, DateTimeFormatter>` 避免重复构造。
- **强 pattern 兜底 `format(date, pattern)` 必须保留**：订单号、文件名、URL 参数等"必须固定格式"的场景需要它；失败时返回 `date.toString()` 而非抛异常。

**输出示例**（同一 `LocalDate(2024, 6, 11)`）：

| App Locale | `short` | `medium` | `long` |
|---|---|---|---|
| `zh_CN` | 2024/6/11 | 2024年6月11日 | 2024年6月11日 |
| `en_US` | 6/11/24 | Jun 11, 2024 | June 11, 2024 |
| `en_GB` | 11/06/2024 | 11 Jun 2024 | 11 June 2024 |

### 3.3 `Basic/.../extend/BaseCommonMultiStateActivityExt.kt`（新增）

**职责**：在 `BaseCommonMultiStateActivity` 上暴露"切换语言"的扩展能力。

**API**：

```kotlin
fun BaseCommonMultiStateActivity.switchLanguage(locale: Locale?)
fun BaseCommonMultiStateActivity.applyLanguage(locale: Locale?, recreate: Boolean = true)
```

**关键决策**：

- **走扩展函数而非基类 `open fun`**：基类现有 4 个 `open fun` 全是生命周期钩子，扩展函数保持"基类只管生命周期"的边界。
- **接收者类型锁 `BaseCommonMultiStateActivity`**：扩展属于 Basic 库内部能力，不污染 `AppCompatActivity`。
- **`switchLanguage` 内部复用 `applyLanguage(locale, recreate = true)`**：不重复实现。
- **`applyLanguage` 第二参数 `recreate: Boolean = true`**：默认走 recreate（满足 95% 场景），`recreate = false` 留给"单 Activity 架构 / 自定义切换动画"的高级场景。
- **不提供 lambda 回调**（已澄清为伪需求）：recreate = false 时调用方自行刷新 UI 即可。
- **不做状态保存**：recreate 走 `onSaveInstanceState`，由业务 Activity 自行决定哪些状态要存。

### 3.4 `app/.../MainActivity.kt`（修改 — 最小示范）

**改动**：在现有"相机权限 / 打开相册"两个按钮**之外**加 3 个语言切换按钮：
- "切到中文" → `switchLanguage(Locale.SIMPLIFIED_CHINESE)`
- "切到英文" → `switchLanguage(Locale.ENGLISH)`
- "跟随系统" → `switchLanguage(null)`

**不动**：现有的相机权限 / 相册逻辑，硬编码中文 Toast 也不动（属于"业务字符串挪到 strings.xml"的另一个 task，范围外）。

### 3.5 `app/src/main/res/layout/activity_main.xml`（修改）

在现有两个按钮下方加 3 个按钮，**不改**原有按钮的 ID / 文案 / 布局属性。

---

## 4. 改动清单

| # | 动作 | 文件 | 行数估计 |
|---|---|---|---|
| 1 | 新增 | `Basic/.../i18n/AppLocale.kt` | ~50 |
| 2 | 新增 | `Basic/.../manager/AppDateFormatter.kt` | ~80 |
| 3 | 新增 | `Basic/.../extend/BaseCommonMultiStateActivityExt.kt` | ~40 |
| 4 | 修改 | `app/.../MainActivity.kt` | +15（3 个按钮 + 1 个 listener 块） |
| 5 | 修改 | `app/src/main/res/layout/activity_main.xml` | +10（3 个 Button） |
| - | **不动** | `Basic/.../manager/DateManager.kt` | 0（向后兼容） |
| - | **不动** | `Basic/.../extend/DateExt.kt` | 0（向后兼容） |
| - | **不动** | `Basic/.../activity/BaseCommonMultiStateActivity.kt` | 0（基类本体不动） |
| - | **不动** | `Basic/.../activity/BaseMultiStateVBActivity.kt` | 0（基类本体不动） |

**总计：3 个新文件 + 2 个旧文件小改 + 4 个旧文件零修改。**

---

## 5. 风险点与边界

### 5.1 兼容性

- `AppCompatDelegate.setApplicationLocales` 在 minSdk 23 上完全可用，AndroidX 1.6+ 已稳定支持。
- API 33+：写入系统级 `LocaleManager`，全设备生效。
- API < 33：写入 AndroidX 内部存储，仅 App 进程内生效。
- 老 API（`DateManager.CN_DATE` / `DateExt.toDisplayString(pattern)`）保持不变，老调用方零迁移成本。

### 5.2 状态 / 线程

- `SharedPreferences.apply()` 异步写入，不阻塞 UI。
- `AppLocale.current` 用 `AtomicReference`，线程安全。
- `recreate()` 在主线程触发，遵循 Activity 生命周期。

### 5.3 可观察行为变化

- 老代码继续使用 `DateManager` / `DateExt` 时，行为**零变化**（这两个文件没改）。
- 新代码使用 `AppDateFormatter` 时，**首次**看到的行为可能与老代码不一致 —— 因为不再硬编码 `yyyy-MM-dd`，而是由 `AppLocale.current` 决定。这是设计意图（业务层不再传 pattern）。

### 5.4 边界条件

- `restore` 未调用：`AppLocale.current = Locale.getDefault()`，所有时间格式化行为与改造前一致。
- `set` 时 prefs 写入失败：apply 异步，不抛异常。
- `AppCompatDelegate` 在某些极端 ROM 上不生效：依赖官方实现，不做兼容层。

---

## 6. 验证方式

```bash
# 1. 编译验证
./gradlew :Basic:assembleRelease   # 关键：发到 JitPack 的 AAR
./gradlew :app:assembleDebug

# 2. 静态检查
./gradlew :Basic:lint
./gradlew :app:lint

# 3. 人工验证（无可运行测试，AGENTS.md 已明确）
# - 安装 debug APK 到设备
# - 点击"切到英文"按钮 → 界面应立即按英文重建
# - 杀掉 App 重启 → 应保持英文（prefs 持久化生效）
# - 点击"跟随系统"按钮 → 应回退到系统 Locale
# - 不调 restore 时，AppLocale.current = 系统 Locale
```

---

## 7. 实施顺序（执行阶段）

1. 新建 `Basic/.../i18n/AppLocale.kt`
2. 新建 `Basic/.../manager/AppDateFormatter.kt`
3. 新建 `Basic/.../extend/BaseCommonMultiStateActivityExt.kt`
4. 修改 `app/src/main/res/layout/activity_main.xml`（加 3 个按钮）
5. 修改 `app/.../MainActivity.kt`（加 3 个按钮的 listener）
6. 跑 `./gradlew :Basic:assembleRelease` + `./gradlew :app:assembleDebug` 双重验证
7. 自检交付清单（KDoc / 行内注释 / TODO 格式 / 中英文空格）

---

## 8. 待确认问题

无。设计阶段所有 4 段已取得用户确认。
