# Android-Basic

Android 基础依赖库，基于 MVVM 架构封装 Activity、Fragment、DialogFragment 基类与常用扩展函数，提供 ViewBinding、ViewModel、本地广播、Result API 等能力的统一抽象。目标是通过 JitPack 集成即可快速搭建业务工程骨架，并内置完整的 OpenCode 开发环境（自定义 agent、编码规范 skill、Git 工作流 skill、代码审查 skill）。

> 开发者文档：[AGENTS.md](AGENTS.md) · OpenCode 开发环境见下方 [OpenCode 开发环境](#opencode-开发环境) · 注释规范见 opencode 全局 skill `android-comment-style`

## 目录

- [项目结构](#项目结构)
- [OpenCode 开发环境](#opencode-开发环境)
- [工具链](#工具链)
- [依赖管理](#依赖管理)
- [添加依赖](#添加依赖)
- [基类使用](#基类使用)
- [适配器](#适配器)
- [扩展函数](#扩展函数)
- [工具类](#工具类)
- [日期格式化](#日期格式化)
- [权限中文名](#权限中文名)
- [使用硬规则](#使用硬规则)
- [新增能力归位](#新增能力归位)
- [验证与发布](#验证与发布)
- [版本历史](#版本历史)
- [License](#license)

## 项目结构

仓库由两个 Gradle 子模块组成，共享 `namespace 'com.letter.basic'`（历史原因，勿随意改动）：

| 模块 | 类型 | 作用 |
|---|---|---|
| `app/` | `com.android.application` | 演示模块，依赖 `:Basic`，用于本地验证基类与扩展函数 |
| `Basic/` | `com.android.library` | 库模块，发布到 JitPack（坐标 `com.github.mstains:android-basic:1.5`，见 `Basic/build.gradle:8-13`） |

JitPack 只发布 `:Basic` 模块的 AAR + sources jar，不含 `app` 演示模块。

## OpenCode 开发环境

本项目在 `.opencode/` 目录下配置了完整的 OpenCode 开发环境。

### 自定义 Agent

| Agent | 模式 | 说明 |
|---|---|---|
| `android-dev` | primary | Android 开发主力 agent。处理 Kotlin/Java/Compose/Gradle 任务时按场景主动加载对应 skill，采用「方案 → 确认 → 执行」双阶段交付模式。详见 `.opencode/agents/android-dev.md` |

### 如何使用 Skill

Skill 是 OpenCode 的专业化指令集，由 `android-dev` agent 自动按「场景 → Skill」强映射规则加载，**无需手动指定**——你在对话中用自然语言描述需求即可：

| 你这样说 | agent 自动加载 |
|---------|-------------|
| "帮我提交代码" | `android-git-commit`（编排审查 → 注释 → 提交 → 推送全流程） |
| "这段代码有什么问题" | `android-code-review`（执行安全/规范/异常/注释五项检查） |
| "注释应该怎么写" | `android-code-style`（编码规范全集） |
| "迁移到 CameraX" | 通过 MCP `android-skills` 搜索 Camera 迁移 skill |
| "适配折叠屏" | 通过 MCP `android-skills` 搜索 adaptive 布局 skill |

Android SDK skill 全部通过 MCP 获取，`android-dev` agent 会自动搜索并加载对应 skill。

### 自定义 Skill（`.opencode/skill/`，6 个）

由项目自行维护，覆盖编码规范、代码审查、Git 工作流等核心流程：

| Skill | 用途 | 典型触发语 |
|---|---|---|
| `android-code-style` | Kotlin/Java 编码规范全集：注释模板、资源命名（布局前缀/Drawable/strings）、业务类命名后缀、架构封装约定（Glide/Retrofit/Toast/协程）、颜色文案规范、国际化规范 | "注释怎么写""命名规范" |
| `android-code-review` | 提交前代码审查编排器：Phase A 安全扫描（硬阻塞）→ Phase B 资源规范自修 → Phase C 代码异常自修 → Section E 注释合规检测，支持 AI 自动修复可修项 | "帮我 review""检查代码" |
| `android-git-commit` | Git 安全提交编排器：Step 1 代码审查 → Step 2 注释检测 → Step 3 同步远端 → Step 4 提交 → Step 5 推送 → Step 6 MR 链接 | "提交代码""push 到远端" |
| `android-git-commit-core` | Git 提交核心：变更分析 + Conventional Commit message 生成 + git add/commit，不包含扫描或 lint | （由 android-git-commit 内部调用） |
| `android-git-commit-sync` | Git 同步流程：SSH 密钥检查、fetch/pull --rebase、冲突报告、push、MR 链接生成 | （由 android-git-commit 内部调用） |
| `android-git-branch` | Git 分支创建：6 类标准化分支命名与校验，基分支硬阻塞，仅本地创建不推送 | "创建分支" |

### Android SDK Skill（MCP 提供）

Android SDK skill 统一通过 MCP 服务 `android-skills` 获取，由 agent 运行时按场景搜索和加载，无需手动管理本地文件。

当前 MCP 提供的 Android skill（通过 `android-skills_list_skills` 可查看最新列表）：

| 分类 | Skill 名称 |
|---|---|
| build | `agp-9-upgrade` |
| camera | `camera1-to-camerax` |
| device-ai | `appfunctions` |
| devtools | `android-cli` |
| identity | `verified-email` |
| jetpack-compose | `adaptive`、`migrate-xml-views-to-jetpack-compose`、`styles` |
| navigation | `navigation-3` |
| performance | `r8-analyzer` |
| play | `engage-sdk-integration`、`play-billing-library-version-upgrade` |
| profilers | `perfetto-sql`、`perfetto-trace-analysis` |
| security | `android-intent-security` |
| system | `edge-to-edge` |
| testing | `testing-setup` |
| wear | `jetpack-compose-m3` |
| xr | `display-glasses-with-jetpack-compose-glimmer` |

### 本地保留的 SDK Skill（`.opencode/skills/`，3 个）

以下 skill 由 MCP 不提供，保留在本地：

| Skill | 用途 |
|---|---|
| `android-intent-security` | Intent 安全审计（Manifest 组件配置 + Intent 防劫持） |
| `kotlin-tooling-java-to-kotlin` | Java → 惯用 Kotlin 转换，支持 Spring/Lombok/Hibernate/Dagger/Hilt 等框架感知 |
| `kotlin-tooling-native-build-performance` | Kotlin/Native iOS 编译/链接性能诊断与优化 |

### 配置结构

| 路径 | 作用 |
|---|---|
| `.opencode/opencode.jsonc` | OpenCode 项目级配置，定义 agent 与权限白名单 |
| `.opencode/agents/android-dev.md` | `android-dev` agent 定义（行为规则/场景映射/双阶段交付约束） |
| `.opencode/skill/` | 项目自定义 skill（6 个） |
| `.opencode/skills/` | MCP 未提供的本地保留 SDK skill（3 个） |
| `AGENTS.md` | 项目级开发指南（模块/工具链/静态分析/硬规则） |
| `~/.config/opencode/AGENTS.md` | 用户级配置（模型分层/开发偏好/会话规则） |

## 工具链

| 维度 | 版本 |
|---|---|
| Gradle | 8.10.2 |
| Android Gradle Plugin | 8.8.2 |
| Kotlin | 1.9.24 |
| compileSdk / targetSdk | 35 |
| minSdk | 23 |
| JVM target | Java 21 |
| coreLibraryDesugaring | `com.android.tools:desugar_jdk_libs:2.1.4`（两模块均启用） |

两模块均统一使用 Java 21（`compileOptions` + `kotlinOptions.jvmTarget`）。仓库镜像（腾讯云 + 阿里云）见 `settings.gradle:11-16, 28-33`。

## 依赖管理

所有版本号集中在 `gradle/libs.versions.toml`，子模块 `build.gradle` 统一通过 `libs.xxx` 引用，禁止直接写坐标。`vanniktechMavenPublish` 等可选发布插件在 toml 中以注释形式保留，按需启用。

## 添加依赖

**Step 1** — 在项目根目录 `settings.gradle` 中添加 JitPack 仓库（与现有 `mavenCentral()` / `google()` 并列）：

```groovy
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    maven { url 'https://jitpack.io' }
    mavenCentral()
    google()
  }
}
```

**Step 2** — 在模块 `build.gradle` 中添加依赖：

```groovy
implementation 'com.github.mstains:android-basic:1.5'
```

> 坐标大小写敏感（`android-basic`，**非** `Android-Basic`）；版本号以 `Basic/build.gradle` 中 `VERSION_NAME` 为准，后续随 tag 演进。

## 基类使用

所有基类都自动入栈 `ActivityController`；若业务工程需要全局 `Application`，须在自家 `Application.onCreate()` 中调用 `ActivityController.setApplication(this)`。

### Activity

提供 6 层基类，按能力从弱到强：

| 基类 | 能力 |
|---|---|
| `BaseMultiStateActivity` | Activity 栈管理、LifecycleObserver |
| `BaseCommonMultiStateActivity` | 生命周期钩子 `initView` / `initData` / `initListener` / `initStatusBar` |
| `BaseMultiStateVBActivity<VB>` | + ViewBinding |
| `BaseMultiStateVBVMActivity<VB, VM>` | + ViewModel |
| `BaseMultiStateVBReceiverActivity<VB>` | + 本地广播 |
| `BaseMultiStateVBVMReceiverActivity<VB, VM>` | + ViewModel + 广播 |

```kotlin
class MainActivity : BaseMultiStateVBActivity<ActivityMainBinding>() {

  // 基类要求子类提供 ViewBinding 实例：
  // 通过抽象方法将 inflate 时机与具体绑定类解耦，
  // 使基类不必感知各页面的布局差异。
  override fun createViewBinding(inflater: LayoutInflater): ActivityMainBinding {
    return ActivityMainBinding.inflate(inflater)
  }

  // 基类 onCreate 已统一按
  // initStatusBar → initView → initData → initListener 顺序调用
  // （见 Basic/.../BaseMultiStateVBActivity.kt:29-36），
  // 子类无需在 onCreate 中重复调度。
  override fun initStatusBar() {
    setStatusBarFullTransparent()
  }

  override fun initView() {
    // 仅构建视图结构，不触发任何数据加载
  }

  override fun initData() {
    // 触发数据请求与状态分发
  }

  override fun initListener() {
    // 注册用户交互回调
  }
}
```

### Fragment

| 基类 | 能力 |
|---|---|
| `BaseMultiStateCommonFragment` | 基础 Fragment 生命周期 |
| `BaseMultiStateVBFragment<VB>` | + ViewBinding |
| `BaseMultiStateVBVMFragment<VB, VM>` | + ViewModel |
| `BaseMultiStateVBVMReceiverFragment<VB, VM>` | + ViewModel + 广播 |

```kotlin
class HomeFragment : BaseMultiStateVBFragment<FragmentHomeBinding>() {

  // 与 Activity 同源：基类通过抽象方法将 binding 创建交给子类，
  // 同时确保容器参数由 Fragment 自身持有（inflater + container + false），
  // 避免在基类中耦合特定布局的 inflate 策略。
  override fun createViewBinding(
    inflater: LayoutInflater, container: ViewGroup?
  ): FragmentHomeBinding {
    return FragmentHomeBinding.inflate(inflater, container, false)
  }

  override fun initView() {
    // 仅构建视图结构
  }

  override fun initData() {
    // 触发首屏数据加载
  }
}
```

### DialogFragment

| 基类 | 能力 |
|---|---|
| `BaseMultiStateVBDialogFragment<VB>` | Dialog 窗口参数配置 + ViewBinding |
| `BaseMultiStateVBVMDialogFragment<VB, VM>` | + ViewModel |
| `BaseMultiStateVBReceiverDialogFragment<VB>` | + 广播 |
| `BaseMultiStateVBVMReceiverDialogFragment<VB, VM>` | + ViewModel + 广播 |

```kotlin
class LoadingDialog : BaseMultiStateVBDialogFragment<DialogLoadingBinding>() {

  override fun createViewBinding(
    inflater: LayoutInflater, container: ViewGroup?
  ): DialogLoadingBinding {
    return DialogLoadingBinding.inflate(inflater, container, false)
  }

  // 通过 WindowBuilder 描述窗口参数（宽 / 高 / 位置 / 触摸外部关闭 / 动画等），
  // 基类在 onStart 中读取并应用，避免在 onCreateView 中直接操作 Window。
  override fun getWindowBuild(dm: DisplayMetrics?): WindowBuilder {
    return WindowBuilder()
      .width(dm?.widthPixels ?: 600)
      .height(WindowBuilder.WRAP_CONTENT)
      .gravity(Gravity.CENTER)
  }
}

// DialogFragment 提交由 FragmentManager 管理，重复 show 会抛异常；
// 若不确定是否已弹出，应由调用方持有实例并判重。
LoadingDialog().show(supportFragmentManager)
```

## 适配器

基于 `BaseRecyclerViewAdapterHelper` 封装的 ViewBinding 通用适配器，内置选择模式、拖拽排序、侧滑删除能力。

### BaseMultiStateVBQuickAdapter

| 能力 | 说明 |
|---|---|
| ViewBinding | 子类通过 `createViewBinding` 提供布局绑定，避免 `findViewById` |
| 多状态页面 | 内置空数据、加载中、错误页支持（通过 `addStateView` 或 DataBinding 配置） |
| 拖拽排序 | 实现 `DragAndSwipeDataCallback`，长按拖拽排序 |
| 侧滑删除 | SwipeAction 支持，子类可自定义侧滑菜单 |
| 选择模式 | `SelectionMode.NONE`（默认）/ `SINGLE`（单选）/ `MULTIPLE`（多选），通过 `selectionMode` 切换 |
| 多选上限 | `maxSelectCount` 限制多选最大数量 |

```kotlin
class UserAdapter : BaseMultiStateVBQuickAdapter<User, ItemUserBinding>() {

    // 提供 ViewBinding 实例
    override fun createViewBinding(
        context: Context, inflater: LayoutInflater, parent: ViewGroup
    ): ItemUserBinding {
        return ItemUserBinding.inflate(inflater, parent, false)
    }

    override fun convert(
        holder: ViewBindingHolder<ItemUserBinding>, item: User
    ) {
        // 通过 holder.viewBinding 直接访问控件
        holder.viewBinding.tvName.text = item.name
        // isSelected 反映当前 item 选中状态，供单选/多选 UI 更新
        holder.viewBinding.checkbox.isChecked = isSelected(holder)
    }
}
```

### ViewBindingHolder

RecyclerView ViewHolder 基类，持有 `val viewBinding: VB`，在构造时通过 `viewBinding.root` 作为 itemView。子类 ViewBinding 类型由泛型 `VB` 约束，避免强制转型。

```kotlin
// 内部使用，一般无需直接继承；由 BaseMultiStateVBQuickAdapter 自动创建
open class ViewBindingHolder<VB : ViewBinding>(val viewBinding: VB) :
    RecyclerView.ViewHolder(viewBinding.root)
```

## 扩展函数

扩展函数统一放在 `Basic/src/main/java/com/letter/basic/extend/`，目前共 11 个文件：

| 文件 | 职责 |
|---|---|
| `ResultCallbackLauncher.kt` | Activity / 权限 / 相册 / 拍照的 Result API 封装（推荐方案） |
| `AppCompatActivityExt.kt` | Fragment 事务、软键盘、状态栏、Activity 重启、Intent 参数获取 |
| `FragmentExt.kt` | 子 Fragment 事务、Arguments / Intent 参数获取 |
| `IntentExt.kt` | Intent 导航（已废弃 `baseStartActivity*`）+ Service 启停 + dp 转换 |
| `FileDirExt.kt` | Context 扩展：各类文件/缓存/数据库目录路径 |
| `ListExt.kt` | 列表扩展：`filterWithIndex` / `mapWithIndex` / `forEachWithIndex` / `findWithIndex` |
| `DateParseExt.kt` | 字符串日期解析：`formatDate` / `formatDateTime` / `daysBetween` |
| `LocalDateExt.kt` | `LocalDate` 扩展：`isToday` / `isYesterday` / `daysBetween` / `dayOfWeek` / `plusDaysOrNull` 等 9 个 |
| `LocalDateTimeExt.kt` | `LocalDateTime` 扩展：`date` / `time` / `daysBetween` |
| `TimestampExt.kt` | 时间戳扩展：`Long.toLocalDate` / `Long.toLocalDateTime` |
| `BaseCommonMultiStateActivityExt.kt` | 多语言切换：`switchLanguage` / `applyLanguage` |

### Intent 导航

`baseStartActivity*` / `baseStartActivityForResult*` **已 `@Deprecated`**，新代码请改用 `ResultCallbackLauncher` 系列；Service 启停（`startService` / `stopService`）目前仍保留在 `IntentExt.kt` 中。

#### 新方案：`ResultCallbackLauncher`

`ResultCallbackLauncher` 基于 `androidx.activity.result.ActivityResultRegistry`，并通过 `DefaultLifecycleObserver` 在 `onCreate` 时注册、`onDestroy` 时注销，避免 launcher 引用泄漏导致的崩溃。

```kotlin
class ProfileActivity : AppCompatActivity() {

  // 字段持有 launcher，依赖 Activity 生命周期自动注销；
  // 必须使用 registerXxxLauncher 工厂方法而非手动 new，
  // 否则 init 块中的 addObserver 不会触发，注册时机将错位。
  private val activityLauncher = registerActivityLauncher()

  fun openDetail(userId: Int) {
    // 输入：Intent；输出：ActivityResult（含 resultCode 与 data）
    activityLauncher.launchActivity<DetailActivity>(
      context = this,
      intentAction = { putExtra("id", userId) }
    ) { resultCode, data ->
      // 回调运行在主线程；resultCode == Activity.RESULT_OK 表示成功
    }
  }

  fun openSettings() {
    // 纯展示型跳转，不关心返回结果，省略 onResult
    activityLauncher.launchActivity<SettingsActivity>(context = this)
  }
}
```

按场景区分的注册入口：

| 场景 | 注册方法 | 启动方法 |
|---|---|---|
| 启动 Activity 并接收结果 | `registerActivityLauncher()` | `launchActivity<T>()` |
| 启动 Activity 不接收结果 | `registerActivityLauncher()` | `launchActivity<T>()`（省略 onResult） |
| 批量申请权限 | `registerMultiplePermissionsLauncher()` | `launchPermissions(...)` |
| 拍照（缩略图） | `registerTakePicturePreviewLauncher()` | `launch(input)` |
| 拍照（写入 Uri） | `registerTakePictureLauncher()` | `launch(uri)` |
| PhotoPicker 单选 | `registerPhotoPickerLauncher()` | `launchImageOnly` / `launchVideoOnly` |
| PhotoPicker 多选 | `registerMultiplePhotoPickerLauncher(maxItems)` | `launchImagesAndVideos` |

Fragment 上有同名重载，签名一致，将 `registerActivityLauncher()` 改为 `fragment.registerActivityLauncher()` 即可。

#### 旧方案（已废弃，仅作迁移参考）

```kotlin
// 启动 Activity
context.baseStartActivity<DetailActivity>("id" to 123, "title" to "详情")

// 带返回结果启动
activity.baseStartActivityForResult<DetailActivity>(REQUEST_CODE)

// Fragment 中启动
fragment.baseStartActivity<DetailActivity>("id" to 456)

// 启动 / 停止 Service
context.startService<MyService>("key" to "value")
context.stopService<MyService>()
```

### Fragment 操作

```kotlin
// 替换 Fragment
appCompatActivity.replaceFragmentInActivity(HomeFragment(), R.id.container)

// 添加 / 显示 / 隐藏 Fragment
appCompatActivity.addFragmentToActivity(fragment)
appCompatActivity.showFragment(fragment)
appCompatActivity.hideFragment(fragment)

// 子 Fragment 操作：作用域限定在 parent fragment 自身
fragment.replaceChildInFragment(ChildFragment(), R.id.childContainer)
fragment.addChildToFragment(ChildFragment())
fragment.showChildInFragment(child)
fragment.hideChildInFragment(child)
```

### Activity 参数获取

```kotlin
val id = activity.getIntExtra("id")
val title = activity.getStringExtra("title")
val data = activity.getSerializableExtra<MyData>("data")
val parcel = activity.getParcelableExtra<MyParcel>("parcel")
```

### 文件路径

```kotlin
val filesDir = context.getFilesDir()
val cacheDir = context.getCacheDir()
val externalCache = context.getExternalCacheDir()
val obbDir = context.getObbDir()
```

## 工具类

### ActivityController — Activity 栈管理

`BaseMultiState*` 在 `onCreate` 中自动 `addActivity`、在 `onDestroy` 中自动 `removeActivity`，业务侧一般无需手动维护。

```kotlin
ActivityController.addActivity(this)
ActivityController.removeActivity(this)
ActivityController.clearAll()      // 结束所有 Activity，用于"退出登录"等场景
ActivityController.getApplication() // 若已 setApplication，则返回非 null
```

### WindowBuilder — Dialog 窗口构建器

```kotlin
WindowBuilder()
  .width(600)
  .height(WindowBuilder.WRAP_CONTENT)
  .gravity(Gravity.CENTER)
  .onTouchOutSide(true)
  .cancelable(true)
  .anim(true)
  .build()
```

### BroadcastUtil

基于已废弃的 `LocalBroadcastManager`，仅作兼容保留。新业务建议：

- 同进程页面间通信 → 改用 `StateFlow` / `SharedFlow` 配合 `lifecycleScope` 订阅
- 跨进程 / 系统事件 → 改用标准 `BroadcastReceiver` 或 `WorkManager`

### ReceiverManager — 广播注册管理器

统一的广播注册与注销管理器，配合 `BaseMultiStateVBReceiverActivity` / `BaseMultiStateVBVMReceiverActivity` 等带 Receiver 能力的基类使用。

```kotlin
// 基类内部已持有 ReceiverManager 实例，子类直接注册即可
registerReceiver(ReceiverManager.Receiver(action = "com.example.ACTION") { intent ->
    val data = intent?.getStringExtra("key")
    // 处理广播事件
})
```

> `ReceiverManager` 在 `onDestroy` 中自动注销所有已注册的广播，无需手动管理。

## 日期格式化

`AppDateFormatter` / `DatePatterns`（`Basic/.../manager/`）提供 14 个 `DateTimeFormatter` 模板，覆盖纯日期、日期时间、纯时间、年月、国际化、中文、HTTP / RFC 等常见场景。`DateTimeFormatter` 线程安全，可作为全局单例复用：

```kotlin
val now = LocalDateTime.now()
val stamp = now.format(AppDateFormatter.DATE_TIME)        // 2026-06-08 14:23:01
val iso = now.format(AppDateFormatter.ISO_8601)            // 2026-06-08T14:23:01Z
val cn = now.format(AppDateFormatter.CN_DATE)              // 2026年06月08日
```

| 模板 | 格式 |
|---|---|
| `DATE` / `DATE_TIME` / `DATE_TIME_MS` | `yyyy-MM-dd` / `yyyy-MM-dd HH:mm:ss` / `yyyy-MM-dd HH:mm:ss.SSS` |
| `ISO_8601` / `RFC_2822` | `yyyy-MM-dd'T'HH:mm:ss'Z'` / `EEE, dd MMM yyyy HH:mm:ss z` |
| `COMPACT_DATE` / `COMPACT_DATE_TIME` | `yyyyMMdd` / `yyyyMMdd_HHmmss` |
| `TIME_FULL` / `TIME_SHORT` | `HH:mm:ss` / `HH:mm` |
| `YEAR_MONTH` / `MONTH_DAY` | `yyyy-MM` / `MM-dd` |
| `US_DATE` / `EU_DATE` | `MM/dd/yyyy` / `dd/MM/yyyy` |
| `CN_DATE` / `CN_DATE_TIME` | `yyyy年MM月dd日` / `yyyy年MM月dd日 HH:mm:ss` |

## 权限中文名

`ChinesePermission`（`Basic/.../utils/ChinesePermission.kt`）枚举约 60 条 Android 权限常量与其中文友好名，配套三个扩展函数用于权限申请提示：

```kotlin
val name = "android.permission.CAMERA".toPermissionChineseName()      // "相机"
val names = arrayOf(
  "android.permission.CAMERA",
  "android.permission.RECORD_AUDIO"
).toPermissionChineseNames()                                          // ["相机", "录音"]
```

辅助查询：

| API | 用途 |
|---|---|
| `ChinesePermission.chineseNameOf(permission)` | 字符串 → 中文名，未找到返回 `null` |
| `ChinesePermission.of(permission)` | 字符串 → 枚举项，未找到返回 `null` |
| `ChinesePermission.allMap` | 全量权限 → 中文名映射表 |

## 使用硬规则

- 继承 `BaseMultiStateVBActivity` 时，`onCreate` 已固定为 `initStatusBar → initView → initData → initListener` 顺序（见 `Basic/.../BaseMultiStateVBActivity.kt:29-36`），**子类不要在 `onCreate` 中重复调用 `super` 或自行调度**。
- `IntentExt.kt` 中 `baseStartActivity*` / `baseStartActivityForResult*` 已 `@Deprecated`，**新代码改用 `ResultCallbackLauncher`**。
- `BaseMultiState*` 自动入栈 `ActivityController`；若需全局 `Application`，在自家 `Application.onCreate()` 中调用 `ActivityController.setApplication(this)`。
- `BroadcastUtil` 基于已废弃的 `LocalBroadcastManager`，仅作兼容保留，**新业务不要使用**。
- 所有版本号集中在 `gradle/libs.versions.toml`，子模块 `build.gradle` 一律通过 `libs.xxx` 引用，**禁止直接写坐标**。

## 新增能力归位

新业务能力按下列约定放置，避免散落到错误位置：

| 能力类型 | 落点文件 |
|---|---|
| 权限 / 相册 / 拍照等 `ActivityResultLauncher` | `extend/ResultCallbackLauncher.kt` |
| Intent 导航 / Service 启停 / dp 转换 | `extend/IntentExt.kt` |
| Fragment 事务（含子 Fragment） | `extend/FragmentExt.kt` + `extend/AppCompatActivityExt.kt` |
| 文件/缓存/数据库目录路径 | `extend/FileDirExt.kt` |
| 列表带索引操作 | `extend/ListExt.kt` |
| 字符串日期解析 | `extend/DateParseExt.kt` |
| `LocalDate` / `LocalDateTime` 扩展 | `extend/LocalDateExt.kt` + `extend/LocalDateTimeExt.kt` |
| 时间戳与日期互转 | `extend/TimestampExt.kt` |
| 多语言切换 | `extend/BaseCommonMultiStateActivityExt.kt` |
| 日期 `DateTimeFormatter` 模板 | `manager/AppDateFormatter.kt` / `manager/DatePatterns.kt` |
| 广播注册管理 | `manager/ReceiverManager.kt` |
| 权限中文名常量与扩展 | `utils/ChinesePermission.kt` |
| Activity 栈管理 | `utils/ActivityController.kt` |
| Activity / Fragment / DialogFragment 基类 | `activity/` / `fragment/` / `dialog/` |
| RecyclerView 适配器与 ViewHolder | `adapter/` |

## 验证与发布

仓库内**无 lint 配置、无 CI 工作流、无有意义的测试**。提交前只能依赖本地编译，CI 流水线由调用方自行配置。

| 命令 | 作用 |
|---|---|
| `./gradlew assembleDebug` | 编译 `app` Debug |
| `./gradlew assembleRelease` | 编译 `app` Release |
| `./gradlew :Basic:assembleRelease` | 产出 AAR + sources jar，供 JitPack 发布 |
| `./gradlew test` | 占位空跑（仓库内只有占位测试） |
| `./gradlew clean` | 清理构建产物 |

## 版本历史

- **1.5** — 新增 `BaseMultiStateVBQuickAdapter` 通用适配器 + `ViewBindingHolder` ViewHolder 基类，支持选择模式、拖拽排序、侧滑删除；新增 `ReceiverManager` 广播注册管理器；新增语言切换扩展（`switchLanguage` / `applyLanguage`）；修复 ViewModel 创建方式，纳入 ViewModelStore 管理；新增 `DateParseExt` / `LocalDateExt` / `LocalDateTimeExt` / `TimestampExt` 日期扩展
- **1.0** — Activity / Fragment / DialogFragment 基类体系与扩展函数；`ResultCallbackLauncher` Result API 封装；`BroadcastUtil` 本地广播；`WindowBuilder` Dialog 窗口构建器；日期格式化（14 个模板）与权限中文名

## License

[MIT](LICENSE)
