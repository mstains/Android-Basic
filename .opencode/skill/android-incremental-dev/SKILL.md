---
name: android-incremental-dev
description: >
  Android 增量编码实现。当 android-feature-workflow 执行 Step 6 时由编排器调用。
  读取 02-plan.md 中的 Task 列表，严格按顺序逐 Task 执行 TDD 循环：
  读 Task 描述 → 找到对应测试 → 预期 FAIL → 编写最小实现 → 预期 PASS → 重构 → git commit。
  每个 Task 完成一个 Conventional Commit，全部 Task 完成后整体测试确认通过。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-30'
  keywords:
  - android
  - tdd
  - incremental
  - development
  - commit
---

# Android 增量编码实现

## 前置条件

以下文件必须存在：
- `docs/features/<feature-name>/02-plan.md` — Task 列表
- 对应测试文件（Step 5 产物）

## 总体工作流

```
Task 1: 读 plan → 读测试 → 运行测试(FAIL) → 写代码 → 运行测试(PASS) → 重构 → commit
Task 2: 读 plan → 读测试 → 运行测试(FAIL) → 写代码 → 运行测试(PASS) → 重构 → commit
...
Task N: ... → commit
→ 全部 Task 完成，整体测试通过
```

## 每个 Task 的 TDD 循环

### 子步骤 1：读取 Task 信息

从 `02-plan.md` 提取当前 Task：
- Task 编号和名称
- 要创建/修改的文件及路径
- 实现目标描述
- 依赖的 Task（确保前置 Task 已完成）

### 子步骤 2：找到对应测试

根据 Task 中 Create/Modify 的文件，定位 Step 5 生成的对应用测试文件。
测试文件命名规则：`<源文件名>Test.kt`

若找不到对应测试文件，标注 `⚠️ 无对应测试` 并继续（测试缺失不在编码环节阻塞）。

### 子步骤 3：运行测试 — 预期 FAIL（红阶段）

```bash
./gradlew testDebugUnitTest --tests "<package>.<TestClass>*"
```

**必须确认 FAIL**。如是 PASS，说明：
- 实现代码已存在 → 直接跳到子步骤 7 commit
- 测试不够严谨 → 标注 `⚠️ 测试未覆盖差异`

### 子步骤 4：编写最小实现

遵循 `android-code-style` skill 中的编码规范。

编写规则：
- **只写让测试 PASS 的最少代码**，不过度设计
- 先创建文件/类骨架，再填充逻辑
- 遵循项目已有的包结构和命名约定
- 引用 `android-code-style` 规范：命名后缀（Activity、ViewModel、Repository 等）、协程用法、Glide/Retrofit 封装

### 子步骤 5：运行测试 — 预期 PASS（绿阶段）

```bash
./gradlew testDebugUnitTest --tests "<package>.<TestClass>*"
```

**结果判定：**
- 全部 PASS → 进入子步骤 6
- 部分 FAIL → 分析失败原因，修正代码后重新运行
- 持续 FAIL（3 次尝试后）→ **阻塞**，输出错误详情

```
🔴 Task N 测试持续失败 — 请人工介入
失败用例:
  ✗ login with valid credentials emits success state
    → 预期: SuccessState, 实际: LoadingState
    → 文件: LoginViewModel.kt:42
```

### 子步骤 6：重构

检查代码质量（但不改变外部行为）：
- 提取重复逻辑
- 优化命名
- 移除 magic number
- 遵循 `android-code-style` 中的编码规范

重构后务必再次运行测试确认 PASS。

### 子步骤 7：运行整体单元测试

```bash
./gradlew testDebugUnitTest
```

确认本次 Task 的修改没有破坏已有测试。

### 子步骤 8：Git Commit

使用 `android-git-commit-core` 的 Conventional Commit 格式：

```bash
git add <modified files>
git commit -m "<type>(<scope>): <task 描述>"
```

Commit 类型选择：

| 情况 | type |
|------|------|
| 新增类/功能 | `feat` |
| 修改已有逻辑 | `fix` 或 `refactor` |
| 仅涉及测试 | `test` |
| 资源文件 | `style` 或 `chore` |
| 构建配置 | `build` |

Scope 使用 feature-name。

示例：
```
feat(login): create LoginViewModel with credential validation
fix(login): handle empty password edge case in LoginRepository
test(login): add network error test for LoginViewModel
```

---

## 固定输出

### 每 Task 完成时

```
🟢 Task 2/5 完成: LoginViewModel — credential validation
   1 file created, 2 files modified
   commit: a1b2c3d feat(login): create LoginViewModel with credential validation
   (2/5 tasks 完成)
```

### 全部完成时

```
## Step 6 完成：增量编码实现

📊 任务统计:
  Total: N Task
  Completed: N
  Commits: N

📄 变更文件:
  + <file1>.kt
  + <file2>.kt
  ~ <file3>.kt
  ~ <file4>.kt

✅ 全部测试 PASS
⏭️ 进入 Step 7
```

---

## 边界

| 场景 | 行为 |
|------|------|
| 02-plan.md 不存在 | 提示用户先完成 Step 1-4 |
| plan 无 Task | 提示用户先完成 Step 4 |
| Task 中 Create/Modify 路径不明确 | 根据 Task 描述推断，标注 `⚠️ 推测路径` |
| 测试文件不存在 | `⚠️ 无对应测试`，继续编码 |
| 测试 3 次仍 FAIL | **阻塞**，输出错误详情，等待用户介入 |
| 整体测试被本次 Task 破坏 | **阻塞**，回退修改，分析原因 |
| 依赖的 Task 未完成 | 跳过，标注 `⏭ 等待依赖 Task N`，等依赖完成后再回来 |
| Task 修改已有文件（修改旧逻辑） | 先阅读要修改的文件，理解上下文 |
| 是否需要 Layout XML | 遵循 `android-drawable-xml` 和 `android-custom-view` skill |
| 是否需要新增第三方依赖 | 暂停并询问用户确认 |

---

## 任务依赖处理

当 Task B 依赖 Task A（Task A 创建了 Task B 要用到的类），严格按顺序执行：
1. Task A 全部完成（含 commit）后，再开始 Task B
2. 若 loop 中遇到依赖未完成的 Task，**跳过**并标注，等依赖完成后再回到该 Task

```
⏭ Task 3/5 跳过: OrderRepository（依赖 Task 1 中的 UserRepository 尚未完成）
→ 继续 Task 4...
→ 完成后回到 Task 3
```

---

## 回退规则

若当前 Task 实现失败且需要回退：

```bash
# 仅回退当前 Task 未提交的修改
git checkout -- <未提交文件>
git clean -fd
```

**不允许** `git reset` 已提交的 commit（除非用户明确要求）。
