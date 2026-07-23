# 注释规范

> 本文档为 android-code-style 的注释规范章节（#0-#7 + 附录A/B）。
> 覆盖 KDoc/Javadoc 模板、行内注释规则、反模式排查与 TODO/FIXME 格式。
> 用户询问注释怎么写、KDoc/Javadoc 格式、注释规范时按需加载。

---

## 0. 原则与适用

### 0.1 适用对象

- **Kotlin** 文件：使用 KDoc（`/** ... */`）
- **Java** 文件：使用 Javadoc（`/** ... */`）
- 范围：所有 Android 模块 `src/main/java/` 下的源码
  - 库模块：`library/src/main/java/...`
  - 应用模块：`app/src/main/java/...`
  - 公共能力模块：`common/src/main/java/...`
  - 等等

### 0.2 三大原则

1. **代码说"做什么"，注释说"为什么"**
   - 命名良好的代码已经传达了"做什么"，注释的价值在于补充"为什么这样做"、"在什么前提下成立"、以及"读这段的人必须知道什么"。

2. **注释与代码同步维护**
   - 修改代码时必须同时审视相关注释，删除失效注释、补充新约束。任何"过期注释"比"无注释"更危险。

3. **注释必须传达代码本身无法表达的信息**
   - 解释"为什么这样做"（业务原因 / 性能考量 / 兼容性约束 / 安全考虑）
   - 解释"上下文"（接口方约定 / 依赖版本约束 / 历史决策）
   - 警示副作用或边界条件
   - **反面**：禁止"词不表意"的应付式注释（详见第 5 节）

### 0.3 强制项

| 类型 | 是否必须 |
|---|---|
| `public class / interface / object` 的 KDoc/Javadoc | **必须** |
| `public fun` 的 KDoc/Javadoc | **必须** |
| `public val / var`（含扩展属性）的 KDoc/Javadoc | **必须** |
| `internal` 级别的关键 API（跨模块被外部使用） | **必须** |
| 复杂逻辑（核心算法、关键分支、魔法值）的行内注释 | **应当** |
| `private` 内部辅助函数 | 鼓励，但不强制 |

---

## 1. 语言与术语约定

### 1.1 语言策略

- **正文**：简体中文
- **技术术语**：保留英文，不翻译
- **代码标识符**：原样保留英文

### 1.2 中英文混排规则

- 中文与英文/数字之间**留一个半角空格**，提升可读性：
  - ✅ `加载 Coroutine 上下文`
  - ❌ `加载Coroutine上下文`
  - ✅ `超时 3000 毫秒`
  - ❌ `超时3000毫秒`
- 英文/数字紧邻中文标点时**不留空格**：
  - ✅ `配置 ViewModel 并启动协程。`
  - ❌ `配置 ViewModel 并启动协程 。`

### 1.3 常用术语对照（节选）

| 英文术语 | 中文表述 | 示例 |
|---|---|---|
| Coroutine | 协程 | 启动协程执行网络请求 |
| StateFlow | StateFlow | 暴露 StateFlow 供 UI 订阅 |
| Lifecycle | 生命周期 | 监听 Activity 生命周期 |
| ViewModel | ViewModel | 持有 ViewModel 实例 |
| ViewBinding | ViewBinding | 通过 ViewBinding 访问视图 |
| DialogFragment | DialogFragment | 继承 DialogFragment |
| Bundle | Bundle | 通过 Bundle 传递参数 |
| Dispatcher | 调度器 | 切换至 IO 调度器 |
| Channel / Flow | Channel / Flow | 使用 Flow 处理流式数据 |
| Lambda | Lambda 表达式 | 传入 Lambda 回调 |
| Callback | 回调 | 注册生命周期回调 |
| Intent | Intent | 启动携带参数的 Intent |
| Adapter | Adapter | 实现 RecyclerView Adapter |
| Listener | 监听器 | 注册点击监听器 |
| Annotation | 注解 | 自定义注解处理器 |
| 等等 | — | — |

> 新增术语请遵循：英文已是行业通用名时**保留英文**，避免强行翻译反而增加理解成本。

---

## 2. Kotlin KDoc 模板

### 2.1 文件头注释

