# 代码审查检查模式

> 本文档包含 android-code-review 各 Section 的具体检查命令和匹配模式。
> SKILL.md 中描述各 Section 的检查范围和判定规则，执行时读取本文档获取具体命令。
> 部分规则检查依赖 android-code-style 中的规范定义，检测到违规时读取对应章节。

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
SCOPE=${SCOPE:-full}  # full | staged, 默认 full

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

### Step A4: 代码混淆检查

检查 release build 是否启用混淆，proguard-rules.pro 是否为空。

```bash
grep -rnE 'isMinifyEnabled\s*=\s*false|minifyEnabled\s+false' --include="*.gradle*" |
  while read match; do
    echo "[A4] release 混淆未启用: $match  → 改为 true 并配置 proguard-rules.pro"
  done

find . -name "proguard-rules.pro" -size 0 2>/dev/null |
  while read f; do
    echo "[A4] 空混淆规则: $f  → 至少保留 @Keep 注解实体类"
  done
```

### Step A5: 网络安全配置检查

检查是否有明文 HTTP 放行、调试证书信任等不安全的网络安全配置。

```bash
find . -path "*/res/xml/network_security_config.xml" -type f 2>/dev/null |
  while read f; do
    if grep -qE 'cleartextTrafficPermitted="true"' "$f"; then
      echo "[A5] 明文 HTTP 放行: $f  → 仅对可信域名开启,生产环境禁用"
    fi
    if grep -qE 'trust-anchors.*<certificates src="user"' "$f"; then
      echo "[A5] 信任用户安装证书: $f  → 生产环境移除,仅 debug buildType 使用"
    fi
  done

grep -rnE 'android:usesCleartextTraffic="true"' --include="AndroidManifest.xml" |
  while read match; do
    echo "[A5] Manifest 明文放行: $match  → 改用 network_security_config 精确控制"
  done
```

### Step A6: WebView 安全检查

检查 WebView 是否不当暴露文件访问和 JS 接口。

```bash
grep -rnE 'setAllowFileAccess\(true\)|setJavaScriptEnabled\(true\)' --include="*.kt" --include="*.java" |
  grep -vE 'test/|androidTest/' |
  while read match; do
    echo "[A6] WebView 安全风险: $match  → 禁止 file:// 协议,仅对可信域名启用 JS"
  done

grep -rnE 'addJavascriptInterface|@JavascriptInterface' --include="*.kt" --include="*.java" |
  grep -vE 'test/|androidTest/' |
  while read match; do
    echo "[A6] JS 接口暴露: $match  → 检查是否可被任意网页调用,必要时加域名白名单"
  done
```

---

## Section B: 资源与布局规范

scope 参数控制扫描范围:
- `full`: 全项目扫描
- `staged`: 仅扫描已暂存文件 (`git diff --cached --name-only`)

### B1: strings.xml 多语种同步

> 规范参见 android-code-style #11.5 (国际化) + #11.6 (同步流程)

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

### B2: 布局文件前缀

> 规范参见 android-code-style #8.1

```bash
find . -path "*/res/layout/*.xml" -type f |
  grep -vE '(fragment_|dialog_|item_|widget_)' |
  while read f; do
    echo "[B2] 布局文件前缀违规: $f"
  done
```

### B3: Drawable 前缀

> 规范参见 android-code-style #8.2

```bash
find . -path "*/res/drawable*/*" -type f |
  grep -vE '(icon_|bg_|selector_|shape_|layer_|ic_launcher)' |
  while read f; do
    echo "[B3] Drawable 前缀违规: $f"
  done
```

### B4: 禁止 px 硬编码

> 规范参见 android-code-style #11.2

```bash
grep -rnE '"[0-9]+px"' --include="*.xml" */src/main/res/layout/ 2>/dev/null |
  while read match; do
    echo "[B4] px 硬编码: $match  → 改用 dp/sp"
  done
```

### B5: XML 颜色硬编码

> 规范参见 android-code-style #11.3

```bash
grep -rnE '"[0-9A-Fa-f]{6,8}"' --include="*.xml" */src/main/res/layout/ */src/main/res/drawable*/ 2>/dev/null |
  while read match; do
    echo "[B5] XML 颜色硬编码: $match  → 改用 @color/xxx"
  done
```

### B6: XML 文案硬编码

> 规范参见 android-code-style #11.4

```bash
grep -rnE 'android:text="[^@]' --include="*.xml" */src/main/res/layout/ 2>/dev/null |
  grep -vE 'android:text=""' |
  while read match; do
    echo "[B6] XML 文案硬编码: $match  → 改用 @string/xxx，或行末添加 // review:skip 显式跳过"
  done
```

#### B6 白名单（自动过滤，不展示）

| 场景 | 示例 | 理由 |
|------|------|------|
| test/ / androidTest/ 目录 | 测试布局文件 | 测试代码不受业务规范约束 |
| 已有 `// review:skip` 标记 | `android:text="跳过" // review:skip` | 开发者显式声明 |

### B7: strings.xml 命名模板

> 规范参见 android-code-style #8.3

