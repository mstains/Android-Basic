---
name: android-git-commit
description: >
  Android 项目 Git 安全提交编排器。按"审查→注释→同步→提交→推送→MR"六步流程编排。
  默认推送并展示 MR 链接。Step 1 代码审查分 Phase A(安全,只报不改)/B(规范自修,统一确认)/C(异常自修,逐条确认)。
  子 skill 分为:android-code-review(代码审查+注释检测)、
  android-git-commit-core(变更分析+commit)、android-git-commit-sync(同步+推送)。
  v7 变更:Section B/D 可修项 AI 自动修复,Section C 可修项逐条确认修复,新增 --skip-auto-fix。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-21'
  keywords:
  - android
  - git
  - commit
  - code-review
  - kdoc
  - javadoc
  - detekt
  - v7
---
# Android Git 提交（v7 安全流程）

## 默认行为

- **默认推送** + 展示 MR 链接。用 `--no-push` 跳过推送。
- 子 skill 仅在被调用时加载，避免上下文膨胀。
- 每一步按固定模板输出，不即兴发挥。

## 标准流程（4 步，不可跳过）

Step 1 内部有三个子阶段（Phase A → Phase B → Phase C），由 android-code-review 自动编排：

```
Step 1  代码审查       → android-code-review
                           Phase A: Section A 安全扫描（只报不改，硬阻塞）
                           Phase B: Section B+D 规范自修（AI 修复 → 统一 diff → 一次性确认）
                           Phase C: Section C 异常自修（AI 逐条修复 → 展示 diff+理由 → 逐条确认）
Step 2  注释检测       → android-code-review Section E（detekt + checkstyle → AI 补全 KDoc）
Step 3  同步远端       → android-git-commit-sync（拉取远端、处理冲突）
Step 4  提交           → android-git-commit-core（变更分析 + 生成 message + commit）
Step 5  推送           → android-git-commit-sync（推送）
Step 6  MR 链接       → android-git-commit-sync（生成 MR 链接）
```

## Step 1：代码审查

加载 android-code-review，按 Phase A → Phase B → Phase C 顺序执行（范围：`git diff HEAD --name-only` + `git ls-files --others --exclude-standard`）。

### Phase A：安全扫描

```
执行 Section A 检查
  → 无命中：进入 Phase B
  → 有命中：硬阻塞，展示报告，流程终止
```

### Phase B：规范自修

```
执行 Section B + D 可修项检查
  → 无命中：进入 Phase C
  → 有命中：AI 逐条自动修复 → 展示统一 git diff → 询问确认
     y → 接受全部修复，git add → 进入 Phase C
     N / skip → 跳过 Phase B，进入 Phase C
```

### Phase C：异常自修

```
执行 Section C 可修项检查
  → 无命中：进入 Step 2
  → 有命中：AI 逐条修复 → 逐条展示 diff + 理由 → 逐条询问
     y → 接受此条修改
     N → 跳过此条，继续下一条
     skip → 跳过整个 Phase C，进入 Step 2
```

### 固定输出

```
## Step 1: 代码审查

🔴 Phase A 安全扫描 — 无安全违规 ✅

🟢 Phase B 规范自修 — 8 处修复，涉及 6 个文件
   → 用户已确认，git add 完成

🟡 Phase C 异常自修 — 3 项修复
   C2: !! 强制解包 → ✅ 已修复
   C4: requireActivity() → ⏭ 跳过
   C8: lateinit var → ✅ 已修复
```

### 边界

| 场景 | 行为 |
|------|------|
| Phase A 命中 | **硬阻塞**，流程终止 |
| 无 Kotlin/Java 文件变更 | 输出 `跳过（无 Java/Kotlin 变更）`，进入 Step 2 |
| `--skip-code-review` | 跳过本步（含 Phase A/B/C + Step 2） |
| `--skip-auto-fix` | Phase B/C 仅展示报告不修复（Phase A 仍硬阻塞） |
| `--dry-run` | Phase A 展示报告不阻塞；Phase B/C 展示修复预览不写文件 |
| Phase B 用户回复 N | 跳过 Phase B，继续 Phase C |
| Phase C 用户回复 skip | 跳过 Phase C，进入 Step 2 |

---

## Step 2：注释检测

```
加载 android-code-review
执行 Section E：
  ./gradlew detekt checkstyleJava
  检测通过 → 进入 Step 3
  检测失败 → agent 从 detekt/checkstyle 输出 grep 提取违规 → 逐条补全 KDoc → git add 补全文件
```

### 固定输出