每个 Kotlin 源文件顶部**应当**有文件头说明，用于交代文件用途与版权信息。

```kotlin
/**
 * [文件用途简述]
 *
 * 详细说明（可选）：补充职责边界、关键依赖、注意事项等。
 *
 * @author 作者名
 * @since 版本号
 */
package com.example.android.xxx
```

**示例**：

```kotlin
/**
 * Activity 栈管理控制器。
 *
 * 通过 Application 的 ActivityLifecycleCallbacks 统一管理所有 Activity 实例，
 * 支持一键结束、获取栈顶、按 class 查找等操作。
 *
 * @author <name>
 * @since 1.0
 */
package com.example.android.manager
```

### 2.2 类 / 接口 / object

```kotlin
/**
 * [类的一句话用途]
 *
 * [补充说明：职责、约束、典型使用场景、线程模型等，可选]
 *
 * @param T 泛型说明（若有）
 * @property 属性说明（若是公开属性）
 * @constructor 创建说明（若有特殊构造逻辑）
 * @author 作者名
 * @since 版本号
 */
class MyClass<T> {
    // ...
}
```

**示例**：

```kotlin
/**
 * 通用 DialogFragment 基类，集成 ViewBinding、生命周期感知与窗口参数配置。
 *
 * 子类通过重写 [createViewBinding] 提供绑定实例，
 * 通过 [getWindowBuild] 自定义窗口尺寸与位置。
 *
 * @param VB ViewBinding 类型，由子类在 createViewBinding 中具体化
 * @author <name>
 * @since 1.0
 */
abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment() {
    // ...
}
```

### 2.3 函数（含扩展函数）

```kotlin
/**
 * [函数的一句话用途]
 *
 * [补充说明：副作用、并发模型、典型调用时机，可选]
 *
 * @param receiver 接收者说明（仅扩展函数）
 * @param paramName 参数说明
 * @return 返回值说明
 * @throws ExceptionType 抛出条件
 * @see 相关函数
 */
fun functionName(paramName: ParamType): ReturnType {
    // ...
}
```

**示例**：

```kotlin
/**
 * 启动指定 Activity 并传递参数。
 *
 * 通过可变参数 pairs 构造 Intent extras，等价于显式调用 putExtra。
 * 接收者支持 Context / Activity / Fragment 三种场景。
 *
 * @param T 目标 Activity 类型
 * @param receiver 上下文接收者（Context / Activity / Fragment）
 * @param pairs 键值对参数，键为 String，值可为基本类型或 Parcelable
 * @throws ActivityNotFoundException 当目标 Activity 未注册时抛出
 */
inline fun <reified T : Activity> Any.baseStartActivity(vararg pairs: Pair<String, Any?>) {
    // ...
}
```

### 2.4 属性（val / var）

```kotlin
/**
 * [属性的一句话用途]
 *
 * [补充说明：默认值、线程约束、可空性缘由，可选]
 *
 * @return 属性值说明（可选，多数情况下省略）
 */
val propertyName: PropertyType = ...
```

**示例**：

```kotlin
/**
 * 当前 Activity 栈中所有未销毁 Activity 的快照。
 *
 * 返回副本而非原列表，调用方修改不影响真实栈。
 * 线程安全：仅主线程访问。
 */
val activityStack: List<Activity>
    get() = stack.toList()
```

### 2.5 枚举 / sealed class

```kotlin
/**
 * 网络请求结果状态。
 */
sealed class NetResult<out T> {
    /**
     * 加载中。
     */
    object Loading : NetResult<Nothing>()

    /**
     * 加载成功，携带业务数据。
     *
     * @property data 业务返回数据
     */
    data class Success<T>(val data: T) : NetResult<T>()

    /**
     * 加载失败。
     *
     * @property code 业务错误码
     * @property message 错误描述，面向用户友好
     */
    data class Error(val code: Int, val message: String) : NetResult<Nothing>()
}
```

---

## 3. Java Javadoc 模板

### 3.1 类

```java
/**
 * [类的一句话用途].
 *
 * [补充说明：职责、约束、线程模型等，可选]
 *
 * @param <T> 泛型说明
 * @author 作者名
 * @since 版本号
 */
public class MyClass<T> {
    // ...
}
```

