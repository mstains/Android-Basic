---
name: android-git-commit
description: >
  Android 项目 Git 安全提交编排器。按"先同步,再单次敏感扫描,最后提交推送"多步流程编排。
  敏感扫描在 pull 之后执行,单次覆盖本地 + 上游合并状态。lint 检查不在本地流程中,由 CI 负责。
  子 skill 分为:android-git-commit-core(变更分析 + commit)、
  android-code-review(敏感扫描 + 资源与布局规范 + 代码异常审查 + 代码规范审查 + 注释合规检查,拆为五节)、
  android-git-commit-sync(拉取 + 冲突 + 推送)。
  v3.1 新增:Section E 注释缺失 → opencode 自动补全 → 人确认,拒绝则回退硬阻塞。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-02'
  keywords:
  - android
  - git
  - commit
  - sensitive-scan
  - security-gate
  - v3
---

# Android Git 提交 (v3 安全流程)

## 标准流程 (强制顺序)

| Step | 动作 | 调用 | 闸门类型 |
|------|------|------|----------|
| 1 | 拉取远端 + 冲突检查 | `sync.step-1~4` | 软阻塞 |
| 2 | 代码审查 (五节合一,单次报告) | `code-review` (scope=full) | 硬阻塞 |
| 2.5 | 注释自动补全 (仅当 Section E 有命中) | opencode 交互 | 软阻塞→硬阻塞 |
| 3 | 本地 commit | `core.step-1~3` | - |
| 4 | push (需 --push) | `sync.step-5` | - |

## 冲突循环

```
Step 1 冲突 → 报告 → 停止
   ↓
用户解决冲突 (git rebase --continue)
   ↓
回到 Step 2 重新审查 (防御性,确认解决过程未引入敏感或违规)
   ↓
Step 2 → Step 2.5 (如有) → Step 3 → Step 4
```

> v3 简化: 冲突解决后无需重跑 Step 1(远端状态未变,rebase --continue 已处理冲突)。

## Step 2.5: 注释自动补全 (opencode 层)

### 触发条件

当 Step 2 `code-review` 报告中 **Section E 有命中** 时触发。Section A-D 无命中 + Section E 有命中时，**跳过** Step 2 的硬阻塞闸门，直接进入本步骤。

### 执行流程

```
Step 2 报告展示 → Section E 共 x 项缺失
   ↓
是否启用自动补全？[Y/n]
   ├── N → 跳过,Section E 回退硬阻塞,用户手动补全
   └── Y (默认) → 启动自动补全
         ↓
      opencode 逐文件处理:
        ① 读取文件,定位缺失 KDoc 的声明
        ② 分析函数体/类体/调用链,理解代码语义
        ③ 按 android-code-style §2 模板生成 KDoc/Javadoc
        ④ 展示 diff (原代码 vs 补全后),逐条询问用户
              ├── y → 应用 diff,写入文件
              ├── n → 该项回退硬阻塞,用户自行补全
              └── s (skip) → 跳过该项,继续下一个
        ⑤ 全部文件处理完毕
              ├── 全部通过 → 放行,进入 Step 3
              └── 有拒绝项 → 展示拒绝清单,停止,等待修复
```

### 自动补全规范

opencode 生成注释时**必须**遵循 `android-code-style` §2 模板规范:
- **class/interface/object**: 含用途 + 补充说明 + @param + @property + @constructor
- **fun**: 含用途 + 补充说明 + @param + @return + @throws + @see
- **val/var**: 含用途 + 补充说明
- **语言**: 简体中文 + 技术术语保留英文
- **格式**: 参见 `android-code-style` §2.1-§2.5

### 边界行为

| 场景 | 行为 |
|------|------|
| Section E 检测到 `private` 声明 (误报) | opencode 读取后识别为 private,标注"跳过(非 public)",不生成 |
| Section E 检测到 `override` 方法 (误报) | opencode 读取后识别为 override,标注"跳过(override)",不生成 |
| 函数体为空 / 仅 TODO() | 标注"无法推断语义,跳过",该项回退硬阻塞 |
| 文件已被外部修改 (diff 失败) | 停止当前项,提示冲突,继续下一项 |
| 用户拒绝全部补全 | 等同 Section E 硬阻塞,用户手动补全后重新提交 |

### 可选开关

- `--skip-comment-autofix`: 跳过 Step 2.5,Section E 命中直接按硬阻塞处理

### 性能特征

