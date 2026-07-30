---
name: android-feature-workflow
description: >
  Android 新功能开发编排器（8 步全流程）。当用户说「开发新功能」「实现需求」「新增功能」「帮我开发」
  「开发一个XX」「实现XX功能」「开始开发XX」时自动触发。按 8 步线性流程严格执行，每步必须产出明确产物后
  才进入下一步，不可跳过。Step 1-4 委托 brainstorming 完成需求理解→方案设计→任务拆分，Step 5-7 调用
  android-test-gen/android-incremental-dev/android-self-check，Step 8 调用 android-git-commit
  完成审查与交付。全流程文档化：spec/plan/test-plan/self-check 均生成本地文档。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-30'
  keywords:
  - android
  - workflow
  - development
  - full-cycle
  - tdd
  - spec
  - plan
  - self-check
---

# Android 全流程开发编排器

## 默认行为

- 8 步线性执行，**不可跳过**，每步必须产出明确产物
- 所有过程文档保存到 `docs/features/<feature-name>/` 目录
- 每步结束时输出固定格式的状态报告
- 子 skill 仅在被调用时加载，避免上下文膨胀

## 8 步流程

```
Step 1-4  需求→方案→拆分  → brainstorming（内部完成 4 步）
Step 5    测试用例编写      → android-test-gen
Step 6    增量编码实现      → android-incremental-dev
Step 7    自测清单与验证    → android-self-check
Step 8    代码审查与交付    → android-git-commit
```

## 前置：确定 feature-name

在执行 Step 1 之前，必须向用户确认功能名称（`feature-name`），用于文档目录命名。
若用户未指定，根据需求描述自动生成（英文、kebab-case，如 `user-login`、`order-refund`）。

确认后输出：
```
📁 功能名称: <feature-name>
📁 文档目录: docs/features/<feature-name>/
➡️ 进入 Step 1-4
```

---

## Step 1-4：需求理解 → 技术方案 → 任务拆分

加载 `brainstorming` skill，指定文档输出：

- **01-spec.md** 路径：`docs/features/<feature-name>/01-spec.md`
- **02-plan.md** 路径：`docs/features/<feature-name>/02-plan.md`

brainstorming 内部完成：
- **Step 1**：需求理解与澄清（提问、澄清、确认范围）
- **Step 2**：技术可行性 & 影响评估（现有架构分析、依赖评估、风险评估）
- **Step 3**：技术方案设计（架构、组件、数据流、接口 → 产出 01-spec.md）
  - **必须** 在 01-spec.md 中包含「Android 版本兼容与碎片化评估」章节，覆盖：
    - minSdkVersion / targetSdkVersion 约束（项目当前最低和最高支持版本）
    - 各版本 API 差异（如 Android 10+ 分区存储、11+ 包可见性、12+ PendingIntent 可变性、13+ 通知权限、14+ 前台服务类型）
    - 厂商 ROM 差异（华为/小米/OPPO/Vivo 等后台限制、通知渠道行为差异、权限弹窗差异）
    - 屏幕尺寸与密度适配（手机/平板/折叠屏）
- **Step 4**：任务拆分与排期（任务列表 + 依赖关系 → 产出 02-plan.md）

这是用户交互密集型阶段，严格按照 brainstorming 的流程执行（一次一个提问、分段确认设计）。

**完成后验证：**

确认 `docs/features/<feature-name>/01-spec.md` 和 `docs/features/<feature-name>/02-plan.md` 两个文件均已存在且内容完整。

### 固定输出

```
## Step 1-4 完成：需求→方案→拆分
📄 产物:
  docs/features/<feature-name>/01-spec.md  — 需求理解 + 技术方案
  docs/features/<feature-name>/02-plan.md  — 任务拆分与排期
📊 状态: ✅ 已完成
⏭️ 进入 Step 5
```

### 边界

| 场景 | 行为 |
|------|------|
| 用户拒绝设计方案 | 回到 Step 1-4 重新澄清，不进入 Step 5 |
| 02-plan.md 未生成 | **阻塞**，等待 brainstorming 完成（它内部会调用 writing-plans） |
| feature-name 与已有目录重复 | 警告并请求确认是否覆盖或使用新名称 |

---

## Step 5：测试用例编写

加载 `android-test-gen` skill。

该 skill 将：
1. 读取 `docs/features/<feature-name>/02-plan.md`，提取所有 Task
2. 对每个 Task 生成测试代码文件（JUnit/Mockito/Espresso，遵循项目已有测试风格）
3. 生成 `docs/features/<feature-name>/03-test-plan.md`
4. 运行测试，确认全部初始状态为 **FAIL**（红阶段）

**完成后验证：**

确认 `docs/features/<feature-name>/03-test-plan.md` 存在且测试文件已生成。

### 固定输出

```
## Step 5 完成：测试用例编写
📄 产物: docs/features/<feature-name>/03-test-plan.md
📊 测试统计: N 个 Task → M 个测试用例，初始状态全部 FAIL ✅
⏭️ 进入 Step 6
```

### 边界

| 场景 | 行为 |
|------|------|
| 02-plan.md 中无 Task | 硬阻塞，回退 Step 1-4 |
| 某 Task 无法生成有效测试 | 标注 `⚠️ 需手动设计`，不阻塞流程 |
| 测试文件与已有测试命名冲突 | 追加序号，警告用户 |

---

## Step 6：增量编码实现

加载 `android-incremental-dev` skill。

