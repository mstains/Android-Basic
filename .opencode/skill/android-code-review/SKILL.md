---
name: android-code-review
description: >
  Android 提交前代码审查编排器。拆分为五节使用:
  Section A - 敏感扫描(文件/内容模式,硬阻塞)
  Section B - 资源与布局规范(layout 前缀/drawable 前缀/px/颜色/文案/strings 命名/国际化,硬阻塞)
  Section C - 代码异常审查(数值转换/空安全/集合越界/上下文丢失/Gson 实体/TODO 遗留/SimpleDateFormat/findViewById/lateinit,硬阻塞)
  Section D - 代码规范审查(类命名/网络封装/图片加载/UI 封装/协程/DI/TAG 硬编码,硬阻塞)
  Section E - 注释合规检查(class/fun/val 缺少 KDoc,全可见性扫描,检测后移交 opencode 按文件批量补全,拒绝则回退硬阻塞)
  注意:完整 lint 检查不在本 skill 中,由 CI 流水线负责。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-02'
  keywords:
  - android
  - code-review
  - pre-commit
  - security
  - code-quality
  - conventions
---

# Android 代码审查

## 总体工作流

五节全部执行，结果合并为**单次审查报告**，一次确认。不再逐节打断。

```
  执行: Section A → Section B → Section C → Section D → Section E (并行收集)
  聚合: 全部命中按 Section 分组，附文件:行号 + 修复建议
  报告: 统一审查报告 (一次展示全部命中,一次确认)
  闸门: Section A-D 命中 → 硬阻塞,确认 y/N 后统一放行
        Section E 命中 → 移交 opencode 自动补全注释 (详见 Section E 行为规则)
```

审查清单:

```
Section A: 敏感扫描
  A1: 敏感文件扫描
  A2: 敏感内容扫描
  A3: scope 参数处理 (full | staged)

Section B: 资源与布局规范
  B1: strings.xml 多语种同步
  B2: 布局文件前缀 (fragment_/dialog_/item_/widget_)
  B3: Drawable 前缀 (icon_/bg_/selector_/shape_/layer_)
  B4: 禁止 px 硬编码
  B5: XML 颜色硬编码
  B6: XML 文案硬编码
  B7: strings.xml 命名模板 (app_xxx_text)
  B8: 缺失 values-en/ 目录

Section C: 代码异常审查
  C1: 数值转换风险
  C2: 空安全风险 (!! / as)
  C3: 集合越界风险
  C4: 上下文丢失风险
  C5: Gson 实体空安全
  C6: TODO() / error() 遗留
  C7: SimpleDateFormat 非线程安全
  C8: findViewById 泛型强转
  C9: lateinit var 声明

Section D: 代码规范审查
  D1: 业务类命名后缀
  D2: 禁止直接 Glide.with()
  D3: 禁止直接 Retrofit.Builder()
  D4: 禁止 DI 框架
  D5: 禁止 RxJava
  D6: 禁止直接 Toast/AlertDialog/ProgressDialog
  D7: Kotlin 颜色硬编码
  D8: Kotlin 文案硬编码
  D9: Log TAG 硬编码

Section E: 注释合规检查
  E1: public class/interface/object 缺少 KDoc (Kotlin + Java)
  E2: 方法缺少 KDoc (Kotlin + Java, 排除 protected/override)
  E3: 属性缺少 KDoc (Kotlin, 排除 protected/override)
```

> **lint 检查不在本 skill 中**。完整静态分析由 CI 流水线负责,本地提交流程只做 grep 级快速检查以保持流畅。

---

## 全局跳过规则 (五节通用)

| 条件 | 行为 | 适用范围 |
|------|------|---------|
| 行末尾有 `// review:skip` | 该行所在检查项跳过 | Section B / C / D / E |
| 文件路径含 `test/` 或 `androidTest/` | 整个文件跳过 | Section C / D / E |
| 文件名为 `*Test.kt` `*Spec.kt` | 整个文件跳过 | Section C / D / E |

---

## Section A: 敏感扫描

### Step A1: 敏感文件模式

```bash
SENSITIVE_FILES=(
  "*.jks" "*.keystore" "*.p12" "*.pfx" "*.bks"
  "signing.properties" "debug.keystore" "release.keystore"
  "google-services.json" "GoogleService-Info.plist"
  "credentials.json" "service_account.json"
  "*.key" "*.pem" "*.crt" "*.cert"
  ".env" ".env.*" "*.env"
  "secrets.properties" "secret.properties" "local.defaults.gradle"
)
```

