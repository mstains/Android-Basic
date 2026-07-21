---
name: android-code-style
description: >
  Android Kotlin/Java 编码规范全集。当用户问到注释怎么写、KDoc/Javadoc 模板、
  资源命名规则（布局前缀/Drawable/strings）、业务类命名后缀、架构封装规范
  （Glide/Retrofit/Toast/协程）、颜色文案规范、国际化规范或代码风格约定时加载。
  是所有 Android 代码规范的唯一真相源。注释规范章节在 references/comment-style.md
  中，用户问注释相关问题时按需读取。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-06-30'
  keywords:
  - android
  - kotlin
  - java
  - code-style
  - conventions
  - kdoc
  - javadoc
  - naming
  - architecture
  - ui
---

# Android 编码规范

> 本规范覆盖 Android 项目（Kotlin + Java）的资源命名、代码命名、架构约定与 UI 开发规范。
> 是所有 Android 编码规范的**唯一真相源**，`android-code-review` 中的规范检查均引用本 skill。
>
> **注释规范（KDoc/Javadoc 模板、行内注释规则、反模式、TODO/FIXME 格式）见 [references/comment-style.md](references/comment-style.md)**，用户询问注释相关问题时按需读取。

---

## 8. 资源命名规范

### 8.1 布局文件前缀 (规则 13)

Android XML 布局文件名必须以组件类型作为前缀。

| 前缀 | 用途 |
|------|------|
| `fragment_` | Fragment 容器布局 |
| `dialog_` | Dialog 容器布局 |
| `item_` | Adapter / RecyclerView 单项布局 |
| `widget_` | 自定义 View 容器布局 |

```xml
✅ fragment_login.xml
✅ dialog_confirm.xml
✅ item_user.xml
✅ widget_badge.xml

❌ login.xml              (无前缀)
❌ layout_login.xml       (无语义前缀)
❌ Fragment_Login.xml     (大小写混用)
❌ v_login.xml            (单字母前缀)
```

**禁止**：`layout_`/`view_`/`my_`/`content_` 等无语义前缀；大小写混用；单字母前缀 (`v_`/`d_`)。

### 8.2 Drawable 前缀 (规则 16)

Drawable 资源按用途前缀命名，统一使用 `icon_` (禁止混用 `ic_`)。

| 前缀 | 用途 |
|------|------|
| `icon_` | 图标 (统一, 不使用 ic_) |
| `bg_` | 背景 |
| `selector_` | 状态选择器 |
| `shape_` | Shape 资源 |
| `layer_` | Layer 资源 |

```xml
✅ icon_arrow.xml
✅ bg_header.xml
✅ selector_button.xml
✅ shape_rounded.xml

❌ login.xml              (无前缀)
❌ ic_back.xml            (混用 ic_/icon_)
```

**例外**：`ic_launcher` 系列 (启动图标, 系统约定)。

### 8.3 strings.xml 命名模板 (规则 29)

string name 必须符合 `app_xxx_text` 蛇形命名模板。

**命名规则**:
- 蛇形命名（snake_case）
- 省略冠词（a / an / the）和介词（in / on / at / for / to / of 等）
- 使用名词原形
- 简短且一目了然

```xml
✅ <string name="app_confirm_text">确认</string>
✅ <string name="app_cancel_text">取消</string>
✅ <string name="app_loading_text">加载中</string>
✅ <string name="app_network_error_text">网络错误</string>

❌ <string name="confirm_text">确认</string>      (缺少 app_ 前缀)
❌ <string name="app_confirm">确认</string>        (缺少 _text 后缀)
❌ <string name="appConfirmText">确认</string>     (驼峰命名)
❌ <string name="app_confirm_the_text">确认</string> (冗余冠词)
```

---

## 9. 代码命名规范

### 9.1 业务类命名后缀 (规则 15)

业务类必须以完整角色后缀结尾，禁止缩写。

| 后缀 | 用途 |
|------|------|
| `Activity` | Activity 子类 |
| `Fragment` | Fragment 子类 |
| `ViewModel` | ViewModel 子类 |
| `Adapter` | RecyclerView Adapter |
| `Holder` | ViewHolder (与 Adapter 配对) |
| `Dialog` | Dialog 子类 |
| `Service` | Service 子类 |

```kotlin
✅ LoginActivity
✅ LoginFragment
✅ LoginViewModel
✅ UserAdapter

❌ LoginAct         (缩写后缀)
❌ LoginFrag        (缩写后缀)
❌ LoginVm          (缩写后缀)
❌ LoginView        (模糊后缀, View 与 Activity 概念冲突)
```

**例外**：工具类 (`XxxUtil`/`XxxHelper`)、扩展函数文件、Entity/Model 数据类不受此约束。

---

## 10. 架构规范

### 10.1 图片加载封装 (规则 17)

必须使用 Glide，但业务层**禁止直接调用** `Glide.with(...)`，必须走 `ImageLoader` 自建入口。

```kotlin
// ✅ 正确: 走 ImageLoader 封装
ImageLoader.load(url, imageView)

// ❌ 禁止: 业务层直接调用 Glide
Glide.with(context).load(url).into(imageView)
```

**第三方库优先级**: Glide > Coil > Picasso > Fresco

