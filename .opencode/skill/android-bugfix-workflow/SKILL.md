---
name: android-bugfix-workflow
description: >
  Android Bug 修复编排器（7 步流程）。当用户说「修复XX」「解决XX bug」「XX崩溃」「XX报错」
  「修一下XX」「处理XX异常」时自动触发。按 7 步线性流程严格执行，每步必须产出明确产物后才进入下一步，
  不可跳过。全流程：问题理解与复现确认 → 根因分析与影响评估 → 修复方案设计 → 回归测试编写 →
  编码修复 → 自测清单与验证 → 代码审查与交付。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-30'
  keywords:
  - android
  - bugfix
  - workflow
  - fix
  - debug
---

# Android Bug 修复编排器

## 默认行为

- 7 步线性执行，**不可跳过**，每步必须产出明确产物
- 所有过程文档保存到 `docs/features/<bug-name>/` 目录（bug-name 为 bug 简短描述，kebab-case）
- 每步结束时输出固定格式的状态报告
- 子 skill 仅在被调用时加载，避免上下文膨胀

## 前置：确定 bug-name

在执行 Step 1 之前，必须向用户确认 bug 名称（`bug-name`），用于文档目录命名。
若用户未指定，根据 bug 描述自动生成（英文、kebab-case，如 `login-crash`、`null-pointer-order`）。

确认后输出：
```
📁 Bug 名称: <bug-name>
📁 文档目录: docs/features/<bug-name>/
➡️ 进入 Step 1
```

## 7 步流程

```
Step 1  问题理解与复现确认  → 理解 bug 现象，确认复现步骤，影响范围
Step 2  根因分析与影响评估  → 定位代码根因，评估改动影响面，产出分析文档
Step 3  修复方案设计        → 设计修复方案，含回归风险分析
Step 4  回归测试编写        → 编写确保 fix 有效 + 无回归的测试用例
Step 5  编码修复            → 实现修复代码，确保测试 PASS
Step 6  自测清单与验证      → 交叉印证：确认 bug 已修复且无回归
Step 7  代码审查与交付      → android-git-commit 审查 + 提交 + 推送 + MR
```

---

## Step 1：问题理解与复现确认

**目标**：理解 bug 的完整上下文，确认可复现。

### 操作步骤

1. 向用户确认以下信息（依次提问，一次一个）：
   - Bug 现象：发生了什么？预期行为是什么？实际行为是什么？
   - 复现步骤：操作序列是什么？必现还是偶现？
   - 影响范围：影响哪些用户/功能？严重程度如何？
   - 相关日志/堆栈：是否有 crash log / ANR trace / 错误日志？

2. **混淆堆栈反解**（如果用户提供了生产环境 crash log）：
   - 检测堆栈是否混淆（启发式规则：类名/方法名为单个小写字母如 `a.b.c`、
     方法名为 `a()` `onClick` 等短名、无包名结构）
   - **未混淆** → 直接跳至步骤 3
   - **已混淆** → 进入反解流程：
     a. 提示用户：「该堆栈来自混淆后的包，请提供对应版本的 mapping 文件」
     b. 用户提供 mapping 的方式（三选一）：
        - 文件路径（如 `app/build/outputs/mapping/release/mapping.txt`）
        - 直接粘贴 mapping 文件内容
        - 让 agent 扫描项目中 `**/mapping/` 目录，列出所有候选 mapping 文件，
          供用户选择对应版本
     c. 若用户提供的是已反解堆栈，跳过 retrace，直接记录
     d. 运行反解命令：
        ```bash
        retrace <mapping文件> <混淆堆栈文件或文本>
        # 优先使用 $ANDROID_HOME/cmdline-tools/latest/bin/retrace
        # 或 ANDROID_HOME/tools/proguard/bin/retrace.sh
        ```
     e. 若 retrace 不可用，尝试 `proguardgui` 或提示用户手动反解

3. 将确认结果与反解信息写入分析文档：

**文档**：`docs/features/<bug-name>/01-bug-analysis.md`

```markdown
# Bug 分析 — <bug-name>

## Bug 现象
- 预期行为: ...
- 实际行为: ...
- 严重程度: [崩溃/功能异常/UI异常/性能]

## 复现条件
- 复现步骤: ...
- 复现概率: [必现/偶现(概率)]
- 影响版本: ...

## 相关日志
\`\`\`
(粘贴 crash log / stack trace)
\`\`\`

## 混淆堆栈反解
- mapping 来源: [用户提供的路径 / 用户粘贴的内容 / 用户从扫描列表选择的文件]
- 反解状态: [✅ 已反解 / ⏭ 无需反解(未混淆) / ⏭ 用户提供的是已反解堆栈]

### 原始混淆堆栈
\`\`\`
(原始 crash log，含混淆后的类名/方法名如 a.b.c.a())
\`\`\`

### 反解后堆栈
\`\`\`
(retrace 输出，含真实类名、方法名、行号)
\`\`\`

### 定位结果
- 崩溃类: <实际类名，如 com.example.app.ui.LoginActivity>
- 崩溃方法: <实际方法名>
- 崩溃行号: <行号>
- 异常类型: <NullPointerException / IndexOutOfBoundsException / ...>

## 影响范围
- 影响用户: ...
- 影响功能: ...
```

