# Android-Basic

Android 基础依赖库，基于 MVVM 架构封装 Activity、Fragment、DialogFragment 基类与常用扩展函数，提供 ViewBinding、ViewModel、本地广播、Result API 等能力的统一抽象，目标是通过 JitPack 集成即可快速搭建业务工程骨架。

> 开发者文档：[AGENTS.md](AGENTS.md) · 注释规范见 opencode 全局 skill `android-comment-style`

## 目录

- [项目结构](#项目结构)
- [工具链](#工具链)
- [依赖管理](#依赖管理)
- [添加依赖](#添加依赖)
- [基类使用](#基类使用)
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
| `Basic/` | `com.android.library` | 库模块，发布到 JitPack（坐标 `com.github.mstains:android-basic:1.0`，见 `Basic/build.gradle:8-13`） |

JitPack 只发布 `:Basic` 模块的 AAR + sources jar，不含 `app` 演示模块。

## 工具链

| 维度 | 版本 |
|---|---|
| Gradle | 8.10.2 |
| Android Gradle Plugin | 8.8.2 |
| Kotlin | 1.9.24 |
| compileSdk / targetSdk | 35 |
| minSdk | 23 |
| JVM target（`app`） | Java 11 |
| JVM target（`Basic`） | Java 21 |

两模块的 `compileOptions` 不一致是有意为之：`Basic` 使用 Java 21 以便后续引入更新的协程与标准库能力，`app` 保持 Java 11 以贴近典型业务工程现状。仓库镜像（腾讯云 + 阿里云）见 `settings.gradle:11-16, 28-33`。

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
implementation 'com.github.mstains:android-basic:1.0'
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

## 扩展函数

扩展函数统一放在 `Basic/src/main/java/com/letter/basic/extend/`，按职责拆分为多个文件（`IntentExt` / `FragmentExt` / `AppCompatActivityExt` / `FileDirExt` / `DateExt` / `ListExt` / `ResultCallbackLauncher`）。

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

## 日期格式化

`DateManager`（`Basic/.../manager/DateManager.kt`）提供 14 个 `DateTimeFormatter` 模板，覆盖纯日期、日期时间、纯时间、年月、国际化、中文、HTTP / RFC 等常见场景。`DateTimeFormatter` 线程安全，可作为全局单例复用：

```kotlin
val now = LocalDateTime.now()
val stamp = now.format(DateManager.DATE_TIME)        // 2026-06-08 14:23:01
val iso = now.format(DateManager.ISO_8601)            // 2026-06-08T14:23:01Z
val cn = now.format(DateManager.CN_DATE)              // 2026年06月08日
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
| 日期 `DateTimeFormatter` 模板 | `manager/DateManager.kt` |
| 权限中文名常量与扩展 | `utils/ChinesePermission.kt` |
| 通用扩展函数（Context / Activity / Fragment / Intent / 文件路径 / 列表 / 日期） | `extend/` 目录下按主题拆分的 *Ext.kt |
| Activity / Fragment / DialogFragment 基类 | `activity/` / `fragment/` / `dialog/` |
| 跨组件能力（Activity 栈、广播工具等） | `utils/` 或 `manager/` |

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

- **1.0** — 基础 Activity / Fragment / DialogFragment 基类与扩展函数；本地广播与 `ResultCallbackLauncher` 体系建立（当前 `Basic/build.gradle` 中 `VERSION_NAME`）
- **0.x** — 内部迭代版本，未发布 JitPack

## License

[MIT](LICENSE)