该 skill 将：
1. 读取 `docs/features/<feature-name>/02-plan.md`，按 Task 顺序执行
2. **每个 Task 的 TDD 循环：**
   - 找到对应测试 → 运行测试（确认 FAIL）
   - 编写最小实现代码
   - 运行测试（确认 PASS）
   - 可行重构
   - `git add` + `git commit`（Conventional Commit 格式）
3. 全部 Task 完成后，确保整体测试通过

**完成后验证：**

所有 Task 完成，`git log` 包含每个 Task 的独立 commit。

### 固定输出

每完成一个 Task：
```
🟢 Task N/N 完成: <task 描述>
   commit: a1b2c3d feat(xxx): <task 描述>
   (M/N tasks 完成)
```

全部完成后：
```
## Step 6 完成：增量编码实现
📊 任务统计: N/N Task 全部完成，N 个 commit
✅ 全部测试 PASS
⏭️ 进入 Step 7
```

### 边界

| 场景 | 行为 |
|------|------|
| 测试持续 FAIL（无法 PASS） | **阻塞**，展示错误信息，请求用户介入 |
| Task 间有强依赖 | 严格按 plan 中的依赖顺序执行 |
| 冲突文件（与其他 branch） | 停止，请求用户先解决冲突 |
| 无 Kotlin/Java 变更 | 输出 `⚠️ 该 Task 未产生代码变更`，询问是否跳过 |

---

## Step 7：自测清单与验证

加载 `android-self-check` skill。

该 skill 将：
1. 加载 `01-spec.md`、`02-plan.md`、`03-test-plan.md` 三份文档
2. **交叉印证：**
   - spec 每个需求点 → plan 中是否有对应 Task？
   - plan 每个 Task → test-plan 中是否有对应测试用例？
   - test-plan 每个用例 → 是否全部 PASS？
   - 输出覆盖矩阵
3. 逐项执行验证（自动化测试运行 + 手动验证项指导）
4. 生成 `docs/features/<feature-name>/04-self-check.md`

**完成后验证：**

确认 `docs/features/<feature-name>/04-self-check.md` 存在，交叉印证无遗漏。

### 固定输出

```
## Step 7 完成：自测清单与验证
📄 产物: docs/features/<feature-name>/04-self-check.md
📊 交叉印证:
  spec 需求点: N → plan Task 覆盖: N/N ✅
  plan Task: N → test 用例覆盖: N/N ✅
  遗漏: 无
  测试结果: M/M PASS ✅
⏭️ 进入 Step 8
```

若存在遗漏：
```
📊 交叉印证:
  ⚠️ spec-R3「异常处理」→ 无对应 plan Task
  ⚠️ plan-T2「缓存逻辑」→ 无对应测试用例
  → 阻塞进入 Step 8，请先修复遗漏
```

### 边界

| 场景 | 行为 |
|------|------|
| 交叉印证发现遗漏 | **硬阻塞**，不进入 Step 8，提示用户补充 |
| 测试未全部 PASS | **硬阻塞**，展示失败用例，要求修复 |
| spec/plan/test-plan 任一文档缺失 | 回退到对应步骤重新执行 |

---

## Step 8：代码审查与交付

加载 `android-git-commit` skill。

该 skill 将执行标准六步流程：
1. 代码审查（Phase A 安全 → Phase B 规范自修 → Phase C 异常自修）
2. 注释检测（detekt + checkstyle → KDoc 补全）
3. 同步远端（fetch + pull --rebase）
4. 提交（变更分析 + message + commit）
5. 推送
6. MR 链接

按 android-git-commit 的固定输出格式展示，不做额外包装。

### 固定输出

```
## Step 8 完成：代码审查与交付
🔗 <MR 链接>
🏁 全流程完成！
```

### 边界

完全委托给 android-git-commit，使用其已有的边界条件。如果 Step 8 被阻塞（安全违规、冲突等），在对应子步骤终止。

---

## 全流程硬阻塞条件

| 条件 | 阻塞点 | 行为 |
|------|--------|------|
| Step 1-4: 用户拒绝所有方案 | Step 4 | 流程终止 |
| Step 1-4: spec/plan 文档未产出 | Step 4 | 等待产出 |
| Step 5: plan 无 Task | Step 5 | 流程终止 |
| Step 6: 测试持续 FAIL | Step 6 | 等待用户介入 |
| Step 7: 交叉印证有遗漏 | Step 7 | 等待用户补充 |
| Step 7: 测试未全部 PASS | Step 7 | 等待用户修复 |
| Step 8: Phase A 安全违规 | Step 8 | 等待用户修复 |
| Step 8: 合并冲突 | Step 8 | 等待用户解决 |

---

## 流程终止条件

| 条件 | 终止点 | 是否可恢复 |
|------|--------|-----------|
| 用户明确说「停止」「取消」 | 任意步骤 | 需重新触发 |
| Step 1-4 用户拒绝所有方案 | Step 4 | 需重新触发 |
| Step 8 Phase A 安全违规无法修复 | Step 8 | 需重新触发 |
| feature-name 冲突且用户拒绝覆盖 | 前置 | 需重新触发 |

---

## 可选开关

| 开关 | 作用 |
|------|------|
| `--quick` | 跳过 Step 7 手动验证项，仅执行自动化测试和交叉印证 |
| `--skip-tests` | 跳过 Step 5-6 的 TDD 循环中运行测试步骤（编码仍正常执行） |
| `--no-commit` | Step 6 逐 task 编码后不 git commit，统一在 Step 8 提交 |