### 固定输出

```
## Step 1 完成：问题理解与复现确认
📄 产物: docs/features/<bug-name>/01-bug-analysis.md
📊 Bug 确认: [已复现/无法复现/已明确]
⏭️ 进入 Step 2
```

### 边界

| 场景 | 行为 |
|------|------|
| Bug 无法复现 | 记录无法复现的条件，询问用户是否继续 |
| 用户无法提供日志 | 标注 `⚠️ 缺少日志`，继续分析 |
| Bug 描述模糊 | 反复追问直到明确，不进入 Step 2 |
| 用户无法提供 mapping 文件 | 标注 `⚠️ 无 mapping 文件，使用混淆堆栈分析`，后续根因分析仅基于混淆后的类名推断 |
| retrace 工具不可用 | 提示安装 Android SDK cmdline-tools，或请用户手动反解后粘贴结果 |
| 堆栈中多个混淆版本共存 | 逐一确认每个版本的 mapping，分别反解 |

---

## Step 2：根因分析与影响评估

**目标**：定位代码层面的根因，评估改动波及范围。

### 操作步骤

1. 读取相关源代码文件（依据堆栈、日志中的类名和方法名）
2. 使用 `grep` / `codegraph explore` 定位关键代码路径
3. 追踪调用链，确认根因
4. 评估影响面：
   - 哪些其他地方调用了出错的方法/类？
   - 修改后可能影响哪些功能？
   - 是否需要同步修改数据库/SharedPreferences/Cache？

5. 补充到 `01-bug-analysis.md`：

```markdown
## 根因分析
- 根因定位: <文件名:行号> — <方法名>
- 原因描述: ...
- 调用链: A → B → C(出错点)

## 影响评估
- 直接修改文件: ...
- 间接影响（调用方）: ...
- 数据影响: [无/数据库迁移/缓存清理]
- 回归风险: [低/中/高]
```

### 固定输出

```
## Step 2 完成：根因分析与影响评估
📄 产物: docs/features/<bug-name>/01-bug-analysis.md（已补充根因分析）
🔍 根因定位: <文件:行号>
📊 影响评估: 直接影响 N 个文件，间接影响 M 个调用方
⏭️ 进入 Step 3
```

---

## Step 3：修复方案设计

**目标**：输出具体可执行的修复方案，含风险评估。

### 操作步骤

1. 基于根因分析，提出修复方案
2. 说明修改哪些文件、哪些方法、如何修改
3. 评估修复后可能的副作用

**文档**：`docs/features/<bug-name>/02-fix-plan.md`

```markdown
# 修复方案 — <bug-name>

## 根因回顾
（引用 01-bug-analysis.md 中的根因分析结论）

## 修复方案

### 1. <文件路径:行号>
- 改动: <具体修改内容>
- 原因: <为什么这样改>

### 2. <文件路径:行号>（如无则略）
- 改动: ...

## 版本兼容与碎片化影响
- 涉及 API: [列出涉及的系统 API 及最低支持版本，如 `ActivityResultContracts` 需 AndroidX `activity:1.2.0+`]
- API 废弃: [是否有 API 在更高版本废弃？替代方案是什么？如 `AsyncTask` → `Coroutines`]
- 厂商差异: [是否在特定厂商 ROM 上有不同行为？如后台启动限制、通知展示差异]
- minSdkVersion: [当前项目最低支持版本，修复方案是否兼容？兼容方案是什么？]
- targetSdkVersion 变更: [修复是否需要提升 targetSdkVersion？]

## 风险评估
- 修复副作用: ...
- 需要回归验证的功能: ...
- 风险等级: [低/中/高]

## 测试策略
- 需要覆盖的路径: ...
- 需要回归的用例: ...
```

### 固定输出

```
## Step 3 完成：修复方案设计
📄 产物: docs/features/<bug-name>/02-fix-plan.md
📊 方案: 修改 N 个文件，风险等级 [低/中/高]
⏭️ 进入 Step 4
```

---

## Step 4：回归测试编写

**目标**：编写确保 fix 有效 + 无回归的测试用例。

### 操作步骤

1. 依据 `02-fix-plan.md` 的修复方案和测试策略
2. 编写两类测试：
   - **Bug 复现测试**：该测试在修复前应 FAIL（确认捕获了 bug）
   - **回归测试**：确保修复不会破坏已有功能
3. 生成测试代码文件（JUnit 4/5 + Mockito，遵循项目已有风格）
4. 生成回归测试计划文档

**文档**：`docs/features/<bug-name>/03-regression-test-plan.md`

