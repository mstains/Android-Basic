# 02-plan.md — 本地演示登录功能（local-login）任务拆分

- 依赖：`docs/features/local-login/01-spec.md`
- 日期：2026-08-01
- 开发方式：TDD（Task 2 含单元测试，其余 UI/配置类 Task 以编译 + 手动验证为准）

## 任务列表

| # | Task | 描述 | 涉及文件 | 依赖 | 验证方式 | 可测试性 |
|---|------|------|---------|------|---------|---------|
| T1 | 依赖与基础配置 | 新增 viewpager2 依赖 + 登录相关 strings 文案 | `gradle/libs.versions.toml`、`app/build.gradle`、`app/src/main/res/values/strings.xml` | 无 | `assembleDebug` 编译通过 | ⚠️ 配置类，需手动确认 |
| T2 | 纯逻辑层 | `LocalAccount`（账号常量 + 随机验证码生成）+ `LoginValidator`（三段校验纯函数） | `app/src/main/java/com/letter/basic/login/LocalAccount.kt`、`LoginValidator.kt`、`app/src/test/java/com/letter/basic/login/` 测试 | T1 | JUnit 单测 PASS | ✅ 单元测试 |
| T3 | 布局文件 | `activity_login.xml`（TabLayout + ViewPager2）、`fragment_account_login.xml`、`fragment_phone_login.xml` | `app/src/main/res/layout/` | T1 | `assembleDebug` 编译通过 | ⚠️ 布局类，需手动确认 |
| T4 | 容器 Activity | `LoginActivity`（BaseMultiStateVBActivity + TabLayout/ViewPager2/FragmentStateAdapter 绑定） | `app/src/main/java/com/letter/basic/login/LoginActivity.kt` | T1、T3 | `assembleDebug` 编译通过 | ⚠️ UI 容器，需手动确认 |
| T5 | 账号密码登录 | `AccountLoginFragment`：输入 → 格式校验（红字）→ 比对硬编码账号 → Toast 结果 | `app/src/main/java/com/letter/basic/login/AccountLoginFragment.kt` | T2、T3、T4 | 编译 + 手动 3 条路径 | ⚠️ UI 交互，需手动验证 |
| T6 | 手机号验证码登录 | `PhoneLoginFragment`：手机号校验 → 获取验证码（随机码 Toast + 60s 倒计时）→ 校验登录 | `app/src/main/java/com/letter/basic/login/PhoneLoginFragment.kt` | T2、T3、T4 | 编译 + 手动 4 条路径 | ⚠️ UI 交互（含 Handler 倒计时），需手动验证 |
| T7 | 入口与注册 | MainActivity 增加「本地登录演示」按钮跳转 + Manifest 注册 `LoginActivity` | `app/src/main/java/com/letter/basic/MainActivity.kt`、`app/src/main/res/layout/activity_main.xml`、`app/src/main/AndroidManifest.xml` | T4 | `assembleDebug` 编译通过 | ⚠️ 需手动确认 |

## 依赖关系

```
T1 ──> T3 ──> T4 ──> T5
 │             │      │
 │             └──────┴──> T6
 │
 └──> T2 ──────────────> T5 / T6（纯逻辑被两 Fragment 复用）

T7（入口）依赖 T4 完成
```

## 执行顺序

T1 → T2 → T3 → T4 → T5 → T6 → T7（严格按序，每 Task 独立 commit）

## Commit 约定（Conventional Commit）

- T1: `build(app): 新增 viewpager2 依赖与登录文案`
- T2: `feat(app): 新增本地登录校验与验证码生成逻辑`
- T3: `feat(app): 新增登录页与登录方式 Fragment 布局`
- T4: `feat(app): 新增 LoginActivity 容器（TabLayout+ViewPager2）`
- T5: `feat(app): 新增账号密码登录 Fragment`
- T6: `feat(app): 新增手机号验证码登录 Fragment`
- T7: `feat(app): 接入登录演示入口并注册 LoginActivity`

## 测试策略

- **T2**：`app/src/test/java/com/letter/basic/login/` 下 JUnit 4 单测：
  - `LoginValidatorTest`：账号表单（空用户名/空密码/合法）、手机号（长度/开头/合法）、
    验证码（空/非 6 位/非数字/合法）各分支
  - `LocalAccountTest`：`generateVerifyCode()` 返回 6 位纯数字
- **其余 Task**：UI/配置类，无有效单测，标注 ⚠️ 需手动验证（走 04-self-check 手动清单），不阻塞流程