```
## Step 2: 注释检测
✅ detekt 通过  ✅ checkstyle 通过

# 或

## Step 2: 注释检测
⚠️ detekt 检测到 N 处 KDoc 缺失
→ 补全 Item.kt (3/3) ✅
→ 补全 UserDto.kt (1/1) ✅
→ 已补全 4 处，git add 完成
```

### 边界

| 场景 | 行为 |
|------|------|
| 无 Kotlin/Java 文件变更 | 输出 `跳过`，进入 Step 3 |
| `--skip-code-review` | 跳过本步 |
| 用户拒绝全部补全 | 硬阻塞，手动补全后重新触发 |
| 函数体为空 / 仅 TODO() | 标注"无法推断，跳过"，该项回退硬阻塞 |

---

## Step 3：同步远端

```
加载 android-git-commit-sync
执行同步流程（SSH 检查 → 远端检查 → fetch + pull --rebase → 冲突处理）
```

### 固定输出

```
## Step 3: 同步远端
✅ 已同步最新代码，无冲突

# 或

## Step 3: 同步远端
⚠️ 合并冲突
冲突文件:
  app/src/main/java/.../OrderActivity.kt (行 42-52)
  → 本地: 添加退款按钮逻辑
  → 远端: 重构支付模块

建议:
  A) 接受远端: git checkout --theirs <file>
  B) 接受本地: git checkout --ours <file>
  C) 手动合并（推荐）

解决后继续: git add <file> && git rebase --continue
```

### 边界

| 场景 | 行为 |
|------|------|
| 无 upstream tracking | 输出 `跳过（本地分支无 upstream）`，进入 Step 4 |
| 合并冲突 | **停止流程**，等用户解决。解决后从 Step 1 重新执行（仅扫描冲突文件） |
| 冲突后恢复 | `git rebase --abort` 可取消合并 |

---

## Step 4：提交

```
加载 android-git-commit-core
执行提交流程（变更分析 → commit message → git commit）
```

### 固定输出

使用 core 生成的 message，按以下格式展示：

```
## Step 4: 提交
feat(order): 添加退款功能

变更:
  新增: OrderRefundActivity.kt
  修改: OrderViewModel.kt    添加 refundOrder() 方法
  删除: OldCouponHelper.kt    已废弃，功能迁移

✅ 已提交: a1b2c3d
```

---

## Step 5：推送

```
加载 android-git-commit-sync
执行推送（git push origin <branch>）
```

### 固定输出

```
## Step 5: 推送
✅ 推送成功 → origin/feature/order-refund

# 推送失败时
⚠️ 推送被拒（远端有新提交）
→ 执行 git fetch && git pull --rebase
→ 如有冲突，参照 Step 3 处理
```

### 边界

| 场景 | 行为 |
|------|------|
| `--no-push` | 跳过本步 + Step 6 |
| 推送被拒 | 拉取 rebase，送回 Step 3 冲突处理 |

---

## Step 6：MR 链接

```
加载 android-git-commit-sync
执行 MR 链接生成
```

### 固定输出

```
## Step 6: MR 链接
🔗 https://gitlab.com/caocao/travel/-/merge_requests/new?...source_branch=feature/xxx&target_branch=master
```

### 边界

| 场景 | 行为 |
|------|------|
| `--no-push` | 跳过本步 |
| 推送失败 | 跳过本步 |
| source = target（如误推 master） | 仍展示链接，附加 `⚠️ 源分支与目标分支相同，请确认` |

---

## 冲突恢复流程

当 Step 3 出现冲突、用户手动解决后，执行增量审查：

```
用户解决冲突 → git add <files> → git rebase --continue
  ↓
从 Step 1 重新执行（仅扫描冲突文件）
  git diff --diff-filter=U + 解决后的 diff
  ↓
通过 → Step 2 → ... → Step 6
```

---

## 可选开关

| 开关 | 作用 |
|------|------|
| `--no-push` | 跳过 Step 5 推送 + Step 6 MR 链接 |
| `--skip-code-review` | 跳过 Step 1 代码审查（含 Phase A/B/C）+ Step 2 注释检测 |
| `--skip-auto-fix` | Phase B/C 仅展示违规报告，不执行自动修复（Phase A 仍硬阻塞） |
| `--dry-run` | Phase A 展示报告不阻塞；Phase B/C 展示修复预览不实际写文件 |

---

## 流程终止条件

| 条件 | 终止点 |
|------|--------|
| Phase A 命中安全违规 | 立即终止 |
| Phase B/C 用户拒绝全部修复 | 跳过对应 Phase，继续后续流程 |
| Step 2 用户拒绝全部补全 | 停止，手动补全后重新触发 |
| Step 3 合并冲突 | 停止，等用户解决后按冲突恢复流程继续 |
| Step 5 推送被拒 2 次以上 | 停止，建议用户手动处理 |
