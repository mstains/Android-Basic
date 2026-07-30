---
name: android-refactor-workflow
description: >
  Android 重构编排器（5 步流程）。当用户说「重构XX」「优化XX架构」「改造XX模块」「整理XX代码」
  「拆分XX」「提取XX」时自动触发。按 5 步线性流程严格执行，每步必须产出明确产物后才进入下一步，
  不可跳过。全流程：重构目标与范围明确 → 技术方案设计 → 任务拆分 → 增量编码 → 自测清单与验证。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-30'
  keywords:
  - android
  - refactor
  - workflow
  - optimization
---

# Android 重构编排器

## 默认行为

- 5 步线性执行，**不可跳过**，每步必须产出明确产物
- 所有过程文档保存到 `docs/features/<refactor-name>/` 目录（refactor-name 为重构目标简短描述，kebab-case）
- 每步结束时输出固定格式的状态报告
- 子 skill 仅在被调用时加载，避免上下文膨胀
- **不包含代码审查与交付步骤**，重构完成后的 commit/MR 由用户手动触发

## 前置：确定 refactor-name

在执行 Step 1 之前，必须向用户确认重构名称（`refactor-name`），用于文档目录命名。
若用户未指定，根据重构目标自动生成（英文、kebab-case，如 `extract-network-layer`、`migrate-mvi`）。

确认后输出：
```
📁 重构名称: <refactor-name>
📁 文档目录: docs/features/<refactor-name>/
➡️ 进入 Step 1
```

## 5 步流程

```
Step 1  重构目标与范围明确  → 明确重构目的、范围边界、成功标准
Step 2  技术方案设计        → 设计重构方案，列出模块间新边界
Step 3  任务拆分            → 将重构拆为可独立执行和验证的 Task
Step 4  增量编码            → 逐 Task 重构，每次 commit，确保测试通过
Step 5  自测清单与验证      → 交叉印证：重构后行为不变，无回归
```

---

## Step 1：重构目标与范围明确

**目标**：明确为什么重构、改多少、什么是成功。

### 操作步骤

1. 使用 `codegraph explore` 分析当前代码结构和调用关系
2. 向用户确认以下信息（依次提问，一次一个）：
   - 为什么要重构？（性能/可维护性/架构升级/消除技术债/新需求驱动）
   - 重构哪些模块/文件？（明确边界，列出具体文件路径）
   - 不改什么？（明确范围外，防止蔓延）
   - 成功标准是什么？（测试通过/性能基线/代码指标）

**文档**：`docs/features/<refactor-name>/01-refactor-goal.md`

```markdown
# 重构目标 — <refactor-name>

## 背景
- 现状问题: ...
- 重构动机: [性能/可维护性/架构升级/消除技术债]

## 范围
- 重构范围:
  - `path/to/Module1.kt` — <目标>
  - `path/to/Module2.kt` — <目标>
- 不重构:
  - `path/to/keep/Klass.kt` — <原因>

## 成功标准
- [ ] 所有现有测试 PASS
- [ ] 无新增 Lint 警告
- [ ] 对外接口不变（调用方无感知）
- [ ] (性能基线，如适用)
```

### 固定输出

```
## Step 1 完成：重构目标与范围明确
📄 产物: docs/features/<refactor-name>/01-refactor-goal.md
📊 范围: N 个文件，M 个模块
⏭️ 进入 Step 2
```

### 边界

| 场景 | 行为 |
|------|------|
| 范围过大（>10 文件且耦合高） | 建议拆分多次重构，每次控制范围 |
| 范围模糊 | 反复追问直到明确，不进入 Step 2 |

---

## Step 2：技术方案设计

**目标**：设计新架构/新模块边界/新接口。

### 操作步骤

1. 基于现有代码结构，设计重构后的模块边界
2. 明确哪些类/接口是新创建的，哪些是修改的，哪些是要删除的
3. 说明数据流变化（如有）

**文档**：`docs/features/<refactor-name>/02-refactor-plan.md`

```markdown
# 重构方案 — <refactor-name>

## 当前架构
（描述现状：类关系图/调用关系）

## 目标架构
（描述目标：新模块边界/新接口/新类关系）

## 具体变更

### 新建
| 文件 | 职责 | 依赖 |
|------|------|------|
| `<NewFile>.kt` | <职责> | <依赖> |

### 修改
| 文件 | 改动 | 原因 |
|------|------|------|
| `<File>.kt` | <改动描述> | <原因> |

### 删除
| 文件 | 原因 | 替代 |
|------|------|------|
| `<OldFile>.kt` | <废弃原因> | `<NewFile>.kt` |

## 版本兼容性检查
- API 级别影响: [重构是否引入仅高版本可用的 API？列出涉及的 API 及最低版本]
- 兼容方案: [如需兼容低版本，使用 `@RequiresApi` 注解 或 `Build.VERSION.SDK_INT` 分支]
- API 废弃替换: [是否有旧 API 被替换？新 API 是否在 minSdkVersion 范围内？]
- 厂商适配: [重构后是否影响厂商 ROM 特殊行为？如后台限制、通知渠道]

## 风险评估
- 调用方影响: ...
- 行为变化: [无(纯重构)/有(需标注)]
- 回滚策略: ...
```

