# Android-Basic

Android 基础依赖库，基于 MVVM 架构封装 Activity、Fragment、DialogFragment 基类及常用扩展函数，支持快速集成 ViewBinding、ViewModel、本地广播。

> 开发者文档：[AGENTS.md](AGENTS.md) · 注释规范见 opencode 全局 skill `android-comment-style`

## 添加依赖

**Step 1** — 在项目根目录 `settings.gradle` 中添加 JitPack 仓库：

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
implementation 'com.github.mstains:Android-Basic:1.1'
```

## 基类使用

### Activity

提供 6 层基类，按能力从弱到强：

| 基类 | 能力 |
|---|---|
| `BaseMultiStateActivity` | Activity 栈管理、LifecycleObserver |
| `BaseCommonMultiStateActivity` | 生命周期钩子 `initView/initData/initListener/initStatusBar` |
| `BaseMultiStateVBActivity<VB>` | + ViewBinding |
| `BaseMultiStateVBVMActivity<VB, VM>` | + ViewModel |
| `BaseMultiStateVBReceiverActivity<VB>` | + 本地广播 |
| `BaseMultiStateVBVMReceiverActivity<VB, VM>` | + ViewModel + 广播 |

```kotlin
class MainActivity : BaseMultiStateVBActivity<ActivityMainBinding>() {

  override fun createViewBinding(inflater: LayoutInflater): ActivityMainBinding {
    return ActivityMainBinding.inflate(inflater)
  }

  override fun initStatusBar() {
    setStatusBarFullTransparent()
  }

  override fun initView() {
    // 初始化视图
  }

  override fun initData() {
    // 加载数据
  }

  override fun initListener() {
    // 设置监听
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

  override fun createViewBinding(
    inflater: LayoutInflater, container: ViewGroup?
  ): FragmentHomeBinding {
    return FragmentHomeBinding.inflate(inflater, container, false)
  }

  override fun initView() {
    // 初始化视图
  }

  override fun initData() {
    // 加载数据
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

  override fun initView() {
    // 自定义窗口参数
  }

  override fun getWindowBuild(dm: DisplayMetrics?): WindowBuilder {
    return WindowBuilder()
      .width(dm?.widthPixels ?: 600)
      .height(WindowBuilder.WRAP_CONTENT)
      .gravity(Gravity.CENTER)
  }
}

// 安全弹出
LoadingDialog().show(supportFragmentManager)
```

## 扩展函数

### Intent 导航

```kotlin
// 启动 Activity
context.baseStartActivity<DetailActivity>("id" to 123, "title" to "详情")

// 带返回结果启动
activity.baseStartActivityForResult<DetailActivity>(REQUEST_CODE)

// Fragment 中启动
fragment.baseStartActivity<DetailActivity>("id" to 456)

// 启动/停止 Service
context.startService<MyService>("key" to "value")
context.stopService<MyService>()
```

### Fragment 操作

```kotlin
// 替换 Fragment
appCompatActivity.replaceFragmentInActivity(HomeFragment(), R.id.container)

// 添加/显示/隐藏 Fragment
appCompatActivity.addFragmentToActivity(fragment)
appCompatActivity.showFragment(fragment)
appCompatActivity.hideFragment(fragment)

// 子 Fragment 操作
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

```kotlin
ActivityController.addActivity(this)
ActivityController.removeActivity(this)
ActivityController.clearAll() // 结束所有 Activity
ActivityController.getApplication()
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

## 版本历史

- **1.1** — 添加 ViewBinding、ViewModel 基类及广播支持
- **1.0** — 初始版本，基础 Activity/Fragment 封装与扩展函数

## License

[MIT](LICENSE)
