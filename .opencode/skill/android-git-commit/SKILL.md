---
name: android-git-commit
description: >
  为 Android 项目提供智能化的 Git 提交工作流。自动生成提交描述、补全代码注释、
  安全拉取远端代码、处理合并冲突，确保每次提交的质量和安全性。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-05-26'
  keywords:
  - android
  - git
  - commit
  - merge-conflict
  - code-comment
  - conventional-commits
---

# Android Git 智能提交 Skill

## 工作流概述

本 skill 定义了一套完整的 Git 提交工作流，包含以下 5 个步骤：

1. **分析变更** → 一次性扫描工作区，缓存变更列表
2. **补全注释** → 为新增/改动的代码添加中文注释
3. **预检查 & 生成提交** → lint 检查 + Conventional Commit
4. **拉取远端** → `git fetch` + `git pull --rebase`
5. **处理冲突或推送** → 冲突则分析报告，无冲突则推送

---

## Step 0：.gitignore 检查与生成

### 0.1 检查 .gitignore 是否存在

```bash
test -f .gitignore && echo "存在" || echo "缺失"
```

### 0.2 缺失时自动生成

若 `.gitignore` 不存在，**不询问用户**，直接根据项目结构生成标准 Android `.gitignore`。

**生成规则**：先检测项目实际结构中有哪些目录/文件类型，再按需添加对应规则。

```bash
# ===== 检测项目结构 =====
HAS_KOTLIN=$(ls *.kt 2>/dev/null || find . -name "*.kt" -not -path "./.gradle/*" -not -path "./build/*" 2>/dev/null | head -1)
HAS_GRADLE=$(ls *.gradle *.gradle.kts settings.gradle* gradlew gradlew.bat gradle.properties 2>/dev/null | head -1)
HAS_IDEA=$(ls -d .idea 2>/dev/null)
HAS_DS_STORE=$(find . -name ".DS_Store" 2>/dev/null | head -1)
HAS_LOCAL_PROP=$(ls local.properties 2>/dev/null)
HAS_CXX=$(ls *.cpp *.c *.h *.hpp CMakeLists.txt 2>/dev/null || find . -name "CMakeLists.txt" -not -path "./.gradle/*" -not -path "./build/*" 2>/dev/null | head -1)
HAS_FLUTTER=$(ls pubspec.yaml 2>/dev/null)
HAS_REACT=$(ls package.json node_modules 2>/dev/null)

# ===== 按规则生成 .gitignore 内容 =====
cat > .gitignore << 'GITIGNORE_EOF'
# ========== Gradle / Android ==========
.gradle/
build/
local.properties

# ========== IDE ==========
.idea/
*.iml
*.iws
*.ipr
.idea_modules/
*.swp
*.swo
*~

# ========== OS ==========
.DS_Store
Thumbs.db
Desktop.ini

# ========== Android ==========
/captures
.externalNativeBuild/
.cxx/
*.apk
*.aab
*.ap_
*.dex
*.class
bin/
gen/
out/
*.log
lint/checksum.*
lint/results.*

Pods/

# ========== Kotlin ==========
*.kotlin_module
/.kotlin/

# ========== Keystore ==========
*.jks
*.keystore
*.p12
signing.properties

# ========== Environment ==========
.env
.env.local
*.env
GITIGNORE_EOF

echo "✓ 已自动生成 .gitignore"
```

以上规则包含了所有 Android 项目通用的排除项。若项目检测到特定框架（Flutter/React Native/C++），也会自动包含对应规则。

### 0.3 已存在时检查完整性

若 `.gitignore` 已存在，检查是否包含 Android 项目的关键排除项：

```bash
# Android 项目必要的最小排除项
ESSENTIAL=(
  ".gradle/"
  "build/"
  "local.properties"
  ".idea/"
  "*.iml"
  ".DS_Store"
)

echo "=== .gitignore 配置检查 ==="
ALL_OK=true
for pattern in "${ESSENTIAL[@]}"; do
  # 支持 glob 匹配：去掉转义字符后匹配
  if grep -q "^${pattern}$" .gitignore 2>/dev/null; then
    echo "✓ $pattern"
  else
    echo "✗ $pattern 未配置"
    ALL_OK=false
  fi
done

if [ "$ALL_OK" = false ]; then
  echo ""
  echo "检测到 .gitignore 缺少关键排除项，是否补充缺失的规则？(y/n)"
fi
```

---

## Step 1：一次性分析工作区变更

执行以下命令，**一次性**获取所有变更信息（结果缓存，后续步骤复用）：