### 固定输出

```
## Step 2 完成：技术方案设计
📄 产物: docs/features/<refactor-name>/02-refactor-plan.md
📊 新建 N 文件, 修改 M 文件, 删除 K 文件
⏭️ 进入 Step 3
```

---

## Step 3：任务拆分

**目标**：将重构拆分为可独立执行和验证的 Task。

### 操作步骤

1. 基于 `02-refactor-plan.md` 拆分为 Task
2. 每个 Task 应：
   - 独立可验证（完成后可跑测试确认无回归）
   - 有明确的输入（依赖）和输出（产物）
   - 有顺序要求（依赖关系）

**追加到** `02-refactor-plan.md`：

```markdown
## 任务拆分

| # | Task | 类型 | 文件 | 依赖 | 预估 |
|---|------|------|------|------|------|
| 1 | 提取接口 `IUserRepo` | 新建 | `IUserRepo.kt` | — | 小 |
| 2 | `UserRepo` 实现接口 | 修改 | `UserRepo.kt` | T1 | 小 |
| 3 | 替换调用方引用 | 修改 | `ViewModel.kt` | T2 | 中 |
| 4 | 删除旧实现 | 删除 | `OldRepo.kt` | T3 | 小 |
| ... | ... | ... | ... | ... | ... |
```

### 固定输出

```
## Step 3 完成：任务拆分
📄 产物: docs/features/<refactor-name>/02-refactor-plan.md（已补充任务拆分）
📊 Task: N 个，按依赖顺序排列
⏭️ 进入 Step 4
```

---

## Step 4：增量编码

**目标**：逐 Task 重构，确保每步测试通过。

加载 `android-incremental-dev` skill 的核心逻辑。

### 每个 Task 循环

```
Task N: 读取 task 描述
  → 修改文件（仅改当前 task 涉及的文件）
  → 运行全部测试
     → PASS → git add + git commit
     → FAIL → 分析原因，修正后重试
  → 下一个 task
```

### 提交格式

使用 Conventional Commit：

```
refactor(<scope>): <task 描述>
```

### 每 Task 完成时输出

```
🟢 Task 2/4 完成: UserRepo 实现 IUserRepo
   2 files modified
   commit: a1b2c3d refactor(data): implement IUserRepo in UserRepo
   (2/4 tasks 完成)
```

### 全部完成时输出

```
## Step 4 完成：增量编码
📊 Task: N/N 全部完成，N 个 commit
✅ 全部测试 PASS
⏭️ 进入 Step 5
```

### 边界

| 场景 | 行为 |
|------|------|
| 测试 FAIL（回归） | **硬阻塞**，回退该 task 修改，分析原因 |
| 3 次修复仍 FAIL | 终止，要求用户介入 |
| 重构后发现可进一步优化 | 记录到 Task 列表末尾，标注 `⏭ 可选`，不阻塞当前流程 |

---

## Step 5：自测清单与验证

**目标**：确认重构后行为不变，无回归，满足成功标准。

### 操作步骤

1. 运行全部测试：
   ```bash
   ./gradlew testDebugUnitTest
   ```

2. 逐项对照 `01-refactor-goal.md` 的成功标准

3. 检查对外接口是否变化（对比重构前后的 public API）

**文档**：`docs/features/<refactor-name>/03-self-check.md`

```markdown
# 自测清单 — <refactor-name>

## 成功标准验证

| # | 标准 | 来源 | 结果 | 证据 |
|---|------|------|------|------|
| 1 | 所有现有测试 PASS | 01-refactor-goal | ✅ | N/N PASS |
| 2 | 无新增 Lint 警告 | 01-refactor-goal | ✅ | 0 new warnings |
| 3 | 对外接口不变 | 01-refactor-goal | ✅ | public API diff 为空 |
| 4 | (性能基线，如适用) | 01-refactor-goal | ✅ | ... |

## 测试结果

| 测试类 | 用例数 | 通过 | 失败 |
|--------|--------|------|------|
| ... | ... | ... | ... |
| **合计** | **N** | **N** | **0** |

## 回归检查

| 检查项 | 结果 |
|--------|------|
| 调用方是否无需修改 | ✅ |
| 数据库兼容（如有） | ✅ |
| ... | ... |

## 结论
- 重构完成: ✅
- 行为不变: ✅
- 全部成功标准达成: ✅
- 整体状态: **通过**
```

### 固定输出

```
## Step 5 完成：自测清单与验证
📄 产物: docs/features/<refactor-name>/03-self-check.md
📊 成功标准: N/N 达成 ✅
✅ 全部测试 PASS，行为不变
🏁 重构流程完成（如需提交/MR，请手动触发 android-git-commit）
```

### 边界

| 场景 | 行为 |
|------|------|
| 测试 FAIL | **硬阻塞**，回到 Step 4 |
| 成功标准有未达成项 | **硬阻塞**，标注未达成项，询问用户是否接受 |
| public API 有破坏性变更 | 标注 `⚠️ 对外接口已变化`，提醒用户通知调用方 |