### 3.2 方法

```java
/**
 * [方法的一句话用途].
 *
 * [补充说明：副作用、调用时机、可选]
 *
 * @param paramName 参数说明
 * @return 返回值说明
 * @throws ExceptionType 抛出条件
 * @see 相关方法
 */
public ReturnType methodName(ParamType paramName) throws ExceptionType {
    // ...
}
```

### 3.3 字段 / 构造器

```java
/**
 * [字段的一句话用途].
 *
 * [补充说明：默认值、线程约束、可选]
 */
private final FieldType field;

/**
 * [构造器用途].
 *
 * @param paramName 参数说明
 */
public MyClass(ParamType paramName) {
    // ...
}
```

**示例**：

```java
/**
 * 图片加载器.
 *
 * 基于 Glide 封装，支持占位图、错误图、变换、加载回调等。
 * 线程不安全：实例方法必须在主线程调用.
 *
 * @author <name>
 * @since 1.0
 */
public class ImageLoader {
    /** 默认占位图资源 ID. */
    private final int placeholderRes;

    /**
     * 构造图片加载器.
     *
     * @param placeholderRes 占位图资源 ID，传 0 表示不显示占位图
     */
    public ImageLoader(int placeholderRes) {
        this.placeholderRes = placeholderRes;
    }

    /**
     * 加载网络图片到目标视图.
     *
     * @param url 图片地址
     * @param target 目标 ImageView
     * @throws IllegalArgumentException 当 url 为空或 target 为 null 时抛出
     */
    public void load(String url, ImageView target) {
        // ...
    }
}
```

---

## 4. 行内注释规则

### 4.1 必须 / 应当写行内注释的场景

| 场景 | 示例 |
|---|---|
| 核心算法解释思路 | `// 二分查找：左闭右开区间，避免 mid 计算时越界` |
| 关键分支解释"为什么这样判" | `// 兼容 Android 7.0 以下对 FileProvider 解析失败的回退方案` |
| 魔法值 / 魔法字符串 | `// 65535 = 16 位无符号最大值，对应协议中 status 字段长度` |
| 副作用警示 | `// 注意：调用后会使 target Activity 进入 onPause` |
| 外部约束说明 | `// 此方法必须主线程调用，因内部操作 View 层级` |
| 业务背景 | `// 业务方要求保留最近 7 天数据，超期清理` |
| 临时方案说明 | `// 临时方案：等待 xxx 接口升级后切换为标准实现` |

### 4.2 不该写行内注释的场景

- 代码本身已经自解释（如 `count++` 不需要写 `// 计数加一`）
- 函数命名已清晰，参数命名已清晰（如 `user.login(token)` 不需要注释）
- 用注释解释一段无效的旧代码（直接删除，不要保留注释代码）
- 注释内容是"今天改了什么"（这是 git log 的事）

### 4.3 格式约定

- **位置**：单行注释统一放在语句**上方**，不与代码同行
- **缩进**：与下方代码保持相同缩进
- **间隔**：注释块与代码之间留一行空行，提升视觉分组
- **标点**：中文语句用全角标点，技术参数用半角

**正确示例**：

```kotlin
fun calculateScore(input: List<Int>): Int {
    // 业务侧要求：满分 100，超出部分按 0.8 系数折算
    // 折算后向下取整，避免出现 99.999 这种情况
    val raw = input.sum()
    val capped = minOf(raw, 100)
    return floor(capped * 0.8).toInt()
}
```

**错误示例**：

```kotlin
fun calculateScore(input: List<Int>): Int {
    val raw = input.sum()  // 求和
    val capped = minOf(raw, 100) // 取最小
    return floor(capped * 0.8).toInt() // 返回结果
}
```

---

## 5. 反模式清单

### 5.0 总纲

> **应付式注释 = 注释与被标注对象（方法 / 字段 / 类）作用不相符、词不表意。**
>
> 判定方式：删除这条注释，读者是否会丢失信息？
> - 会丢失 → 保留
> - 不会丢失 → **必须删除**

### 5.1 变动类注释（Hard Ban）

仅复述"我做了什么变更"，这是 git commit 的事，不该出现在源码里。

