---
name: android-code-review
description: >
  Android 提交前代码审查编排器。当用户说要提交代码、检查代码、code review、
  审查代码、看看有什么问题、帮我检查改动、提交前检查时自动触发。执行五项检查：
  敏感信息扫描(A)、资源与布局规范(B)、代码异常审查(C)、代码规范审查(D)、
  注释合规检查(E)。Section A 硬阻塞只报不改；Section B/D 可修项 AI 自动修复后统一确认；
  Section C 可修项 AI 逐条修复逐条确认。完整 lint 检查不在本 skill 中，由 CI 流水线负责。
license: MIT
metadata:
  author: mstains
  last-updated: '2026-07-21'
  keywords:
  - android
  - code-review
  - security
  - code-quality
  - conventions
  - auto-fix
---

# Android 代码审查

## 总体工作流

按 Phase A → Phase B → Phase C → Section E 顺序执行，不可跳过：

```
Phase A  安全扫描      → Section A（只报不改，硬阻塞）
Phase B  规范自修      → Section B + D 可修项（AI 自动修复 → 统一 diff → 一次性确认）
Phase C  异常自修      → Section C 可修项（AI 逐条修复 → 展示 diff + 理由 → 逐条确认）
Section E  注释合规    → detekt + checkstyle → AI 补全 KDoc
```

具体检查命令均参见 **[references/check-patterns.md](references/check-patterns.md)**，执行各 Phase 时读取。

---

## 跳过规则

| 条件 | 适用范围 |
|------|---------|
| 行末尾 `// review:skip` | Section B / C / D / E |
| 文件在 `test/` `androidTest/` 或 `*Test.kt` `*Spec.kt` | Section C / D / E |

---

## Phase A：安全扫描（Section A）

检查命令: `references/check-patterns.md` → Section A

**任何命中都硬阻塞，只报不改。** 包括: 敏感文件(密钥/证书/.env)、代码中硬编码凭证、混淆未启用、明文 HTTP 放行、WebView 安全风险。

命中时输出：
```
🔴 安全扫描 — 发现 x 项安全违规

A2: 硬编码凭证
  📁 app/build.gradle:15  storePassword = "myPass123"
  → 建议: 移至 local.properties 或环境变量

A4: 混淆未启用
  📁 app/build.gradle:30  isMinifyEnabled = false
  → 建议: Release build 启用 minifyEnabled = true

提交已阻塞。请手动修复后重新触发。
```

**安全违规不可跳过**（不接受 `--skip-auto-fix` 或 `--dry-run` 豁免）。

---

## Phase B：规范自修（Section B + D 可修项）

### 扫描范围

检查命令: `references/check-patterns.md` → Section B + Section D

Section B 全部 11 项（B1-B11）和 Section D 部分项（D1、D6-D9）属于规范违规，AI 可自动修复。

Section D 的 D2（Glide 直接调用）、D3（Retrofit Builder）、D5（RxJava）无法自动修复，仅报告。

白名单自动过滤，`// review:skip` 标记的行跳过。

### AI 修复流程

1. 逐条读取 check-patterns.md 中对应规则，理解修复目标
2. 对每个命中项执行修复（重命名文件/修改代码/提取资源）
3. 全部修复完成后，展示统一 `git diff`

### 统一 diff 确认

修复完成后，展示汇总 diff，**一次性确认**：

```
=== Phase B 自动修复汇总 ===

📁 res/layout/activity_login.xml (+2 -2)
  - px → dp: android:padding="16px" → android:padding="16dp"
  - 颜色硬编码: android:textColor="#FF0000" → android:textColor="@color/red_primary"

📁 res/values/strings.xml (+3)
  + <string name="app_confirm_text">确认</string>
  + <string name="app_cancel_text">取消</string>

📁 app/.../LoginActivity.kt (+1 -1)
  - setText("确认") → setText(R.string.app_confirm_text)

📁 app/.../adapter/UserAdp.kt → UserAdapter.kt（重命名）
  - class UserAdp → class UserAdapter

📁 app/.../LogUtil.kt (+1 -1)
  - Log.d("TAG", msg) → companion object { const val TAG = "LogUtil" }

📁 res/values-en/strings.xml（新建）
  + 创建缺失的英文资源目录

共 8 处修改，涉及 6 个文件

> 确认这些修改？[y/N/skip]

（y: 接受全部并 git add / N: 拒绝全部 / skip: 跳过 Phase B）
```

### D2/D3/D5（仅报告，不修复）

这些项涉及架构层决策，AI 无法自动修复，仅汇入 Phase A 后的统一报告。

---

## Phase C：异常自修（Section C 可修项）

### 扫描范围

检查命令: `references/check-patterns.md` → Section C

C1-C5、C7-C8 为可修异常项；C6（TODO/error 遗留）无法自动修复，仅报告。

每条命中展示 **±2 行上下文** + AI 修复后的 diff + 修改理由。**逐条确认**。

### 逐条确认格式

```
=== C2: !! 强制解包 ===

📁 app/viewmodel/UserViewModel.kt:42
  - val name = user!!.name
  + val name = user?.name ?: ""
  理由: user 可能为 null，!! 会抛 NPE。改为安全调用 + 默认值。

> 接受此修改？[y/N/skip]
```

y → 应用修改；N → 跳过此项不修改；skip → 跳过整个 Phase C。

**C6（TODO/error 遗留）**：仅展示文件位置和上下文，不尝试修改，提示开发者手动实现。

---