### Step A2: 敏感内容模式

```bash
SENSITIVE_PATTERNS=(
  'password\s*='
  'api[_-]?key\s*='
  'secret\s*='
  'token\s*='
  'signingConfigs\s*\{'
  'storePassword\s+'
  'keyPassword\s+'
  'keyAlias\s+'
  'storeFile\s+'
)
```

### Step A3: 扫描范围 (scope 参数)

```bash
SCOPE=${SCOPE:-full}  # full | staged,默认 full

if [ "$SCOPE" = "full" ]; then
  TARGET_FILES=$(find . -type f \
    -not -path "./.gradle/*" \
    -not -path "*/build/*" \
    -not -path "./.git/*" \
    -not -path "./.opencode/*")
else
  TARGET_FILES=$(git diff --cached --name-only)
fi
```

### Section A 执行逻辑

```bash
for pattern in "${SENSITIVE_FILES[@]}"; do
  echo "$TARGET_FILES" | grep -q "$pattern" && echo "🔴 敏感文件: $pattern 命中"
done

echo "$TARGET_FILES" | grep -E '\.(kt|java|xml|gradle|properties|json|yml|yaml|txt|conf)$' |
  grep -vE '^\.opencode/' | while IFS= read -r f; do
  for pat in "${SENSITIVE_PATTERNS[@]}"; do
    matches=$(grep -nE "$pat" "$f" 2>/dev/null | head -5)
    if [ -n "$matches" ]; then
      echo "🟡 敏感内容: $f"
      echo "$matches" | sed 's/^/    /'
    fi
  done
done
```

### Section A 行为规则

| 发现 | 行为 |
|------|------|
| 敏感文件 (`.jks`/`.env` 等) | **硬阻塞**,要求用户删除或确认忽略 |
| 代码中硬编码凭证 | **硬阻塞**,脱敏展示,询问确认 |
| `google-services.json` 等 | **硬阻塞**,建议检查是否应保密 |

**任何命中都阻塞**(对应 E1=A / E2=A),不放过任何敏感信息。

---

## Section B: 资源与布局规范

所有子节均为**硬阻塞**。scope 参数控制扫描范围:
- `full`: 全项目扫描
- `staged`: 仅扫描已暂存文件 (`git diff --cached --name-only`)

### B1: strings.xml 多语种同步

> 规范参见 `android-code-style` §11.5 (国际化) + §11.6 (同步流程)

检测 main `strings.xml` 中新增的 key 是否在 `values-*/strings.xml` 中同步。

```bash
git diff --name-only | grep -E 'res/values/strings\.xml$' | while read f; do
  new_keys=$(git diff "$f" | grep '^+<string name="' | sed 's/.*name="\([^"]*\)">.*/\1/')
  for lang_res in $(find $(dirname "$f")/../values-* -name "strings.xml" 2>/dev/null); do
    for key in $new_keys; do
      grep -q "name=\"$key\"" "$lang_res" || echo "⚠️ $lang_res 缺少 string: $key"
    done
  done
done
```

### B2: 布局文件前缀 (规则 13)

> 规范参见 `android-code-style` §8.1

```bash
find . -path "*/res/layout/*.xml" -type f |
  grep -vE '(fragment_|dialog_|item_|widget_)' |
  while read f; do
    echo "[B2] 布局文件前缀违规: $f"
  done
```

### B3: Drawable 前缀 (规则 16)

> 规范参见 `android-code-style` §8.2

```bash
find . -path "*/res/drawable*/*" -type f |
  grep -vE '(icon_|bg_|selector_|shape_|layer_|ic_launcher)' |
  while read f; do
    echo "[B3] Drawable 前缀违规: $f"
  done
```

### B4: 禁止 px 硬编码 (规则 24)

> 规范参见 `android-code-style` §11.2

```bash
grep -rnE '"[0-9]+px"' --include="*.xml" */src/main/res/layout/ 2>/dev/null |
  while read match; do
    echo "[B4] px 硬编码: $match  → 改用 dp/sp"
  done
```

### B5: XML 颜色硬编码 (规则 25)

> 规范参见 `android-code-style` §11.3

