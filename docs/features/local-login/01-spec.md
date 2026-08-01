# 01-spec.md — 本地演示登录功能（local-login）

- 功能名称：`local-login`
- 模块：`app/`（演示模块）
- 日期：2026-08-01

## 1. 需求理解

在 `app` 演示模块实现一个**纯本地**的登录功能演示，用于展示 Basic 库的
`BaseMultiStateVBActivity` 基类 + Material 组件 + ViewPager2 的组合用法。

### 1.1 需求清单（含澄清结果）

| # | 需求点 | 澄清结果 |
|---|--------|---------|
| R1 | 两种登录方式 | 用户名+密码、手机号+验证码，TabLayout + ViewPager2 切换 |
| R2 | 数据独立性 | 两个登录方式 Fragment 数据完全独立；切 Tab 时各 Fragment 输入内容**保留**（ViewPager2 默认行为） |
| R3 | 纯本地演示 | 不调用任何接口 |
| R4 | 登录成功行为 | Toast「登录成功」，停留原页面，无跳转 |
| R5 | 账号数据 | 硬编码固定账号：用户名 `admin` / 密码 `123456`；手机号 `13800138000` |
| R6 | 验证码逻辑 | 点击「获取验证码」后**本地随机生成** 6 位数字验证码，通过 Toast 展示给用户，用户输入该随机码才能登录 |
| R7 | 发送倒计时 | 60s 倒计时，倒计时期间按钮禁用并显示剩余秒数 |
| R8 | 校验提示 | 格式类校验失败 → 输入框下方红字（页面内文案）；登录失败（账号/验证码错误）→ Toast |

### 1.2 非目标（YAGNI）

- 不做注册、找回密码、退出登录
- 不做账号持久化 / 记住账号 / 自动登录
- 不调用网络、不使用数据库
- 不引入 ViewModel / 协程 / DI（项目无此惯例，纯本地演示状态极简）

## 2. 技术方案

### 2.1 架构

```
LoginActivity (BaseMultiStateVBActivity<ActivityLoginBinding>)
  ├─ TabLayout            — 两个 Tab：账号密码登录 / 手机号登录
  ├─ ViewPager2           — FragmentStateAdapter 管理两个 Fragment
  │    ├─ AccountLoginFragment   — 用户名 + 密码输入区
  │    └─ PhoneLoginFragment     — 手机号 + 验证码输入区（含获取验证码按钮）
  └─ 依赖的工具类
       ├─ LocalAccount (object)  — 硬编码账号常量 + 随机验证码生成器
       └─ LoginValidator         — 纯函数格式校验（非空 / 手机号 / 验证码）
```

- 各 Fragment 通过 `onCreateView` 创建自己的 ViewBinding，输入状态随 Fragment 实例
  保留（ViewPager2 默认预加载相邻页且 FragmentStateAdapter 持有实例，切 Tab 不销毁）。
- 校验逻辑抽为纯函数 `LoginValidator`，便于单元测试；账号常量与验证码生成抽为
  `LocalAccount`（object），与 UI 解耦。

### 2.2 组件职责

| 组件 | 职责 | 依赖 |
|------|------|------|
| `LoginActivity` | TabLayout + ViewPager2 容器，绑定 FragmentStateAdapter | BaseMultiStateVBActivity、material、viewpager2 |
| `AccountLoginFragment` | 用户名/密码输入、格式校验、比对硬编码账号、结果提示 | LoginValidator、LocalAccount |
| `PhoneLoginFragment` | 手机号/验证码输入、格式校验、获取验证码+倒计时、比对验证码、结果提示 | LoginValidator、LocalAccount |
| `LocalAccount` | 暴露 `ACCOUNT_NAME` / `ACCOUNT_PASSWORD` / `PHONE_NUMBER` 常量；`generateVerifyCode()` 生成 6 位随机数字 | 无 |
| `LoginValidator` | `validateAccountForm` / `validatePhoneForm` / `validateVerifyCode` 纯函数，返回错误文案或 null | 无 |

### 2.3 数据流

**账号密码登录（AccountLoginFragment）**
1. 点击「登录」→ `LoginValidator.validateAccountForm(用户名, 密码)`：
   - 用户名或密码为空 → 对应输入框下红字提示（TextInputLayout.error）
2. 比对 `LocalAccount.ACCOUNT_NAME` / `ACCOUNT_PASSWORD`
   - 不匹配 → Toast「用户名或密码错误」
   - 匹配 → Toast「登录成功」，停留原页

**手机号验证码登录（PhoneLoginFragment）**
1. 输入手机号 → 点击「获取验证码」→ `LoginValidator.validatePhone(手机号)`：
   - 非 11 位或非 1 开头 → 红字提示，不生成验证码
   - 手机号 ≠ `13800138000` → Toast「该手机号未注册」，不生成验证码
   - 校验通过 → `LocalAccount.generateVerifyCode()` 生成 6 位随机码，Toast 展示
     「验证码已发送，请注意查收：XXXXXX」→ 启动 60s 倒计时（按钮禁用 + 显示「XXs 后重新获取」）