## Section E：注释合规检查

`./gradlew detekt` (Kotlin) + `./gradlew checkstyleJava` (Java) 检测注释缺失。

- 检测通过 → 继续
- 检测失败 → agent 从 detekt/checkstyle 输出中 grep 提取违规 → 按 android-code-style 注释模板补全 KDoc/Javadoc
- 用户可拒绝某条补全（不阻塞），其余自动写入后 git add
- 仅拦截新增违规，存量由 baseline.xml 管理

排除范围: `test/` `androidTest/` `*Test.kt` `*Spec.kt`、`override` 方法/属性、`companion object` 自身声明。

---

## 统一审查报告（仅 Phase A + 不可修复项）

Phase B、Phase C、Section E 各有独立确认流程，不汇入统一报告。

Phase A 安全违规 + D2/D3/D5 + C6 等无法自动修复的项，合并为单次报告：

```
=== 无法自动修复的违规 ===

🔴 Phase A - 安全扫描 (x 项)
   逐条展示命中项 + 修复建议

⚠️ D2 - 直接 Glide.with() (x 项)
   逐条展示命中项 + 建议改用 ImageLoader 封装

⚠️ D3 - 直接 Retrofit.Builder() (x 项)
   ...

⚠️ D5 - RxJava 引入 (x 项)
   ...

⚠️ C6 - TODO/error 遗留 (x 项)
   ...
```

---

## 确认逻辑总览

| 阶段 | 确认方式 | y | N | skip |
|------|---------|---|-----|------|
| Phase A | 无确认 — 直接阻塞 | — | — | — |
| Phase B 统一 diff | 一次性确认 | 接受全部，git add | 拒绝全部，跳过 Phase B | 同 N |
| Phase C 逐条修复 | 逐条确认 | 接受此条修改 | 跳过此条，继续下一条 | 跳过整个 Phase C |
| Section E | 补全完成后展示 | — | 拒绝某条不阻塞 | — |

---

## 行为总则

| 条件 | 行为 |
|------|------|
| Phase A 命中 | 硬阻塞，立即终止，不进入 Phase B/C |
| Phase B 无可修项 | 直接跳过，进入 Phase C |
| Phase C 无可修项 | 直接跳过，进入 Section E |
| 全部通过 | 直接放行，进入下一步 |
| 行末尾 `// review:skip` | 该行跳过扫描 |
| 文件在 `test/` `androidTest/` | 整文件跳过 (Section C/D/E) |
| `--dry-run` | Phase A 展示报告不阻塞；Phase B/C 展示修复预览但不实际修改文件 |

---

## 自动修复清单

以下列出所有检查项的修复策略，供 Phase B/C 执行时查阅：

| 编号 | 检查项 | 策略 | 确认方式 |
|------|--------|------|---------|
| **A1-A6** | 安全扫描 | 🚫 只报不改 | — |
| **B1** | strings 多语种同步 | ✅ 自动修复 | Phase B 统一确认 |
| **B2** | 布局文件前缀 | ✅ 自动修复 | Phase B 统一确认 |
| **B3** | Drawable 前缀 | ✅ 自动修复 | Phase B 统一确认 |
| **B4** | px 硬编码 | ✅ 自动修复 | Phase B 统一确认 |
| **B5** | XML 颜色硬编码 | ✅ 自动修复 | Phase B 统一确认 |
| **B6** | XML 文案硬编码 | ✅ 自动修复 | Phase B 统一确认 |
| **B7** | strings 命名模板 | ✅ 自动修复 | Phase B 统一确认 |
| **B8** | 缺失 values-en/ | ✅ 自动修复 | Phase B 统一确认 |
| **B9** | contentDescription 缺失 | ✅ 自动修复 | Phase B 统一确认 |
| **B10** | 触摸目标 <48dp | ✅ 自动修复 | Phase B 统一确认 |
| **B11** | focusable 缺失 | ✅ 自动修复 | Phase B 统一确认 |
| **C1** | 数值转换风险 | ✅ 自动修复 | Phase C 逐条确认 |
| **C2** | 空安全风险 | ✅ 自动修复 | Phase C 逐条确认 |
| **C3** | 集合越界风险 | ✅ 自动修复 | Phase C 逐条确认 |
| **C4** | 上下文丢失风险 | ✅ 自动修复 | Phase C 逐条确认 |
| **C5** | Gson 实体空安全 | ✅ 自动修复 | Phase C 逐条确认 |
| **C6** | TODO/error 遗留 | 🚫 只报不改 | — |
| **C7** | SimpleDateFormat | ✅ 自动修复 | Phase C 逐条确认 |
| **C8** | lateinit var | ✅ 自动修复 | Phase C 逐条确认 |
| **D1** | 类名缩写后缀 | ✅ 自动修复 | Phase B 统一确认 |
| **D2** | 直接 Glide.with() | 🚫 只报不改 | — |
| **D3** | 直接 Retrofit.Builder() | 🚫 只报不改 | — |
| **D4** | DI 框架 | — 不检测 | — |
| **D5** | RxJava 引入 | 🚫 只报不改 | — |
| **D6** | 直接原生 UI API | ✅ 自动修复 | Phase B 统一确认 |
| **D7** | 代码颜色硬编码 | ✅ 自动修复 | Phase B 统一确认 |
| **D8** | 代码文案硬编码 | ✅ 自动修复 | Phase B 统一确认 |
| **D9** | Log TAG 硬编码 | ✅ 自动修复 | Phase B 统一确认 |
