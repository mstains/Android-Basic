---
name: android-self-check
description: >
  Android 自测清单与验证。当 android-dev-workflow 执行 Step 7 时由编排器调用，
  或用户说「自测」「验证功能」「检查功能是否完整」「交叉印证」时独立触发。
  读取 01-spec.md（需求）、02-plan.md（任务拆分）、03-test-plan.md（测试计划）三份文档，
  执行交叉印证确保 spec 需求→plan 任务→test 用例全覆盖无遗漏，
  逐项执行验证操作（自动化测试 + 手动操作指引），
  生成 04-self-check.md 自测清单并记录验证结果与证据。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-30'
  keywords:
  - android
  - self-check
  - validation
  - cross-reference
  - testing
  - checklist
---

# Android 自测清单与验证

## 前置条件

以下文件必须存在：
- `docs/features/<feature-name>/01-spec.md` — 需求理解 + 技术方案
- `docs/features/<feature-name>/02-plan.md` — 任务拆分与排期
- `docs/features/<feature-name>/03-test-plan.md` — 测试计划

若任一缺失，提示用户先完成对应步骤。

## 总体工作流

```
Phase 1: 加载文档 — 提取需求点、Task、测试用例
Phase 2: 交叉印证 — 需求→Task→测试 全覆盖检查
Phase 3: 自动化验证 — 运行全部测试
Phase 4: 手动验证 — 逐项操作指引，收集用户反馈
Phase 5: 生成报告 — 输出 04-self-check.md
```

---

## Phase 1：加载与提取

### 从 01-spec.md 提取需求点

将 spec 中的功能需求提取为编号列表：
```
R1: 用户输入用户名和密码后点击登录，验证通过则跳转首页
R2: 用户名或密码为空时禁用登录按钮
R3: 登录失败显示错误提示（网络错误、服务端错误分别显示不同文案）
...
```

提取规则：
- 每条功能性描述为一个需求点
- 跳过纯架构说明（如"使用 MVVM 模式"）
- 跳过非功能需求（如性能、安全，除非 spec 中有具体指标）

### 从 02-plan.md 提取 Task

提取每个 Task 的名称和描述：
```
T1: 创建 LoginViewModel — 处理登录逻辑，暴露 LiveData state
T2: 创建 LoginRepository — 封装 API 调用和错误处理
T3: 修改 LoginActivity — 绑定 ViewModel，实现按钮状态联动
...
```

### 从 03-test-plan.md 提取测试用例

提取所有测试用例，按 Task 分组：
```
T1: LoginViewModelTest
  - test1: login with valid credentials emits success state
  - test2: login with empty password emits error state
  - test3: login when network fails emits network error state
T2: LoginRepositoryTest
  - test1: login success returns user token
  - test2: login network error throws NetworkException
...
```

---

## Phase 2：交叉印证

### 2.1 需求→Task 覆盖检查

对每个 spec 需求点 R，检查是否被至少一个 plan Task 覆盖：

```
┌──────────────────┬────┬────┬────┐
│                  │ T1 │ T2 │ T3 │
├──────────────────┼────┼────┼────┤
│ R1: 登录验证     │ ✅ │    │ ✅ │
│ R2: 按钮禁用     │ ✅ │    │ ✅ │
│ R3: 错误提示     │ ✅ │ ✅ │    │
└──────────────────┴────┴────┴────┘
```

若有需求点未被覆盖 → **硬阻塞**，输出：

```
🔴 覆盖缺失:
  R4「记住密码功能」→ 无对应 Task
  → 请回到 Step 1-4 补充 Task，或确认该需求已移除
```

### 2.2 Task→测试覆盖检查

对每个 plan Task T，检查是否在 test-plan 中有对应测试用例：

```
┌───────────────────┬─────┬──────┬──────┐
│                   │ 正常 │ 边界 │ 异常 │
├───────────────────┼─────┼──────┼──────┤
│ T1: LoginViewModel│ ✅  │ ✅   │ ✅   │
│ T2: LoginRepo     │ ✅  │      │ ✅   │
│ T3: LoginActivity │ ✅  │      │      │
└───────────────────┴─────┴──────┴──────┘
```

若有 Task 无对应测试 → **硬阻塞**，输出：

```
🔴 测试缺失:
  T3「LoginActivity UI」→ 无对应测试用例
  → 请回到 Step 5 补充测试
```

若有 Task 缺少某类覆盖（边界/异常）→ **警告**：

```
⚠️ 覆盖不足:
  T2「LoginRepository」→ 缺少边界条件测试
  T3「LoginActivity」→ 缺少边界条件和异常路径测试
  → 建议补充，可选择性阻塞
```

### 2.3 印证结论

输出覆盖矩阵汇总：

```
📊 交叉印证结果:
  spec 需求点: 3 个
  plan Task: 3 个
  test 用例: 6 个

  需求→Task 覆盖: 3/3 ✅
  Task→测试 覆盖: 3/3 ✅
  测试类型覆盖: 正常 3/3 ✅, 边界 2/3 ⚠️, 异常 2/3 ⚠️
  遗漏: 无
```

---

## Phase 3：自动化验证

运行全部单元测试：

```bash
./gradlew testDebugUnitTest
```

运行 UI 测试（如有）：

```bash
./gradlew connectedAndroidTest
```

记录结果：