| 反例 | 问题 |
|---|---|
| `// 新增` | git 记录变更，源码无需说明 |
| `// 修改` | 同上 |
| `// 优化` | 没说明优化什么、为什么更优 |
| `// 添加功能` | "功能"是什么没说 |
| `// fix bug` | 哪个 bug？怎么修的？没交代 |
| `// update` | 毫无信息 |

### 5.2 自明类注释（Hard Ban）

复述代码动作，与代码本身表达的内容完全重复。

| 反例 | 问题 |
|---|---|
| `// 初始化` | 函数名已经是 initXxx |
| `// 设置监听` | `setXxxListener` 已经表达 |
| `// 返回结果` | `return` 已经是返回 |
| `// 加载数据` | `loadData` 已经表达 |
| `// 创建对象` | `new MyClass()` 已经表达 |

### 5.3 鼓励型注释（Hard Ban）

没说明"为什么"重要，读者无法从注释中得到具体指引。

| 反例 | 问题 |
|---|---|
| `// 这段代码很重要` | 为什么重要？读者该关注什么？没说 |
| `// 注意！` | 注意什么？没说 |
| `// 关键逻辑` | "关键"在哪里、为什么关键？没说 |
| `// 性能瓶颈` | 瓶颈表现是什么、如何避免？没说明 |

### 5.4 空 TODO 注释（Hard Ban）

TODO 没有具体行动人、原因、计划，等于没写。

| 反例 | 问题 |
|---|---|
| `// TODO 后续优化` | 谁优化？优化什么？什么时候？ |
| `// FIXME` | 哪里有 bug？怎么修？ |
| `// 待完善` | 完善什么？ |

**正确写法参考第 6 节。**

### 5.5 注释掉的代码（Hard Ban）

**禁止**保留注释掉的旧代码。理由：
- Git 已经保留了历史，源码里留这些只会干扰阅读
- 长期不删的"注释代码"几乎不会再被恢复

如确有临时禁用需求，使用特性开关（feature flag）或版本控制处理。

### 5.6 不属于"应付式"的合法注释

为避免矫枉过正，明确**允许**的注释类型：

- 解释"为什么这样做"（业务原因 / 性能 / 兼容性 / 安全）
- 解释"上下文"（接口方约定 / 依赖版本约束 / 历史决策）
- 警示副作用或边界条件
- KDoc / Javadoc 中的 `@param @return @throws @see`
- 第 4.1 节列出的行内注释场景

---

## 6. TODO / FIXME 格式

### 6.1 TODO 模板

```
// TODO(作者/issue号): 原因 → 计划方案
```

**字段说明**：
- **作者/issue号**：必填，用于追溯责任人与上下文
- **原因**：当前为什么这样写（不写原因的 TODO 等于没写）
- **计划方案**：下一步要做什么（不写方案等于没写）

**示例**：

```kotlin
// TODO(zhangsan#456): 当前对 200KB 以上图片未做压缩，触发 OOM → 切换至 Coil 缩放策略
fun loadImage(url: String) { ... }

// FIXME(lisi#789): 在 Android 7.0 上 FileProvider 解析失败 → 升级 compileSdk 35 后统一改造
fun getUriFromFile(file: File): Uri { ... }
```

### 6.2 不允许的 TODO 形式

```kotlin
// TODO
// TODO:
// TODO: 后续处理
// FIXME
// 临时方案
```

以上形式全部按"空 TODO"处理，**禁止使用**。

### 6.3 清理机制

- 每个迭代周期（建议 2 周）应清空陈旧 TODO
- 超过 3 个迭代周期仍未处理的 TODO，应拆为独立 issue 跟踪
- 解决的 TODO 提交时连同修改一起删除

---

## 7. 提交流程与自检清单

### 7.1 提交前自检（每条 PR 必须过）

**KDoc / Javadoc 覆盖**：
- [ ] 所有新增的 public class / interface / object 有文档
- [ ] 所有新增的 public fun 有文档（用途 + `@param` + `@return` + `@throws`）
- [ ] 所有新增的 public val / var 有文档
- [ ] 重命名 / 修改签名的 API 已同步更新 KDoc