```bash
grep -rnE '"[0-9A-Fa-f]{6,8}"' --include="*.xml" */src/main/res/layout/ */src/main/res/drawable*/ 2>/dev/null |
  while read match; do
    echo "[B5] XML 颜色硬编码: $match  → 改用 @color/xxx"
  done
```

### B6: XML 文案硬编码 (规则 26)

> 规范参见 `android-code-style` §11.4

```bash
grep -rnE 'android:text="[^@]' --include="*.xml" */src/main/res/layout/ 2>/dev/null |
  grep -vE 'android:text=""' |
  while read match; do
    echo "[B6] XML 文案硬编码: $match  → 改用 @string/xxx"
  done
```

### B7: strings.xml 命名模板 (规则 29)

> 规范参见 `android-code-style` §8.3

```bash
grep -rnE '<string name="' --include="*.xml" */src/main/res/values/strings.xml 2>/dev/null |
  grep -vE 'name="app_[a-z_]+_text"' |
  grep -vE 'google_|gcm_|default_|fcm_' |
  while read match; do
    echo "[B7] strings 命名违规: $match  → 改为 app_xxx_text 模板"
  done
```

### B8: 缺失 values-en/ 目录 (规则 27)

> 规范参见 `android-code-style` §11.5

```bash
if ! find . -path "*/src/main/res/values-en" -type d 2>/dev/null | grep -q .; then
  echo "[B8] 缺少 values-en/ 目录  → 创建并同步双语 strings.xml"
fi
```

### Section B 行为规则

| 发现 | 行为 |
|------|------|
| 任一命中 | 汇入统一报告,按 Section 分组展示 |

---

## Section C: 代码异常审查

所有子节均为**硬阻塞**。scope 参数控制扫描范围:
- `full`: 全项目扫描 (默认)
- `staged`: 仅扫描已暂存文件

扫描 `.kt` `.java` 文件,排除 `test/` `androidTest/` `*Test.kt` `*Spec.kt`。

每条命中展示 **±2 行上下文** 供肉眼判断是否已有 try-catch 保护。
行末尾有 `// review:skip` 标记的该行跳过。

### C1: 数值转换风险

String → Number 类型强转，无 null 保护时抛 NumberFormatException。

**Kotlin**:
```bash
grep -rnE '\.toInt\(\)|\.toDouble\(\)|\.toLong\(\)|\.toFloat\(\)|\.toShort\(\)|\.toByte\(\)' \
  --include="*.kt" |
  grep -v 'toIntOrNull' |
  while read match; do
    echo "[C1] toXxx() 风险: $match  → 改用 toXxxOrNull() ?: default"
  done
```

**Java**:
```bash
grep -rnE 'Integer\.parseInt|Double\.parseDouble|Long\.parseLong|Float\.parseFloat|Integer\.valueOf|Long\.valueOf|new BigDecimal|new BigInteger' \
  --include="*.java" |
  while read match; do
    echo "[C1] parseInt 风险: $match  → 用 try-catch 包裹或先校验输入"
  done
```

### C2: 空安全风险

`!!` 强制解包 (→ NPE) 和 `as` 不安全转型 (→ ClassCastException)。

```bash
# !! 强制解包
grep -rnE '!!' --include="*.kt" |
  while read match; do
    echo "[C2] !! 强制解包: $match  → 改用 ?.let{} 或 ?: 安全处理"
  done

# as 不安全转型 (排除 as? / import as / as suspend)
grep -rnE '\bas\b' --include="*.kt" |
  grep -vE 'as\?|import\s+.*\s+as\s+|as\s+suspend' |
  while read match; do
    echo "[C2] as 不安全转型: $match  → 改用 as? 加 null 检查"
  done
```

### C3: 集合越界风险

直接索引访问 / first() / last() 无边界保护。

```bash
grep -rnE '\[0\]|\.get\(0\)|\.first\(\)|\.last\(\)' --include="*.kt" --include="*.java" |
  grep -vE 'firstOrNull|lastOrNull|getOrNull|getOrDefault' |
  while read match; do
    echo "[C3] 集合越界: $match  → 改用 firstOrNull() ?: return 或先检查 isEmpty()"
  done
```

### C4: 上下文丢失风险

Fragment 未 attach 时调 `requireXxx()` 抛 IllegalStateException。

