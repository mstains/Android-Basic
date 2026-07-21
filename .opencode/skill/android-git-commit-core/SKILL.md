---
name: android-git-commit-core
description: >
  Android 项目 Git 提交核心流程：变更分析、Conventional Commit 生成、git add/commit。
  是安全提交编排流程的提交环节调用目标,本身不包含敏感扫描或 lint,
  代码审查由调用方在调用前完成(android-code-review,单次统一报告)。
  无副作用。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-17'
  keywords:
  - android
  - git
  - commit
  - conventional-commits
---

# Android Git 提交核心

## 前置检查 (v2 流程约定)

本 skill 在提交前,**依赖**调用方完成以下检查:
- `android-code-review` 代码审查 (Step 1, Sections A-D)

本 skill 内部**不重复**资源检查,职责单一。完整 lint 检查不在本流程,由 CI 流水线负责。

## 工作流

```
Step 1: 变更分析 → 缓存 STAGED/UNSTAGED/UNTRACKED
Step 2: 推断 scope + commit type → 生成 commit message
Step 3: git add -A + git commit
```

---

## Step 1：一次性分析变更

```bash
STAGED=$(git diff --cached --name-status)
UNSTAGED=$(git diff --name-status)
UNTRACKED=$(git ls-files --others --exclude-standard)
STAGED_DETAIL=$(git diff --cached)
STATUS_SHORT=$(git status --short)
BRANCH=$(git rev-parse --abbrev-ref HEAD)

echo "=== 当前分支: $BRANCH ==="
echo "$STATUS_SHORT"
```

### 文件分类

| 标记 | 含义 |
|------|------|
| A    | 新增 |
| M    | 修改 |
| D    | 删除 |
| R    | 重命名 |
| ??   | 未跟踪 |

---

## Step 2：生成 Commit Message

### 2.1 自动推断 scope

```bash
# 合并暂存区和工作区变更，确保不论是否提前 staging 都能正确推断
ALL_CHANGED=$( (echo "$STAGED"; echo "$UNSTAGED") | grep -E '^[AM]\s+' | cut -f2 | sort -u)
SCOPE=$(echo "$ALL_CHANGED" | grep -E '\.(kt|java|xml)$' |
  sed -nE 's|.*/([^/]+)/src/.*|\1|p' |
  sort | uniq -c | sort -rn | head -1 |
  awk '{print $2}')

if [ -z "$SCOPE" ]; then
  SCOPE=$(echo "$ALL_CHANGED" | head -1 | cut -d'/' -f1)
fi

SCOPE=${SCOPE:-app}
```

### 2.2 确定 commit type

| 优先级 | type | 判断依据 |
|--------|------|---------|
| 1 | feat | 新增功能类文件 + 接口定义 |
| 2 | fix | 文件内容涉及 bug、crash、issue 关键字 |
| 3 | refactor | 大量删除 + 新增但无新功能 |
| 4 | test | 变更只涉及 `src/test/` 或 `src/androidTest/` |
| 5 | docs | 变更只涉及 `README`、`*.md` |
| 6 | style | 变更只涉及格式、import 顺序 |
| 7 | chore | 变更涉及 `build.gradle`、proguard、CI 配置 |

### 2.3 生成 message 格式

```
<type>(<scope>): <简短描述（50字以内）>

变更文件清单：
  <路径>
    · <具体变更说明>
```

**生成规则**：
- 简短描述从文件名和 diff 中提取关键动词和名词，中文，主动语态
- 变更说明以"添加/修复/优化/重构/删除"开头

---

## Step 3：提交

```bash
git add -A
git commit -m "$COMMIT_MSG"
```