```bash
# ===== 完整变更信息采集 =====

# 1. 已暂存的文件列表及状态（staged）
STAGED=$(git diff --cached --name-status)

# 2. 未暂存的修改（unstaged）
UNSTAGED=$(git diff --name-status)

# 3. 未跟踪的文件
UNTRACKED=$(git ls-files --others --exclude-standard)

# 4. 详细变更内容（仅 staged，用于生成 commit message）
STAGED_DETAIL=$(git diff --cached)

# 5. 简短状态一览
STATUS_SHORT=$(git status --short)

# 6. 当前分支名
BRANCH=$(git rev-parse --abbrev-ref HEAD)

# ===== 输出摘要 =====
echo "=== 当前分支: $BRANCH ==="
echo ""
echo "--- 已暂存文件 ---"
echo "$STAGED"
echo ""
echo "--- 未暂存修改 ---"
echo "$UNSTAGED"
echo ""
echo "--- 未跟踪文件 ---"
echo "$UNTRACKED"
```

将文件分类为：

| 标记 | 含义 | 是否需要注释 |
|------|------|-------------|
| A    | 新增 | ✅ 需要 |
| M    | 修改 | ✅ 需要 |
| D    | 删除 | ❌ 跳过 |
| R    | 重命名 | ❌ 跳过 |
| ??   | 未跟踪 | 询问用户是否添加 |

---

## Step 2：补全代码注释

### 2.1 提取需要注释的文件

从 Step 1 缓存的 `STAGED` 和 `UNSTAGED` 中提取 **新增（A）** 和 **修改（M）** 的源文件：

```bash
echo "$STAGED
$UNSTAGED" | grep -E '^[AM]\s+.*\.(kt|java|xml)$' | cut -f2 | sort -u
```

### 2.2 注释补全规则

对每个源文件执行以下检查：

| 文件类型 | 检查内容 | 注释风格 |
|---------|---------|---------|
| `.kt` / `.java` | 类/接口/enum 定义前 | `// 类说明` |
| `.kt` / `.java` | 非 getter/setter 方法前 | `// 方法说明` |
| `.kt` | 顶层属性/常量前 | `// 常量说明` |
| `.kt` | 复杂 lambda/回调块 | `// 该回调作用` |
| `.xml` | `include` 标签 | `<!-- 引入的布局说明 -->` |
| `.xml` | 自定义 View 标签 | `<!-- 组件说明 -->` |
| `.xml` | dimen/string/color 等资源 | 若名称含义不明确则加注释 |

> **注释风格约定**：
> - **方法/函数**：描述"做什么"，而非"怎么做"
> - **类/接口**：一句话说明类的职责
> - **关键字段/常量**：说明用途
> - **XML 布局**：对有歧义的自定义组件添加注释
> - **全部使用中文**

### 2.3 使用 opencode 执行注释补全

对所有需要补全注释的文件，使用 **Read** 工具读取文件内容，分析后使用 **Edit** 工具逐文件添加注释。

**严格规则**：
1. **已存在注释**的代码块不重复添加
2. **空方法、自动生成的 override 方法**不添加（如 `onCreate`、`toString`、`equals`、`hashCode` 等）
3. **getter/setter** 不添加
4. **单行简单委托**（如 `fun foo() = bar()`）不添加
5. **测试文件**（`src/test/`、`src/androidTest/`）不添加
6. **生成/编译产物**不处理
7. **注释用中文，简短扼要**（1-2 句话）

---

## Step 3：预检查 & 生成提交

### 3.1 运行 lint 预检查

执行静态检查，及早发现问题：

```bash
# 如果项目有 gradle wrapper
if [ -f "./gradlew" ]; then
  ./gradlew lint --quiet 2>&1 || true
fi
```

若 lint 报错，**向用户展示错误列表**，询问是否继续提交或先修复：

```
⚠️ lint 检查到以下问题:
  app/src/main/.../Foo.kt:42: Unused import
  Basic/src/main/.../Bar.kt:18: Redundant null check
是否继续提交？(y/n)
```

### 3.2 敏感信息扫描

提交前扫描所有**已暂存文件**中是否包含敏感信息，防止签名文件、密码、Token 等意外提交。