```bash
grep -rnE 'requireActivity\(\)|requireContext\(\)|requireArguments\(\)' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[C4] 上下文强制获取: $match  → 改用 activity?.let{} / context?.let{} 安全访问"
  done
```

### C5: Gson 实体空安全

两步检测:
1. 从 Retrofit service 接口提取返回实体类名
2. 检查实体类中**非基本类型字段**是否声明为可空 `?`

**判定**:
| 字段类型 | 示例 | 结果 |
|---------|------|------|
| 非基本类型,非可空 | `val name: String` | 🔴 **阻塞**,应改为 `String?` |
| 非基本类型,非可空 | `val items: List<Item>` | 🔴 **阻塞**,应改为 `List<Item>?` |
| 基本数值类型 | `val age: Int` | ✅ 跳过 (Gson 填 0,不崩) |
| 基本数值类型 | `val done: Boolean` | ✅ 跳过 (Gson 填 false) |
| 已声明可空 | `val name: String?` | ✅ 跳过 (正确) |

**基本类型白名单**: `Int` `Long` `Double` `Float` `Boolean` `Short` `Byte` `Char`。

```bash
# Step 1: 找到 @GET/@POST/@PUT/@DELETE 注解的 Retrofit service 接口
# Step 2: 从方法签名提取 Response<X>/List<X>/Call<X> 中的实体类名 X
# Step 3: 定位实体类文件 (优先 entity/ 包,其次 *Entity.kt/*DTO.kt/*Response.kt/*Model.kt)
# Step 4: 检查实体类中所有字段:非基本类型且未声明 ? 的 → 标记
```

### C6: TODO() / error() 遗留

`TODO()` 和 `error()` 在运行时抛 `NotImplementedError` / `IllegalStateException`，严禁遗留到生产代码。

```bash
grep -rnE '\bTODO\(\)|\berror\(' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[C6] TODO/error 遗留: $match  → 上线前必须实现或替换"
  done
```

### C7: SimpleDateFormat 非线程安全

`SimpleDateFormat` 在多线程环境下会导致日期解析错误或崩溃，必须用 `java.time.*` 或 `SimpleDateFormat` + `ThreadLocal` 包裹。

```bash
grep -rnE 'SimpleDateFormat\(' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[C7] SimpleDateFormat 声明: $match  → 改用 DateTimeFormatter(API 26+) 或 ThreadLocal 包裹"
  done
```

### C8: findViewById 泛型强转

`findViewById<T>(id)` (Kotlin 扩展) 在 API < 26 或类型不匹配时抛 `ClassCastException`。

```bash
grep -rnE 'findViewById<' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[C8] findViewById 泛型强转: $match  → 改用 ViewBinding 或手动 null 检查后转型"
  done
```

### C9: lateinit var 声明

`lateinit var` 未初始化访问抛 `UninitializedPropertyAccessException`，禁止使用。

```bash
grep -rnE 'lateinit\s+var\b' --include="*.kt" |
  while read match; do
    echo "[C9] lateinit var 声明: $match  → 改用 ? = null 可空 + 安全调用"
  done
```

### Section C 行为规则

| 发现 | 行为 |
|------|------|
| 任一命中 | 汇入统一报告,按 Section 分组展示 |

---

## Section D: 代码规范审查

所有子节均为**硬阻塞**。scope 参数控制扫描范围:
- `full`: 全项目扫描 (默认)
- `staged`: 仅扫描已暂存文件

扫描 `.kt` `.java` 文件,排除 `test/` `androidTest/` `*Test.kt` `*Spec.kt`。

### D1: 业务类命名后缀 (规则 15)

> 规范参见 `android-code-style` §9.1

```bash
# 检测缩写后缀
grep -rnE 'class\s+\w+(Act|Frag|Vm|Adp)\b' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D1] 类名缩写后缀: $match  → 改用完整后缀 Activity/Fragment/ViewModel/Adapter"
  done
```

### D2: 禁止直接 Glide.with() (规则 17)

> 规范参见 `android-code-style` §10.1

```bash
grep -rnE 'Glide\.with' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D2] 直接调 Glide: $match  → 改用 ImageLoader 封装"
  done
```

### D3: 禁止直接 Retrofit.Builder() (规则 18)

> 规范参见 `android-code-style` §10.2

```bash
grep -rnE 'Retrofit\.Builder' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D3] 直接构建 Retrofit: $match  → 改用 RetrofitManager 封装"
  done
```

