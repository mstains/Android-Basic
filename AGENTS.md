# AGENTS.md — Android-Basic

## 基础规则
- 始终使用简体中文回答
- 公共 API 与行内注释使用中文（KDoc 规范见 `~/.config/opencode/skill/android-comment-style`）

## 模块
- `app/` — 演示模块，依赖 `:Basic`
- `Basic/` — 库模块，发布到 JitPack（坐标 `com.github.mstains:android-basic:1.0`，见 `Basic/build.gradle:8-13`）
- 两者共享 `namespace 'com.letter.basic'`（历史原因，勿随意改动）
- 扩展函数统一放在 `Basic/src/main/java/com/letter/basic/extend/`

## 构建命令
- `./gradlew assembleDebug` / `assembleRelease`
- `./gradlew :Basic:assembleRelease`（产出 AAR + sources jar，供 JitPack 发布）
- `./gradlew test` — 仓库内只有占位测试，等同空跑；提交前请用本地编译验证
- `./gradlew clean`

## 工具链
- Gradle 8.10.2 / AGP 8.8.2 / Kotlin 1.9.24 / compileSdk 35 / minSdk 23
- JVM target：`app` 用 Java 11、`Basic` 用 Java 21（两模块不一致是有意的）
- 仓库镜像：腾讯云 + 阿里云（`settings.gradle:11-16, 28-33`）

## 依赖管理
- 所有版本号在 `gradle/libs.versions.toml` 集中维护
- 子模块 `build.gradle` 通过 `libs.xxx` 引用，禁止直接写坐标

## 代码层硬规则
- 继承 `BaseMultiStateVBActivity` 时，`onCreate` 固定顺序为
  `initStatusBar → initView → initData → initListener`
  （见 `Basic/.../BaseMultiStateVBActivity.kt:29-36`），子类不要重复调用 super
- `IntentExt.kt` 中 `baseStartActivity*` / `baseStartActivityForResult*` 已 `@Deprecated`，
  新代码改用 `ResultCallbackLauncher` 系列（`Basic/.../ResultCallbackLauncher.kt`）
- `BaseMultiState*` 自动入栈 `ActivityController`；若需全局 `Application`，
  在自家 `Application.onCreate()` 中调用 `ActivityController.setApplication(this)`
- `BroadcastUtil` 基于已废弃的 `LocalBroadcastManager`，仅作兼容保留

## 新增能力的归位
- 权限 / 相册 / 拍照等 `ActivityResultLauncher` → `ResultCallbackLauncher.kt`
- 日期 `DateTimeFormatter` 模板 → `DateManager.kt`
- 权限中文名 → `ChinesePermission` 枚举 + `toPermissionChineseName*` 扩展

## 验证 / CI
- 仓库内无 lint 配置、无 CI 工作流、无有意义的测试
- 提交前只能依赖本地编译；CI 流水线由调用方自行配置

## 注释规范
正文见 opencode 全局 skill `~/.config/opencode/skill/android-comment-style/SKILL.md`。
速记：public API 必带 KDoc；行内注释回答"为什么"而非"做什么"；TODO 格式
`// TODO(作者/issue号): 原因 → 计划方案`。