```bash
# ===== 定义敏感文件扩展名和文件名模式 =====
SENSITIVE_FILES=(
  "*.jks"
  "*.keystore"
  "*.p12"
  "*.pfx"
  "*.bks"
  "signing.properties"
  "debug.keystore"
  "release.keystore"
  "google-services.json"
  "GoogleService-Info.plist"
  "credentials.json"
  "service_account.json"
  "*.key"
  "*.pem"
  "*.crt"
  "*.cert"
  ".env"
  ".env.*"
  "*.env"
  "secrets.properties"
  "secret.properties"
  "local.defaults.gradle"
)

# ===== 定义敏感内容正则 =====
SENSITIVE_PATTERNS=(
  'password\s*=\s*["\x27]?[^"\x27[:space:]]+["\x27]?'       # password=
  'api[_-]?key\s*[=:]\s*["\x27]?[a-zA-Z0-9_\-]{16,}["\x27]?' # api_key=
  'secret\s*[=:]\s*["\x27]?[^"\x27[:space:]]{8,}["\x27]?'    # secret=
  'token\s*[=:]\s*["\x27]?[^"\x27[:space:]]{16,}["\x27]?'    # token=
  'signingConfigs\s*\{'                                        # signing config block
  'storePassword\s+'                                           # keystore password
  'keyPassword\s+'                                             # key password
  'keyAlias\s+'                                                # key alias
  'storeFile\s+'                                               # keystore file ref
  'ALIAS_'
  'PASSWORD_'
  'API_KEY'
  'SECRET_KEY'
  'ACCESS_KEY'
  'PRIVATE_KEY'
)

# ===== 扫描已暂存文件 =====
echo "=== 敏感信息扫描 ==="

HAS_SENSITIVE_FILE=false
HAS_SENSITIVE_CONTENT=false
SENSITIVE_DETAILS=""

# 扫描敏感文件
for pattern in "${SENSITIVE_FILES[@]}"; do
  while IFS= read -r -d '' f; do
    # 检查该文件是否被暂存
    if echo "$STAGED" | grep -qF "$f"; then
      HAS_SENSITIVE_FILE=true
      SENSITIVE_DETAILS+="  🔴 敏感文件: $f"$'\n'
    fi
  done < <(find . -name "$pattern" -not -path "./.gradle/*" -not -path "./build/*" -not -path "./.idea/*" -print0 2>/dev/null)
done

# 扫描已暂存文件中的敏感内容（仅扫描文本文件）
echo "$STAGED" | grep -E '^[AM]\s+' | cut -f2 | grep -E '\.(kt|java|xml|gradle|properties|json|yml|yaml|txt|conf)$' | while IFS= read -r f; do
  for pat in "${SENSITIVE_PATTERNS[@]}"; do
    matches=$(git diff --cached "$f" | grep '^+' | grep -iE "$pat" | head -5)
    if [ -n "$matches" ]; then
      HAS_SENSITIVE_CONTENT=true
      SENSITIVE_DETAILS+="  🟡 敏感内容: $f"$'\n'
      while IFS= read -r line; do
        # 对密码类内容做脱敏显示
        masked=$(echo "$line" | sed -E 's/(password|secret|token|key)[=:]["'"'"']?[^"'"'"'[:space:]]+/****/gi')
        SENSITIVE_DETAILS+="      ├─ $masked"$'\n'
      done <<< "$matches"
    fi
  done
done
```

#### 处理规则

| 发现类型 | 处理方式 |
|---------|---------|
| 敏感文件（`.jks`/`.keystore`/`signing.properties` 等） | **阻塞**，向用户展示文件路径，要求确认后才能继续 |
| 代码中的密码/密钥硬编码 | **警告**，展示所在文件和脱敏后的内容行，询问用户是否确认提交 |
| Google 服务配置文件 | **警告**，建议检查是否应保密 |
| `.env` 文件 | **阻塞**（通常不应提交到 Git） |

#### 输出格式

```
=== 敏感信息扫描 ===
🟡 敏感内容: app/build.gradle.kts
    ├─ storePassword = ****
    ├─ keyPassword = ****
🔴 敏感文件: app/release.keystore

⚠️ 检测到敏感信息，请二次确认：
  ① .jks/.keystore 文件不应提交到版本控制（强化: 加入 .gitignore）
  ② build.gradle 中的签名密码建议通过环境变量注入
是否仍然提交？(y/n)
```

若用户确认提交，继续流程。否则停止流程，提示用户清理敏感信息后再提交。

### 3.3 资源文件专项检查

若 Step 1 中有 `strings.xml` 或 `colors.xml` 变更，检查是否有缺失或不一致：