- bash 检测 (Step 2 内): +< 3 秒 (grep + sed 上下文检查)
- opencode 自动补全: 取决于文件数量和复杂度,通常每文件 2-5 秒

## 边界行为

| 场景 | 行为 |
|------|------|
| Step 2 (A-D) 发现敏感信息 / 代码违规 | **阻塞** (统一报告,一次确认) |
| Step 2 (E) 发现注释缺失 | **移交 Step 2.5** opencode 自动补全 |
| Step 2.5 用户拒绝自动补全 | 该项回退**硬阻塞**,等待手动修复 |
| 本地分支无 upstream tracking | **跳过 Step 1** (E4=A) |
| pre-commit hook 失败 | **自动重试一次**,仍失败停止 |
| 推送被拒 | **不 --force**,询问后 rebase 重试 |

### E4 边界定义 (v2 实战改进, 2026-06-03)

**E4 = "本地分支无 upstream tracking"**,**严格按**以下命令判断:

```bash
if git rev-parse --abbrev-ref --symbolic-full-name '@{u}' &>/dev/null; then
  # 有 upstream → 走 Step 1 (fetch + pull --rebase)
else
  # 无 upstream → E4 触发,跳过 Step 1
fi
```

**严禁使用** `git ls-remote --exit-code origin <branch>` 判断"远端分支是否存在":
- `ls-remote` 在某些网络/协议条件下会误报"远端不存在"
- 远端是否存在同名分支 ≠ 本地是否有 upstream tracking
- 这是两个独立概念,只有后者决定 `git pull --rebase` 能否执行

## 可选开关

- `--push`                  : 启用 Step 4 (默认不推)
- `--skip-code-review`      : 跳过 Step 2 代码审查 (含 Section E)
- `--skip-comment-autofix`  : 跳过 Step 2.5,Section E 命中直接按硬阻塞处理
- `--dry-run`               : Step 2 仅展示审查报告不阻塞
- `--scope=staged`          : Step 2 仅扫描已暂存文件 (默认 full)

## 性能特征

- Step 1 拉取: 取决于网络,通常 1~10 秒;无冲突时直接通过
- Step 2 代码审查: grep 级全项目扫描,通常 < 12 秒 (含 Section E grep+sed 上下文检查)
- Step 2.5 注释自动补全: 取决于命中文件数,通常每文件 2-5 秒
- **总闸门耗时**: ~15 秒 (无 Section E 命中) / +文件数×5 秒 (含自动补全)
- **lint 不在本地流程**,完整静态分析由 CI 流水线负责

## 子 skill 速查

| Skill | 职责 | 在本流程中被调用 |
|-------|------|------------------|
| `android-git-commit-core` | 变更分析 + commit message + commit | Step 3 |
| `android-code-review` | Section A-E 五节合一代码审查 | Step 2 (单次调用,统一报告) |
| `android-git-commit-sync` | 拉取 + 冲突 + 推送 | Step 1, 4 |

## 变更记录

- 2026-07-02: **v3.1 注释自动补全**。新增 Step 2.5: 当 `code-review` Section E 检测到注释缺失时,opencode 自动读取代码生成 KDoc,展示 diff 逐条确认。拒绝则回退硬阻塞。新增 `--skip-comment-autofix` 开关。审查从四节扩展至五节 (Section A-E)。
- 2026-06-30: **Step 重编号 + 审查合并**。Step 从 2→1→4a~4e 重排为 1→2→3→4。Step 2 由分节调用改为单次 `code-review` 调用 (四节统一报告)。新增 `--dry-run` 开关。
- 2026-06-30: **适配 android-code-review 重构**。`android-git-commit-review` 重命名为 `android-code-review`,Section A-D 四节并入流程。
- 2026-06-03: **v3 流程优化**。重排顺序为 2→1→4a→4b→4c。Step 2 (拉取) 提前,Step 1 (敏感扫描) 在 post-pull 状态单次执行,取消 v2 的 Step 3 重复扫描。冲突循环回程保持"回到 Step 1"。
- 2026-06-03: 升级为 v2 安全流程。Step 1/3 全项目敏感扫描为强制闸门,Step 2 冲突回到 Step 1,Step 4 默认不推送。**移除 lint 检查**,完整静态分析移交 CI。
- 2026-06-03: E4 边界定义改写。明确"无 upstream tracking"判定标准,严禁用 `git ls-remote` 判断远端分支存在性。
- 2026-06-02: 拆分为 core/review/sync 三个独立 skill。
