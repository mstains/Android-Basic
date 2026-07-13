---
name: android-git-commit-sync
description: >
  Android 项目 Git 同步流程：SSH 密钥管理、fetch/pull --rebase、冲突报告生成、push。
  作为 android-git-commit 的后置环节，或独立使用于同步场景。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-06-02'
  keywords:
  - android
  - git
  - sync
  - conflict
  - ssh
---

# Android Git 同步

## 工作流

```
Step 1: SSH 密钥检查（仅 SSH 协议）
Step 2: 远端连接检查
Step 3: git fetch + git pull --rebase
Step 4: 冲突处理（若有）
Step 5: git push
```

---

## Step 1：SSH 密钥检查

```bash
REMOTE_URL=$(git remote get-url origin)
if echo "$REMOTE_URL" | grep -q '^git@'; then
  echo "远端使用 SSH 协议"
  for key in ~/.ssh/id_rsa ~/.ssh/id_ed25519 ~/.ssh/id_ecdsa; do
    [ -f "$key" ] || continue
    if ssh-keygen -y -f "$key" -P "" &>/dev/null; then
      echo "✓ $key（无密码）"
    else
      echo "🔑 $key（有密码保护）"
      if ! ssh-add -l | grep -q "$(ssh-keygen -lf "$key" 2>/dev/null | awk '{print $2}')"; then
        echo "  尚未缓存到 ssh-agent，请执行: ssh-add $key"
      fi
    fi
  done
  ssh -T git@github.com 2>&1 || echo "⚠️ 认证失败"
fi
```

### 1.1 macOS 自动回退链 (v2 实战改进, 2026-06-03)

针对 opencode / IDE 等非交互式进程,SSH 认证失败时自动尝试以下回退 (避免卡在密码输入):

```bash
# 检测当前是否在 macOS 且 SSH 协议下
if [ "$(uname)" = "Darwin" ] && echo "$REMOTE_URL" | grep -q '^git@'; then
  # 先测试一次认证
  if ! ssh -T -o ConnectTimeout=5 -o BatchMode=yes git@github.com &>/dev/null; then
    echo "⚠️ SSH 认证失败,尝试 macOS 自动回退..."
    
    # 路径 A: 当前 agent 是 launchd 全局 agent (默认 macOS)
    # 这种情况下 SSH_AUTH_SOCK 指向 /var/folders/.../com.apple.launchd.*/Listeners
    if echo "$SSH_AUTH_SOCK" | grep -q 'com.apple.launchd'; then
      echo "  检测到 macOS launchd 全局 agent"
      echo "  提示用户在终端执行: ssh-add --apple-use-keychain ~/.ssh/id_ed25519"
      echo "  (把密码永久存入 Keychain,以后所有进程自动可见)"
    else
      # 路径 B: 当前是本地 agent,尝试从 Keychain 跨进程加载
      echo "  尝试从 macOS Keychain 加载密钥..."
      ssh-add --apple-load-keychain ~/.ssh/id_ed25519 2>&1
      if ssh-add -l &>/dev/null; then
        echo "  ✓ 密钥已从 Keychain 加载"
      else
        echo "  ⚠️ Keychain 中未找到密钥密码"
        echo "  请在终端执行: ssh-add --apple-use-keychain ~/.ssh/id_ed25519"
        echo "  (一次性存密码,后续本进程可自动加载)"
      fi
    fi
    
    # 重试认证
    if ssh -T -o ConnectTimeout=5 -o BatchMode=yes git@github.com &>/dev/null; then
      echo "✓ SSH 认证成功 (回退后)"
    else
      echo "🔴 SSH 认证仍失败,停止流程"
      exit 1
    fi
  fi
fi
```

**回退链优先级**:
1. launchd 全局 agent → 提示用户终端一次性存密码
2. 本地 agent → 自动从 Keychain 加载 (如果密码已存)
3. 都失败 → 报错停止,绝不阻塞等待密码输入

**前置条件** (用户一次性操作): `ssh-add --apple-use-keychain ~/.ssh/id_ed25519`

---

## Step 2：远端连接检查

```bash
BRANCH=$(git rev-parse --abbrev-ref HEAD)
if ! git ls-remote --exit-code origin "$BRANCH" &>/dev/null; then
  echo "⚠️ 无法连接远端仓库，请检查："
  echo "  1. SSH key 是否配置（ssh -T git@github.com）"
  echo "  2. 远端仓库地址是否正确（git remote -v）"
  echo "  3. 网络代理是否正常"
  exit 1
fi
```

---

## Step 3：拉取远端

```bash
git fetch origin
GIT_EDITOR=true git pull --rebase origin "$BRANCH"
```

- 退出码 0 → 成功，跳转 Step 5
- 退出码非 0 → 有冲突，执行 Step 4

---

## Step 4：冲突处理

### 4.1 逃生指南

```
⚠️ 检测到合并冲突
若不想手动解决，可执行: git rebase --abort
```

### 4.2 冲突报告

```bash
CONFLICT_FILES=$(git diff --name-only --diff-filter=U)
echo "=== 冲突文件 ==="
echo "$CONFLICT_FILES"

echo "$CONFLICT_FILES" | while IFS= read -r f; do
  echo ""
  echo "--- $f ---"
  awk '
    /<<<<<<</ { line=NR; ours=$0 }
    /=======/ { sep=NR }
    />>>>>>>/ { print "位置: " line "-" NR; print "本地: " substr(ours, index(ours," ")+1); print "远端: " substr($0, index($0," ")+1) }
  ' "$f"
done
```

### 4.3 冲突分析报告格式

```
⚠️ 合并冲突分析报告

冲突文件:
  1. app/src/main/java/.../LoginActivity.kt

冲突块:
  位置: LoginActivity.kt:42-52
  本地: 添加了记住密码的 SharedPreferences 存储逻辑
  远端: 重构了 SharedPreferences 为 EncryptedSharedPreferences

建议解决方案:
  ├─ 方案 A: 接受远端
  │    git checkout --theirs <file>
  ├─ 方案 B: 接受本地
  │    git checkout --ours <file>
  └─ 方案 C: 手动合并（推荐）

解决完后执行:
  git add <file>
  git rebase --continue
```

**停止自动流程**，等用户确认冲突解决后继续。

---

## Step 5：推送

```bash
git push origin "$BRANCH"
```

### 推送失败处理

若推送被拒绝（远端有新提交），**不使用 --force**，执行：

```bash
git fetch origin
GIT_EDITOR=true git pull --rebase origin "$BRANCH"
```

然后回到 Step 4 冲突检查。连续失败 2 次以上，建议用户手动处理。

## 变更记录

- 2026-06-03: Step 1.1 新增 macOS 自动回退链。SSH 认证失败时,launchd agent 提示用户终端操作,本地 agent 自动从 Keychain 加载,避免阻塞等待密码输入。
- 2026-06-02: 初版,从 android-git-commit 拆出,作为独立 sync skill。