```bash
grep -rnE '<string name="' --include="*.xml" */src/main/res/values/strings.xml 2>/dev/null |
  grep -vE 'name="app_[a-z_]+_text"' |
  grep -vE 'google_|gcm_|default_|fcm_' |
  while read match; do
    echo "[B7] strings 命名违规: $match  → 改为 app_xxx_text 模板"
  done
```

### B8: 缺失 values-en/ 目录

> 规范参见 android-code-style #11.5

```bash
if ! find . -path "*/src/main/res/values-en" -type d 2>/dev/null | grep -q .; then
  echo "[B8] 缺少 values-en/ 目录  → 创建并同步双语 strings.xml"
fi
```

### B9: contentDescription 缺失 (无障碍)

> 参考 Material Design 无障碍指南：所有 ImageView/ImageButton 必须设置 android:contentDescription

```bash
grep -rnE '<(ImageView|ImageButton|IconButton)' --include="*.xml" */src/main/res/layout/ |
  grep -vE 'android:contentDescription' |
  while read match; do
    echo "[B9] 缺少 contentDescription: $match  → 添加 android:contentDescription=\"@string/xxx\""
  done
```

### B10: 触摸目标尺寸 <48dp (无障碍)

> 参考 Material Design 无障碍指南：所有可交互控件最小触摸区域 48dp x 48dp

```bash
grep -rnE 'android:minWidth="[0-9]{1,2}dp"|android:minHeight="[0-9]{1,2}dp"' --include="*.xml" */src/main/res/layout/ |
  grep -vE '(48|56|64|72|80|96|120|160|192|200|240|256|320|360|480)dp' |
  while read match; do
    echo "[B10] 触摸目标 <48dp: $match  → 最小触摸区域 48dp x 48dp"
  done
```

### B11: focusable 缺失 (无障碍)

> 参考 Material Design 无障碍指南：交互控件需设置 android:focusable="true" 供键盘/方向键导航

```bash
grep -rnE '<(Button|TextView|EditText|CheckBox|RadioButton|Switch|SeekBar)' --include="*.xml" */src/main/res/layout/ |
  grep -vE '(android:focusable|android:importantForAccessibility)' |
  while read match; do
    echo "[B11] 交互控件缺少 focusable: $match  → 添加 android:focusable=\"true\""
  done
```

---

## Section C: 代码异常审查

scope 参数控制扫描范围:
- `full`: 全项目扫描 (默认)
- `staged`: 仅扫描已暂存文件

扫描 `.kt` `.java` 文件, 排除 `test/` `androidTest/` `*Test.kt` `*Spec.kt`。

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
| 非基本类型, 非可空 | `val name: String` | 🔴 **阻塞**, 应改为 `String?` |
| 非基本类型, 非可空 | `val items: List<Item>` | 🔴 **阻塞**, 应改为 `List<Item>?` |
| 基本数值类型 | `val age: Int` | ✅ 跳过 (Gson 填 0, 不崩) |
| 基本数值类型 | `val done: Boolean` | ✅ 跳过 (Gson 填 false) |
| 已声明可空 | `val name: String?` | ✅ 跳过 (正确) |

**基本类型白名单**: `Int` `Long` `Double` `Float` `Boolean` `Short` `Byte` `Char`。

```bash
# Step 1: 找到 @GET/@POST/@PUT/@DELETE 注解的 Retrofit service 接口
# Step 2: 从方法签名提取 Response<X>/List<X>/Call<X> 中的实体类名 X
# Step 3: 定位实体类文件 (优先 entity/ 包, 其次 *Entity.kt/*DTO.kt/*Response.kt/*Model.kt)
# Step 4: 检查实体类中所有字段: 非基本类型且未声明 ? 的 → 标记
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

### C8: lateinit var 声明

`lateinit var` 未初始化访问抛 `UninitializedPropertyAccessException`，禁止使用。

```bash
grep -rnE 'lateinit\s+var\b' --include="*.kt" |
  while read match; do
    echo "[C8] lateinit var 声明: $match  → 改用 ? = null 可空 + 安全调用"
  done
```

---

## Section D: 代码规范审查

scope 参数控制扫描范围:
- `full`: 全项目扫描 (默认)
- `staged`: 仅扫描已暂存文件

扫描 `.kt` `.java` 文件, 排除 `test/` `androidTest/` `*Test.kt` `*Spec.kt`。

### D1: 业务类命名后缀

> 规范参见 android-code-style #9.1

```bash
# 检测缩写后缀
grep -rnE 'class\s+\w+(Act|Frag|Vm|Adp)\b' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D1] 类名缩写后缀: $match  → 改用完整后缀 Activity/Fragment/ViewModel/Adapter"
  done
```

### D2: 禁止直接 Glide.with()

> **核心规则：不能直接调用 `Glide.with()`，必须使用 Glide 封装类（如 `ImageLoader`）。**
> 封装类统一管理缓存策略、占位图、错误图，避免分散配置。
> AI 无法自动修复此项，发现后仅报告，不修改。
>
> 规范参见 android-code-style #10.1

```bash
grep -rnE 'Glide\.with' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D2] 直接调 Glide: $match  → 改用 ImageLoader 封装"
  done
