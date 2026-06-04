# AGENTS.md - Android-Basic 项目指南

## Additional Rules
- 始终使用简体中文回答
- Always use Chinese comments
- Prefer async/await over callbacks
- Error messages should be user-friendly

## 项目结构

- `app/` - Android 应用模块
- `Basic/` - Android 库模块，发布到 JitPack (com.github.mstains:Android-Basic:1.1)

## 构建命令

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 构建库模块 AAR
./gradlew :Basic:assembleRelease

# 运行测试
./gradlew test

# 清理构建
./gradlew clean
```

## 重要配置

- Gradle: 8.10.2
- AGP: 8.8.2
- Kotlin: 1.9.24
- compileSdk: 35, minSdk: 23
- JVM: Java 11

## 依赖仓库

项目使用阿里云镜像 + JitPack + Google + MavenCentral

## 模块规范

- Basic 模块命名空间: `com.letter.basic`
- app 模块命名空间: `com.letter.basic`
- 两者均启用 viewBinding

## 项目规则
- 扩展函数统一放在 `Basic/src/main/java/com/letter/basic/extend/`
- 使用 `./gradlew :Basic:assembleRelease` 构建库模块发布到 JitPack

## 注释规范

本项目遵循通用 Android 注释规范，正文位于 opencode 全局 skill：

`~/.config/opencode/skill/android-comment-style/SKILL.md`

**速记**：

- 公共 API（`public class / fun / val`）**必须**有 KDoc / Javadoc（用途、`@param`、`@return`、`@throws`）
- 复杂逻辑（核心算法、关键分支、魔法值）**应当**有行内注释，统一放在语句**上方**
- 注释语言：中文为主，技术术语（Coroutine / StateFlow / Lifecycle 等）保留英文
- `@param` / `@return` / `@throws` 说明**全部中文**，参数名与类型保留英文
- TODO 格式：`// TODO(作者/issue号): 原因 → 计划方案`，禁止空 TODO
- **禁止**应付式注释（"新增"、"初始化"、"这段代码很重要"等词不表意的注释），反例见规范第 5 节