```bash
# 检查新增的 string key 是否在默认 strings.xml 中存在
git diff --cached --name-only | grep -E 'res/values/strings\.xml$' | while read f; do
  # 提取新增的 string name
  new_keys=$(git diff --cached "$f" | grep '^+<string name="' | sed 's/.*name="\(.*\)">.*/\1/')
  # 检查其他语言的对应文件是否也有此 key
  for lang_res in $(find $(dirname "$f")/../values-* -name "strings.xml" 2>/dev/null); do
    for key in $new_keys; do
      grep -q "name=\"$key\"" "$lang_res" || echo "⚠️ $lang_res 缺少 string: $key"
    done
  done
done
```

### 3.3 自动推断 scope

根据变更文件的路径前缀，统计出现最多的模块名作为 scope：

```bash
# 从已暂存文件中统计路径前缀
echo "$STAGED" | grep -E '^[AM]\s+' | cut -f2 |
  grep -E '\.(kt|java|xml)$' |
  sed -E 's|.*/([^/]+)/src/.*|\1|' |
  sort | uniq -c | sort -rn | head -1 |
  awk '{print $2}'
```

优先级：统计结果 > `app` > 空

### 3.4 确定 commit type

检查所有变更文件，按优先级取最高级别：

| 优先级 | type | 判断依据 |
|--------|------|---------|
| 1 | feat | 新增功能类文件 + 接口定义 |
| 2 | fix | 文件内容涉及 bug、crash、issue 关键字 |
| 3 | refactor | 文件有大量删除 + 新增但无新功能 |
| 4 | test | 变更只涉及 `src/test/` 或 `src/androidTest/` |
| 5 | docs | 变更只涉及 `README`、`*.md` |
| 6 | style | 变更只涉及格式、import 顺序 |
| 7 | chore | 变更涉及 `build.gradle`、`proguard`、CI 配置 |

若同时涉及多种类型，取优先级最高的类型，并在详细说明中分组列出其他类型。

### 3.5 生成 commit message

格式：

```
<type>(<scope>): <简短描述（50字以内）>

变更文件清单：
  <路径>
    · <具体变更说明>
```

**简短描述生成规则**：
- 从文件名和 diff 内容中提取关键动词和名词
- 使用中文，主动语态
- 示例：`feat(login): 添加记住密码功能`、`fix(network): 修复 OkHttp 超时配置未生效`

**文件变更说明生成规则**：
- 对每个 A/M 文件，分析 `git diff --cached` 的变更块
- 提取新增函数名、修改的逻辑块
- 每条说明以"添加/修复/优化/重构/删除"开头

---

## Step 4：暂存 & 提交 & 拉取远端

### 4.1 询问用户是否包含未跟踪文件

若有未跟踪文件（`??`），**询问用户**是否添加：

```
检测到以下未跟踪文件：
  app/src/main/.../NewFeature.kt
  Basic/src/main/.../temp_test.log
是否全部添加？(y/n/选择添加)
```

### 4.2 暂存 & 提交

```bash
# 暂存所有变更（加上用户确认的未跟踪文件）
git add -A

# 提交
git commit -m "$COMMIT_MSG"
```

### 4.3 处理 pre-commit hook 失败

若 commit 失败（退出码非 0），可能是因为 pre-commit hook（如 ktlint/Spotless）修改了文件：

```bash
# 检查 hook 是否修改了文件
if git diff --name-only | grep -q .; then
  echo "pre-commit hook 修改了以下文件:"
  git diff --name-only
  # 重新暂存并提交
  git add -A
  GIT_EDITOR=true git commit -m "$COMMIT_MSG"
fi
```

若仍然失败，向用户展示 hook 错误信息，停止流程。

### 4.4 拉取远端

```bash
# 获取远端最新内容
git fetch origin

# 尝试 rebase
GIT_EDITOR=true git pull --rebase origin "$BRANCH"
```

### 4.5 判断拉取结果

- **退出码为 0** → 拉取成功，无冲突 → 跳转到 **Step 5**
- **退出码非 0** → 存在冲突 → 执行 **Step 4.6**

### 4.6 冲突处理

首先提供逃生路径：

```
若不想手动解决冲突，可随时执行:
  git rebase --abort
回到 rebase 前的状态。
```

然后批量提取所有冲突区域：