```

### D3: 禁止直接 Retrofit.Builder()

> 规范参见 android-code-style #10.2

```bash
grep -rnE 'Retrofit\.Builder' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D3] 直接构建 Retrofit: $match  → 改用 RetrofitManager 封装"
  done
```

### D4: DI 框架

DI 框架 (Dagger/Hilt/Koin 等) **不禁止也不推荐**：团队/模块可自行选择使用或手动管理，本项**不检测、不阻塞**。

### D5: 禁止 RxJava

> 规范参见 android-code-style #10.4

```bash
grep -rnE 'import io\.reactivex|import rx\.' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D5] RxJava 引入: $match  → 改用 Kotlin 协程"
  done
```

### D6: 禁止直接调 UI 原生 API

> 规范参见 android-code-style #11.1

```bash
grep -rnE 'Toast\.makeText|AlertDialog\.Builder|ProgressDialog' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D6] 直接调原生 UI: $match  → 改用 ToastUtils/DialogUtils/LoadingUtils 封装"
  done
```

### D7: Kotlin 颜色硬编码

> 规范参见 android-code-style #11.3

```bash
grep -rnE 'Color\.parseColor|0x[0-9A-Fa-f]{6,8}\.toInt\(\)' --include="*.kt" --include="*.java" |
  while read match; do
    echo "[D7] 代码颜色硬编码: $match  → 改用 R.color.xxx 或 MaterialTheme.colorScheme"
  done
```

### D8: Kotlin 文案硬编码

> 规范参见 android-code-style #11.4

```bash
grep -rnE 'setText\(\s*"[^"]{2,}"\s*\)|text\s*=\s*"[^"]{2,}"' --include="*.kt" --include="*.java" |
  grep -vE '// review:skip|getString|setText\(R\.string|setText\(getString' |
  while read match; do
    echo "[D8] 文案硬编码: $match  → 改用 R.string.xxx，或行末添加 // review:skip 显式跳过"
  done
```

#### D8 白名单（自动过滤，不展示）

| 场景 | 示例 | 理由 |
|------|------|------|
| Log / Timber 日志字符串 | `Log.d(TAG, "xxx")` / `Timber.d("xxx")` | 日志不受文案规范约束 |
| 异常 / error 信息 | `throw Exception("xxx")` / `error("xxx")` | 异常信息面向开发者 |
| test/ / androidTest/ 目录 | 测试代码 | 测试代码不受业务规范约束 |
| companion object TAG 常量 | `const val TAG = "xxx"` | TAG 是类标识，非业务文案 |
| URL / 正则 / 格式化模板 | `"%d 小时"` / `"https://..."` | 格式模板和 URL 非面向用户 |
| 已有 `// review:skip` 标记 | `text = "跳过" // review:skip` | 开发者显式声明 |

### D9: Log TAG 硬编码

> 规范参见 android-code-style #10.6

```bash
grep -rnE 'Log\.\w\(\s*"[A-Z]+"' --include="*.kt" --include="*.java" |
  grep -vE 'com\.example|android\.util' |
  while read match; do
    echo "[D9] Log TAG 硬编码: $match  → 改用 companion object { const val TAG = \"xxx\" }"
  done

grep -rnE 'Timber\.\w\(\s*"' --include="*.kt" |
  while read match; do
    echo "[D9] Timber TAG 内联: $match  → TAG 前缀走 Timber Tree 配置, 勿内嵌在 msg 中"
  done
```

---

## 附录: 规则索引

android-code-style 各章节与 AGENTS.md 规则号的映射：

| code-style 章节 | AGENTS 规则 | 内容 |
|------|-----------|------|
| 8.1 | 规则 13 | 布局文件前缀 (fragment_/dialog_/item_/widget_) |
| 8.2 | 规则 16 | Drawable 前缀 (icon_/bg_/selector_/shape_/layer_) |
| 8.3 | 规则 29 | strings.xml 命名模板 (app_xxx_text) |
| 9.1 | 规则 15 | 业务类命名后缀 (Activity/Fragment/ViewModel...) |
| 10.1 | 规则 17 | 图片加载封装 (ImageLoader) |
| 10.2 | 规则 18,20 | 网络层封装 + JSON 解析 (Gson 默认) |
| 10.3 | 规则 19 | DI 框架 (不强制) |
| 10.4 | 规则 21 | 强制协程 / 禁 RxJava |
| 10.5 | 规则 22 | 日志 Timber / TAG 常量 |
| 11.1 | 规则 23 | UI 工具封装 (ToastUtils/DialogUtils) |
| 11.2 | 规则 24 | 尺寸单位 dp/sp / 禁 px |
| 11.3 | 规则 25 | 颜色禁止硬编码 (View + Compose) |
| 11.4 | 规则 26 | 文案禁止硬编码 (strings.xml) |
| 11.5 | 规则 27,29 | 强制国际化 + strings 双语同步 |

