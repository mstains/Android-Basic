---
name: android-git-branch
description: >
  Android 项目 Git 分支创建。提供 6 类标准化分支命名与校验，
  基分支硬阻塞，仅本地创建不推送。独立于 android-git-commit，
  作为提交前的前置操作。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-30'
  keywords:
  - android
  - git
  - branch
  - naming
---

# Android Git 分支创建

## 工作流

```
Step 1: 确定分支类型 + 简短描述
Step 2: 校验基分支（硬阻塞）
Step 3: 同步 develop（合并远端 master）
Step 4: 校验工作区清洁度（硬阻塞）
Step 5: 校验命名合法性
Step 6: git checkout -b <prefix>/<desc>
Step 7: 输出创建结果
```

---

## Step 1：确定分支类型与描述

### 1.1 分支类型

| 类型 | 前缀 | 用途 |
|------|------|------|
| 新功能开发 | `feature/` | 新增功能类代码 |
| 常规缺陷修复 | `bugfix/` | 修复已知 bug |
| 线上紧急热修复 | `hotfix/` | 修复线上紧急问题 |
| 版本发布准备 | `release/` | 版本发布前准备 |
| 代码重构与优化 | `refactor/` | 重构既有代码 |
| 文档更新 | `docs/` | 更新文档 |

### 1.2 描述格式

- 全小写英文单词
- 单词之间用 `-` 连接
- 简短且一目了然
- 禁止空格、下划线、特殊字符

**合法示例**: `login-page`、`payment-crash`、`user-service`

**非法示例**: `login_page`、`LoginPage`、`Fix Bug #123`、`用户模块`

### 1.3 获取输入

```
分支类型: [feature|bugfix|hotfix|release|refactor|docs]
简短描述: <kebab-case 描述>
```

---

## Step 2：校验基分支

### 2.1 基分支映射

| 分支类型 | 要求基分支 | 备注 | 原因 |
|----------|-----------|------|------|
| `feature/` | `develop` | 同步后创建 | 功能开发基于主开发分支，创建前先将远端 master 合并到 develop |
| `bugfix/` | `develop` | 同步后创建 | 缺陷修复基于主开发分支，创建前先将远端 master 合并到 develop |
| `refactor/` | `develop` | 同步后创建 | 重构基于主开发分支，创建前先将远端 master 合并到 develop |
| `docs/` | `develop` | 同步后创建 | 文档更新基于主开发分支，创建前先将远端 master 合并到 develop |
| `release/` | `develop` | 同步后创建 | 发布准备基于主开发分支，创建前先将远端 master 合并到 develop |
| `hotfix/` | `master` | 无需同步 | 热修复基于线上主干，不触发合并 |

### 2.2 校验逻辑

```bash
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
# 由 Step 2.1 映射表确定: feature/bugfix/refactor/docs/release → develop, hotfix → master
case "$BRANCH_TYPE" in
  hotfix) REQUIRED_BASE="master" ;;
  *)      REQUIRED_BASE="develop" ;;
esac

if [ "$CURRENT_BRANCH" != "$REQUIRED_BASE" ]; then
  echo "🔴 分支类型 '$BRANCH_TYPE' 要求基于 '$REQUIRED_BASE'，当前在 '$CURRENT_BRANCH'"
  echo "   请先切换到 '$REQUIRED_BRANCH': git checkout $REQUIRED_BASE"
  exit 1
fi
```

**硬阻塞**：不满足直接终止。

---

## Step 3：同步 develop（合并远端 master）

仅当基分支为 `develop` 时执行此步骤（`hotfix` 类型跳过）。

### 操作说明

在 `develop` 上执行 `git pull origin master`，将远端 `master` 的最新代码合并到本地 `develop`，确保新分支基于包含 master 全部提交的最新代码。

### 执行逻辑

```bash
if [ "$REQUIRED_BASE" = "develop" ]; then
  echo "📥 正在同步 develop（合并远端 master）..."
  CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
  if [ "$CURRENT_BRANCH" != "develop" ]; then
    echo "🔴 当前不在 develop 上，请先执行 Step 2"
    exit 1
  fi
  git pull origin master
  if [ $? -ne 0 ]; then
    echo "🔴 合并冲突，请手动解决后重试"
    exit 1
  fi
  echo "✅ develop 已同步到远端 master 的最新提交"
fi
```