2. 输入验证码 → 点击「登录」→ `LoginValidator.validateVerifyCode(验证码)`：
   - 非 6 位数字 → 红字提示
3. 比对本次生成的验证码
   - 不匹配 → Toast「验证码错误」
   - 匹配 → Toast「登录成功」，停留原页

### 2.4 UI 设计

- `activity_login.xml`：垂直 LinearLayout → 顶部标题 + TabLayout（`tabMode="fixed"`）
  + ViewPager2（`layout_weight=1`）
- `fragment_account_login.xml` / `fragment_phone_login.xml`：ConstraintLayout 包裹
  Material `TextInputLayout`（`endIconMode="password_toggle"` 提供密码可见性切换）+ 登录按钮
- 校验失败红字直接用 TextInputLayout 的 `error` 属性（自带文案展示，无需自绘错误视图）
- 文案统一放 `strings.xml`（`app/src/main/res/values/strings.xml`），遵守项目资源规范

### 2.5 依赖变更

- `gradle/libs.versions.toml`：
  - `[versions]` 新增 `viewpager2 = "1.1.0"`
  - `[libraries]` 新增 `androidx-viewpager2 = { group = "androidx.viewpager2", name = "viewpager2", version.ref = "viewpager2" }`
- `app/build.gradle`：新增 `implementation libs.androidx.viewpager2`
- 不新增协程：倒计时用 `Handler.postDelayed` + `Handler.removeCallbacks`（Fragment 销毁时清理，防泄漏）

### 2.6 入口与 Manifest

- `MainActivity` 增加「本地登录演示」按钮 → `launchActivity<LoginActivity>(this)`（沿用项目现有
  `ResultCallbackLauncher.launchActivity` 扩展）
- `AndroidManifest.xml` 注册 `LoginActivity`（`android:exported="false"`，非启动入口）

## 3. Android 版本兼容与碎片化评估

### 3.1 版本约束

| 项 | 值 |
|----|-----|
| minSdk | 23（Android 6.0） |
| targetSdk | 35（Android 15） |
| compileSdk | 35 |

### 3.2 各版本 API 差异影响分析

| 系统能力 | 涉及 API | 版本差异 | 影响 |
|---------|---------|---------|------|
| ViewPager2 | androidx.viewpager2 | 支持 minSdk 21+ | 无差异 |
| TabLayout | material 1.10.0 | 支持 minSdk 21+ | 无差异 |
| TextInputLayout error / password_toggle | material | 全版本一致 | 无差异 |
| Toast | android.widget.Toast | 全版本一致（无通知权限要求） | 无差异 |
| Handler 倒计时 | android.os.Handler | 全版本一致 | 无差异 |
| 分区存储 / 包可见性 / 通知权限 / 前台服务 | — | Android 10/11/13/14 相关 | **本功能不涉及**（纯 UI + 内存状态，无文件/外部 Intent/通知/服务） |

结论：本功能全部使用跨版本稳定的 UI 组件与基础 API，**无版本差异风险**。

### 3.3 厂商 ROM 差异

- 无后台任务、无通知渠道、无自启动：不受华为/小米/OPPO/Vivo 后台限制策略影响
- 验证码通过 Toast 展示，属前台 UI 交互，各 ROM 表现一致
- 结论：**无厂商差异风险**

### 3.4 屏幕尺寸与密度适配

- 布局基于 ConstraintLayout + 固定宽度输入框 + weight 权重，可自适应手机/平板/折叠屏
- 不锁定横竖屏（保持系统默认），TabLayout `tabMode="fixed"` 在宽屏下 Tab 居中展示
- 字号使用系统默认 sp，不做硬编码

## 4. 错误处理

| 场景 | 提示方式 | 提示内容 |
|------|---------|---------|
| 用户名/密码为空 | 红字（输入框下） | 请输入用户名 / 请输入密码 |
| 账号或密码错误 | Toast | 用户名或密码错误 |
| 手机号格式错误 | 红字（输入框下） | 请输入正确的手机号 |
| 手机号未注册 | Toast | 该手机号未注册 |
| 验证码为空 | 红字（输入框下） | 请输入验证码 |
| 验证码非 6 位数字 | 红字（输入框下） | 验证码格式不正确 |
| 验证码错误（与本次生成码不符） | Toast | 验证码错误 |
| 登录成功 | Toast | 登录成功 |

## 5. 验证方式

- `./gradlew :app:assembleDebug` 编译通过
- 手动操作路径（见 04-self-check.md）：
  1. 账号密码登录：正确 / 错误账号、空输入 3 条路径
  2. 手机号登录：正确流程（获取验证码 → 输入 Toast 随机码 → 登录成功）、错误手机号、
     错误验证码、倒计时禁用状态 4 条路径
  3. 切 Tab 输入保留验证