### D4: 禁止 DI 框架 (规则 19)

> 规范参见 `android-code-style` §10.3

```bash
grep -rhE 'import dagger\.|import io\.insert-koin|import org\.koin' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D4] DI 框架引入: $match  → 移除 DI 依赖,手动管理"
  done
```

### D5: 禁止 RxJava (规则 21)

> 规范参见 `android-code-style` §10.4

```bash
grep -rnE 'import io\.reactivex|import rx\.' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D5] RxJava 引入: $match  → 改用 Kotlin 协程"
  done
```

### D6: 禁止直接调 UI 原生 API (规则 23)

> 规范参见 `android-code-style` §11.1

```bash
grep -rnE 'Toast\.makeText|AlertDialog\.Builder|ProgressDialog' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D6] 直接调原生 UI: $match  → 改用 ToastUtils/DialogUtils/LoadingUtils 封装"
  done
```

### D7: Kotlin 颜色硬编码 (规则 25)

> 规范参见 `android-code-style` §11.3

```bash
grep -rnE 'Color\.parseColor|0x[0-9A-Fa-f]{6,8}\.toInt\(\)' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D7] 代码颜色硬编码: $match  → 改用 R.color.xxx 或 MaterialTheme.colorScheme"
  done
```

### D8: Kotlin 文案硬编码 (规则 26)

> 规范参见 `android-code-style` §11.4

```bash
grep -rnE 'setText\(\s*"[^"]{2,}"\s*\)|text\s*=\s*"[^"]{2,}"' --include="*.kt" --include="*.java" |
  grep -vE '// review:skip|getString|setText\(R\.string|setText\(getString' |
  while read match; do
    echo "[D8] 文案硬编码: $match  → 改用 R.string.xxx"
  done
```

### D9: Log TAG 硬编码 (规则 22)

> 规范参见 `android-code-style` §10.6

```bash
grep -rnE 'Log\.\w\(\s*"[A-Z]+"' --include="*.kt" --include="*.java" |
  grep -vE 'com\.example|android\.util' |
  while read match; do
    echo "[D9] Log TAG 硬编码: $match  → 改用 companion object { const val TAG = \"xxx\" }"
  done

grep -rnE 'Timber\.\w\(\s*"' --include="*.kt" |
  while read match; do
    echo "[D9] Timber TAG 内联: $match  → TAG 前缀走 Timber Tree 配置,勿内嵌在 msg 中"
  done
```

### Section D 行为规则

| 发现 | 行为 |
|------|------|
| 任一命中 | 汇入统一报告,按 Section 分组展示 |

---

## Section E: 注释合规检查

### Section E 执行模型

Section E 与 A-D 不同：由 **detekt + Checkstyle** 做检测，命中条目由调用方（`android-git-commit`）移交 opencode 层**一次性补全所有缺失 KDoc/Javadoc，逐项确认**。

```
  Gradle task 检测: ./gradlew :Basic:checkComments -Pscope=full|staged
      ↓ (输出 JSON 报告)
  opencode 一次性补全: 读取代码,生成 KDoc/Javadoc,逐项展示 diff,逐项确认
      ↓ 单条拒绝
  该项回退硬阻塞: 用户自行补全该条,其余已确认的写入
```

### 检测引擎

| 工具 | 负责语言 | 启用规则 | 配置文件 |
|------|---------|---------|---------|
| **detekt** | Kotlin | `UndocumentedPublicClass` (E1) / `UndocumentedPublicFunction` (E2) / `UndocumentedPublicProperty` (E3) | `.detekt.yml` |
| **Checkstyle** | Java | `JavadocType` (E1) / `JavadocMethod` (E2) / `JavadocVariable` (E3) | `checkstyle.xml` |

### 检测范围

- **Kotlin** `.kt` 文件：class / interface / object / enum class / sealed class / data class / fun / val / var
- **Java** `.java` 文件：class / interface / enum / 所有可见性方法 / 字段
- 排除: `test/` `androidTest/` `*Test.kt` `*Spec.kt` `*Test.java` `*Spec.java`
- 排除: `protected` 声明 (Kotlin + Java)
- 排除: `override` 方法 (Kotlin)
- scope 参数: `staged` (默认，仅检测 git 暂存文件) 或 `full`

### 前置检查

Section E 执行前，检查项目是否已配置 detekt + Checkstyle：