**⚠️ 合并冲突处理**：如果 `git pull origin master` 产生冲突，则硬阻塞终止，由用户手动解决冲突后再重试。

---

## Step 4：校验工作区清洁度

```bash
if [ -n "$(git status --porcelain)" ]; then
  echo "🔴 工作区存在未提交的变更，请先处理："
  echo "   git stash       # 暂存工作区变更"
  echo "   git commit      # 提交当前变更"
  exit 1
fi
```

**硬阻塞**：工作区存在任何未暂存或未提交文件时终止。

---

## Step 5：校验命名

### 5.1 命名规则校验

```bash
BRANCH_NAME="${PREFIX}${DESC}"

# 规则 1: 整体必须全小写
if [ "$BRANCH_NAME" != "$(echo "$BRANCH_NAME" | tr '[:upper:]' '[:lower:]')" ]; then
  echo "🔴 分支名必须全小写: $BRANCH_NAME"
  exit 1
fi

# 规则 2: 描述部分不含空格、下划线、特殊字符 (仅允许 a-z0-9 和 -)
if echo "$DESC" | grep -qE '[^a-z0-9-]'; then
  echo "🔴 描述部分仅允许小写字母、数字和连字符: $DESC"
  exit 1
fi

# 规则 3: 描述不以 - 开头或结尾
if [ "${DESC:0:1}" = "-" ] || [ "${DESC: -1}" = "-" ]; then
  echo "🔴 描述不能以连字符开头或结尾: $DESC"
  exit 1
fi
```

### 5.2 重名检测

```bash
# 检查本地分支
if git branch --list "$BRANCH_NAME" | grep -q .; then
  echo "🔴 本地分支已存在: $BRANCH_NAME"
  exit 1
fi

# 检查远端分支 (不阻塞，仅警告)
if git ls-remote --exit-code origin "$BRANCH_NAME" &>/dev/null; then
  echo "⚠️  远端已存在同名分支: $BRANCH_NAME"
fi
```

---

## Step 6：创建分支

```bash
git checkout -b "$BRANCH_NAME"
```

仅本地创建，不推送到远端。

---

## Step 7：输出结果

```
✅ 分支创建成功

   类型:    feature
   分支名:  feature/login-page
   基于:    develop（已同步远端 master）
  同步:    git pull origin master → 已合并

下一个动作:
  修改代码后执行提交: /android-git-commit
```

---

## master 分支保护

master 为受保护主干，禁止直接操作。

```bash
# 本地预检: 禁止在 master 上直接提交
CURRENT=$(git rev-parse --abbrev-ref HEAD)
if [ "$CURRENT" = "master" ] || [ "$CURRENT" = "main" ]; then
  echo "❌ 禁止在 $CURRENT 直接提交, 请基于 develop 创建分支"
  exit 1
fi
```

**强制项**:
- 禁止 `git push origin master` (本机 + CI 双重防护)
- 禁止 `git push -f origin master`
- 禁止在 master 上直接 commit
- `develop` / `master` 必须通过 PR 合并
- 功能开发基于 `develop` 拉 `feature/`, 完成后 PR 回 `develop`

---

## 边界行为

| 场景 | 行为 |
|------|------|
| 基分支不匹配 | **硬阻塞**，提示应切换到哪个分支 |
| 合并远端 master 产生冲突 | **硬阻塞**，提示手动解决冲突后重试 |
| 工作区有未提交变更 | **硬阻塞**，提示 stash 或 commit 后重试 |
| 命名含大写/特殊字符 | **硬阻塞** |
| 本地已存在同名分支 | **硬阻塞** |
| 远端已存在同名分支 | **警告**，不阻塞 |
| 分支创建后 | 仅本地，不自动推送 |
| 当前在 master 分支直接操作 | **硬阻塞**，提示基于 develop 创建分支 |

---

## 与 android-git-commit 的关系

本 skill 独立于 `android-git-commit`，作为提交前的可选前置步骤：

```
android-git-branch → android-git-commit (v3 流程)
```

两者正交，互不耦合。分支创建不触发任何提交流程。
