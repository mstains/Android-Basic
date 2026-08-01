# 测试计划 — local-login（本地演示登录）

- 日期：2026-08-01
- 依赖：`docs/features/local-login/02-plan.md`

## 概览

| 指标 | 数值 |
|------|------|
| Task 总数 | 7 |
| 测试类总数 | 2 |
| 测试用例总数 | 13 |
| 正常路径覆盖 | 5 |
| 边界条件覆盖 | 2 |
| 异常路径覆盖 | 6 |
| 初始状态 | 全部 FAIL ✅（T2 测试因实现类不存在编译失败） |

## Task-测试映射

| Task | 测试类 | 覆盖类型 | 用例数 | 状态 |
|------|-------|---------|--------|------|
| T1: 依赖与基础配置 | —（配置类，无测试） | — | 0 | ⚠️ 需手动确认（编译验证） |
| T2: 纯逻辑层 | `LoginValidatorTest` | 正常 3 / 边界 2 / 异常 6 | 11 | FAIL |
| T2: 纯逻辑层 | `LocalAccountTest` | 正常 2 / 边界 0 / 异常 0 | 2 | FAIL |
| T3: 布局文件 | —（XML，无测试） | — | 0 | ⚠️ 需手动确认（编译 + UI 目检） |
| T4: 容器 Activity | —（UI 容器，无单测依赖） | — | 0 | ⚠️ 需手动确认 |
| T5: 账号密码登录 | —（UI 交互，业务逻辑已被 T2 单测覆盖） | — | 0 | ⚠️ 需手动验证 3 条路径 |
| T6: 手机号验证码登录 | —（UI 交互 + Handler 倒计时，业务逻辑已被 T2 单测覆盖） | — | 0 | ⚠️ 需手动验证 4 条路径 |
| T7: 入口与注册 | —（配置类，无测试） | — | 0 | ⚠️ 需手动确认 |

> 说明：T1/T3/T4/T7 为配置与布局类，无有效单元测试载体；T5/T6 的格式校验与比对逻辑
> 已全部下沉到 `LoginValidator` / `LocalAccount`（T2 单测覆盖），Fragment 仅做 UI 绑定与
> 结果提示，交互路径走 04-self-check 手动清单验证。全部标注 ⚠️ 需手动验证，不阻塞流程。

## 测试文件清单

- `app/src/test/java/com/letter/basic/login/LoginValidatorTest.kt` — 登录校验纯函数测试
  （账号表单 / 手机号 / 验证码三类校验的正常、边界、异常路径）
- `app/src/test/java/com/letter/basic/login/LocalAccountTest.kt` — 账号常量一致性 +
  随机验证码长度与数字约束测试

## 依赖

- JUnit 4（`app/build.gradle` 已有 `testImplementation libs.junit`）
- 无 Mockito / Robolectric / Espresso（纯函数测试，无需 Android 运行时）

## 验证命令

```bash
./gradlew :app:testDebugUnitTest --tests "com.letter.basic.login.*"
```