| 检查项 | 检测方式 |
|--------|---------|
| detekt 插件 | `Basic/build.gradle` 是否包含 `alias(libs.plugins.detekt)` |
| Checkstyle 插件 | `Basic/build.gradle` 是否包含 `id("checkstyle")` |
| .detekt.yml | 项目根目录是否存在 |
| checkstyle.xml | 项目根目录是否存在 |

**未配置时引导安装**：展示将要添加的依赖/插件/配置清单，用户确认后自动写入文件。

### 调用方式

```bash
# 仅暂存文件（默认）
./gradlew :Basic:checkComments

# 全量检查
./gradlew :Basic:checkComments -Pscope=full

# 解析 JSON 输出
cat Basic/build/reports/comments/check-comments.json
```

JSON 输出格式：

```json
{
  "scope": "full",
  "detekt": [
    {
      "file": "app/src/main/java/.../LoginViewModel.kt",
      "line": 15,
      "rule": "UndocumentedPublicClass",
      "message": "Class LoginViewModel is missing documentation."
    }
  ],
  "checkstyle": [
    {
      "file": "app/src/main/java/.../UserManager.java",
      "line": 42,
      "rule": "com.puppycrawl...JavadocMethod",
      "message": "Missing a Javadoc comment.",
      "category": "E2"
    }
  ],
  "summary": {
    "total": 5,
    "kotlin": 3,
    "java": 2
  }
}
```

### E1/E2/E3 映射关系

| Section E 分类 | detekt 规则 | Checkstyle 规则 |
|---------------|------------|----------------|
| E1 (class/interface/object) | `UndocumentedPublicClass` | `JavadocType` |
| E2 (方法) | `UndocumentedPublicFunction` | `JavadocMethod` |
| E3 (属性) | `UndocumentedPublicProperty` | `JavadocVariable` |

> 规则配置细节见项目根目录 `.detekt.yml` 与 `checkstyle.xml`。

### Section E 行为规则

| 发现 | 行为 |
|------|------|
| 未配置 detekt/Checkstyle | 先引导安装，用户确认后自动写入配置 |
| E1/E2/E3 任一命中 | 汇入统一报告,标记 `[NEEDS_AI]` |
| 报告展示后 | opencode 解析 JSON，一次性生成所有缺失注释，逐项展示 diff |
| 用户确认（单条） | 该项注释写入文件 |
| 用户拒绝（单条，含 "skip"） | 该项回退**硬阻塞**，用户自行补全；其余已确认的写入 |
| 全部拒绝 / 无命中 | 等同普通硬阻塞,等待修复 |

> **设计意图**: Section E 以 Gradle task 为检测入口，利用 detekt/Checkstyle 的语义分析能力替代原 grep 正则匹配。AI 补全逐项确认粒度更高，减少批量接受时的误写风险。

---

## 统一审查报告

五节执行完毕后，合并所有命中为单次报告。**不再逐节打断**。

### 输出格式

```
=== android-code-review 审查报告 ===

🔴 Section A - 敏感扫描 (x 项命中)
   <如果命中则逐条展示>

⚠️ Section B - 资源与布局规范 (x 项命中)
   B2 布局前缀违规: app/src/main/res/layout/login.xml
       → 重命名为 fragment_login.xml
   B5 XML 颜色硬编码: activity_main.xml:42
       → 改用 @color/xxx

⚠️ Section C - 代码异常审查 (x 项命中)
   C1 toXxx() 风险: LoginViewModel.kt:42
   40: fun parseInput(input: String): Int {
   41:     return input.toInt()
   → 改用 toIntOrNull() ?: 0
   C9 lateinit var: MainActivity.kt:15
   → 改用 ? = null 可空 + 安全调用

⚠️ Section D - 代码规范审查 (x 项命中)
    命中时逐条展示

💬 Section E - 注释合规检查 (x 项命中)
   E1 缺少 KDoc: ./app/src/main/java/.../LoginViewModel.kt:15
       → 移交 opencode 自动补全
   E2 缺少 KDoc: ./app/src/main/java/.../UserManager.kt:42
       → 移交 opencode 自动补全

✅ 共 x 项命中
```

### 确认闸门

**Section A-D 命中时**:

```
是否继续提交？[y/N]

y → 放行所有命中,继续提交流程
N → 停止,用户修复后重新提交 (默认)
```

**Section E 命中时**:

```
Section E 共 x 项缺失注释,是否启用 opencode 自动补全？[Y/n]

Y (默认) → 启动自动补全,一次性生成所有缺失注释,逐项展示 diff,逐项确认 (确认=写入; 拒绝=回退硬阻塞)
n → 跳过自动补全,回退为硬阻塞,用户自行补全后重新提交
```

Section C 命中展示 ±2 行上下文方便肉眼判断 try-catch 保护。
未命中 Section 展示 `✅ 通过`。

### 行为总则

| 条件 | 行为 |
|------|------|
| 任一命中 + dry-run=false | **硬阻塞**,展示统一报告,等待确认 |
| 任一命中 + dry-run=true | 仅展示报告,末尾打印 "dry-run 模式,继续提交",不阻塞 |
| 全部通过 | 直接放行,不打断 |
| 行末尾 `// review:skip` | 该行跳过 (Section B/C/D/E) |
| 文件在 `test/` `androidTest/` | 整文件跳过 (Section C/D/E) |
| Section E 命中 | Gradle task 检测 + 标记 `[NEEDS_AI]`,由调用方启动 opencode 自动补全（逐项确认） |
| 用户拒绝 opencode 自动补全 | 该项回退硬阻塞,手动修复后重新提交 |

---

## 使用示例

### 标准模式 (默认阻塞)
```bash
SCOPE=full
# 调用所有 Section,收集命中,统一报告,阻塞等待确认
```

### 仅敏感扫描 - 已暂存 (降级模式)
```bash
SCOPE=staged
# 调用所有 Section,仅扫描已暂存文件
```

### Dry-run 模式 (不阻塞)
```bash
DRY_RUN=true
# 调用所有 Section,展示报告,直通不阻塞
```

---

## 变更记录

- 2026-07-13: **Section E 重构：grep → detekt + Checkstyle**。检测引擎从 bash grep/sed 正则替换为 detekt（Kotlin KDoc） + Checkstyle（Java Javadoc），通过 Gradle 自定义 task `checkComments` 统一驱动。新增前置引导安装流程（自动写入 detekt/Checkstyle 插件与配置）。AI 补全粒度从「按文件批量」改为「逐项确认」。新增配置文件 `.detekt.yml` / `checkstyle.xml`。
- 2026-07-02: **新增 Section E - 注释合规检查**。bash 层检测 public class/fun/val 缺少 KDoc/Javadoc (E1-E3),命中后标记 `[NEEDS_AI]` 移交 opencode 层自动补全。Section E 采用"检测→AI 补全→人确认"三层模型,拒绝则回退硬阻塞。全局跳过规则扩展至 Section E。总计 5 节 32 项检查。
- 2026-06-30: **规范与执行分离**。Section B/D 中所有规范定义迁移至 `android-code-style`，本 skill 仅保留 grep 检测逻辑与规范引用链接。`android-comment-style` 重命名为 `android-code-style` 并扩展为全集编码规范。
- 2026-06-30: **补充 8 项检查**。Section B 新增 B7(strings 命名模板 app_xxx_text) + B8(缺失 values-en/ 目录); Section C 新增 C6(TODO/error 遗留) + C7(SimpleDateFormat 非线程安全) + C8(findViewById 泛型强转) + C9(lateinit var 禁止); Section D 新增 D9(Log TAG 硬编码)。总计 4 节 29 项检查。
- 2026-06-30: **重大重构**。从 `android-git-commit-review` 重命名为 `android-code-review`。Section B 从仅 strings.xml 一致性扩展为资源与布局规范 (布局前缀/drawable 前缀/px/颜色/文案)。新增 Section C (代码异常审查: 数值转换/空安全/集合越界/上下文丢失/Gson 实体空安全)。新增 Section D (代码规范审查: 类命名/网络/图片/UI/协程/DI/颜色/文案)。所有 Section 统一硬阻塞,新增 `// review:skip` 跳过机制。
- 2026-06-03: 拆分为 Section A (敏感扫描) + Section B (资源一致性),**移除 lint 检查**(移交 CI)。敏感扫描支持 scope=full|staged 参数。
- 2026-06-03: 适配 v3 流程。Section A 调用次数从 v2 的 Step 1+Step 3 改为 v3 的 Step 1 单次。
- 2026-06-02: 从 android-git-commit 拆出,作为独立 review skill。