```
🧪 自动化测试结果:
  单元测试: 6/6 PASS ✅
  UI 测试: 1/1 PASS ✅
  总计: 7/7 PASS
```

若任一测试 FAIL → **硬阻塞**，输出失败用例详情：

```
🔴 测试失败:
  ✗ LoginViewModelTest > login with empty password emits error state
    → 预期: ErrorState("Password cannot be empty")
    → 实际: ErrorState("Invalid input")
    → 文件: LoginViewModel.kt:42
  请修复后重新执行 Phase 3
```

---

## Phase 4：逐项手动验证

对不适合自动化的验证项（UI 表现、多设备行为、用户感知），逐项指导用户操作：

### 手动验证项识别规则

以下情况归为手动验证：
- UI 视觉效果（布局、颜色、动画）
- 多设备/多分辨率适配
- 触觉反馈（震动、音效）
- 跨 Activity 导航行为
- 后台行为（通知、Service）
- 权限弹窗交互
- 网络切换行为

### 逐项验证格式

```
📱 手动验证项 1/3: 登录按钮禁用状态
  来源: spec-R2「用户名或密码为空时禁用登录按钮」
  操作:
    1. 打开应用
    2. 不输入任何内容
    3. 观察登录按钮
  预期: 按钮为灰色不可点击状态
  实际: [用户自行判断]
  证据: [截图路径，如 docs/features/<feature-name>/assets/login-disabled.png]
  结果: ⬜ 待验证
```

### 收集用户反馈

逐项展示手动验证项，每次一项，等待用户回复结果：

```
📱 手动验证 1/N: <验证项描述>
结果: [✅ 通过 / ❌ 不通过 / ⏭ 跳过]
证据路径（可选）:
```

---

## Phase 5：生成 04-self-check.md

文档路径：`docs/features/<feature-name>/04-self-check.md`

**文档模板：**

```markdown
# 自测清单 — <feature-name>

## 交叉印证

| 维度 | 结果 |
|------|------|
| spec 需求点总数 | N |
| plan Task 总数 | N |
| test 用例总数 | N |
| 需求→Task 覆盖率 | N/N (100%) |
| Task→测试 覆盖率 | N/N (100%) |
| 遗漏 | 无 |

### 覆盖矩阵

| 需求点 | 覆盖 Task | 测试用例 |
|--------|----------|---------|
| R1: <描述> | T1, T3 | test1, test2 |
| R2: <描述> | T1, T3 | test2, test4 |
| ... | ... | ... |

## 自动化测试

| 测试类 | 用例数 | 通过 | 失败 | 状态 |
|--------|--------|------|------|------|
| LoginViewModelTest | 3 | 3 | 0 | ✅ |
| LoginRepositoryTest | 2 | 2 | 0 | ✅ |
| LoginActivityInstrumentedTest | 1 | 1 | 0 | ✅ |
| **合计** | **6** | **6** | **0** | **✅** |

## 手动验证

| # | 验证项 | 来源 | 操作 | 结果 | 证据 |
|---|-------|------|------|------|------|
| 1 | 登录按钮禁用状态 | spec-R2 | 不输入内容，观察按钮 | ✅ | assets/login-disabled.png |
| 2 | 错误提示文案 | spec-R3 | 输入错误密码，观察提示 | ✅ | assets/login-error.png |
| 3 | 键盘收起行为 | spec-R2 | 点击登录后键盘收起 | ✅ | 无 |

## 结论

- 自动化测试: 6/6 PASS ✅
- 手动验证: 3/3 PASS ✅
- 交叉印证: 无遗漏 ✅
- 整体状态: **通过**
```

---

## 固定输出

### 全部完成且全部通过

```
## Step 7 完成：自测清单与验证

📄 产物: docs/features/<feature-name>/04-self-check.md

📊 交叉印证:
  spec 需求点: 3 → Task 覆盖: 3/3 ✅
  plan Task: 3 → test 覆盖: 3/3 ✅
  遗漏: 无

🧪 自动化测试:
  单元测试: 6/6 PASS ✅
  UI 测试: 1/1 PASS ✅

📱 手动验证:
  已验证: 3/3 PASS ✅

🏁 自测通过，进入 Step 8
```

### 有遗漏或失败

```
## Step 7 阻塞：自测清单发现遗漏

🔴 阻塞项:
  - spec-R4「记住密码」→ 无对应 Task
  - T3「LoginActivity」→ 无对应测试用例

🔴 测试失败:
  ✗ LoginViewModelTest.testNetworkError → 预期 ErrorState, 实际 null

📱 手动验证未完成: 1/3

→ 请修复上述问题后重新执行 Step 7
```

---

## 边界

| 场景 | 行为 |
|------|------|
| 01-spec/02-plan/03-test-plan 任一缺失 | 提示用户先完成对应步骤 |
| 交叉印证有遗漏 | **硬阻塞**，不继续，要求补充 |
| 自动化测试未全部 PASS | **硬阻塞**，不继续，要求修复 |
| 手动验证存在 FAIL | **硬阻塞**，询问用户是否接受风险继续 |
| 用户选择跳过全部手动验证 | 标注 `⏭ 跳过手动验证`，自动化和交叉印证通过后可继续 |
| 无 UI 测试（纯后端逻辑变更） | 跳过 Phase 4 手动验证 UI 部分 |
| spec 中需求点过少（无功能需求） | 标注 `⚠️ spec 中未提取到明确功能需求点` |
