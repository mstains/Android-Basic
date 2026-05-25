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