```markdown
# 回归测试计划 — <bug-name>

## Bug 复现测试
| # | 测试用例 | 文件 | 初始状态 |
|---|---------|------|---------|
| 1 | <描述: 覆盖 bug 复现路径> | <TestClass> | FAIL（bug 未修复） |

## 回归验证测试
| # | 测试用例 | 文件 | 覆盖范围 |
|---|---------|------|---------|
| 1 | <描述> | <TestClass> | <已有功能> |
| 2 | <描述> | <TestClass> | <边界条件> |
```

### 固定输出

```
## Step 4 完成：回归测试编写
📄 产物: docs/features/<bug-name>/03-regression-test-plan.md
📊 测试: 1 个 Bug 复现测试（当前 FAIL ✅）+ N 个回归测试
⏭️ 进入 Step 5
```

### 边界

| 场景 | 行为 |
|------|------|
| 项目无测试目录 | 自动创建 `src/test/` 目录 |
| 同名测试已有 | 追加 `_regression` 后缀 |
| 无法编写自动化测试（纯 UI bug） | 生成手动测试清单，标注 `⚠️ 需手动验证` |

---

## Step 5：编码修复

**目标**：实现修复代码，确保所有测试 PASS。

### 操作步骤

1. 按 `02-fix-plan.md` 逐文件修改
2. 运行 Bug 复现测试，确认从 FAIL → PASS
3. 运行全部回归测试，确认无破坏
4. 每个文件修改后 `git add` + `git commit`

```bash
./gradlew testDebugUnitTest
```

### 固定输出

```
## Step 5 完成：编码修复
📊 修改文件: N 个
✅ Bug 复现测试: PASS（bug 已修复）
✅ 回归测试: N/N PASS（无回归）
⏭️ 进入 Step 6
```

### 边界

| 场景 | 行为 |
|------|------|
| 回归测试 FAIL | **硬阻塞**，分析失败原因，修正后重试 |
| 修复后 Bug 复现测试未 PASS | **硬阻塞**，检查修复是否完整 |
| 3 次修复尝试仍 FAIL | 终止，要求用户介入 |

---

## Step 6：自测清单与验证

**目标**：交叉印证确认 bug 已修复，无回归。

### 操作步骤

1. 读取 `01-bug-analysis.md`、`02-fix-plan.md`、`03-regression-test-plan.md`
2. 交叉印证：
   - Bug 现象中每个问题点 → 修复方案是否覆盖？
   - 影响评估中每项 → 回归测试是否覆盖？
   - 所有测试是否 PASS？
3. 手动验证项（如果 fix 涉及 UI）：
   - 按复现步骤操作，确认 bug 不再出现
   - 检查相关功能正常

**文档**：`docs/features/<bug-name>/04-self-check.md`

```markdown
# 自测清单 — <bug-name>

## 交叉印证
| 维度 | 结果 |
|------|------|
| Bug 问题点 → 修复覆盖 | N/N ✅ |
| 影响面 → 回归测试覆盖 | N/N ✅ |
| Bug 复现测试 | 1/1 PASS ✅ |
| 回归测试 | N/N PASS ✅ |

## 手动验证（如有）
| # | 验证项 | 操作 | 结果 | 证据 |
|---|-------|------|------|------|
| 1 | Bug 复现步骤 | <操作> | ✅ | — |

## 结论
- Bug 已修复: ✅
- 无回归: ✅
- 整体状态: **通过**
```

### 固定输出

```
## Step 6 完成：自测清单与验证
📄 产物: docs/features/<bug-name>/04-self-check.md
📊 交叉印证: 全部覆盖 ✅
✅ Bug 已修复，无回归
⏭️ 进入 Step 7
```

### 边界

| 场景 | 行为 |
|------|------|
| 交叉印证发现遗漏 | **硬阻塞**，不进入 Step 7，补充后重试 |
| 手动验证仍有 bug 现象 | **硬阻塞**，回到 Step 2 重新分析 |

---

## Step 7：代码审查与交付

加载 `android-git-commit` skill。

该 skill 将执行标准六步流程：
1. 代码审查（Phase A 安全 → Phase B 规范自修 → Phase C 异常自修）
2. 注释检测
3. 同步远端
4. 提交（Conventional Commit，type 使用 `fix`）
5. 推送
6. MR 链接

按 android-git-commit 的固定输出格式展示。

### 固定输出

```
## Step 7 完成：代码审查与交付
🔗 <MR 链接>
🏁 Bug 修复流程完成！
```

---

## 全流程硬阻塞条件

| 条件 | 阻塞点 | 行为 |
|------|--------|------|
| Bug 无法复现且用户无更多信息 | Step 1 | 终止 |
| 根因无法定位（信息不足） | Step 2 | 要求用户提供更多信息 |
| 回归测试 FAIL | Step 5 | 等待修复 |
| 交叉印证有遗漏 | Step 6 | 等待补充 |
| Step 7 Phase A 安全违规 | Step 7 | 等待修复 |