```bash
# 列出所有冲突文件
CONFLICT_FILES=$(git diff --name-only --diff-filter=U)

echo "=== 冲突文件列表 ==="
echo "$CONFLICT_FILES"

# 提取每个冲突文件中的冲突标记位置
echo "$CONFLICT_FILES" | while IFS= read -r f; do
  echo ""
  echo "--- $f ---"
  awk '
    /<<<<<<</ { our=$0; line=NR }
    /=======/ { sep=NR }
    />>>>>>>/ {
      print "冲突块: " line "-" NR " 行"
      print "  本地: " substr(our, index(our, " ") + 1)
      print "  远端: " substr($0, index($0, " ") + 1)
    }
  ' "$f"
done
```

对每个冲突文件执行：
1. 用 **Read** 工具读取冲突区域上下文
2. 分析本地变更和远端变更的意图
3. 生成冲突分析报告

#### 冲突分析报告格式

```
⚠️ 合并冲突分析报告
━━━━━━━━━━━━━━━━━━━━━━

冲突文件:
  1. app/src/main/java/.../LoginActivity.kt

冲突块:
  位置: LoginActivity.kt:42-52
  本地: 添加了记住密码的 SharedPreferences 存储逻辑
  远端: 重构了 SharedPreferences 为 EncryptedSharedPreferences

建议解决方案:
  ├─ 方案 A: 接受远端，本地适配
  │  执行: git checkout --theirs app/src/main/java/.../LoginActivity.kt
  │  然后使用 Edit 工具将记住密码逻辑适配到 EncryptedSharedPreferences
  │
  ├─ 方案 B: 接受本地，忽略远端
  │  执行: git checkout --ours app/src/main/java/.../LoginActivity.kt
  │  注意: 这会丢弃远端的重构
  │
  └─ 方案 C: 手动合并（推荐）
     执行: 使用 Edit 工具手动编辑冲突区域
     步骤: 1. 保留远端的 EncryptedSharedPreferences 工具类
           2. 将本地的记住密码逻辑迁移到新工具类
           3. 删除 <<<<<<< / ======= / >>>>>>> 标记

解决完后执行:
  git add app/src/main/java/.../LoginActivity.kt
  git rebase --continue
```

**停止自动流程**，等用户确认冲突解决后继续。

---

## Step 5：推送到远端

### 5.1 无冲突时直接推送

```bash
# 推送到远端
git push origin "$BRANCH"
```

### 5.2 推送失败处理

如果推送被拒绝（如远端有新提交），**不要使用 `--force`**。执行：

```bash
# 再次拉取并 rebase
git fetch origin
GIT_EDITOR=true git pull --rebase origin "$BRANCH"
```

然后退回 Step 4.5 的冲突检查流程。

若连续失败 2 次以上，告知用户当前状态并建议手动处理。

---

## 完整工作流速查

```
Step 0: 检查 .gitignore 配置
Step 1: 一次性采集变更信息 → 缓存 STAGED/UNSTAGED/UNTRACKED
Step 2: 从缓存提取 AM 文件 → 逐文件补全注释
Step 3: lint 预检查 → 资源一致性检查 → 推断 scope/type → 生成 commit message
Step 4: 询问未跟踪文件 → git add → git commit
  ├─ pre-commit hook 失败 → 重新 stage → 重试 commit
  ├─ git fetch → git pull --rebase
  │   ├─ 无冲突 → 跳转 Step 5
  │   └─ 有冲突 → 提取冲突标记 → 输出报告 → 停止
Step 5: git push origin <branch>
  └─ 推送被拒 → 再次 fetch + rebase → 重试
```

## 异常速查表

| 场景 | 处理方式 |
|------|---------|
| `.gitignore` 缺失 | 自动检测项目结构并生成标准配置 |
| 工作区有未暂存更改 | 合并到 staged 一起处理 |
| 未跟踪文件 | 询问用户是否添加 |
| lint 检查有错误 | 展示错误列表，询问是否继续 |
| 检测到敏感文件（`.jks`/`.keystore`/`signing.properties`） | 阻塞并展示文件路径，要求用户确认 |
| 代码中检测到密码/密钥硬编码 | 脱敏展示所在行，询问是否确认提交 |
| `.env` 文件被暂存 | 阻塞，建议加入 `.gitignore` |
| pre-commit hook 修改了文件 | 自动重新 stage + commit |
| `git pull --rebase` 冲突 | 输出冲突报告，提供逃生 `rebase --abort` |
| 用户解决完冲突 | `git rebase --continue` → Step 5 |
| 推送被拒绝 | 再次 fetch + rebase，不超过 2 次 |
| 二进制文件变更 | 跳过注释，在变更描述中标注 |
| 文件删除（D） | 跳过注释 |
| 资源文件（strings/colors）缺少翻译 | 警告但不阻塞 |