**行内注释质量**：
- [ ] 核心算法、关键分支、魔法值有行内说明
- [ ] 自明注释已删除（"初始化"、"设置监听"等）
- [ ] 变动类注释已删除（"新增"、"修改"等）
- [ ] 鼓励型注释已删除或补充具体内容
- [ ] 空 TODO 已按第 6 节格式补全

**反模式排查**：
- [ ] 注释掉的代码已删除（git 留有历史）
- [ ] 过期注释已更新或删除
- [ ] 中英文混排已正确空格化

### 7.2 Code Review 关注点

Reviewer 在审阅代码时应主动：
- 质疑每条新注释"删除后是否会丢失信息"
- 指出"应付式"反例
- 推动 TODO 落地为 issue

---

## 附录 A. 完整示例（重构前后对照）

### A.1 反例集合（典型应付式注释）

```kotlin
class UserManager {
    // 新增
    private var name: String = ""

    // 初始化
    fun init() {
        name = "default"
    }

    // 设置监听
    fun setOnClickListener() {
        // TODO 后续优化
    }

    // 这段代码很重要
    fun process() {
        // 临时方案
        return null
    }
}
```

### A.2 正例集合（按规范重写）

```kotlin
/**
 * 用户信息管理。
 *
 * 负责用户基础信息的本地缓存与查询，所有方法仅主线程访问。
 * 数据源优先级：内存缓存 → DataStore → 网络。
 *
 * @author <name>
 * @since 1.0
 */
class UserManager {

    /**
     * 当前登录用户姓名。
     *
     * 默认值 "default" 用于未登录态的占位展示，避免 UI 层处理 null。
     */
    private var name: String = "default"

    /**
     * 初始化本地缓存。
     *
     * 必须在 Application.onCreate 中调用，否则后续查询会触发同步 IO。
     */
    fun init() {
        name = "default"
    }

    /**
     * 注册点击监听器。
     *
     * @param onClick 点击回调，参数为触发点击的视图实例
     */
    fun setOnClickListener(onClick: (View) -> Unit) {
        // 监听到达后必须先经过去抖，避免快速连点触发多次网络请求
        val debounced = debounce(300, onClick)
        listener = debounced
    }

    /**
     * 处理用户数据。
     *
     * @return 处理结果，返回 null 表示无有效数据
     */
    fun process(): Result? {
        // 临时方案：当前业务方尚未提供完整数据模型，先返回 null
        // TODO(zhangsan#234): 等待后端字段补齐后切换为完整对象
        return null
    }
}
```

---

## 附录 B. 常用术语对照表

| 类别 | 英文 | 中文表述 |
|---|---|---|
| 异步 | Coroutine | 协程 |
| 异步 | Dispatcher | 调度器 |
| 异步 | suspend | suspend |
| 异步 | Channel / Flow | Channel / Flow |
| 异步 | Job | Job |
| 异步 | Deferred | Deferred |
| 架构 | ViewModel | ViewModel |
| 架构 | LiveData | LiveData |
| 架构 | StateFlow | StateFlow |
| 架构 | SharedFlow | SharedFlow |
| 架构 | Repository | 仓库 |
| UI | Activity | Activity |
| UI | Fragment | Fragment |
| UI | DialogFragment | DialogFragment |
| UI | ViewBinding | ViewBinding |
| UI | RecyclerView | RecyclerView |
| UI | Adapter | Adapter |
| UI | LayoutInflater | LayoutInflater |
| UI | Listener | 监听器 |
| 生命周期 | Lifecycle | 生命周期 |
| 生命周期 | onCreate | onCreate |
| 生命周期 | onResume | onResume |
| 生命周期 | onPause | onPause |
| 生命周期 | onDestroy | onDestroy |
| 组件 | Intent | Intent |
| 组件 | Bundle | Bundle |
| 组件 | BroadcastReceiver | 广播接收器 |
| 组件 | Service | Service |
| 组件 | ContentProvider | ContentProvider |
| 工具 | Annotation | 注解 |
| 工具 | Lambda | Lambda 表达式 |
| 工具 | Inline | inline |
| 工具 | Extension | 扩展 |
| 工具 | Generic | 泛型 |
| 工具 | Sealed | sealed |
| 工具 | DataClass | data class |

> 新增术语请遵循"行业通用则保留英文"原则，并补充到本表中。