### 10.2 网络层封装 (规则 18)

必须使用 Retrofit + OkHttp，但业务层**禁止直接** `new Retrofit.Builder()`，必须走 `RetrofitManager` 或 `ApiService` 封装。

```kotlin
// ✅ 正确: 走 RetrofitManager 封装
val api = RetrofitManager.create(ApiService::class.java)

// ❌ 禁止: 业务层直接构建 Retrofit
val retrofit = Retrofit.Builder().baseUrl(...).build()
```

**禁止**混用其他网络库 (Volley/HttpURLConnection，历史遗留除外)。JSON 解析默认 Gson，Moshi / Kotlinx Serialization 也可。

### 10.3 DI 框架 (规则 19)

DI 框架 (Hilt/Koin/Dagger) **不禁止也不推荐**，团队自行选择。

### 10.4 强制协程 (规则 21)

异步任务**必须**使用 Kotlin 协程 (`viewModelScope`/`lifecycleScope`/`GlobalScope`)。

```kotlin
// ✅ 正确: 使用协程
viewModelScope.launch { api.fetchData() }

// ❌ 禁止: RxJava
import io.reactivex.*
```

**禁止**引入 RxJava (`io.reactivex.rxjava3`) 依赖，**禁止** `Observable<T>`/`Flowable<T>` 出现在业务代码中。

**例外**：历史项目已集成 RxJava 不在本规则覆盖范围，但新代码不应继续使用；第三方库内部使用 RxJava 不可控。

### 10.5 日志优先 Timber (规则 22)

日志优先 Timber，`android.util.Log` 允许使用。TAG 必须走常量定义。

```kotlin
// ✅ 正确: TAG 显式定义为常量
companion object {
    private const val TAG = "LoginActivity"
}
Log.d(TAG, "message")

// ❌ 禁止: TAG 硬编码
Log.d("TAG", "message")
Log.d("Login", "message")
```

---

## 11. UI 开发规范

### 11.1 UI 工具封装 (规则 23)

Toast/Dialog/Loading 等 UI 工具**必须**走自建工具类，业务代码禁止直接调用原生 API。

```kotlin
// ✅ 正确: 走工具类封装
ToastUtils.show(this, "操作成功")
DialogUtils.showConfirm(this, title, message)
LoadingUtils.show(this, "加载中")

// ❌ 禁止: 业务层直接调原生 API
Toast.makeText(this, "操作成功", Toast.LENGTH_SHORT).show()
AlertDialog.Builder(this).setTitle(title).show()
ProgressDialog(this).show()
```

**自建工具类命名约定**: `ToastUtils`/`DialogUtils`/`LoadingUtils`。

### 11.2 尺寸单位 (规则 24)

控件尺寸**必须**使用 `dp`，文字大小**必须**使用 `sp`，**禁止** `px` 单位。

```xml
<!-- ✅ 正确 -->
<View android:layout_width="100dp" android:textSize="16sp" />

<!-- ❌ 禁止 -->
<View android:layout_width="100px" android:textSize="16px" />
```

```kotlin
// ❌ 禁止: 代码中硬编码 px
view.setPadding(10, 10, 10, 10)
```

**例外**：自定义 View 内部 `onMeasure` 计算允许 px 转换。

### 11.3 颜色禁止硬编码 (规则 25)

**View 体系**：颜色值必须定义在 `colors.xml`，禁止 `#RRGGBB`/`#AARRGGBB` 硬编码。

```xml
<!-- ✅ 正确 -->
<TextView android:textColor="@color/primary_text" />

<!-- ❌ 禁止 -->
<TextView android:textColor="#333333" />
<TextView android:textColor="#FF000000" />
```

**Compose 体系**：颜色必须走 `MaterialTheme.colorScheme` 或自定义 Color Token。

```kotlin
// ✅ 正确
Text(color = MaterialTheme.colorScheme.primary)

// ❌ 禁止
Text(color = Color(0xFF333333))
Color.parseColor("#FF0000")
```

**Kotlin / Java 代码中禁止**：

```kotlin
// ❌ 禁止
Color.parseColor("#FF0000")
0xFF0000.toInt()
```

### 11.4 文案禁止硬编码 (规则 26)

业务文案**必须**定义在 `strings.xml`，代码与 XML 中均禁止硬编码。

```xml
<!-- ✅ 正确 -->
<TextView android:text="@string/app_login_text" />

<!-- ❌ 禁止 -->
<TextView android:text="登录" />
```

```kotlin
// ✅ 正确
textView.text = getString(R.string.app_login_text)

// ❌ 禁止
textView.text = "登录"
textView.setText("登录")
```

**例外**：日志输出、单元测试 mock 数据、错误堆栈/异常信息。

### 11.5 强制国际化 (规则 27)

**正式业务项目**：必须提供 `values-zh` + `values-en` (至少双语)。

```bash
app/src/main/res/
  values/strings.xml        # 中文
  values-en/strings.xml     # 英文
```

**演示/工具项目**：推荐但非强制。

**禁止**："先单语后续补"——开始时就必须双语。

**同步流程**：新增 string 时检查 `values/strings.xml` 与 `values-en/strings.xml` 是否已有同名 key，不存在则同时追加两条（中文→`values/`，英文→`values-en/`）。

---
